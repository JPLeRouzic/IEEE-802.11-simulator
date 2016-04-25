package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import zzInfra.ARC.JE802Sme;
import zzInfra.ARC.JE802Station;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer0.WirelessChannel;
import zzInfra.layer3_network.JE802HopInfo;

public class JE802HybridChannelManager extends JEEventHandler implements JE802IChannelManager {

	private final JE802Sme sme;

	// packet queues for each channel
	private final Map<Integer, List<JE802QueueEntry>> packetQueues;

	// list of available Channels
	private final List<WirelessChannel> availableChannels;

	// statistics about channel usages
	private final Map<Integer, List<JE802UsageEntry>> channelUsagesTime;

	private final Map<Integer, Integer> dataSizeOnChannel;

	private int fixedChannel;

	private JETime fixedSwitchingUntil = new JETime(0);

	private int switchableChannel;

	private JETime switchingUntil = new JETime(0);

	// current state, broadcasting or uni casting, broadcast packets have
	// priority
	private JEHybridChannelState state;

	public JE802HybridChannelManager(final JE802Sme aSme, final Random aGenerator, final JEEventScheduler aScheduler,
			JE802Station station) {
		super(aScheduler, null);
		this.sme = aSme;
		this.sme.setChannelHandlerId(getHandlerId());
		this.theUniqueRandomGenerator = aGenerator;
		this.availableChannels = this.sme.getAvailableChannels();
		this.packetQueues = new HashMap<Integer, List<JE802QueueEntry>>();
		this.channelUsagesTime = new HashMap<Integer, List<JE802UsageEntry>>();
		this.dataSizeOnChannel = new HashMap<Integer, Integer>();
		this.state = new JEUnicastState();
		for (WirelessChannel channel : this.availableChannels) {
			List<JE802QueueEntry> packetList = new ArrayList<JE802QueueEntry>();
			this.packetQueues.put(channel.getChannelNumber(), packetList);
			List<JE802UsageEntry> entryList = new LinkedList<JE802HybridChannelManager.JE802UsageEntry>();
			this.channelUsagesTime.put(channel.getChannelNumber(), entryList);
			this.dataSizeOnChannel.put(channel.getChannelNumber(), 0);
		}

		// initialize fixed channel to random channel
		int randomNr;
		if (station.isMobile()) {
			randomNr = theUniqueRandomGenerator.nextInt(availableChannels.size());
			this.fixedChannel = this.availableChannels.get(randomNr).getChannelNumber();
		} else {
			this.fixedChannel = station.getFixedChannel();
		}

		randomNr = theUniqueRandomGenerator.nextInt(availableChannels.size());
		this.switchableChannel = this.availableChannels.get(randomNr).getChannelNumber();
		while (this.switchableChannel == this.fixedChannel) {
			randomNr = theUniqueRandomGenerator.nextInt(availableChannels.size());
			this.switchableChannel = this.availableChannels.get(randomNr).getChannelNumber();
		}

		this.message("Station " + this.sme.getAddress() + " is on fixed channel " + this.fixedChannel, 100);
		this.message("Station " + this.sme.getAddress() + " is on switchable channel " + this.switchableChannel, 70);

		this.assignInitialChannels();
	}

	/*
	 * private JE802IPPacket addMetricRecord(final JE802IPPacket packet, final
	 * int channel) {
	 * 
	 * if(packet instanceof JE802RREQPacket || packet instanceof
	 * JE802RREPPacket) { double ett = routeStats.getETT(channel); double
	 * channelUsage = this.getChannelUsage(channel); double switchingcost =
	 * (1-channelUsage
	 * )*JE802RoutingConstants.CHANNEL_SWITCHING_DELAY.getTimeMs()/1000;
	 * JE802MCRHopRecord record = new JE802MCRHopRecord(ett, switchingcost,
	 * channel); if(packet instanceof JE802RREQPacket){ JE802RREQPacket rreq =
	 * (JE802RREQPacket) JE802IPPacket.copyPacket(packet);
	 * rreq.addMetricRecord(record); return rreq; } else if( packet instanceof
	 * JE802RREPPacket){ JE802RREPPacket rrep = (JE802RREPPacket)
	 * JE802IPPacket.copyPacket(packet); rrep.addMetricRecord(record); return
	 * rrep; } else { return null; } } else { return packet; } }
	 */

