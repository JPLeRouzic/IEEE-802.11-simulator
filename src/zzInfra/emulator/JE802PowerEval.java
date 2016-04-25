package zzInfra.emulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import zzInfra.kernel.JETime;

public class JE802PowerEval {

	private String path2Results;

	private JETime evalStartTime;

	private JETime evalEndTime;

	private Map<Integer, Map<Integer, JE802PowerMeter>> powerConsumptions = new HashMap<Integer, Map<Integer, JE802PowerMeter>>();

	public JE802PowerEval(String path2Results, JETime evaluationStart, JETime evaluationEnd) {
		this.path2Results = path2Results;
		this.evalStartTime = evaluationStart;
		this.evalEndTime = evaluationEnd;
	}

	public void recordPowerTx(int station, int phyId, JETime when, JETime duration) {
		if (when.isLaterThan(evalStartTime)) {
			Map<Integer, JE802PowerMeter> stationMap = powerConsumptions.get(station);
			if (stationMap == null) {
				stationMap = new HashMap<Integer, JE802PowerMeter>();
				powerConsumptions.put(station, stationMap);
			}
			JE802PowerMeter powerMeter = stationMap.get(phyId);
			if (powerMeter == null) {
				powerMeter = new JE802PowerMeter(evalEndTime.minus(evalStartTime));
				stationMap.put(phyId, powerMeter);
			}
			powerMeter.addPowerTx(when, duration);
		}
	}

	public void recordPowerRx(int station, int phyId, JETime when, JETime duration) {
		if (when.isLaterThan(evalStartTime)) {
			Map<Integer, JE802PowerMeter> stationMap = powerConsumptions.get(station);
			if (stationMap == null) {
				stationMap = new HashMap<Integer, JE802PowerMeter>();
				powerConsumptions.put(station, stationMap);
			}
			JE802PowerMeter powerMeter = stationMap.get(phyId);
			if (powerMeter == null) {
				powerMeter = new JE802PowerMeter(evalEndTime.minus(evalStartTime));
				stationMap.put(phyId, powerMeter);
			}
			powerMeter.addPowerRx(when, duration);
		}
	}

	public void evaluatePowerConsumption() {
		// Write power Files
		double totalRxPowerConsumption = 0.0;
		double totalTxPowerConsumption = 0.0;
		double totalIdlePowerConsumption = 0.0;
		for (Integer addr : powerConsumptions.keySet()) {
			File powerFile = new File(path2Results + "/power_Station " + addr + ".txt");
			try {
				FileWriter powerFileWriter = new FileWriter(powerFile);
				Map<Integer, JE802PowerMeter> powerAtStation = powerConsumptions.get(addr);
				int i = 1;
				double stationTotal = 0.0;
				for (JE802PowerMeter meter : powerAtStation.values()) {
					String id = "Station " + addr + " Phy " + i;
					String tx = id + " Rx: " + meter.getRxTotalConsumption_J() + " J\n";
					String rx = id + " Tx: " + meter.getTxTotalConsumption_J() + " J\n";
					String idle = id + " Idle: " + meter.getIdleConsumption_J() + " J\n";
					String total = id + " Total: " + meter.getTotalConsumption_J() + " J\n";
					powerFileWriter.append(tx);
					powerFileWriter.append(rx);
					powerFileWriter.append(idle);
					powerFileWriter.append(total);
					totalRxPowerConsumption += meter.getRxTotalConsumption_J();
					totalTxPowerConsumption += meter.getTxTotalConsumption_J();
					totalIdlePowerConsumption += meter.getIdleConsumption_J();
					stationTotal += meter.getTotalConsumption_J();
					i++;
				}
				powerFileWriter.append("Station " + addr + " Total " + stationTotal + " J\n");
				powerFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File powerFile = new File(path2Results + "/power_Overall.txt");
		FileWriter powerFileWriter;
		try {
			powerFileWriter = new FileWriter(powerFile);
			powerFileWriter.append("Overall Rx: " + totalRxPowerConsumption + " J\n");
			powerFileWriter.append("Overall Tx: " + totalTxPowerConsumption + " J\n");
			powerFileWriter.append("Overall Idle: " + totalIdlePowerConsumption + " J\n");
			powerFileWriter.append("Overall: " + (totalTxPowerConsumption + totalRxPowerConsumption + totalIdlePowerConsumption)
					+ " J\n");
			powerFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class JE802PowerMeter {

		private double rxTotalConsumption_J = 0.0;

		private double txTotalConsumption_J = 0.0;

		private final double txPowerLevel_W = 1.280;

		private final double rxPowerLevel_W = 0.820;

		private final double idlePowerLevel_W = 0.045;

		private double activeDuration_ms = 0.0;

		private final JETime evaluationLength;

		public JE802PowerMeter(JETime evaluationLength) {
			this.evaluationLength = evaluationLength;
		}

		// when intended for possible later use, if a more complex model would
		// be needed
		public void addPowerTx(JETime when, JETime duration) {
			txTotalConsumption_J += duration.getTimeS() * txPowerLevel_W;
			activeDuration_ms += duration.getTimeMs();
		}

		// when intended for possible later use, if a more complex model would
		// be needed
		public void addPowerRx(JETime when, JETime duration) {
			rxTotalConsumption_J += duration.getTimeS() * rxPowerLevel_W;
			activeDuration_ms += duration.getTimeMs();
		}

		public double getRxTotalConsumption_J() {
			return rxTotalConsumption_J;
		}

		public double getIdleConsumption_J() {
			double idleTime_S = (evaluationLength.getTimeMs() - activeDuration_ms) / 1000;
			return idleTime_S * idlePowerLevel_W;
		}

		public double getTxTotalConsumption_J() {
			return txTotalConsumption_J;
		}

		public double getTotalConsumption_J() {
			return rxTotalConsumption_J + txTotalConsumption_J + getIdleConsumption_J();
		}

		@SuppressWarnings("unused")
		public double getTotalConsumption_Wh() {
			return getTxTotalConsumption_J() / 3600;
		}
	}
}
