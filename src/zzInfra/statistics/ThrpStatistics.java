package zzInfra.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ThrpStatistics {

	private File resultFolder;

	private List<File> files;

	private String type;

	private int columnIndex;

	public ThrpStatistics(File resultFolder, List<File> files, String type, int columnIndex) {
		this.files = files;
		this.type = type;
		this.resultFolder = resultFolder;
		this.columnIndex = columnIndex;
	}

	public double[][] computeSum(String filePrefix) throws IOException {
		if (files.isEmpty()) {
			return null;
		}
		List<List<Double>> throughputs = new ArrayList<List<Double>>();
		List<Double> times = OfflineStatisticsMain.readColumn(0, files.get(0));
		for (File file : files) {
			// System.out.println(file.getName());
			List<Double> throughputList = OfflineStatisticsMain.readColumn(columnIndex, file);
			throughputs.add(throughputList);
		}

		if (type.equals("")) {
			type = "total";
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(resultFolder + "/" + filePrefix + type + ".txt")));
		double sum = 0.0;
		double[][] result = new double[times.size()][2];
		int size = throughputs.get(0).size();
		for (int i = 0; i < size; i++) {
			sum = 0;
			for (List<Double> thrpList : throughputs) {
				sum += thrpList.get(i);
			}
			out.write(times.get(i) + " " + sum + "\n");
			result[i][0] = times.get(i);
			result[i][1] = sum;
		}
		out.close();
		return result;
	}

	// used for files which do not have a fixed number of result entries
	public double[][] computeSumNotFixedResultSize(String filePrefix, List<Double> times) throws IOException {
		if (files.isEmpty()) {
			return null;
		}
		Vector<Double> sumAtTime = new Vector<Double>(times.size());
		sumAtTime.setSize(times.size());
		for (int i = 0; i < times.size(); i++) {
			sumAtTime.set(i, new Double(0.0));
		}
		for (File file : files) {
			System.out.println(file.getName());
			List<Double> sumList = OfflineStatisticsMain.readColumn(columnIndex, file);
			List<Double> timesInFile = OfflineStatisticsMain.readColumn(0, file);
			for (int i = 0; i < timesInFile.size(); i++) {
				int timeIndex = times.indexOf(timesInFile.get(i));
				double newSum = sumAtTime.get(timeIndex) + sumList.get(i);
				sumAtTime.set(timeIndex, newSum);
			}
		}

		if (type.equals("")) {
			type = "total";
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(resultFolder + "/result" + filePrefix + type + ".txt")));
		double sum = 0.0;
		double[][] result = new double[times.size()][2];
		int size = times.size();
		for (int i = 0; i < size; i++) {
			out.write(times.get(i) + " " + sumAtTime.get(i) + "\n");
			result[i][0] = times.get(i);
			result[i][1] = sumAtTime.get(i);
		}
		out.close();
		return result;
	}

}
