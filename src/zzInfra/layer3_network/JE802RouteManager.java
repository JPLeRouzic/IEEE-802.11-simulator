package zzInfra.layer3_network;

import zzInfra.layer3_network.JE802HopInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import zzInfra.ARC.JE802Sme;
import zzInfra.ARC.JE802Station;
import zzInfra.emulator.JE802StatEval;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer0.WirelessChannel;
import zzInfra.layer4_transport.JE802TCPPacket;

/**
 * This class is the core of the IP-Layer. It is implemented according to RFC
 * 3561 (Adhoc On Demand Distance Vector Protocol) Reference description can be
 * found at <link href="http://tools.ietf.org/rfc/rfc3561.txt">
 * http://tools.ietf.org/rfc/rfc3561.txt</link> Only a subset is implemented (no
 * handling of subnets, no control of RREQ dissemination) Numbers in brackets in
 * the comments refer to the according section in the RFC document
 *
 * @author fdreier
 *
 */
public class JE802RouteManager extends JEEventHandler {

    private int tcpHandlerId;

    private final JE802Sme sme;

    private final JE802AODVRoutingTable rt;

    private long rreqId = 0;

    private long sourceSeqNo = 0;

    private final JE802PacketQueue packetQueue;

    private final JE802RREQManager rreqManager;

    private JE802HopInfo ownAddress;

    private final JE802IChannelManager channelMgr;

    private final List<WirelessChannel> availableChannels;

    private ArrayList<Integer> helloPacketsSeen;

    private JE802RoutingStatistics routeStats;

    private final JE802StatEval statEval;

    public JE802RouteManager(JEEventScheduler aScheduler, final Random aGenerator,
            JE802Sme sme, JE802StatEval statEval,
            JE802Station ourStation) {
        super(aScheduler, aGenerator);
        this.sme = sme;
        this.statEval = statEval;
        this.availableChannels = sme.getAvailableChannels();
        this.helloPacketsSeen = new ArrayList<Integer>();
        this.routeStats = new JE802RoutingStatistics(this.availableChannels);
        if (JE802RoutingConstants.channelSwitchingEnabled) {
            this.channelMgr = new JE802HybridChannelManager(sme, aGenerator, aScheduler, ourStation);
        } else {
            this.channelMgr = new JE802FixedChannelManager(aScheduler, aGenerator, sme);
        }

        this.ownAddress = new JE802HopInfo(sme.getAddress(), channelMgr.getFirstChannelNo());

        if (JE802RoutingConstants.routingEnabled) {
            this.rt = new JE802AODVRoutingTable(aScheduler, ownAddress);
            this.packetQueue = new JE802PacketQueue(aScheduler);
            this.rreqManager = new JE802RREQManager(this, aScheduler, aGenerator, ownAddress);
            JETime interval = JE802RoutingConstants.HELLO_INTERVAL_MS;
            JETime nextHelloTime = theUniqueEventScheduler.now().plus(interval)
                    .plus(new JETime((theUniqueRandomGenerator.nextDouble() - 0.5) * 0.01 * interval.getTimeMs()));
            this.send(new JEEvent("nextHello", this.getHandlerId(), nextHelloTime));
        } else {
            this.rt = null;
            this.packetQueue = null;
            this.rreqManager = null;
        }
    }

    public void checkQueueSize(int size) {
        sme.checkQueueSize(size);
    }

