package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;
import zzInfra.layer3_network.JE802HopInfo;

public class JE802AODVRoutingTable extends JEmula {

	private Map<Integer, JE802RoutingTableEntry> routingTable;

	protected final JE802HopInfo ownAddress;

	public JE802AODVRoutingTable(JEEventScheduler scheduler, JE802HopInfo address) {
		routingTable = new HashMap<Integer, JE802RoutingTableEntry>();
		this.ownAddress = address;
		this.theUniqueEventScheduler = scheduler;
		// makes update handling easier
		JE802RoutingTableEntry ownEntry = new JE802RoutingTableEntry(this.ownAddress, 0, this.ownAddress,
				JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT, 0);
		ownEntry.setValid(true);
		ownEntry.setValidDestSeqNo(true);
		routingTable.put(this.ownAddress.getAddress(), ownEntry);
	}

	public List<JE802RoutingTableEntry> invalidateRoutes(JE802HopInfo destAddr) {
		JE802RoutingTableEntry destEntry = this.lookupRoute(destAddr);
		destEntry.setValid(false);
		JE802HopInfo nextHop = destEntry.getNextHop();

		List<JE802RoutingTableEntry> toNotifyList = new ArrayList<JE802RoutingTableEntry>();

		for (JE802RoutingTableEntry entry : routingTable.values()) {
			if ((entry.getNextHop().equals(nextHop) || entry.getDA().equals(nextHop))) {
				entry.setValid(false);
				toNotifyList.add(entry);
			}
		}
		return toNotifyList;
	}

	// this returns a list with destinations with precursors
	public List<JE802HopInfo> invalidateRoutes(JE802RERRPacket rerr) {
		List<JE802HopInfo> newUnreachDest = new ArrayList<JE802HopInfo>();
		for (int i = 0; i < rerr.getDestCount(); i++) {
			JE802HopInfo unreachDest = rerr.getUnreachDestAddr().get(i);
			long unreachDestSeqNo = rerr.getUnreachDestSeqNo().get(0);
			JE802RoutingTableEntry destEntry = routingTable.get(unreachDest.getAddress());
			// not all the stations receiving a RERR have this destination
			// because the RERR might have been broadcasted
			if (destEntry != null) {
				destEntry.setValid(false);
				destEntry.updateExpiryTime(theUniqueEventScheduler.now().plus(JE802RoutingConstants.DELETE_PERIOD));
				destEntry.setDestinationSeqNo(unreachDestSeqNo);
				if (!destEntry.getPrecursorList().isEmpty()) {
					newUnreachDest.add(unreachDest);
				}
			}
		}
		return newUnreachDest;
	}

	public JE802RoutingTableEntry invalidateSingleRoute(JE802HopInfo destAddr) {
		JE802RoutingTableEntry destEntry = this.lookupRoute(destAddr);
		if (destEntry != null) {
			destEntry.setValid(false);
		}

		return destEntry;
	}

	public JE802RoutingTableEntry lookupRoute(JE802HopInfo destAddr) {
		return routingTable.get(destAddr.getAddress());
	}

	// only returns a route if it is not outdated, valid and not unrepairable
	public JE802RoutingTableEntry lookupValidRoute(JE802HopInfo destAddr) {
		JE802RoutingTableEntry entry = routingTable.get(destAddr.getAddress());
		if (entry != null) {
			if (entry.getExpiryTime().isEarlierThan(theUniqueEventScheduler.now()) || !entry.isValid() || !entry.isRepairable()) {
				return null;
			}
		}
		return entry;
	}

	// creates a map with the number of neighbors on each channel
	protected Map<Integer, Integer> getNeighborhoodChannelUsages() {
		Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();

		for (JE802RoutingTableEntry entry : routingTable.values()) {
			if (entry.getHopCount() <= 2 && !entry.getDA().equalsAddr(ownAddress)) {
				int channel = entry.getNextHop().getChannel();
				Integer count = resultMap.get(channel);
				if (count == null) {
					count = 1;
				} else {
					count++;
				}
				resultMap.put(channel, count);
			}
		}
		return resultMap;
	}

