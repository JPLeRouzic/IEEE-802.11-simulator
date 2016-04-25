package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JETimer;
import zzInfra.layer3_network.JE802HopInfo;

public class JE802RREQManager extends JEEventHandler {

	private List<JE802RREQEntry> seenRREQs;

	private List<JE802RREQEntry> lastSentRREQs;

	private final JE802HopInfo ownAddress;

	private final JE802RouteManager manager;

	public JE802RREQManager(JE802RouteManager manager, JEEventScheduler scheduler, Random randomGen, JE802HopInfo ownAddress) {
		super(scheduler, randomGen);
		this.seenRREQs = new ArrayList<JE802RREQEntry>();
		this.ownAddress = ownAddress;
		this.manager = manager;
		this.lastSentRREQs = new LinkedList<JE802RREQEntry>();
	}

	protected void addRREQ(JE802RREQPacket rreq) {
		JETime expiry = theUniqueEventScheduler.now().plus(JE802RoutingConstants.PATH_DISCOVERY_TIME);
		JE802RREQEntry entry = new JE802RREQEntry(rreq, expiry);
		lastSentRREQs.add(new JE802RREQEntry(rreq, theUniqueEventScheduler.now()));
		if (lastSentRREQs.size() > JE802RoutingConstants.RREQ_RATE_LIMIT) {
			lastSentRREQs.remove(0);
		}
		if (rreq.getSA().equalsAddr(ownAddress)) {
			Vector<Object> params = new Vector<Object>();
			params.add(entry);
			JEEvent timeout = new JEEvent("RREQ_expired", this.getHandlerId(), expiry, params);
			JETimer rreqTimeout = new JETimer(this.theUniqueEventScheduler, this.theUniqueRandomGenerator, timeout,
					this.getHandlerId());
			rreqTimeout.start(JE802RoutingConstants.PATH_DISCOVERY_TIME);
		}
		seenRREQs.add(entry);
	}

	protected List<JE802HopInfo> checkForExpriredRREQs() {
		// create a copy because list is modified during iteration
		List<JE802HopInfo> rreqList = new ArrayList<JE802HopInfo>();
		for (JE802RREQEntry entry : seenRREQs) {
			if (entry.getExpiryTime().isEarlierThan(theUniqueEventScheduler.now())) {
				rreqList.add(entry.getPacket().getDA());
			}
		}
		return rreqList;
	}

	public boolean isRREQSendAllowed() {
		if (lastSentRREQs.isEmpty() || lastSentRREQs.size() < JE802RoutingConstants.RREQ_RATE_LIMIT) {
			return true;
		}
		JETime timeOfEarliest = lastSentRREQs.get(0).getExpiryTime();
		return timeOfEarliest.isEarlierThan(new JETime(theUniqueEventScheduler.now().getTimeMs() - 1000));
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		String eventName = anEvent.getName();
		if (eventName.equals("RREQ_expired")) {
			JE802RREQEntry entry = (JE802RREQEntry) anEvent.getParameterList().get(0);
			this.message("Station " + manager.getAddress() + " has expired RREQ: " + entry.getPacket().toString(), 70);
			seenRREQs.remove(entry);
			manager.expiredRREQ(entry.getPacket());
		} else {
			this.error("undefined event " + eventName + " at RREQManager, Station " + ownAddress);
		}
	}

	protected boolean hasPendingOwnRREQ() {
		for (JE802RREQEntry entry : seenRREQs) {
			if (entry.getSA().equalsAddr(ownAddress) && entry.getExpiryTime().isLaterThan(theUniqueEventScheduler.now())) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasPendingRREQ(JE802HopInfo destAddr) {
		for (JE802RREQEntry entry : seenRREQs) {
			if (entry.packet.getDA().equalsAddr(destAddr) && entry.getSA().equalsAddr(ownAddress)
					&& entry.getExpiryTime().isLaterThan(theUniqueEventScheduler.now())) {
				return true;
			}
		}
		return false;
	}

	// also removes outdated RREQS from list
	protected boolean isDuplicateRREQ(JE802RREQPacket packet) {
		List<JE802RREQEntry> old = new ArrayList<JE802RREQEntry>();
		for (JE802RREQEntry entry : seenRREQs) {
			// duplicate if rreqId and source are same
			if ((entry.getRREQId() == packet.getRreqId() && entry.getSA().equalsAddr(packet.getSA())
					&& entry.getExpiryTime().isLaterThan(theUniqueEventScheduler.now()) || packet.getSA().equalsAddr(ownAddress))) {
				// if cost of new path is lower, don't count it as duplicate
				// such that routing table gets updated accordingly
				if (JE802RoutingConstants.MCRMetricEnabled) {
					return entry.getPacket().getMcrCost() <= packet.getMcrCost();
				} else {
					return entry.getPacket().getHopCount() <= packet.getHopCount();
				}
			} else if (entry.getExpiryTime().isEarlierThan(theUniqueEventScheduler.now())) {
				old.add(entry);
			}
		}
		seenRREQs.removeAll(old);
		return false;
	}

	protected void removeRREQs(JE802HopInfo da) {
		List<JE802RREQEntry> toRemove = new ArrayList<JE802RREQEntry>();
		for (JE802RREQEntry entry : seenRREQs) {
			if (entry.getPacket().getDA().equalsAddr(da)) {
				toRemove.add(entry);
			}
		}
		seenRREQs.removeAll(toRemove);
	}

	private class JE802RREQEntry {

		private final JE802RREQPacket packet;

		private final JETime expiryTime;

		public JE802RREQEntry(JE802RREQPacket packet, JETime expiry) {
			this.packet = packet;
			this.expiryTime = expiry;
		}

		public JETime getExpiryTime() {
			return expiryTime;
		}

		public JE802RREQPacket getPacket() {
			return packet;
		}

		public long getRREQId() {
			return packet.getRreqId();
		}

		public JE802HopInfo getSA() {
			return packet.getSA();
		}

		@Override
		public String toString() {
			return packet.toString() + " Expires: " + expiryTime;
		}
	}
}
