package zzInfra.layer1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;

public class JE802MobilitySimTraceParser extends JEmula implements JE802TraceParser {

	private File txtFile;

	private JETime startTime_ms;

	public JE802MobilitySimTraceParser(String filename, JETime startTimeMs) {
		txtFile = new File(filename);
		if (!txtFile.exists()) {

			TraceFiles tf = TraceFiles.getInstance();

			txtFile = new File(tf.getRandomTraceFile());
			System.out.println(txtFile);
			// this.error("File " + filename + " not found");
		}
		this.startTime_ms = startTimeMs;
	}

	@Override
	public List<JE802LocationInfo> parseFile() {
		List<JE802LocationInfo> infos = new ArrayList<JE802LocationInfo>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				// split at whitespace
				String[] parts = line.split("\\s+");
				double lat = new Double(parts[1]);
				double lon = new Double(parts[2]);
				double timeStamp = new Double(parts[0]) * 1000;
				JE802LocationInfo info = new JE802LocationInfo(lat, lon, 0, new JETime(timeStamp), 0);
				infos.add(info);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(infos);
		// double minTime = getMinimumTime(infos);
		for (JE802LocationInfo info : infos) {
			double newTime = startTime_ms.getTimeMs() + (info.getTime().getTimeMs());
			info.setTime(new JETime(newTime));
		}
		return infos;
	}

	private double getMinimumTime(List<JE802LocationInfo> infos) {
		double min = Double.MAX_VALUE;
		for (JE802LocationInfo info : infos) {
			double ms = info.getTime().getTimeMs();
			if (ms < min) {
				min = ms;
			}
		}
		return min;
	}

	private static class TraceFiles {

		private static TraceFiles instance;
		private static File[] listOfFiles;
		private static int call = 0;

		public TraceFiles() {

			String dir = "H:/MobilitySim/Ugur_test/Filter_0";
			File folder = new File(dir);
			listOfFiles = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String arg1) {
					if (arg1.contains("Guest"))
						return true;

					return false;
				}
			});
		}

		public String getRandomTraceFile() {

			return listOfFiles[call++].getAbsolutePath();

		}

		public static TraceFiles getInstance() {

			if (instance == null)
				instance = new TraceFiles();

			return instance;
		}
	}

}
