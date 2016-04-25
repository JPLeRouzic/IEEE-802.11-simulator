package zzInfra.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OfflineStatisticsMain {

	private static String resultPath;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Usage: offlineStatistics.jar resultFolder");
			System.exit(-1);
		}
		// resultPath = args[0];
		resultPath = "D:/simulations/MultiPlayer/Single2AP/";
		File resultPathFile = new File(resultPath);
		List<File> subDirectories = new ArrayList<File>();
		for (File file : resultPathFile.listFiles()) {
			if (file.isDirectory()) {
				subDirectories.add(file);
			}
		}
		String[] types = { "" }; // , "voice", "video", "http" };

		for (String type : types) {
			makeThrpStatistics(subDirectories, type);
			makeDelayStatistics(subDirectories, type, 2000);
			makePowerStatistics(subDirectories);
			makePacketLossPerStreamStatistics(subDirectories, type);
			makeAvgDelayStatistics(subDirectories, type, 2000, 0.5, 800);
		}

		String[] udpTypes = { "" }; // , "voice", "video" };
		for (String type : udpTypes) {
			makeUdpPacketLossStatistics(subDirectories, type);
		}

		String[] tcpTypes = { "" }; // , "http"};
		/*
		 * for(String type : tcpTypes){
		 * makeTcpPacketLossStatistics(subDirectories, type); }
		 */
	}

	private static void makeTcpPacketLossStatistics(List<File> subDirectories, String type) throws IOException {
		String prefix = "retransmissions_SA";
		List<Double> times = OfflineStatisticsMain.readColumn(0, new File(subDirectories.get(0).getAbsolutePath()
				+ "/total_thrp.m"));
		double[][][] packetsSentPerDirectory = new double[subDirectories.size()][1][1];
		double[][][] packetsAckedPerDirectory = new double[subDirectories.size()][1][1];
		int i = 0;
		for (File folder : subDirectories) {
			List<File> retransmissionFiles = getFileNames(folder, prefix, type);
			ThrpStatistics sent = new ThrpStatistics(folder, retransmissionFiles, type, 1);
			packetsSentPerDirectory[i] = sent.computeSumNotFixedResultSize(prefix, times);
			ThrpStatistics acked = new ThrpStatistics(folder, retransmissionFiles, type, 2);
			packetsAckedPerDirectory[i] = acked.computeSumNotFixedResultSize(prefix, times);
			i++;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(resultPath + "/packetLossTcp" + type + ".txt")));
		int timeSteps = times.size();
		int sentSum = 0;
		int ackedSum = 0;
		for (int j = 0; j < timeSteps; j++) {
			List<Double> sentPacketAcrossDirectories = new ArrayList<Double>();
			List<Double> ackedPacketsAcrossDirectories = new ArrayList<Double>();
			for (int k = 0; k < subDirectories.size(); k++) {
				sentPacketAcrossDirectories.add(packetsSentPerDirectory[k][j][1]);
				ackedPacketsAcrossDirectories.add(packetsAckedPerDirectory[k][j][1]);
			}
			List<Double> packetLossRatios = new ArrayList<Double>();
			for (int k = 0; k < subDirectories.size(); k++) {
				if (ackedPacketsAcrossDirectories.get(k) > sentPacketAcrossDirectories.get(k)) {
					sentSum += ackedPacketsAcrossDirectories.get(k);
				} else {
					sentSum += sentPacketAcrossDirectories.get(k);
				}
				ackedSum += ackedPacketsAcrossDirectories.get(k);
				double ratio = 1 - ackedPacketsAcrossDirectories.get(k) / sentPacketAcrossDirectories.get(k);
				if (ratio < 0) {
					ratio = 0;
				}
				packetLossRatios.add(ratio);
			}
			double avg = getAverage(packetLossRatios);
			double interval = getCoefficientOfVariance(packetLossRatios);
			double time = times.get(j);
			writer.write(time + " " + avg + " " + interval + "\n");
		}
		System.out.println("Packetloss " + type + ":" + (1 - ((double) ackedSum / sentSum)));
		writer.close();
	}

	private static void makeThrpStatistics(List<File> subDirectories, String type) throws IOException {
		double[][][] resultsPerDirecory = new double[subDirectories.size()][1][1];
		int i = 0;
		for (File folder : subDirectories) {
			List<File> typeThrpFiles = getFileNames(folder, "thrp", type);
			ThrpStatistics thrp = new ThrpStatistics(folder, typeThrpFiles, type, 5);
			resultsPerDirecory[i] = thrp.computeSum("ThroughputSum");
			i++;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(resultPath + "/thrp" + type + ".txt")));
		int timeSteps = resultsPerDirecory[0].length;
		List<Double> averages = new ArrayList<Double>();
		for (int j = 0; j < timeSteps; j++) {
			List<Double> valuesAcrossDirectories = new ArrayList<Double>();
			for (int k = 0; k < subDirectories.size(); k++) {
				valuesAcrossDirectories.add(resultsPerDirecory[k][j][1]);
			}
			double avg = getAverage(valuesAcrossDirectories);
			averages.add(avg);
			double interval = getCoefficientOfVariance(valuesAcrossDirectories);
			double time = resultsPerDirecory[0][j][0];
			writer.write(time + " " + avg + " " + interval + "\n");
		}
		double avgThrp = getAverage(averages);
		double interval = getCoefficientOfVariance(averages);
		System.out.println("Thrp " + type + ": " + avgThrp + " +-" + interval);
		writer.close();
	}

	private static void makeUdpPacketLossStatistics(List<File> subDirectories, String type) throws IOException {
		double[][][] packetsThrpPerDirectory = new double[subDirectories.size()][1][1];
		double[][][] packetsOfferPerDirectory = new double[subDirectories.size()][1][1];
		int i = 0;
		for (File folder : subDirectories) {
			List<File> typeThrpFiles = getFileNames(folder, "thrp", type);
			List<File> typeOfferFiles = getFileNames(folder, "offer", type);
			ThrpStatistics thrp = new ThrpStatistics(folder, typeThrpFiles, type, 1);
			ThrpStatistics offer = new ThrpStatistics(folder, typeOfferFiles, type, 1);
			packetsThrpPerDirectory[i] = thrp.computeSum("packetSumThrp");
			packetsOfferPerDirectory[i] = offer.computeSum("packetOfferThrp");
			i++;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(resultPath + "/packetLoss" + type + ".txt")));
		int timeSteps = packetsThrpPerDirectory[0].length;
		// per Timestep average
		double tpSum = 0.0;
		double offerSum = 0.0;
		for (int j = 0; j < timeSteps; j++) {
			List<Double> thrpPacketDirectories = new ArrayList<Double>();
			List<Double> offerPacketDirectories = new ArrayList<Double>();
			for (int k = 0; k < subDirectories.size(); k++) {
				thrpPacketDirectories.add(packetsThrpPerDirectory[k][j][1]);
				offerPacketDirectories.add(packetsOfferPerDirectory[k][j][1]);
			}
			List<Double> packetLossRatios = new ArrayList<Double>();
			for (int k = 0; k < subDirectories.size(); k++) {
				if (thrpPacketDirectories.get(k) > offerPacketDirectories.get(k)) {
					tpSum += offerPacketDirectories.get(k);
				} else {
					tpSum += thrpPacketDirectories.get(k);
				}

				offerSum += offerPacketDirectories.get(k);

				double ratio = 1 - thrpPacketDirectories.get(k) / offerPacketDirectories.get(k);
				if (ratio < 0) {
					ratio = 0;
				}
				packetLossRatios.add(ratio);
			}

			double avg = getAverage(packetLossRatios);
			double interval = getCoefficientOfVariance(packetLossRatios);
			double time = packetsThrpPerDirectory[0][j][0];
			writer.write(time + " " + avg + " " + interval + "\n");
		}
		System.out.println("PacketLoss total for " + type + ": " + (1 - (tpSum / offerSum)));
		writer.close();
	}

	private static void makePacketLossPerStreamStatistics(List<File> subDirectories, String type) {
		int outage = 0;
		List<File> typeThrpFiles = getFileNames(subDirectories.get(0), "thrp_SA", "");
		for (File file : typeThrpFiles) {
			List<File> thrpOfSameStream = new ArrayList<File>();
			List<File> offerOfSameStream = new ArrayList<File>();
			for (File folder : subDirectories) {
				thrpOfSameStream.addAll(getFileNames(folder, file.getName(), type));
			}
			String offerFileName = file.getName().replace("thrp", "offer");
			offerFileName = offerFileName.replace("_End.m", "");
			for (File folder : subDirectories) {
				offerOfSameStream.addAll(getFileNames(folder, offerFileName, type));
			}
			int thrpPackets = 0;
			int offerPackets = 0;
			for (int i = 0; i < thrpOfSameStream.size(); i++) {
				File thrpFile = thrpOfSameStream.get(i);
				List<Double> thrp = readLastLine(thrpFile, 2, 3);
				File offerFile = offerOfSameStream.get(i);
				List<Double> offer = readLastLine(offerFile, 2, 3);

				if (thrp.get(0) <= offer.get(0)) {
					thrpPackets += thrp.get(0);
				} else {
					thrpPackets += offer.get(0);
				}

				offerPackets += offer.get(0);

			}
			double packetLoss = 1 - (double) thrpPackets / offerPackets;
			// System.out.println("PacketLoss stream " + file.getName() + "is:"
			// + packetLoss);
			if (packetLoss > 0.1) {
				outage++;
			}
		}
		System.out.println("Outage PacketLoss" + type + " " + outage + " " + ((double) outage / typeThrpFiles.size()));
	}

	private static void makePowerStatistics(List<File> subDirectories) {
		List<Double> total = new ArrayList<Double>();
		List<Double> rx = new ArrayList<Double>();
		List<Double> tx = new ArrayList<Double>();
		List<Double> idle = new ArrayList<Double>();
		for (File folder : subDirectories) {
			List<File> powerFiles = getFileNames(folder, "power_Overall", "");
			File overall = powerFiles.get(0);
			try {
				String line;
				BufferedReader in = new BufferedReader(new FileReader(overall));
				while ((line = in.readLine()) != null) {
					if (line.contains("Overall:")) {
						String[] parts = line.split("\\s");
						Double val = new Double(parts[1]);
						total.add(val);
					} else if (line.contains("Tx:")) {
						String[] parts = line.split("\\s");
						Double val = new Double(parts[2]);
						tx.add(val);
					} else if (line.contains("Rx:")) {
						String[] parts = line.split("\\s");
						Double val = new Double(parts[2]);
						rx.add(val);
					} else if (line.contains("Idle:")) {
						String[] parts = line.split("\\s");
						Double val = new Double(parts[2]);
						idle.add(val);
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		double avgTot = getAverage(total);
		double intervalTot = getCoefficientOfVariance(total);
		double avgRx = getAverage(rx);
		double intervalRx = getCoefficientOfVariance(rx);
		double avgTx = getAverage(tx);
		double intervalTx = getCoefficientOfVariance(tx);
		double avgIdle = getAverage(idle);
		double intervalIdle = getCoefficientOfVariance(idle);

		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(resultPath + "/powerConsumption.txt")));
			writer.write("%Rx rxConfidence Tx txConfidence Idle idleConfidence Total totalConfidence\n");
			writer.write(avgRx + " " + intervalRx + " " + avgTx + " " + intervalTx + " " + avgIdle + " " + intervalIdle + " "
					+ avgTot + " " + intervalTot);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void makeAvgDelayStatistics(List<File> subDirectories, String type, int bucketCount, double bucketSize,
			double maxDelay) {
		List<File> delayFiles = getFileNames(subDirectories.get(0), "delay_SA", type);
		int outages = 0;
		for (File file : delayFiles) {
			List<File> delayFilesOfSameStream = new ArrayList<File>();
			for (File folder : subDirectories) {
				List<File> fileInFolder = getFileNames(folder, file.getName(), "");
				delayFilesOfSameStream.add(fileInFolder.get(0));
			}
			int[] packetsPerBucket = new int[bucketCount];
			for (File delayFile : delayFilesOfSameStream) {
				List<Double> packets = readLastLine(delayFile, 8, 8 + bucketCount);
				assert (packets.size() == packetsPerBucket.length);
				for (int i = 0; i < packetsPerBucket.length; i++) {
					packetsPerBucket[i] += packets.get(i);
				}
			}
			int sum = 0;
			for (int i = 0; i < packetsPerBucket.length; i++) {
				sum += packetsPerBucket[i];
			}
			int partialSum = 0;
			double delay = 0.0;
			int i = 0;
			while (delay < maxDelay && i < packetsPerBucket.length) {
				partialSum += packetsPerBucket[i];
				delay += bucketSize;
				i++;
			}
			double percentHigherThanMaxDelay = (1 - (double) partialSum / sum);

			if (percentHigherThanMaxDelay > 0.01) {
				outages++;
			}
		}
		System.out.println("Outage Delay " + type + ": " + (double) outages / delayFiles.size());
	}

	private static void makeDelayStatistics(List<File> subDirectories, String type, int bucketCount) throws IOException {
		double[][] perBucketPacketCounts = new double[subDirectories.size()][bucketCount];
		int j = 0;
		double width = 0;
		for (File folder : subDirectories) {
			List<File> delayFiles = getFileNames(folder, "delay", type);
			DelayStatistics delay = new DelayStatistics(folder, delayFiles, type, 8, 8 + bucketCount);
			width = delay.getBinWidth();
			double[] typeBucketCounts = delay.computeHistogramStats("histo");
			for (int i = 0; i < bucketCount; i++) {
				perBucketPacketCounts[j][i] += typeBucketCounts[i];
			}
			j++;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(resultPath + "/delayHisto" + type + ".txt")));
		double totalPackets = 0;
		for (int i = 0; i < bucketCount; i++) {
			List<Double> valuesAcrossFiles = new ArrayList<Double>();
			for (j = 0; j < subDirectories.size(); j++) {
				valuesAcrossFiles.add(perBucketPacketCounts[j][i]);
			}
			double sum = 0.0;
			for (Double val : valuesAcrossFiles) {
				if (!Double.isNaN(val)) {
					sum += val;
				}
			}
			totalPackets += sum;
		}
		double bucketSize = 0.5;
		double weightedSum = 0.0;
		for (int i = 0; i < bucketCount; i++) {
			List<Double> valuesAcrossFiles = new ArrayList<Double>();
			for (j = 0; j < subDirectories.size(); j++) {
				valuesAcrossFiles.add(perBucketPacketCounts[j][i]);
			}
			double sum = 0.0;
			for (Double val : valuesAcrossFiles) {
				sum += val;
			}
			weightedSum += bucketSize * i * sum;
			double interval = getCoefficientOfVariance(valuesAcrossFiles);
			writer.write(i * width + " " + sum / totalPackets + " " + interval / totalPackets + "\n");
		}
		System.out.println("Delay " + type + ":" + weightedSum / totalPackets);
		writer.close();
	}

	private static double getAverage(List<Double> values) {
		double n = values.size();
		double sum = 0.0;
		for (Double val : values) {
			sum += val;
		}
		double mean = sum / n;
		return mean;
	}

	private final static double coeff95 = 1.96;

	private static double getCoefficientOfVariance(List<Double> values) {
		double n = values.size();
		double mean = getAverage(values);
		double squareSum = 0.0;
		for (Double val : values) {
			squareSum += (mean - val) * (mean - val);
		}
		double deviation = Math.sqrt(squareSum / (n - 1));
		return deviation / Math.abs(mean);
	}

	/*
	 * private static double getConfidenceInterval(List<Double> values) { double
	 * n = values.size(); double mean = getAverage(values);
	 * 
	 * double squareSum = 0.0; for (Double val : values) { squareSum += (mean -
	 * val) * (mean - val); } double deviation = Math.sqrt(squareSum / (n - 1));
	 * double term = deviation / Math.sqrt(n); return coeff95 * term; }
	 */

	private static List<File> getFileNames(File folder, String prefix, String pattern) {
		List<File> result = new ArrayList<File>();
		for (File file : folder.listFiles()) {
			String name = file.getName();
			if (name.startsWith(prefix)) {
				if (!name.startsWith(prefix + "_AC") && name.contains(pattern)) {
					result.add(file);
				}
			}
		}
		return result;
	}

	public static List<Double> readLastLine(File file, int fromColum, int toColumn) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			String validLine = "";

			while ((line = in.readLine()) != null) {
				if (!line.startsWith("%") && !line.startsWith("result") && !line.startsWith("]")) {
					validLine = line;
				}
			}
			String[] parts = validLine.split("\\s");
			List<Double> valueList = new ArrayList<Double>();
			for (int i = fromColum; i < toColumn; i++) {
				Double value = new Double(parts[i]);
				valueList.add(value);
			}
			return valueList;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public static List<Double> readColumn(int index, File file) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			List<Double> valueList = new ArrayList<Double>();
			while ((line = in.readLine()) != null) {
				if (!line.startsWith("%") && !line.startsWith("result") && !line.startsWith("]")) {
					String[] parts = line.split("\\s");
					Double value = new Double(parts[index]);
					if (!value.isNaN()) {
						valueList.add(value);
					} else {
						valueList.add(0.0);
					}
				}
			}
			return valueList;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
}
