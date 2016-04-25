package zzInfra.layer3_network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.layer0.WirelessChannel;

public class JE802RoutingStatistics {

	// per channel, per neighbor
	private Map<Integer, Map<Integer, Double>> linkFailureProbabilities;

	public JE802RoutingStatistics(List<WirelessChannel> channels) {
		linkFailureProbabilities = new HashMap<Integer, Map<Integer, Double>>();
		for (WirelessChannel channel : channels) {
			Map<Integer, Double> linkfailures = new HashMap<Integer, Double>();
			linkFailureProbabilities.put(channel.getChannelNumber(), linkfailures);
		}
	}

	public void addPacketReport(int channel, int destination, boolean delivered, int retries) {
		Map<Integer, Double> probabilitiesPerLink = linkFailureProbabilities.get(channel);
		Double probability = probabilitiesPerLink.get(destination);
		if (probability == null) {
			probabilitiesPerLink.put(destination, 0.0);
		}
		double probabilityOld = probabilitiesPerLink.get(destination);

		double pNew = 0.99 * probabilityOld;
		for (int i = 0; i < retries; i++) {
			pNew = 0.99 * probabilityOld + 0.01 * 1.0;
		}
		if (delivered) {
			pNew = 0.99 * probabilityOld;
		}
		pNew = Math.min(pNew, 1);
		probabilitiesPerLink.put(destination, pNew);
	}

	public double getETT(int channel, int destination) {
		Map<Integer, Double> linkFailureProb = linkFailureProbabilities.get(channel);
		Double prob = linkFailureProb.get(destination);
		if (prob == null) {
			prob = 0.0;
		}
		double etx = 1 / (1 - prob);
		double bytePerSecond = 54 / 8.0 * 1E6; // assumes highest phymode
		double ett = etx * (1024 / bytePerSecond);
		return ett;
	}
}