	public void print() {
		for (JE802RoutingTableEntry entry : routingTable.values()) {
			this.message("At " + ownAddress + " : " + entry.toString(), 70);
		}
	}

	public void clear() {
		routingTable.clear();
	}

	// remove all outdated and invalid entries
	public void purge() {
		List<JE802RoutingTableEntry> toRemove = new ArrayList<JE802RoutingTableEntry>();
		for (JE802RoutingTableEntry entry : routingTable.values()) {
			if (!entry.isValid()
					&& entry.getExpiryTime().plus(JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT).times(5)
							.isEarlierThan(theUniqueEventScheduler.now())) {
				toRemove.add(entry);
			}
		}
		for (JE802RoutingTableEntry oldEntry : toRemove) {
			routingTable.remove(oldEntry.getDA().getAddress());
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Routing table at Station " + ownAddress + " ");
		for (JE802RoutingTableEntry entry : routingTable.values()) {
			builder.append(entry + "\n");
		}
		return builder.toString();
	}

	// update the route to the destination, return value indicates if cost was
	// updated
	private boolean update(JE802HopInfo destAddr, long destSeqNo, int hopCount, double mcrCost, JE802HopInfo nextHop, JETime now,
			boolean validDestSeq) {

		JE802RoutingTableEntry entry = routingTable.get(destAddr.getAddress());

		// if entry does not exist, create new one
		if (entry == null) {
			entry = new JE802RoutingTableEntry(destAddr, destSeqNo, nextHop,
					now.plus(JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT), hopCount);
			entry.setValidDestSeqNo(validDestSeq);
			entry.setMcrCost(mcrCost);
			routingTable.put(destAddr.getAddress(), entry);
			return true;
			// print();
			// if entry exists, check for freshness and update accordingly
		} else {

			boolean lowerCost;
			if (JE802RoutingConstants.MCRMetricEnabled) {
				lowerCost = mcrCost <= entry.getMcrCost();
			} else {
				lowerCost = hopCount <= entry.getHopCount();
			}

			// as in 6.2 (i), (ii), (iii)
			if (entry.getDestinationSeqNo() < destSeqNo || (entry.getDestinationSeqNo() == destSeqNo && lowerCost)
					|| !entry.isValidDestSeqNo()) {
				entry.setDestinationChannel(destAddr.getChannel());
				entry.setDestinationSeqNo(destSeqNo);
				entry.setHopCount(hopCount);
				if (lowerCost) {
					entry.setMcrCost(mcrCost);
				}
				entry.setNextHop(nextHop);
				if (!entry.isValidDestSeqNo()) {
					entry.setValidDestSeqNo(validDestSeq);
				}
			}
			entry.updateExpiryTime(now.plus(JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT));
			return lowerCost;
		}
	}

	// when an Data packet is forwarded, then update expiry times of the route
	public void update(JE802IPPacket packet) {
		JETime now = theUniqueEventScheduler.now();
		JETime newExpiryTime = now.plus(JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT);
		// update the expiry time of the destination
		JE802HopInfo destAddr = packet.getDA();
		JE802RoutingTableEntry destEntry = routingTable.get(destAddr.getAddress());
		if (destEntry != null) {
			destEntry.updateExpiryTime(newExpiryTime);
		}

		// update the expiry time of the source
		JE802HopInfo sourceAddr = packet.getSA();
		JE802RoutingTableEntry sourceEntry = routingTable.get(sourceAddr.getAddress());
		if (sourceEntry != null) {
			sourceEntry.updateExpiryTime(newExpiryTime);
		}

		// update the expiry time of the next hop
		JE802RoutingTableEntry nextHopEntry = this.lookupValidRoute(destAddr);
		if (nextHopEntry != null) {
			JE802HopInfo nextHop = nextHopEntry.getNextHop();
			nextHopEntry = routingTable.get(nextHop.getAddress());
			nextHopEntry.updateExpiryTime(newExpiryTime);
		}

		// update the expiry time of the previous Hop
		JE802RoutingTableEntry prevHopEntry = this.lookupValidRoute(sourceAddr);
		if (prevHopEntry != null) {
			JE802HopInfo prevHop = prevHopEntry.getNextHop();
			prevHopEntry = routingTable.get(prevHop.getAddress());
			prevHopEntry.updateExpiryTime(newExpiryTime);
		}
	}

	public boolean update(JE802RREPPacket packet, JE802HopInfo from) {
		JETime now = theUniqueEventScheduler.now();
		JE802HopInfo destAddr = packet.getRreqDestAddr();
		// JE802HopInfo from2 = new JE802HopInfo(from.getAddress(),
		// destAddr.getChannel());
		long destSeqNo = packet.getDestSeqNo();
		int hopCount = packet.getHopCount();
		// also create a route to the last hop, but we don't have a valid
		// destSeqNo information from that node
		this.update(from, destSeqNo, 1, 0.0, from, now, false);
		// create a route to the destination
		boolean updated = this.update(destAddr, destSeqNo, hopCount, packet.getPathMcr(), from, now, true);

		if (packet.getRreqOriginAddr().getAddress() != 255) {
			JE802RoutingTableEntry fromEntry = routingTable.get(packet.getRreqOriginAddr().getAddress());
			if (fromEntry != null) {
				fromEntry.setValid(true);
			}
		}

		JE802RoutingTableEntry destEntry = routingTable.get(packet.getRreqDestAddr().getAddress());
		destEntry.setRepairable(true);
		destEntry.setRepairing(false);
		destEntry.setValid(true);
		updateChannel(from);
		updateChannel(destAddr);
		return updated;
	}

	public boolean update(JE802RREQPacket packet, JE802HopInfo fromAddr) {
		JETime now = theUniqueEventScheduler.now();
		// when we receive a packet from a neighbor (SA = neighbors addr), we
		// set the next hop for the source of this packet to the nextHop to
		// the address of this neighbor
		JE802HopInfo originAddr = packet.getSA();
		long originSeqNo = packet.getOriginSeqNo();
		int hopCount = packet.getHopCount();
		double mcr = packet.getMcrCost();
		// create a route to the last hop were we received the packet from, but
		// we don't have a valid destSeqNo information from that node
		this.update(fromAddr, 0, 1, Double.MAX_VALUE, fromAddr, now, false);

		// create a route to the source of the packet
		boolean updated = this.update(originAddr, originSeqNo, hopCount, mcr, fromAddr, now, true);

		if (packet.getDA().equalsAddr(ownAddress)) {
			JE802RoutingTableEntry sourceEntry = routingTable.get(packet.getSA().getAddress());
			sourceEntry.setValid(true);
		}
		return updated;
	}

	public List<Integer> getOutdatedRouteDestinations() {
		List<Integer> outdatedDestinations = new ArrayList<Integer>();
		for (Integer key : routingTable.keySet()) {
			JE802RoutingTableEntry entry = routingTable.get(key);
			if (entry.getExpiryTime().isEarlierThan(theUniqueEventScheduler.now())) {
				outdatedDestinations.add(key);
			}
		}
		return outdatedDestinations;
	}

	private void updateChannel(JE802HopInfo addr) {
		for (JE802RoutingTableEntry entry : routingTable.values()) {
			if (entry.getDA().equalsAddr(addr)) {
				entry.setDestinationChannel(addr.getChannel());
			}
			if (entry.getNextHop().equalsAddr(addr)) {
				entry.setNextHop(new JE802HopInfo(entry.getNextHop().getAddress(), addr.getChannel()));
			}
		}
	}

	public List<JE802RoutingTableEntry> getActiveRoutes() {
		List<JE802RoutingTableEntry> entrys = new ArrayList<JE802RoutingTableEntry>();

		for (JE802RoutingTableEntry aEntry : routingTable.values()) {
			if (aEntry.isValid()) {
				entrys.add(aEntry);
			}
		}
		return entrys;
	}
}
