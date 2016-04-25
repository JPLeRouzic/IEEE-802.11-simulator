/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) Stefan Mangold
 *    All rights reserved. Urheberrechtlich geschuetzt.
 * 
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 * 
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 * 
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 * 
 */
package IEEE11ac.layer2;

import zzInfra.layer0.WirelessMedium;
import IEEE11ac.layer1.acPhy;
import IEEE11ac.station.acStation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import zzInfra.emulator.JE802StatEval;
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer2.JE802Mac;

public final class acMac extends JE802Mac {

    private int dot11BroadcastAddress;

    private int dot11RTSThreshold;

    private int dot11ShortRetryLimit;

    private int dot11LongRetryLimit;

    private int dot11MacHeaderRTS_byte;

    @SuppressWarnings("unused")
    private int dot11FragmentationThreshold;

    @SuppressWarnings("unused")
    private int dot11MaxTransmitMSDULifetime;

    @SuppressWarnings("unused")
    private int dot11MaxReceiveLifetime;

    private boolean dot11WepEncr;

    private int dot11MacAddress4_byte;

    private int dot11MacFCS_byte;

    private int dot11MacHeaderACK_byte;

    private int dot11MacHeaderCTS_byte;

    private int dot11MacHeaderDATA_byte;

    private acVCollisionHandler theVch;

    private acMlme theMlme;

    private final Map<Integer, acBackoffEntity> backoffEntityMap;

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private acPhy myPhy = null ;

    /**
     * @param sta
     * @param aScheduler
     * @param statEval
     * @param aGenerator
     * @param aGui
     * @param aChannel
     * @param aTopLevelNode
     * @param smeHandlerId
     * @throws XPathExpressionException
     */
    public acMac(acStation sta, final JEEventScheduler aScheduler, final JE802StatEval statEval, 
            final Random aGenerator, final JE802Gui aGui, final WirelessMedium aChannel, 
            final Node aTopLevelNode, final int smeHandlerId, String std)
            throws XPathExpressionException {

        super(aScheduler, statEval, aGenerator, aGui, aTopLevelNode, smeHandlerId);
        this.backoffEntityMap = new HashMap<Integer, acBackoffEntity>();

        if (aTopLevelNode.getNodeName().equals("JE80211MAC")) {
            this.message("JE80211MAC XML definition " + aTopLevelNode.getNodeName() + " found.", 1);

            Element macNode = (Element) aTopLevelNode;

            // before creating a MAC we must create a PHY, because a MAC contains a PHY
            // create PHY layer
            Node phyNode = (Node) this.xpath.evaluate("JE80211PHY", aTopLevelNode, XPathConstants.NODE);
            if (phyNode == null) {
                error("Mac with no Phy found at Station " + this.getMacAddress());
            }
            sta.thePhy = new acPhy(aScheduler, statEval, aGenerator, aChannel, aGui, phyNode, sta, std);
            this.myPhy = (acPhy) sta.thePhy;

            // create MIB
            Element mibNode = (Element) this.xpath.evaluate("MIB802.11-1999", aTopLevelNode, XPathConstants.NODE);

            this.theMacAddress = new Integer(mibNode.getAttribute("dot11MACAddress"));
            this.dot11BroadcastAddress = new Integer(mibNode.getAttribute("dot11BroadcastAddress"));
            this.dot11RTSThreshold = new Integer(mibNode.getAttribute("dot11RTSThreshold"));
            this.dot11ShortRetryLimit = new Integer(mibNode.getAttribute("dot11ShortRetryLimit"));
            this.dot11LongRetryLimit = new Integer(mibNode.getAttribute("dot11LongRetryLimit"));
            this.dot11FragmentationThreshold = new Integer(mibNode.getAttribute("dot11FragmentationThreshold"));
            this.dot11MaxTransmitMSDULifetime = new Integer(mibNode.getAttribute("dot11MaxTransmitMSDULifetime"));
            this.dot11MaxReceiveLifetime = new Integer(mibNode.getAttribute("dot11MaxReceiveLifetime"));

            // some times and settings
            this.dot11WepEncr = new Boolean(macNode.getAttribute("dot11WepEncryption"));
            this.dot11MacAddress4_byte = new Integer(macNode.getAttribute("dot11MacAddress4_byte"));
            this.dot11MacFCS_byte = new Integer(macNode.getAttribute("dot11MacFCS_byte"));
            this.dot11MacFCS_byte = new Integer(macNode.getAttribute("dot11MacFCS_byte"));
            this.dot11MacHeaderACK_byte = new Integer(macNode.getAttribute("dot11MacHeaderACK_byte"));
            this.dot11MacHeaderCTS_byte = new Integer(macNode.getAttribute("dot11MacHeaderCTS_byte"));
            this.dot11MacHeaderDATA_byte = new Integer(macNode.getAttribute("dot11MacHeaderDATA_byte"));
            this.dot11MacHeaderRTS_byte = new Integer(macNode.getAttribute("dot11MacHeaderRTS_byte"));

            String isFixedStr = macNode.getAttribute("isFixed");
            if (!isFixedStr.isEmpty()) {
                isFixed = new Boolean(isFixedStr);
            }

            // create virtual collision handler
            this.theVch = new acVCollisionHandler(aScheduler, aGenerator, (acPhy) sta.getPhy(), this);
            this.send(new JEEvent("start_req", this.theVch, this.theUniqueEventScheduler.now()));

            // create backoff entities
            NodeList baList = (NodeList) this.xpath.evaluate("JE802BackoffEntity", aTopLevelNode, XPathConstants.NODESET);
            for (int i = 0; i < baList.getLength(); i++) {
                Node baNode = baList.item(i);
                // System.out.println("fin qui");
                acBackoffEntity aNewBE = new acBackoffEntity(aScheduler, aGenerator, aGui, baNode, this, this.theVch);
                this.backoffEntityMap.put(aNewBE.getAC(), aNewBE);
            }
            if (baList.getLength() < 1) {
                error("at least one backoff entity per station is required for data reception.");
            }

            // create MAC layer management entity MLME
            Node mlmeNode = (Node) this.xpath.evaluate("JE802Mlme", aTopLevelNode, XPathConstants.NODE);
            this.theMlme = new acMlme(aScheduler, aGenerator, new Vector<acBackoffEntity>(
                    this.backoffEntityMap.values()), sta, this, mlmeNode);
            this.send(new JEEvent("start_req", this.theMlme, this.theUniqueEventScheduler.now()));
        } else {
            error("JE80211MAC XML messed up");
        }
    }

