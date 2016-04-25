package zzInfra.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.kernel.JETime;
import zzInfra.layer3_network.JE802HopInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.emulator.JE802RouteEval;
import zzInfra.emulator.JE802RouteEval.JE802RouteInfo;
import zzInfra.gui.JE802GuiTimePanel;

import zzInfra.ARC.JE802Station;

public class JE802ChannelLinksKml extends JE802LinkKmlGenerator {

	private HashMap<Integer, JE802Station> stationMap;

	private List<JE802RouteHopRecord> linkRecords;

	public JE802ChannelLinksKml(Document doc, List<JE802Station> stations) {
		super(doc, stations);
		stationMap = new HashMap<Integer, JE802Station>();
		for (JE802Station station : stations) {
			stationMap.put(station.getMacAddress(), station);
		}
		linkRecords = new ArrayList<JE802RouteHopRecord>();

		JE802RouteEval routeEval = stations.get(0).getStatEval().getRouteEval();
		Map<Integer, Map<Integer, List<JE802RouteInfo>>> routes = routeEval.getRouteMap();
		for (Integer sa : routes.keySet()) {
			Map<Integer, List<JE802RouteInfo>> destinations = routes.get(sa);
			// to all destinations of that source
			for (Integer destination : destinations.keySet()) {

				// choose random color for link
				List<JE802RouteInfo> routeList = destinations.get(destination);
				for (JE802RouteInfo info : routeList) {
					// hopList includes Source and destination
					List<JE802HopInfo> hopList = info.getHops();
					JE802Station hopSource = stationMap.get(hopList.get(0).getAddress());

					double start = info.getStartTime().getTimeMs();
					start = start - start % (int) hopSource.getMobility().getInterpolationInterval_ms().getTimeMs();
					double stop = info.getStopTime().getTimeMs();
					for (int i = 1; i < hopList.size(); i++) {
						JE802Station hopDestination = stationMap.get(hopList.get(i).getAddress());
						Color hopColor = JE802GuiTimePanel.channel2color(hopList.get(i).getChannel());
						JETime interval = hopSource.getMobility().getInterpolationInterval_ms();
						int timeSteps = (int) ((stop - start) / interval.getTimeMs());
						// for each location update, draw the hop
						for (int j = 0; j < timeSteps; j++) {
							JETime currentTime = new JETime(start + j * interval.getTimeMs());
							JE802RouteHopRecord rec = new JE802RouteHopRecord(hopSource.getMacAddress(),
									hopDestination.getMacAddress(), currentTime, currentTime.plus(interval), hopColor, sa,
									destination);
							linkRecords.add(rec);
						}
						hopSource = hopDestination;
					}
				}
			}
		}
	}

	@Override
	public Element createDOM() {
		// the elements are keyed by ROUTESource and RouteDestination, which are
		// later put into the same folder in the kml
		Map<Integer, Map<Integer, List<Element>>> routeElements = new HashMap<Integer, Map<Integer, List<Element>>>();

		// the hop records are keyed by hopSouce, hopDestination
		Map<Integer, Map<Integer, List<JE802RouteHopRecord>>> hopRecords = computeDuplicateLinks(linkRecords);
		for (Integer hopSource : hopRecords.keySet()) {
			Map<Integer, List<JE802RouteHopRecord>> hopDestinations = hopRecords.get(hopSource);
			for (Integer hopDestination : hopDestinations.keySet()) {
				List<JE802RouteHopRecord> linksOverTime = hopDestinations.get(hopDestination);
				for (JE802RouteHopRecord rec : linksOverTime) {
					Element link = createLink(rec.getColor(), rec.getStartTime(), rec.getStopTime(), rec.getIndexInNeighbors(),
							rec.getNeighborCount(), stationMap.get(hopSource), stationMap.get(hopDestination));
					Map<Integer, List<Element>> destMap = routeElements.get(rec.getRouteSource());
					if (destMap == null) {
						destMap = new HashMap<Integer, List<Element>>();
						routeElements.put(rec.getRouteSource(), destMap);
					}
					List<Element> routeLinks = destMap.get(rec.getRouteDestination());
					if (routeLinks == null) {
						routeLinks = new ArrayList<Element>();
						destMap.put(rec.getRouteDestination(), routeLinks);
					}
					routeLinks.add(link);
				}
			}
		}

		// for all source stations
		List<Element> routeFolders = new ArrayList<Element>();
		for (Integer source : routeElements.keySet()) {
			Map<Integer, List<Element>> destMap = routeElements.get(source);
			for (Integer destination : destMap.keySet()) {
				Element routeFolder = createFolder(destMap.get(destination), "From " + source + " To " + destination, true);
				routeFolders.add(routeFolder);
			}
		}
		return createFolder(routeFolders, "Channel Links", false);
	}

}