	// Calculates the cost of a the path according to some Multichannel Routing
    // metric
    private double computeMCR(JE802RREQPacket rreq, JE802HopInfo lastHop) {

        List<JE802MCRHopRecord> recordList = rreq.getMetricList();
        // calculate cost sum of previous hops
        double ettScSum = 0.0;
        for (int i = 0; i < recordList.size() - 1; i++) {
            JE802MCRHopRecord record = recordList.get(i);
            ettScSum += record.getEtt() + record.getSwitchingCost();
        }
        double ett = routeStats.getETT(lastHop.getChannel(), lastHop.getAddress());
        ettScSum += ett + rreq.getLastHopSwitchingCost();
        rreq.addMetricRecord(new JE802MCRHopRecord(ett, 0.0, lastHop.getChannel()));
        double maxChannel = -1.0;
        for (WirelessChannel channel : availableChannels) {
            double channelSum = 0;
            for (JE802MCRHopRecord record : recordList) {
                if (record.getChannel() == channel.getChannelNumber()) {
                    channelSum += record.getEtt();
                }
            }
            if (channelSum > maxChannel) {
                maxChannel = channelSum;
            }
        }
        double mcr = (1 - JE802RoutingConstants.beta) * ettScSum + JE802RoutingConstants.beta * maxChannel;
        return mcr;
    }

    @Override
    public void event_handler(JEEvent anEvent) {
        String eventName = anEvent.getName();
        JETime now = anEvent.getScheduledTime();
        if (eventName.equals("TCP_Deliv_req")) {
            if (JE802RoutingConstants.routingEnabled) {
                this.route(anEvent);
            } else {
                Vector<Object> parameterList = anEvent.getParameterList();
                JE802HopInfo sa = ownAddress;
                Integer da_address = ((JE802HopInfo)parameterList.get(0)).getAddress();
                JE802HopInfo da = new JE802HopInfo(da_address, sa.getChannel()); // the
                // DA
                // address
                // field
                // is
                // known,
                // but
                // the
                // channel
                // is
                // taken
                // from
                // SA
                parameterList.set(0, da); // updating parameterlist: DA now
                // contains the channel information
                JE802TCPPacket tcpPacket = (JE802TCPPacket) parameterList.get(3);
                Integer ac = (Integer) parameterList.get(1);
                JE802IPPacket ipPacket = new JE802IPPacket(sa, da, tcpPacket, this.getHandlerId(), ac);
                parameterList.setElementAt(ipPacket, 3); // Deliver an IP packet
                // instead of a TCP
                // Packet
                this.send(new JEEvent("IP_Deliv_req", sme.getHandlerId(), now, parameterList));
            }
        } else if (eventName.equals("hop_evaluation")) {
            JE802IPPacket ipPacket = (JE802IPPacket) anEvent.getParameterList().get(0);
            if (!isControlPacket(ipPacket)) {
                if (ipPacket.getSA().equalsAddr(ownAddress)) {
                    this.send(new JEEvent("hop_evaluation", tcpHandlerId, now, anEvent.getParameterList()));
                } else {
                    this.send(new JEEvent("hop_evaluation", ipPacket.getSourceHandlerId(), now, anEvent.getParameterList()));
                }
            }
        } else if (eventName.equals("packet_exiting_system_ind")) {
            if (JE802RoutingConstants.routingEnabled) {
                this.handleIncomingPacket(anEvent.getParameterList());
            } else {
                JE802IPPacket ipPacket = (JE802IPPacket) anEvent.getParameterList().get(0);
                anEvent.getParameterList().setElementAt(ipPacket.getPayload(), 0);
                this.send(new JEEvent("packet_exiting_system_ind", tcpHandlerId, now, anEvent.getParameterList()));
            }

        } else if (eventName.equals("TCP_Timeout")) {
            if (JE802RoutingConstants.routingEnabled) {
                JE802HopInfo dest = (JE802HopInfo) anEvent.getParameterList().get(0);
                JE802RoutingTableEntry entry = rt.lookupRoute(dest);
                if (entry != null) {
                    this.sendRREQ(dest, entry.getDestinationSeqNo(), entry.isValidDestSeqNo());
                }
            }
        } else if (eventName.equals("empty_queue_ind")) {
            /*
             * if(JE802RoutingConstants.channelSwitchingEnabled &&
             * channelMgr.hasPacketsInQueue()){
             * channelMgr.sendPacketFromQueue(); }
             */
            this.send(new JEEvent("empty_queue_ind", tcpHandlerId, now, anEvent.getParameterList()));

        } else if (eventName.equals("nextHello")) {
            helloPacketsSeen = new ArrayList<Integer>();
            if (!rreqManager.hasPendingOwnRREQ()) {
                int newChannel = channelMgr.checkFixedSwitch(rt.getNeighborhoodChannelUsages());
                if (newChannel != this.ownAddress.getChannel()) {
                    this.ownAddress = new JE802HopInfo(ownAddress.getAddress(), newChannel);
                }
                this.sendHello();
                this.message("Send Hello at Station" + ownAddress, 60);
            }
			// 1% Jitter in interval times, avoids that all stations send hello
            // messages at the same time
            JETime interval = JE802RoutingConstants.HELLO_INTERVAL_MS;
            JETime nextHelloTime = now.plus(interval).plus(
                    new JETime((theUniqueRandomGenerator.nextDouble() - 0.5) * 0.01 * interval.getTimeMs()));
            this.send(new JEEvent("nextHello", this.getHandlerId(), nextHelloTime));
            List<Integer> outdated = rt.getOutdatedRouteDestinations();
            for (Integer oldDestination : outdated) {
                statEval.linkBroken(ownAddress, new JE802HopInfo(oldDestination, 1), theUniqueEventScheduler.now());
            }

        } else if (eventName.equals("IPPacket_discarded_ind")) {
            if (JE802RoutingConstants.routingEnabled) {
                JE802IPPacket undeliveredPacket = (JE802IPPacket) anEvent.getParameterList().get(0);
                this.handleLinkBreak(undeliveredPacket);
                Integer retries = (Integer) anEvent.getParameterList().get(1);
                Integer destination = (Integer) anEvent.getParameterList().get(2);
                Integer channel = (Integer) anEvent.getParameterList().get(3);
                routeStats.addPacketReport(channel, destination, false, retries);
            }
        } else if (eventName.equals("IPPacket_delivered_ind")) {
            Integer retries = (Integer) anEvent.getParameterList().get(0);
            Integer destination = (Integer) anEvent.getParameterList().get(1);
            Integer channel = (Integer) anEvent.getParameterList().get(2);
            routeStats.addPacketReport(channel, destination, true, retries);

        } else if (eventName.equals("killRoute")) {
            /*
             * JE802HopInfo dest = (JE802HopInfo)
             * anEvent.getParameterList().get(0); JE802RoutingTableEntry entry =
             * rt.lookupRoute(dest); rt.clear(); System.out.println("killed");
             * this.sendRREQ(dest, entry.getDestinationSeqNo(), false);
             */
        } else {
            this.error("Undefined Event:" + eventName);
        }
    }