	// computes the initial channel assignment and changes the channel of the
	// different MACs's if necessary
	private void assignInitialChannels() {
		// switch channels according to the chosen fixed and switchable channel
		// numbers
		List<Integer> inUse = this.sme.getChannelsInUse();
		if (inUse.size() < 2) {
			error("For using the Hybrid Channel Manager, each station has to have at least two MACS, here at Station"
					+ this.sme.getAddress()
					+ " this is not the case, please set channelSwitching in the Routing layer to false or add a second mac to the station");
		}
		if (inUse.contains(this.fixedChannel)) {
			// fixed channel remains, switchable has to switch :)
			if (!inUse.contains(this.switchableChannel)) {
				if (inUse.indexOf(this.fixedChannel) == 0) {
					switchFromTo(inUse.get(1), this.switchableChannel, this.theUniqueEventScheduler.now());
				} else {
					switchFromTo(inUse.get(0), this.switchableChannel, this.theUniqueEventScheduler.now());
				}
			}
		} else {
			// both channels have to switch
			if (!inUse.contains(this.switchableChannel)) {
				switchFromTo(inUse.get(0), this.fixedChannel, this.theUniqueEventScheduler.now());
				switchFromTo(inUse.get(1), this.switchableChannel, this.theUniqueEventScheduler.now());
				// fixedChannel has to switch, switchable remains
			} else {
				if (inUse.indexOf(this.switchableChannel) == 0) {
					switchFromTo(inUse.get(1), this.fixedChannel, this.theUniqueEventScheduler.now());
				} else {
					switchFromTo(inUse.get(0), this.fixedChannel, this.theUniqueEventScheduler.now());
				}
			}
		}
	}

	@Override
	public void broadcastIPPacketAll(final JE802IPPacket packet) {
		// do not further broadcast packet if TTL not big enough
		if (packet.getTTL() >= 1) {
			if (this.state instanceof JEUnicastState) {
				this.state = new JEBroadcastState();
			}
			this.state.sendPacket(packet, null);
		}
	}

	@Override
	public void event_handler(final JEEvent anEvent) {
		String anEventName = anEvent.getName();
		if (anEventName.equals("Channel_Switched_ind")) {
			state = state.handleChannelSwitched(anEvent);
		} else if (anEventName.equals("broadcast_sent")) {
			state = state.handleBroadcastSent(anEvent);
			if (state instanceof JEUnicastState) {
				state.handleBroadcastSent(anEvent);
			}
		} else if (anEventName.equals("Channel_Switch_req")) {
			state = state.handleChannelSwitchReq(anEvent);
		} else if (anEventName.equals("push_back_packet")) {
			JE802IPPacket packet = (JE802IPPacket) anEvent.getParameterList().get(0);
			Integer channel = (Integer) anEvent.getParameterList().get(1);
			Integer da = (Integer) anEvent.getParameterList().get(2);
			JE802HopInfo nextHop = new JE802HopInfo(da, channel);
			List<JE802QueueEntry> queue = packetQueues.get(channel);
			queue.add(0, new JE802QueueEntry(packet, nextHop));
		} else {
			System.err.println("Undefined Event " + anEventName);
		}
	}

	@Override
	public void broadcastIPPacketChannel(final JE802IPPacket packet, final int channel) {
		System.err.println("Not yet implemented because not needed");
	}