    public void checkQueueSize(final int queueSize) {
        for (acBackoffEntity be : this.backoffEntityMap.values()) {
            if (be != null) {
                if (queueSize > be.getQueueSize()) {
                    warning("TCP buffer size bigger than backoff entity queue size at Station" + this.theMacAddress + "AC"
                            + be.getAC());
                }
            }

        }
    }

    public void event_handler(final JEEvent anEvent) {

        JETime now = anEvent.getScheduledTime();
        String anEventName = anEvent.getName();

        this.message("MAC at Station " + this.theMacAddress + " received event '" + anEventName
                + "'", 10);

        if (this.theState == state.idle) {

            if (anEventName.equals("stop_req")) {
                // ignore;

            } else if (anEventName.equals("start_req")) {
                this.theState = state.active;
            } else {
                error("undefined event '" + anEventName + "' in state " + this.theState.toString());
            }

        } else if (this.theState == state.active) {

            if (anEventName.equals("PHY_RxEnd_ind")) { // send this event to all
                // backoff entities
                for (acBackoffEntity aBE : this.backoffEntityMap.values()) {
                    this.send(new JEEvent("PHY_RxEnd_ind", aBE, now, anEvent.getParameterList()));
                }

            } else if (anEventName.equals("PHY_SyncStart_ind")) { // inform all
                // backoff
                // entities
                // that we
                // are
                // starting
                // to
                // receive
                // something

                for (acBackoffEntity aBE : this.backoffEntityMap.values()) {
                    this.send(new JEEvent(anEventName, aBE, now));
                }

            } else if (anEventName.equals("PHY_RxStart_ind")) {
                this.parameterlist = anEvent.getParameterList();
                Integer anAc = (Integer) this.parameterlist.elementAt(1);

                Integer aWinningBeId = this.theVch.getTransmitterId();
                acBackoffEntity aTargetBe;

                if (aWinningBeId == null) { // no Mpdu was sent before, just
                    // send the packet to the BE for CTS
                    // or ACK if the BE with the
                    // respective AC does not exist,
                    // create it!
                    aTargetBe = this.backoffEntityMap.get(anAc);
                    if (aTargetBe == null) {
                        try {
                            aTargetBe = new acBackoffEntity(this.theUniqueEventScheduler, this.theUniqueRandomGenerator,
                                    this.theUniqueGui, null, this, this.theVch);
                            aTargetBe.setAC(anAc);
                            this.backoffEntityMap.put(anAc, aTargetBe);
                            this.send("start_req", aTargetBe);
                        } catch (XPathExpressionException e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                    this.send(new JEEvent("MPDUReceive_ind", aTargetBe.getHandlerId(), now, this.parameterlist));

                    // for the purpose of virtual CS for the backoff entities
                    for (acBackoffEntity aBE : this.backoffEntityMap.values()) {
                        if (aBE.getAC() != anAc) {
                            this.send(new JEEvent("MPDUReceive_ind", aBE, now, this.parameterlist));
                        }
                    }
                } else {
                    // one BE sent something before
                    this.send(new JEEvent("MPDUReceive_ind", aWinningBeId, now, this.parameterlist));

                    // for the purpose of virtual CS for the backoff entities,
                    // except the winner
                    for (acBackoffEntity aBE : this.backoffEntityMap.values()) {
                        if (aBE.getHandlerId() != aWinningBeId) {
                            this.send(new JEEvent("MPDUReceive_ind", aBE, now, this.parameterlist));
                        }
                    }
                    this.theVch.setTransmitterId(null);
                }
            } else if (anEventName.equals("MSDU_delivered_ind")) {
                // each time a packet is successfully delivered, we update the
                // etx
                acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                Integer mpduDA = aMpdu.getDA();
                int retried = (Integer) anEvent.getParameterList().get(1);
                Vector<Object> parameterList = new Vector<Object>();
                parameterList.add(retried);
                parameterList.add(mpduDA);
//				parameterList.add(getChannel());
                this.send(new JEEvent(anEventName, this.smeHandlerId, now, parameterList));

            } else if (anEventName.equals("MSDU_discarded_ind")) {

                boolean queueFull = (Boolean) anEvent.getParameterList().get(1);

                // the ip layer is only interested in packets discarded because
                // of too many retries
                if (!queueFull) {
                    acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                    Vector<Object> parameterList = new Vector<Object>();
                    Integer retries = 0;
                    if (anEvent.getParameterList().size() > 2) {
                        retries = (Integer) anEvent.getParameterList().get(2);
                    }
                    parameterList.add(aMpdu);
                    parameterList.add(retries);
//					parameterList.add(getChannel());
                    this.send(new JEEvent(anEvent.getName(), this.smeHandlerId, now, parameterList));
                }

            } else if (anEventName.equals("empty_queue_ind")) {
                // forward empty queue to ipLayer
                this.parameterlist = anEvent.getParameterList();
//				this.parameterlist.add(getChannel());
                this.send(new JEEvent(anEventName, this.smeHandlerId, now, this.parameterlist));

            } else if (anEventName.equals("MSDUDeliv_req")) {
                this.parameterlist = anEvent.getParameterList();
                int anAC = (Integer) this.parameterlist.elementAt(1);
                ArrayList<acHopInfo> hopAddresses = (ArrayList<acHopInfo>) this.parameterlist.get(2);
                // remove first hop address because its already set as
                // destination address.
                hopAddresses.remove(0);

                // forward request to backoff entity
                acBackoffEntity aBE = this.backoffEntityMap.get(anAC);
                anEvent.setTargetHandlerId(aBE.getHandlerId());
                this.send(anEvent);

            } else if (anEventName.equals("packet_exiting_system_ind")) {
                acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                if (aMpdu.getDA() != dot11BroadcastAddress && aMpdu.isData() && aMpdu.getPayload().getPayload() != null) {
                    statEval.recordPhyMCS(aMpdu.getSA(), this.theMacAddress, now, aMpdu.getPhyMcs());
                }
//				anEvent.getParameterList().add(getChannel());
                this.send(new JEEvent("packet_exiting_system_ind", this.smeHandlerId, this.theUniqueEventScheduler.now(), anEvent
                        .getParameterList()));

            } else if (anEventName.equals("Channel_Switch_req")) {
                for (acBackoffEntity be : this.backoffEntityMap.values()) {
                    if (be != null) {
                        be.discardQueue();
                    }
                }
                JETime switchTime;
                if (currentTransmissionEnd.isLaterThan(now)) {
                    switchTime = currentTransmissionEnd;
                } else {
                    switchTime = now;
                }
                this.send(new JEEvent("PHY_ChannelSwitch_req", this.getHandlerId(), switchTime, anEvent.getParameterList()));

            } else if (anEventName.equals("broadcast_sent")) {
                Vector<Object> parameterList = new Vector<Object>();
//				parameterList.add(getChannel());
                this.send(new JEEvent("broadcast_sent", this.smeHandlerId, anEvent.getScheduledTime(), parameterList));

            } else if (anEventName.equals("PHY_TxEnd_ind")) {
                this.parameterlist = anEvent.getParameterList();
                Integer anAc = (Integer) this.parameterlist.elementAt(0);
                this.send(new JEEvent(anEventName, this.backoffEntityMap.get(anAc), now));

            } else if (anEventName.equals("VCH_TxStart_ind")) { //
                // one backoff entity transmits. If there are others, send the
                // frame to them, so they behave as if a real collision
                // occurred.
                this.parameterlist = anEvent.getParameterList();
                acMPDU anMpdu = ((acMPDU) this.parameterlist.elementAt(0)).clone();
                Integer aWinningBeId = this.theVch.getTransmitterId();
                this.currentTransmissionEnd = now.plus(anMpdu.getTxTime());
                if (aWinningBeId != null) {
                    // one BE sent something before
                    this.parameterlist.clear();
                    this.parameterlist.add(anMpdu);
                    // for the purpose of virtual CS for the backoff
                    // entities, except the winner
                    for (acBackoffEntity aBE : this.backoffEntityMap.values()) {
                        if (aBE.getHandlerId() != aWinningBeId) {
                            this.parameterlist.clear();
                            this.parameterlist.add(anMpdu.getDA());
                            this.parameterlist.add(anMpdu.getAC());
                            this.parameterlist.add(anMpdu.getNav());
                            this.send(new JEEvent("MPDUReceive_ind", aBE, now.plus(getPhy().getPLCPHeaderDuration()),
                                    this.parameterlist));
                        }
                    }
                    this.theVch.setTransmitterId(null);
                } else {
                    // there is no winner, because there was no internal virtual
                    // collision.
                }
            } else if (anEventName.equals("packet_forward")) { // a packet was
                // received, and
                // will be
                // forwarded (in
                // mesh)
                this.send(new JEEvent("packet_forward", this.smeHandlerId, now, anEvent.getParameterList()));

            } else if (anEventName.equals("groupForwardEvent")) {
                this.parameterlist = anEvent.getParameterList();
                ArrayList<acHopInfo> hops = (ArrayList<acHopInfo>) this.parameterlist.get(2);
                if (hops != null) {
                    this.parameterlist.set(0, hops.get(0));
                    JEEvent newPacketEvent = new JEEvent("MSDUDeliv_req", getHandlerId(), this.theUniqueEventScheduler.now(),
                            this.parameterlist);
                    this.send(newPacketEvent);
                }
            } else if (anEventName.equals("start_req")) {
                // ignore
            } else if (anEventName.equals("stop_req")) {
                this.theState = state.idle;
            } else if (anEventName.equals("push_back_packet")) {
                this.send(new JEEvent("push_back_packet", smeHandlerId, now, anEvent.getParameterList()));
            } else if (anEventName.equals("location_update")) {
                this.send(new JEEvent("location_update", getPhy(), now));

            } else {
                error("undefined event '" + anEventName + "' in state " + this.theState.toString());
            }
        }
    }

    public int getDot11BroadcastAddress() {
        return this.dot11BroadcastAddress;
    }

    public int getDot11LongRetryLimit() {
        return this.dot11LongRetryLimit;
    }

    public int getDot11MacAddress4_byte() {
        return this.dot11MacAddress4_byte;
    }

    public int getDot11MacFCS_byte() {
        return this.dot11MacFCS_byte;
    }

    public int getDot11MacHeaderACK_byte() {
        return this.dot11MacHeaderACK_byte;
    }

    public int getDot11MacHeaderCTS_byte() {
        return this.dot11MacHeaderCTS_byte;
    }

    public int getDot11MacHeaderDATA_byte() {
        return this.dot11MacHeaderDATA_byte;
    }

    public int getDot11MacHeaderRTS_byte() {
        return this.dot11MacHeaderRTS_byte;
    }

    public int getDot11RTSThreshold() {
        return this.dot11RTSThreshold;
    }

    public int getDot11ShortRetryLimit() {
        return this.dot11ShortRetryLimit;
    }

    public boolean isDot11WepEncr() {
        return this.dot11WepEncr;
    }

    public void setDot11MACAddress(final Integer dot11macAddress) {
        this.theMacAddress = dot11macAddress;
    }

    @Override
    public int getMacAddress() {
        return this.theMacAddress;
    }

    @Override
    public String toString() {
        return "MacAddress: " + this.theMacAddress;
    }

    // TODO:check if this is OK. Added by Divya
    public acMlme getMlme() {
        return this.theMlme;
    }

    public acBackoffEntity getBackoffEntity(int ac) {
        return this.backoffEntityMap.get(ac);
    }

    public acPhy getPhy() {
        return myPhy;
    }
}
