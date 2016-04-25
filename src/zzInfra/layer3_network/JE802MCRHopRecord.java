package zzInfra.layer3_network;

public class JE802MCRHopRecord {

	private final double ett;

	private final double switchingCost;

	private final int channel;

	// Multi Channel Routing Hop Record
	protected JE802MCRHopRecord(double ett, double switchingcost, int channel) {
		this.ett = ett;
		this.switchingCost = switchingcost;
		this.channel = channel;
	}

	protected int getChannel() {
		return channel;
	}

	protected double getEtt() {
		return ett;
	}

	protected double getSwitchingCost() {
		return switchingCost;
	}
}
