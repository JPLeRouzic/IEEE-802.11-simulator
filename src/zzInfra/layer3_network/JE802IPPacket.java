package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.List;

import zzInfra.layer3_network.JE802HopInfo;;
import zzInfra.layer4_transport.JE802TCPPacket;

public class JE802IPPacket {

	protected final JE802HopInfo sourceAddress;

	protected final JE802HopInfo destinationAddress;

	protected final JE802TCPPacket payload;

	protected final int sourceHandlerId;

	protected final int AC;

	// part of the MCR extension, not part of rfc 3561, in datapackets only
	// needed for logging
	protected int lastHopFixedChannel;

	protected int ttl = JE802RoutingConstants.maxTTL;

	protected List<JE802HopInfo> route;

	public JE802IPPacket(JE802HopInfo sourceAddress, JE802HopInfo destinationAddress, JE802TCPPacket tcpPacket,
			int sourceHandler, int ac) {
		this.sourceAddress = sourceAddress;
		this.destinationAddress = destinationAddress;
		this.payload = tcpPacket;
		this.sourceHandlerId = sourceHandler;
		this.AC = ac;
		this.route = new ArrayList<JE802HopInfo>();
	}

	public JE802IPPacket(JE802IPPacket toCopy) {
		this.sourceAddress = toCopy.getSA();
		this.destinationAddress = toCopy.getDA();
		this.payload = toCopy.getPayload();
		this.AC = toCopy.getAC();
		this.sourceHandlerId = toCopy.getSourceHandlerId();
		this.ttl = toCopy.getTTL();
		this.route = new ArrayList<JE802HopInfo>(toCopy.getRouteHops());
		this.lastHopFixedChannel = toCopy.getLastHopFixedChannel();
	}

	public void decreaseTTL() {
		ttl--;
	}

	public void setLastHopFixedChannel(int lastHopFixedChannel) {
		this.lastHopFixedChannel = lastHopFixedChannel;
	}

	public int getLastHopFixedChannel() {
		return lastHopFixedChannel;
	}

	public JE802HopInfo getDA() {
		return destinationAddress;
	}

	public boolean isControlPacket() {
		return false;
	}

	public int getLength() {
		if (payload != null) {
			return payload.getLength() + JE802RoutingConstants.IP_HEADER_BYTE;
		} else {
			return JE802RoutingConstants.IP_HEADER_BYTE;
		}
	}

	public JE802TCPPacket getPayload() {
		return payload;
	}

	public JE802HopInfo getSA() {
		return sourceAddress;
	}

	public int getSourceHandlerId() {
		return sourceHandlerId;
	}

	public int getAC() {
		return AC;
	}

	public int getTTL() {
		return ttl;
	}

	public void addRouteHop(JE802HopInfo hop) {
		if (!route.contains(hop)) {
			route.add(hop);
		}
	}

	public List<JE802HopInfo> getRouteHops() {
		return route;
	}

	public static JE802IPPacket copyPacket(final JE802IPPacket packet) {
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
	public String toString() {
		return "Source: " + sourceAddress + " Destination" + destinationAddress + " TTL:" + ttl;
	}
}