    protected void expiredRREQ(JE802RREQPacket rreq) {
        JE802RoutingTableEntry rtEntry = rt.lookupRoute(rreq.getDA());
        // if a repairing entry expires, the route is not reparable
        if (rtEntry != null) {
            if (rtEntry.isRepairing() && !rtEntry.isValid()) {
                rtEntry.setRepairable(false);
                rtEntry.setRepairing(false);
				// else if a normal RREQ expires and there is no valid route
                // yet, retry to create a route
            } else {
                if (!rtEntry.isValid() || rtEntry.getExpiryTime().isEarlierThan(theUniqueEventScheduler.now())) {
                    rtEntry.setValid(false);
                    if (!rreqManager.hasPendingRREQ(rreq.getDA())) {
                        this.sendRREQ(rreq.getDA(), rtEntry.getDestinationSeqNo(), rtEntry.isValidDestSeqNo());
                    }
                }
            }
        } else {
            this.sendRREQ(rreq.getDA(), 0, false);
        }
    }

    public int getAddress() {
        return ownAddress.getAddress();
    }

    private void handleData(Vector<Object> parameters) {
        JE802IPPacket ipPacket = (JE802IPPacket) parameters.get(0);
        int hopAddr = (Integer) parameters.get(5);
        JE802HopInfo lastHop = new JE802HopInfo(hopAddr, ipPacket.getLastHopFixedChannel());
        // we are the destination
        rt.update(ipPacket);
        if (ipPacket.getDA().equalsAddr(ownAddress)) {
            parameters.setElementAt(ipPacket.getPayload(), 0);
            ipPacket.addRouteHop(lastHop);
            ipPacket.addRouteHop(ownAddress);
            statEval.addRoute(ipPacket.getSA(), ipPacket.getDA(), theUniqueEventScheduler.now(), ipPacket.getRouteHops());
            // this.message("Packet exiting at station " + ownAddress,60);
            this.send(new JEEvent("packet_exiting_system_ind", tcpHandlerId, theUniqueEventScheduler.now(), parameters));
            // forward packet to the station
        } else {
            JE802RoutingTableEntry entry = rt.lookupValidRoute(ipPacket.getDA());
            if (entry != null && entry.isValid()) {
                ipPacket.addRouteHop(lastHop);
                ipPacket.setLastHopFixedChannel(ownAddress.getChannel());
                JE802HopInfo nextHop = entry.getNextHop();
                entry.resetLinkLostCount();
				// this.message("Packet forwarded to station " + nextHop +
                // "at Station" + ownAddress,90);
                channelMgr.unicastIPPacket(ipPacket, nextHop);
            } else {
                this.handleNoActiveRoute(ipPacket);
            }
        }
    }