	@Override
	public double getChannelUsage(final int channel) {
		WirelessChannel aChannel = null;
		for (WirelessChannel chan : this.availableChannels) {
			if (chan.getChannelNumber() == channel) {
				aChannel = chan;
			}
		}
		if (aChannel != null) {
			List<JE802UsageEntry> usages = this.channelUsagesTime.get(aChannel.getChannelNumber());
			if (!usages.isEmpty()) {
				if (usages.size() > JE802RoutingConstants.USAGE_WINDOW) {
					int fistSize = usages.get(0).getSize();
					usages.remove(0);
					Integer newSize = dataSizeOnChannel.get(channel) - fistSize;
					dataSizeOnChannel.put(channel, newSize);
				}
				int sizeSum = dataSizeOnChannel.get(channel);

				double interval = theUniqueEventScheduler.now().getTimeMs() - usages.get(0).getExpiryTime().getTimeMs();
				double bytesPerSecond = sizeSum / (interval / 1000);
				double maxBytesPerSecond = 6750000;
				double usage = bytesPerSecond / maxBytesPerSecond;
				return usage;
			}
			return 0.0;
		}
		return 0.0;
	}

	@Override
	public int getFirstChannelNo() {
		return this.fixedChannel;
	}

	private boolean isSwitching() {
		return this.switchingUntil.isLaterThan(this.theUniqueEventScheduler.now())
				|| this.fixedSwitchingUntil.isLaterThan(this.theUniqueEventScheduler.now());
	}

	private boolean isSwitchingSwitchable() {
		return this.switchingUntil.isLaterThan(this.theUniqueEventScheduler.now()); // ||
																					// this.switchingUntil.getTimeMs()
																					// ==
																					// theUniqueEventScheduler.now().getTimeMs();
	}

	private boolean isSwitchingFixed() {
		return this.fixedSwitchingUntil.isLaterThan(this.theUniqueEventScheduler.now()); // ||
																							// this.fixedSwitchingUntil.getTimeMs()
																							// ==
																							// theUniqueEventScheduler.now().getTimeMs();
	}

	private void switchFromTo(final int from, final int to, final JETime when) {
		if (isSwitching()) {
			System.err.println("Switching while switching");
		}
		if (from == this.fixedChannel) {
			this.error("Trying to change fixed channel");
		} else if (to == this.fixedChannel) {
			this.error("Trying to change the switchable channel to the fixed channel");
		} else if (from == to) {
			this.error("Trying to change channel to same channel");
			return;
		}
		// this.message(" Station " + sme.getAddress() + " switching from " +
		// from + " to "+ to, 70);
		Vector<Object> parameterList = new Vector<Object>();
		parameterList.add(from);
		parameterList.add(to);
		this.switchingUntil = when;
		this.send(new JEEvent("Channel_Switch_req", this.sme, this.switchingUntil, parameterList));
		this.send(new JEEvent("Channel_Switched_ind", getHandlerId(), this.switchingUntil, parameterList));
	}

	@Override
	public void switchTo(final int channeNum) {
		this.error("Switch to not implemented, should not be called");
	}

	@Override
	public void switchToNextChannel() {
		int maxChannelNr = 0;
		int maxSize = 0;
		for (WirelessChannel channel : this.availableChannels) {
			if (channel.getChannelNumber() != this.fixedChannel && channel.getChannelNumber() != this.switchableChannel
					&& this.packetQueues.get(channel.getChannelNumber()).size() > maxSize) {
				maxSize = this.packetQueues.get(channel.getChannelNumber()).size();
				maxChannelNr = channel.getChannelNumber();
			}
		}
		// only switch if not already on the channel with the biggest number of
		// packets in queue, and not currently switching
		if (maxSize > 0 && this.switchableChannel != maxChannelNr && this.fixedChannel != maxChannelNr && !isSwitching()) {
			switchFromTo(this.switchableChannel, maxChannelNr,
					this.theUniqueEventScheduler.now().plus(JE802RoutingConstants.CHANNEL_SWITCHING_DELAY));
			this.switchableChannel = maxChannelNr;
		} else {
			this.send(new JEEvent("Channel_Switch_req", getHandlerId(), this.theUniqueEventScheduler.now().plus(
					JE802RoutingConstants.MAX_SWITCHING_INTERVAL)));
		}
	}

	@Override
	public String toString() {
		return "ChannelMgr station " + this.sme.getAddress();
	}

