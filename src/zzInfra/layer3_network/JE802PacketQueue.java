package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.List;

import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JEmula;
import zzInfra.layer3_network.JE802HopInfo;;

public class JE802PacketQueue extends JEmula {

	private List<JE802IPPacket> packetList;

	public JE802PacketQueue(JEEventScheduler scheduler) {
		this.theUniqueEventScheduler = scheduler;
		this.packetList = new ArrayList<JE802IPPacket>();
	}

	public void addPacket(JE802IPPacket packet) {
		// TODO: remove this, make queue fixed size
		if (packetList.size() < 100) {
			packetList.add(packet);
		} else {
			packetList.clear();
		}

	}

	public void discardPackets(JE802HopInfo da) {
		List<JE802IPPacket> toDiscard = new ArrayList<JE802IPPacket>();
		for (JE802IPPacket packet : packetList) {
			if (packet.getDA().equalsAddr(da)) {
				toDiscard.add(packet);
			}
		}
		packetList.removeAll(toDiscard);
	}

	public List<JE802IPPacket> getPacketsForDestination(JE802HopInfo dest) {
		List<JE802IPPacket> toRemove = new ArrayList<JE802IPPacket>();
		for (JE802IPPacket packet : packetList) {
			if (packet.getDA().equalsAddr(dest)) {
				toRemove.add(packet);
			}
		}
		packetList.removeAll(toRemove);
		return toRemove;
	}
}