    private void handleIncomingPacket(Vector<Object> parameters) {
        JE802IPPacket packet = (JE802IPPacket) parameters.get(0);
        int hopAddr = (Integer) parameters.get(5);
        int channel = (Integer) parameters.get(3);
        JE802HopInfo lastHop = new JE802HopInfo(hopAddr, channel);
        packet = copyPacket(packet);
        packet.decreaseTTL();
        if (packet instanceof JE802RREQPacket) {
            this.handleRREQ((JE802RREQPacket) packet, lastHop);
        } else if (packet instanceof JE802RREPPacket) {
            this.handleRREP((JE802RREPPacket) packet, lastHop);
        } else if (packet instanceof JE802RERRPacket) {
            this.handleRERR((JE802RERRPacket) packet);
        } else {
            this.handleData(parameters);
        }
    }

    private void handleLinkBreak(JE802IPPacket undeliveredPacket) {

        JE802RoutingTableEntry dest = rt.lookupRoute(undeliveredPacket.getDA());

        if (dest.increaseLinkLostCount()) {
            this.message("Station " + ownAddress + " has broken link to " + dest.getNextHop(), 70);
            if (undeliveredPacket.getSA().equalsAddr(ownAddress)) {
                if (!rreqManager.hasPendingOwnRREQ()) {
                    this.sendRREQ(undeliveredPacket.getDA(), dest.getDestinationSeqNo(), dest.isValidDestSeqNo());
                }
            } else {
                /*
                 * List<JE802HopInfo>precursorList = new
                 * ArrayList<JE802HopInfo>();
                 * precursorList.add(undeliveredPacket.getDA());
                 * this.sendRRER(precursorList,
                 * undeliveredPacket.getSA(),false);
                 */
            }
        } else {
            channelMgr.unicastIPPacket(undeliveredPacket, dest.getNextHop());
        }
        /*
         * if(dest.isRepairable()){ if(!undeliveredPacket.isControlPacket()){
         * statEval.linkBroken(ownAddress, dest.getNextHop(),
         * theUniqueEventScheduler.now()); } //tryLocal Repair
         * dest.setRepairing(true); if(!rreqManager.hasPendingOwnRREQ()){
         * this.sendRREQ(undeliveredPacket.getDA(), dest.getDestinationSeqNo(),
         * dest.isValidDestSeqNo()); }
         * 
         * //route is not repairable, issue a Route ERROR } else {
         * dest.resetLinkLostCount(); List<JE802RoutingTableEntry>
         * affectedDestinations =
         * rt.invalidateRoutes(undeliveredPacket.getDA()); //hash set has the
         * advantage of no duplicates Collection<JE802HopInfo> precursors = new
         * LinkedHashSet<JE802HopInfo>(); for(JE802RoutingTableEntry entry :
         * affectedDestinations) { this.message("Affected Dest: " +
         * entry.getDA(),60); precursors.addAll(entry.getPrecursorList());
         * packetQueue.discardPackets(entry.getDA()); }
         * System.err.println("Sending RERR"); List<JE802HopInfo>precursorList =
         * new ArrayList<JE802HopInfo>(precursors); this.sendRRER(precursorList,
         * precursorList.size()>1); }
         */
    }

