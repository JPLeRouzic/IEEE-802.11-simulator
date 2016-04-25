package zzInfra.layer3_network;

import IEEE11ac.layer2.acHopInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import zzInfra.ARC.JE802Sme;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.layer3_network.JE802HopInfo;;

public class JE802FixedChannelManager extends JEEventHandler implements JE802IChannelManager {

	private final JE802Sme sme;

	private final List<Integer> availableChannels;

	public JE802FixedChannelManager(JEEventScheduler aScheduler, Random aGenerator, JE802Sme aSme) {
		super(aScheduler, aGenerator);
		this.sme = aSme;
		this.sme.setChannelHandlerId(this.getHandlerId());
		this.availableChannels = this.sme.getChannelsInUse();
	}

	@Override
	public void broadcastIPPacketAll(JE802IPPacket packet) {
		if (packet.getTTL() >= 1) {
			for (Integer channel : availableChannels) {
				this.broadcastIPPacketChannel(packet, channel);
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

	@Override
	public void broadcastIPPacketChannel(JE802IPPacket packet, int channel) {
		packet = copyPacket(packet);
		Vector<Object> parameterList = new Vector<Object>();
		acHopInfo DA = new acHopInfo(255, channel);
		parameterList.add(DA);
		parameterList.add(packet.getAC());
		List<acHopInfo> hops = new ArrayList<acHopInfo>();
		hops.add(DA);
		parameterList.add(hops);
		parameterList.add(packet);
		this.send(new JEEvent("IP_Deliv_req", sme, theUniqueEventScheduler.now(), parameterList));
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		String eventName = anEvent.getName();
		if (eventName.equals("broadcast_sent")) {
			// ignore
		} else {
			this.error("Undefined Event " + eventName + " at FixedChannelManager at Station " + sme.getAddress());
		}
	}

	@Override
	public int checkFixedSwitch(Map<Integer, Integer> neighborhoodChannelUsages) {
		return availableChannels.get(0);
	}

	@Override
	public double getChannelUsage(int channel) {
		// we don't log channel usage when using fixed channel model
		return 0;
	}

	@Override
	public int getFirstChannelNo() {
		return availableChannels.get(0);
	}

	@Override
	public void switchTo(int channeNum) {
		this.error("No Channel Switching possible with fixed Channel Manager");
	}

	@Override
	public void switchToNextChannel() {
		this.error("No Channel Switching possible with fixed Channel Manager");
	}

//	@Override
	public void unicastIPPacket(JE802IPPacket packet, acHopInfo nextHop) {
		if (packet.getTTL() >= 1) {
			Vector<Object> parameterList = new Vector<Object>();
			parameterList.clear();
			parameterList.add(nextHop);
			parameterList.add(packet.getAC());
			List<acHopInfo> hops = new ArrayList<acHopInfo>();
			hops.add(nextHop);
			parameterList.add(hops);
			parameterList.add(packet);
			this.send(new JEEvent("IP_Deliv_req", sme, theUniqueEventScheduler.now(), parameterList));
		}
	}

	@Override
	// retrun false since in this type of channel manager does not have a queue
	public boolean hasPacketsInQueue() {
		return false;
	}

	@Override
	public void sendPacketFromQueue() {
		// do nothing since in this type of channel manager, we don't have a
		// queue

	}

	@Override
	public void unicastIPPacket(JE802IPPacket packet, JE802HopInfo nextHop) {
		if (packet.getTTL() >= 1) {
			Vector<Object> parameterList = new Vector<Object>();
			parameterList.clear();
			parameterList.add(nextHop);
			parameterList.add(packet.getAC());
			List<JE802HopInfo> hops = new ArrayList<JE802HopInfo>();
			hops.add(nextHop);
			parameterList.add(hops);
			parameterList.add(packet);
			this.send(new JEEvent("IP_Deliv_req", sme, theUniqueEventScheduler.now(), parameterList));
		}
	}

}
