package zzInfra.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DelayStatistics {

	private File resultFolder;

	private List<File> files;

	private String type;

	private int fromIndex;

	private int toIndex;

	public DelayStatistics(File resultFolder, List<File> files, String type, int fromIndex, int toIndex) {
		this.type = type;
		this.resultFolder = resultFolder;
		this.files = files;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	public double getBinWidth() {
		List<Double> widths = OfflineStatisticsMain.readLastLine(files.get(0), fromIndex - 1, fromIndex);
		return widths.get(0);
	}

	public double[] computeHistogramStats(String filePrefix) throws IOException {
		if (files.isEmpty()) {
			return null;
		}
		double[] binPacketSum = new double[(toIndex - fromIndex)];
		List<Double> widths = OfflineStatisticsMain.readLastLine(files.get(0), fromIndex - 1, fromIndex);
		double width = widths.get(0);
		for (File file : files) {

			// System.out.println(file.getName());
			List<Double> numPackets = OfflineStatisticsMain.readLastLine(file, fromIndex, toIndex);
			for (int i = 0; i < binPacketSum.length; i++) {
				binPacketSum[i] += numPackets.get(i);
			}
		}

		if (type.equals("")) {
			type = "total";
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(resultFolder + "/" + filePrefix + type + ".txt")));
		for (int i = 0; i < binPacketSum.length; i++) {
			out.write(i * width + " " + binPacketSum[i] + "\n");
		}
		out.close();
		return binPacketSum;

	}

}