    private void handleNoActiveRoute(JE802IPPacket packet) {
        // invalidate the route
        JE802RoutingTableEntry entry = rt.invalidateSingleRoute(packet.getDA());
        // try to locally repair the route
        if (entry != null) {
            if (entry.isRepairable()) {
                if (!entry.isRepairing()) {
                    this.sendRREQ(packet.getDA(), entry.getDestinationSeqNo(), false);
                    entry.setRepairing(true);
                }
                packetQueue.addPacket(packet);
				// else, repairing is not possible, send a RERR message to the
                // precursors
            } else {
                List<JE802HopInfo> precursors = entry.getPrecursorList();
                packetQueue.discardPackets(entry.getDA());
                precursors.add(entry.getDA());
                this.sendRRER(precursors, packet.getSA(), false);
            }
        }
    }

    private void handleRERR(JE802RERRPacket rerr) {
        this.message("Station " + ownAddress + " received RERR from " + rerr.getSA(), 90);
        if (rerr.getDA().equalsAddr(ownAddress)) {
            // failed addr
            JE802RoutingTableEntry entry = rt.lookupRoute(rerr.getUnreachDestAddr().get(0));
            this.sendRREQ(entry.getDA(), entry.getDestinationSeqNo(), entry.isValidDestSeqNo());
        } else {
            JE802RoutingTableEntry entry = rt.lookupRoute(rerr.getDA());
            this.sendRRER(rerr.getUnreachDestAddr(), entry.getDA(), false);
        }

		// List<JE802HopInfo> newUnreachable = rt.invalidateRoutes(rerr);

        /*
         * for(JE802HopInfo unreachDest : newUnreachable){
         * JE802RoutingTableEntry unreachEntry = rt.lookupRoute(unreachDest);
         * precursors.addAll(unreachEntry.getPrecursorList()); }
         */
    }

    private JETime lastKillEvent = new JETime(-10000);

    private void handleRREP(JE802RREPPacket packet, JE802HopInfo lastHop) {
        JE802RREPPacket rrep = new JE802RREPPacket(packet);
        JE802HopInfo lastHop2 = new JE802HopInfo(lastHop.getAddress(), rrep.getLastHopFixedChannel());
        rrep.incrementHopCount();
        rt.update(rrep, lastHop2);
		// the RREQ was created by ourselves or a gratuitous RREP from a
        // intermediate station

        if (rrep.getDA().equalsAddr(ownAddress)) {
            this.message(
                    "Station " + ownAddress + " got RREP to own RREQ from Station " + rrep.getSA() + " for "
                    + rrep.getRreqDestAddr() + " Hops " + rrep.getHopCount() + " Mcr " + rrep.getPathMcr(), 90);
            // send possibly queued Packets
            List<JE802IPPacket> toSend = packetQueue.getPacketsForDestination(rrep.getRreqDestAddr());
            for (JE802IPPacket toSendPacket : toSend) {
                channelMgr.unicastIPPacket(toSendPacket, lastHop2);
            }
            rreqManager.removeRREQs(rrep.getRreqDestAddr());
            JE802RoutingTableEntry entry = rt.lookupRoute(rrep.getDA());
            if (entry != null) {
                entry.resetLinkLostCount();
            }

            JETime now = theUniqueEventScheduler.now();
            if (JE802RoutingConstants.MCRMetricEnabled && lastKillEvent.isEarlierEqualThan(now.minus(new JETime(2000)))) {
                lastKillEvent = now;
                Vector<Object> parameterList = new Vector<Object>();
                parameterList.add(rrep.getSA());
                this.send(new JEEvent("killRoute", this.getHandlerId(), now.plus(new JETime(2000)), parameterList));
            }
            // packet is a hello message
        } else if (rrep.getDA().getAddress() == 255) {
            // forward the hello message
            this.message("Station " + ownAddress + " Got Hello from " + rrep.getSA(), 60);
            rrep.setLastHopFixedChannel(ownAddress.getChannel());
            if (!helloPacketsSeen.contains(rrep.getSA().getAddress())) {
                helloPacketsSeen.add(rrep.getSA().getAddress());
                channelMgr.broadcastIPPacketAll(rrep);
            }
        } else {
            JE802RoutingTableEntry entry = rt.lookupValidRoute(rrep.getDA());
            rreqManager.removeRREQs(rrep.getDA());
            rrep.setLastHopFixedChannel(ownAddress.getChannel());
            if (entry != null) {
                // add the lasthop to the precursor list (6.2, last paragraph)
                entry.addPrecursor(lastHop2);
                JE802HopInfo nextHop = entry.getNextHop();
                JE802RoutingTableEntry nextHopEntry = rt.lookupRoute(nextHop);
                nextHopEntry.addPrecursor(lastHop2);
                channelMgr.unicastIPPacket(rrep, nextHop);

            }
        }
    }

