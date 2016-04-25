package zzInfra.layer1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;

public class JE802TxtTraceParser extends JEmula implements JE802TraceParser {

	private File txtFile;

	private JETime startTime_ms;

	private double minAccuracy;

	public JE802TxtTraceParser(String filename, double minAccuracy, JETime startTimeMs) {
		txtFile = new File(filename);
		if (!txtFile.exists()) {
			this.error("File " + filename + " not found");
		}
		this.minAccuracy = minAccuracy;
		this.startTime_ms = startTimeMs;
	}

	@Override
	public List<JE802LocationInfo> parseFile() {
		List<JE802LocationInfo> infos = new ArrayList<JE802LocationInfo>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				double lat = new Double(parts[1].replaceFirst("lat:", ""));
				double lon = new Double(parts[2].replaceFirst("lon:", ""));
				double alt = new Double(parts[3].replaceFirst("alt:", ""));
				double timeStamp = new Double(parts[4].replaceFirst("time:", ""));
				double accuracy = new Double(parts[5].replaceFirst("accuracy:", ""));
				if (accuracy < minAccuracy) {
					JE802LocationInfo info = new JE802LocationInfo(lat, lon, alt, new JETime(timeStamp), accuracy);
					infos.add(info);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(infos);
		double minTime = getMinimumTime(infos);
		for (JE802LocationInfo info : infos) {
			double newTime = startTime_ms.getTimeMs() + (info.getTime().getTimeMs() - minTime);
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
}