	@Override
	// send packet to address specified in nextHop
	public void unicastIPPacket(final JE802IPPacket packet, final JE802HopInfo nextHop) {
		if (packet.getTTL() >= 1) {
			if (state instanceof JEBroadcastState) {
				// quick fix for a bug in the channel manager
				// TODO: somewhere is a starvation bug where it never comes out
				// of the broadcast state again
				JEBroadcastState broadState = (JEBroadcastState) state;
				if (broadState.getBroadcastStart().isEarlierThan(theUniqueEventScheduler.now().minus(new JETime(20)))) {
					this.state = new JEUnicastState();
					this.state.sendPacket(packet, nextHop);
				}
				List<JE802QueueEntry> queue = this.packetQueues.get(nextHop.getChannel());
				queue.add(new JE802QueueEntry(packet, nextHop));
			} else {
				this.state.sendPacket(packet, nextHop);
			}
		}
	}

	private void addUsage(int length, int channel) {
		JE802UsageEntry usage = new JE802UsageEntry(length, this.theUniqueEventScheduler.now());
		channelUsagesTime.get(channel).add(usage);
		Integer newSize = dataSizeOnChannel.get(channel) + length;
		dataSizeOnChannel.put(channel, newSize);
	}

	@Override
	// checks whether to switch the fixed channel to a new channel or not
	public int checkFixedSwitch(final Map<Integer, Integer> neighborhoodChannelUsages) {
		int min = Integer.MAX_VALUE;
		// determine the minimal number of neighbors
		int minChannelNr = 0;
		for (WirelessChannel channel : availableChannels) {
			Integer count = neighborhoodChannelUsages.get(channel.getChannelNumber());
			if (count == null) {
				count = 0;
			}
			if (count < min) {
				min = count;
				minChannelNr = channel.getChannelNumber();
			}
		}
		// put all channels with this minimal number of neighbors into a list
		List<Integer> minimalChannels = new ArrayList<Integer>();
		for (WirelessChannel channel : availableChannels) {
			Integer count = neighborhoodChannelUsages.get(channel.getChannelNumber());
			if (count != null) {
				if (count == min) {
					minimalChannels.add(channel.getChannelNumber());
				}
			} else {
				minimalChannels.add(channel.getChannelNumber());
			}
		}
		// of all the channels which have minimal number of neighbors, select
		// one of them randomly
		// this is done because if just the first one is taken, the channels are
		// not uniformly distributed and more stations occupy the lower numbered
		// channels, which is suboptimal
		if (minimalChannels.size() > 1) {
			minChannelNr = minimalChannels.get(theUniqueRandomGenerator.nextInt(minimalChannels.size()));
		}
		// only switch with a certain probability
		double rand = theUniqueRandomGenerator.nextDouble();
		if (rand <= JE802RoutingConstants.switchProbability) {
			if (minChannelNr != this.fixedChannel && minChannelNr != this.switchableChannel) {
				Vector<Object> parameterList = new Vector<Object>();
				parameterList.add(this.fixedChannel);
				parameterList.add(minChannelNr);
				this.fixedSwitchingUntil = this.theUniqueEventScheduler.now().plus(JE802RoutingConstants.CHANNEL_SWITCHING_DELAY);
				this.send(new JEEvent("Channel_Switch_req", this.sme, this.fixedSwitchingUntil, parameterList));
				this.send(new JEEvent("Channel_Switched_ind", this.getHandlerId(), this.fixedSwitchingUntil, parameterList));
				this.message("Station " + this.sme.getAddress() + " switching fixed channel from " + this.fixedChannel + " to "
						+ minChannelNr);
				this.fixedChannel = minChannelNr;
				return minChannelNr;
			}
		}
		return this.fixedChannel;
	}

	private class JE802QueueEntry {

		private final JE802IPPacket packet;

		private final JE802HopInfo nextHop;

		public JE802QueueEntry(final JE802IPPacket packet, final JE802HopInfo nextHop) {
			this.packet = packet;
			this.nextHop = nextHop;
		}

		public JE802HopInfo getNextHop() {
			return this.nextHop;
		}

		public JE802IPPacket getPacket() {
			return this.packet;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof JE802IPPacket) {
				JE802IPPacket otherPacket = (JE802IPPacket) obj;
				return otherPacket == this.packet;
			}
			return false;
		}