    private void handleRREQ(JE802RREQPacket aPacket, JE802HopInfo lastHop) {
        JE802RREQPacket rreqPacket = new JE802RREQPacket(aPacket); // create
        // copy to
        // avoid
        // aliasing
        if (JE802RoutingConstants.MCRMetricEnabled) {
            rreqPacket.setMcrCost(computeMCR(rreqPacket, lastHop));
        }
        rreqPacket.incrementHopCount();
        // check if we have already seen this RREQ
        if (!rreqManager.isDuplicateRREQ(rreqPacket)) {

			// this.message("Station " + ownAddress + " got RREQ: " +
            // packet.toString(),60);
            rreqManager.addRREQ(rreqPacket);

			// last hop for the multichannel routing extension, replace the
            // usages of this variable by lastHop if you want to use it without
            // extension
            JE802HopInfo lastHop2 = new JE802HopInfo(lastHop.getAddress(), rreqPacket.getLastHopFixedChannel());
			// rt.update(packet, lastHop); //withouht Multichannel extension
            // this would be used
            rreqPacket.setLastHopFixedChannel(ownAddress.getChannel());
            // we are the destination of the route, generate Route Reply
            if (rreqPacket.getDA().equalsAddr(ownAddress)) {
                // update SequenceNumber
                sourceSeqNo = Math.max(rreqPacket.getDestSeqNo(), sourceSeqNo);
                JE802RoutingTableEntry entry = rt.lookupValidRoute(rreqPacket.getSA());
                if (entry == null || isLowerCost(entry, rreqPacket)) {
                    // update routing table
                    rt.update(rreqPacket, lastHop2);
                    JE802RREPPacket rrep = new JE802RREPPacket(ownAddress, rreqPacket.getSA(), rreqPacket.getSA(), ownAddress,
                            rreqPacket.getOriginSeqNo(), this.getHandlerId(), 0, rreqPacket.getMcrCost());
                    rrep.setLastHopFixedChannel(ownAddress.getChannel());
                    channelMgr.unicastIPPacket(rrep, lastHop2);
                    this.message(
                            "Station " + sme.getAddress() + " sent RREP to " + rrep.getDA() + "over channel "
                            + lastHop.getChannel(), 60);
                }
                JE802RoutingTableEntry rrepDestEntry = rt.lookupRoute(rreqPacket.getDA());
                if (rrepDestEntry != null) {
                    rrepDestEntry.resetLinkLostCount();
					// add the lasthop to the precursor list (6.2, last
                    // paragraph)
                    rrepDestEntry.addPrecursor(lastHop2);
                }

                // packet is not for us
            } else {
				// JE802RoutingTableEntry destinationEntry =
                // rt.lookupValidRoute(rreqPacket.getDA());
                rt.update(rreqPacket, lastHop2);
                /*
                 * if(destinationEntry != null){
                 * sendIntermediateRREP(rreqPacket, destinationEntry, lastHop2);
                 * } else {
                 */

				// this.message("Station " + ownAddress +
                // " forward broadcast RREQ: " + packet.getRreqId() + " Hops: "
                // + packet.getHopCount(),60);
                if (rreqManager.isRREQSendAllowed()) {
                    channelMgr.broadcastIPPacketAll(rreqPacket);
                }
                // }
            }
			// we have already seen this RREQ, we can discard the currently
            // received RREQ
        } else {
            // else silently discard RREQ
        }
    }

