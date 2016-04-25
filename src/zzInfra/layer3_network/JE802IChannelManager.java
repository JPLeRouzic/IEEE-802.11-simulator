package zzInfra.layer3_network;

import java.util.Map;

import zzInfra.layer3_network.JE802HopInfo;;

public interface JE802IChannelManager {

	public void broadcastIPPacketAll(JE802IPPacket packet);

	public void broadcastIPPacketChannel(JE802IPPacket packet, int channel);

	public double getChannelUsage(int channel);

	public int getFirstChannelNo();

	public void switchTo(int channeNum);

	public void switchToNextChannel();

	public void unicastIPPacket(JE802IPPacket packet, JE802HopInfo nextHop);

	public int checkFixedSwitch(Map<Integer, Integer> neighborhoodChannelUsages);

	public boolean hasPacketsInQueue();

	public void sendPacketFromQueue();

}