		@Override
		public String toString() {
			return "QueueEntry: " + this.packet + " NextHop: " + this.nextHop;
		}
	}

	private class JE802UsageEntry {

		private final JETime expiryTime;

		private final int size;

		public JE802UsageEntry(final int size, final JETime expiryTime) {
			this.size = size;
			this.expiryTime = expiryTime;
		}

		public JETime getExpiryTime() {
			return this.expiryTime;
		}

		public int getSize() {
			return this.size;
		}

		@Override
		public String toString() {
			return this.size + " at " + this.expiryTime;
		}
	}

	private class JEBroadcastState extends JEHybridChannelState {

		// queue for broadcast, special because broadcasts are sent on all
		// channels
		private final List<JE802IPPacket> broadcastQueue = new ArrayList<JE802IPPacket>();

		private final JETime broadcastStart = theUniqueEventScheduler.now();

		private List<WirelessChannel> toSendBroadcastChannels = new ArrayList<WirelessChannel>(availableChannels);

		@Override
		protected JEHybridChannelState handleBroadcastSent(JEEvent event) {
			Integer channelNo = (Integer) event.getParameterList().get(0);

			for (int i = 0; i < toSendBroadcastChannels.size(); i++) {
				WirelessChannel channel = toSendBroadcastChannels.get(i);
				if (channel.getChannelNumber() == channelNo) {
					toSendBroadcastChannels.remove(i);
					break;
				}
			}
			if (!toSendBroadcastChannels.isEmpty()) {
				// ignore this if the the broadcast was sent on the fixed
				// channel because we're only interested in switching the
				// switchable channel
				if (channelNo != fixedChannel) {
					if (!isSwitchingSwitchable()) {
						if (toSendBroadcastChannels.size() == 1
								&& toSendBroadcastChannels.get(0).getChannelNumber() == fixedChannel) {
							return new JEUnicastState();
						}
						int switchTo = toSendBroadcastChannels.get(0).getChannelNumber();
						int i = 0;
						while (switchTo == fixedChannel) {
							switchTo = toSendBroadcastChannels.get(i).getChannelNumber();
							i++;
						}
						if (switchTo != switchableChannel) {
							switchFromTo(switchableChannel, switchTo,
									theUniqueEventScheduler.now().plus(JE802RoutingConstants.CHANNEL_SWITCHING_DELAY));
							switchableChannel = switchTo;
						}
					}
				} else {
					return this;
				}
				// send next broadcast from broadcast Queue
			} else {
				if (!broadcastQueue.isEmpty()) {
					broadcastQueue.remove(0);
				}
				if (!broadcastQueue.isEmpty()) {
					toSendBroadcastChannels = new ArrayList<WirelessChannel>(availableChannels);
					// List<WirelessChannel> shuffled = new
					// ArrayList<WirelessChannel>(availableChannels);
					// Collections.shuffle(shuffled, theUniqueRandomGenerator);
					for (WirelessChannel channel : availableChannels) {
						if (channel.getChannelNumber() == fixedChannel && !isSwitchingFixed()) {
							broadcast(broadcastQueue.get(0), fixedChannel, theUniqueEventScheduler.now());
						} else if (channel.getChannelNumber() == switchableChannel && !isSwitchingSwitchable()) {
							broadcast(broadcastQueue.get(0), switchableChannel, theUniqueEventScheduler.now());
						}
					}
				} else {
					return new JEUnicastState();
				}
			}
			return this;
		}

		@Override
		protected JEHybridChannelState handleChannelSwitchReq(JEEvent event) {
			// ignore the event, we don't want to switch while broadcasting
			return this;
		}

		public JETime getBroadcastStart() {
			return broadcastStart;
		}

		@Override
		protected JEHybridChannelState handleChannelSwitched(JEEvent event) {
			Integer channel = (Integer) event.getParameterList().get(1);
			if (channel == fixedChannel) {
				broadcast(this.broadcastQueue.get(0), fixedChannel, theUniqueEventScheduler.now());
			}
			broadcast(this.broadcastQueue.get(0), switchableChannel, theUniqueEventScheduler.now());
			if (toSendBroadcastChannels.isEmpty()) {
				broadcastQueue.remove(0);
				if (broadcastQueue.isEmpty()) {
					return new JEUnicastState();
				}
			}
			return this;
		}

		@Override
		protected JEHybridChannelState sendPacket(JE802IPPacket packet, JE802HopInfo nextHop) {
			this.broadcastQueue.add(packet);
			boolean isSwitching = isSwitching();
			if (this.broadcastQueue.size() < 2 && !isSwitching) {
				// for all available channels, send broadcast on that channel,
				// send broadcast on current channel directly now
				// List<WirelessChannel> shuffled = new
				// ArrayList<WirelessChannel>(availableChannels);
				// Collections.shuffle(shuffled, theUniqueRandomGenerator);
				for (WirelessChannel channel : availableChannels) {
					if (channel.getChannelNumber() == fixedChannel && !isSwitchingFixed()) {
						this.toSendBroadcastChannels.remove(channel);
						broadcast(packet, fixedChannel, theUniqueEventScheduler.now());
					} else if (channel.getChannelNumber() == switchableChannel && !isSwitchingSwitchable()) {
						this.toSendBroadcastChannels.remove(channel);
						broadcast(packet, switchableChannel, theUniqueEventScheduler.now());
					}
				}
			} else if (!isSwitching) {
				broadcast(broadcastQueue.get(0), switchableChannel, theUniqueEventScheduler.now());
			}
			return this;
		}

		private void broadcast(final JE802IPPacket packet, final int channel, final JETime when) {
			JE802IPPacket broadcastPacket = packet;
			broadcastPacket = JE802IPPacket.copyPacket(packet);
			if (broadcastPacket instanceof JE802RREQPacket) {
				JE802RREQPacket rreq = (JE802RREQPacket) broadcastPacket;
				double channelUsage = getChannelUsage(channel);
				double switchingcost = (1 - channelUsage) * JE802RoutingConstants.CHANNEL_SWITCHING_DELAY.getTimeMs() / 1000;
				rreq.setLastHopSwitchingCost(switchingcost);
			}
			addUsage(broadcastPacket.getLength(), channel);
			Vector<Object> parameterList = new Vector<Object>();
			JE802HopInfo DA = new JE802HopInfo(255, channel);
			parameterList.add(DA);
			parameterList.add(broadcastPacket.getAC());
			List<JE802HopInfo> hops = new ArrayList<JE802HopInfo>();
			hops.add(DA);
			parameterList.add(hops);
			parameterList.add(broadcastPacket);
			send(new JEEvent("IP_Deliv_req", sme, when, parameterList));
		}
	}

	private class JEUnicastState extends JEHybridChannelState {

		@Override
		protected JEHybridChannelState handleBroadcastSent(JEEvent event) {
			this.sendOutstandingPackets();
			return this;
		}

		@Override
		protected JEHybridChannelState handleChannelSwitchReq(JEEvent event) {
			switchToNextChannel();
			return this;
		}

		@Override
		protected JEHybridChannelState handleChannelSwitched(JEEvent event) {
			sendOutstandingPackets();
			return this;
		}

		private void sendOutstandingPackets() {
			// send all packets in the queue for the fixed channel
			List<JE802QueueEntry> channelQueue = packetQueues.get(fixedChannel);
			for (JE802QueueEntry entry : channelQueue) {
				addUsage(entry.getPacket().getLength(), entry.getNextHop().getChannel());
				Vector<Object> parameterList = new Vector<Object>();
				parameterList.add(entry.getNextHop());
				parameterList.add(entry.getPacket().getAC());
				List<JE802HopInfo> hops = new ArrayList<JE802HopInfo>();
				hops.add(entry.getNextHop());
				parameterList.add(hops);
				parameterList.add(entry.getPacket());
				send(new JEEvent("IP_Deliv_req", sme, theUniqueEventScheduler.now(), parameterList));
			}
			channelQueue.clear();
			// send all packets in the queue for the switchable channel
			channelQueue = packetQueues.get(switchableChannel);
			if (!channelQueue.isEmpty() && !isSwitching()) {
				for (JE802QueueEntry entry : channelQueue) {
					addUsage(entry.getPacket().getLength(), entry.getNextHop().getChannel());
					Vector<Object> parameterList = new Vector<Object>();
					parameterList.add(entry.getNextHop());
					parameterList.add(entry.getPacket().getAC());
					List<JE802HopInfo> hops = new ArrayList<JE802HopInfo>();
					hops.add(entry.getNextHop());
					parameterList.add(hops);
					parameterList.add(entry.getPacket());
					send(new JEEvent("IP_Deliv_req", sme, theUniqueEventScheduler.now(), parameterList));
				}
				channelQueue.clear();
				send(new JEEvent("Channel_Switch_req", getHandlerId(), theUniqueEventScheduler.now().plus(
						JE802RoutingConstants.MAX_SWITCHING_INTERVAL)));
			} else {
				switchToNextChannel();
			}
		}

		// only possible if switchable queue is empty and all other queues are
		// empty except the on for the channel to switch to
		private boolean checkFastSwitchPossible(final int chan) {
			if (isSwitching() || switchableChannel == chan || fixedChannel == chan) {
				return false;
			}
			for (WirelessChannel channel : availableChannels) {
				int channelNum = channel.getChannelNumber();
				List<JE802QueueEntry> queue = packetQueues.get(channelNum);
				if (channelNum != chan) {
					if (!queue.isEmpty()) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		protected JEHybridChannelState sendPacket(JE802IPPacket packet, JE802HopInfo nextHop) {
			JE802IPPacket unicastPacket = packet;
			unicastPacket = JE802IPPacket.copyPacket(packet);
			/*
			 * if(JE802RoutingConstants.MCRMetricEnabled){ unicastPacket =
			 * addMetricRecord(packet, nextHop.getChannel()); } else {
			 * unicastPacket = JE802IPPacket.copyPacket(packet); }
			 */

			// only transmit if medium is not switching
			if (!isSwitching() && (fixedChannel == nextHop.getChannel() || switchableChannel == nextHop.getChannel())) {
				addUsage(unicastPacket.getLength(), nextHop.getChannel());
				Vector<Object> parameterList = new Vector<Object>();
				parameterList.add(nextHop);
				parameterList.add(unicastPacket.getAC());
				List<JE802HopInfo> hops = new ArrayList<JE802HopInfo>();
				hops.add(nextHop);
				parameterList.add(hops);
				parameterList.add(unicastPacket);
				send(new JEEvent("IP_Deliv_req", sme, theUniqueEventScheduler.now(), parameterList));
			} else {
				List<JE802QueueEntry> queue = packetQueues.get(nextHop.getChannel());
				queue.add(new JE802QueueEntry(unicastPacket, nextHop));
				if (checkFastSwitchPossible(nextHop.getChannel())) {
					switchFromTo(switchableChannel, nextHop.getChannel(),
							theUniqueEventScheduler.now().plus(JE802RoutingConstants.CHANNEL_SWITCHING_DELAY));
					switchableChannel = nextHop.getChannel();
				}
			}
			return this;
		}
	}

	private abstract class JEHybridChannelState {

		protected abstract JEHybridChannelState handleChannelSwitched(JEEvent event);

		protected abstract JEHybridChannelState handleBroadcastSent(JEEvent event);

		protected abstract JEHybridChannelState handleChannelSwitchReq(JEEvent event);

		protected abstract JEHybridChannelState sendPacket(JE802IPPacket packet, JE802HopInfo nextHop);

	}

	@Override
	public boolean hasPacketsInQueue() {
		return !packetQueues.get(switchableChannel).isEmpty();
	}

	@Override
	public void sendPacketFromQueue() {
		if (state instanceof JEUnicastState) {
			List<JE802QueueEntry> queue = packetQueues.get(switchableChannel);
			if (!queue.isEmpty()) {
				JE802QueueEntry queueEntry = queue.get(0);
				this.unicastIPPacket(queueEntry.getPacket(), queueEntry.getNextHop());
				queue.remove(0);
			}
		}
	}
}