	// if we already have a valid route to the requested destination, report
    // RREP back to originator of RREQ
    // and also send gratuitous RREP to the requested destination
    // PLEASE DO NOT DELETE, COULD be USED in future
    private void sendIntermediateRREP(JE802RREQPacket rreqPacket, JE802RoutingTableEntry destinationEntry, JE802HopInfo lastHop) {

        // send RREP back (6.6.2)
        if (!rreqPacket.getSA().equalsAddr(ownAddress)) {
            long destSeqNo = destinationEntry.getDestinationSeqNo();
            int hopCount = destinationEntry.getHopCount();
            JE802RREPPacket rrep = new JE802RREPPacket(ownAddress, rreqPacket.getSA(), rreqPacket.getSA(),
                    destinationEntry.getDA(), destSeqNo, this.getHandlerId(), hopCount, destinationEntry.getMcrCost());
            rrep.setLastHopFixedChannel(channelMgr.getFirstChannelNo());
            channelMgr.unicastIPPacket(rrep, lastHop);
            destinationEntry.addPrecursor(lastHop);
            this.message("Station " + ownAddress + " sent intermediate RREP to " + rreqPacket.getSA() + " for route to"
                    + destinationEntry.getDA() + "Hops " + hopCount, 90);

            // send gratuitous RREP (6.6.3)
            JE802RREPPacket gratuitousRREP = new JE802RREPPacket(ownAddress, rreqPacket.getDA(), destinationEntry.getDA(),
                    rreqPacket.getSA(), rreqPacket.getOriginSeqNo(), this.getHandlerId(), rreqPacket.getHopCount(),
                    rreqPacket.getMcrCost());
            gratuitousRREP.setIntermediateRrep(true);
            gratuitousRREP.setLastHopFixedChannel(channelMgr.getFirstChannelNo());
            channelMgr.unicastIPPacket(gratuitousRREP, destinationEntry.getNextHop());
            this.message("Staton " + ownAddress + "Sent gratuitous RREP to " + rreqPacket.getDA(), 90);

            // add precursor for source Entry
            JE802RoutingTableEntry sourceEntry = rt.lookupRoute(rreqPacket.getSA());
            sourceEntry.addPrecursor(destinationEntry.getNextHop());
            sourceEntry.resetLinkLostCount();
        }
    }

    private boolean isLowerCost(JE802RoutingTableEntry entry, JE802RREQPacket packet) {
        boolean lowerCost;
        if (JE802RoutingConstants.MCRMetricEnabled) {
            return true;
        } else {
            lowerCost = packet.getHopCount() <= entry.getHopCount();
        }
        return lowerCost;
    }

    private boolean isControlPacket(JE802IPPacket packet) {
        return (packet instanceof JE802RREQPacket || packet instanceof JE802RREPPacket || packet instanceof JE802RERRPacket);
    }

