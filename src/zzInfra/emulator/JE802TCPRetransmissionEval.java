package zzInfra.emulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JEmula;

public class JE802TCPRetransmissionEval extends JEmula {

	private String path2Results;

	private Map<Integer, Map<Integer, JE802TCPLossRecord>> recordMap;

	// for naming the files accordingly
	private Map<Integer, Map<Integer, String>> trafficTypes;

	public JE802TCPRetransmissionEval(String path2Results, JEEventScheduler aScheduler) {
		this.path2Results = path2Results;
		this.theUniqueEventScheduler = aScheduler;
		this.recordMap = new HashMap<Integer, Map<Integer, JE802TCPLossRecord>>();
		this.trafficTypes = new HashMap<Integer, Map<Integer, String>>();
	}

	public void sentPacket(int station, int port) {
		Map<Integer, JE802TCPLossRecord> portMap = recordMap.get(station);
		if (portMap == null) {
			portMap = new HashMap<Integer, JE802TCPLossRecord>();
			recordMap.put(station, portMap);
		}
		JE802TCPLossRecord record = portMap.get(port);
		if (record == null) {
			record = new JE802TCPLossRecord();
			portMap.put(port, record);
		}
		record.increaseSent();
	}

	public void ackedPacket(int station, int port) {
		Map<Integer, JE802TCPLossRecord> portMap = recordMap.get(station);
		if (portMap == null) {
			portMap = new HashMap<Integer, JE802TCPLossRecord>();
			recordMap.put(station, portMap);
		}
		JE802TCPLossRecord record = portMap.get(port);
		if (record == null) {
			record = new JE802TCPLossRecord();
			portMap.put(port, record);
		}
		record.increaseAcked();
	}

	public void evaluate() {
		for (Integer station : recordMap.keySet()) {
			Map<Integer, JE802TCPLossRecord> portMap = recordMap.get(station);
			for (Integer port : portMap.keySet()) {
				JE802TCPLossRecord record = portMap.get(port);
				String type = trafficTypes.get(station).get(port);
				String filename = path2Results + "/retransmissions_SA" + station + "_Port" + port + "_" + type + ".txt";
				double rate = (double) record.getSent() / record.getAcked() - 1.0;
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename), true));
					String line = theUniqueEventScheduler.now().getTimeMs() + " " + record.getSent() + " " + record.getAcked()
							+ " " + rate + "\n";
					writer.write(line);
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				record.reset();
			}
		}
	}

	public void endOfEmulation() {
		// nothing to do here;
	}

	private class JE802TCPLossRecord {

		private int sent = 0;

		private int acked = 0;

		public void reset() {
			sent = 0;
			acked = 0;
		}

		public void increaseSent() {
			sent++;
		}

		public void increaseAcked() {
			acked++;
		}

		public int getSent() {
			return sent;
		}

		public int getAcked() {
			return acked;
		}
	}

	public void setTrafficType(int station, int port, String type) {
		Map<Integer, String> stationMap = trafficTypes.get(station);
		if (stationMap == null) {
			stationMap = new HashMap<Integer, String>();
			trafficTypes.put(station, stationMap);
		}
		stationMap.put(port, type);
	}
}