    @SuppressWarnings("unchecked")
    private void route(JEEvent anEvent) {

        List<JE802HopInfo> hopList = (List<JE802HopInfo>) anEvent.getParameterList().get(2);
        JE802HopInfo da = hopList.get(hopList.size() - 1);
        JE802TCPPacket tcpPacket = (JE802TCPPacket) anEvent.getParameterList().get(3);
        Integer ac = (Integer) anEvent.getParameterList().get(1);
        // create ip packet
        JE802IPPacket ipPacket = new JE802IPPacket(ownAddress, da, tcpPacket, this.getHandlerId(), ac);

        JE802RoutingTableEntry entry = rt.lookupRoute(da);
		// no route to the destination exists, case where no route existed in
        // the path (no expired entry)
        if (entry == null) {
			// check whether a route request for this destination is already
            // pending
            if (!rreqManager.hasPendingRREQ(da)) {
                // make a route request for destination address
                this.sendRREQ(da, 0, true);
            }
            packetQueue.addPacket(ipPacket);
            // route to the destination exists, send packet over that route
        } else {
            JE802RoutingTableEntry validEntry = rt.lookupValidRoute(da);
            // a valid route exists, send packet over that route
            if (validEntry != null) {
                rt.update(ipPacket);
                ipPacket.setLastHopFixedChannel(ownAddress.getChannel());
                channelMgr.unicastIPPacket(ipPacket, entry.getNextHop());
                // we had a route once but it expired or was marked invalid
            } else {
                long destSeqNo = entry.getDestinationSeqNo();
                if (!rreqManager.hasPendingRREQ(da)) {
                    this.sendRREQ(da, destSeqNo, false);
                }
                packetQueue.addPacket(ipPacket);
            }
        }
    }

    private void sendHello() {
        JE802RREPPacket hello = new JE802RREPPacket(ownAddress, new JE802HopInfo(255, 1), new JE802HopInfo(255, 1), ownAddress,
                sourceSeqNo, this.getHandlerId(), 0, 0.0);
        hello.setLastHopFixedChannel(ownAddress.getChannel());
        hello.setTTL(1);
        this.channelMgr.broadcastIPPacketAll(hello);
    }

    private void sendRREQ(JE802HopInfo destAddr, long destSeqNo, boolean unknownDestSeq) {
        rreqId++;
        sourceSeqNo++;
        JE802RREQPacket rreq = new JE802RREQPacket(ownAddress, destAddr, this.getHandlerId(), rreqId, sourceSeqNo, destSeqNo);
        rreq.setUnknownSequenceNumber(unknownDestSeq);
        rreq.setLastHopFixedChannel(ownAddress.getChannel());
        rreqManager.addRREQ(rreq);
        if (rreqManager.isRREQSendAllowed()) {
            channelMgr.broadcastIPPacketAll(rreq);
        }
        this.message("Station " + ownAddress + " issued RREQ for destination" + destAddr, 60);
    }

    private void sendRRER(List<JE802HopInfo> precursorList, JE802HopInfo da, boolean broadcast) {

        if (!precursorList.isEmpty() && !da.equalsAddr(ownAddress)) {
            JE802RERRPacket rerr = new JE802RERRPacket(ownAddress, da, this.getHandlerId());

            for (JE802HopInfo precursor : precursorList) {
                JE802RoutingTableEntry preEntry = rt.lookupRoute(precursor);
                rerr.addUnreachableDestination(precursor, preEntry.getDestinationSeqNo());
            }
            JE802RoutingTableEntry entry = rt.lookupRoute(da);

            if (broadcast) {
                channelMgr.broadcastIPPacketAll(rerr);
            } else {
                if (entry != null) {
                    channelMgr.unicastIPPacket(rerr, entry.getNextHop());
                }
            }
        }
    }

    private JE802IPPacket copyPacket(final JE802IPPacket packet) {
        JE802IPPacket newPacket;
        if (packet instanceof JE802RREQPacket) {
            newPacket = new JE802RREQPacket((JE802RREQPacket) packet);
        } else if (packet instanceof JE802RREPPacket) {
            newPacket = new JE802RREPPacket((JE802RREPPacket) packet);
        } else if (packet instanceof JE802RERRPacket) {
            newPacket = new JE802RERRPacket((JE802RERRPacket) packet);
        } else {
            newPacket = new JE802IPPacket(packet);
        }
        return newPacket;
    }

    public void setTcpHandlerId(int handlerId) {
        this.tcpHandlerId = handlerId;
    }

    @Override
    public String toString() {
        return "Route Manager at Station " + ownAddress;
    }
}
