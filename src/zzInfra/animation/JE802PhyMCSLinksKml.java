package zzInfra.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.kernel.JETime;

import zzInfra.layer1.JE802PhyMCS;
import zzInfra.layer3_network.JE802HopInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.emulator.JE802PhyMCSEval;
import zzInfra.emulator.JE802RouteEval;
import zzInfra.emulator.JE802StatEval;
import zzInfra.emulator.JE802RouteEval.JE802RouteInfo;
import zzInfra.gui.JE802GuiTimePanel;

import zzInfra.ARC.JE802Station;

public class JE802PhyMCSLinksKml extends JE802LinkKmlGenerator {

	private Map<Integer, JE802Station> stationMap = new HashMap<Integer, JE802Station>();

	public JE802PhyMCSLinksKml(Document doc, List<JE802Station> stations) {
		super(doc, stations);
		for (JE802Station station : stations) {
			stationMap.put(station.getMacAddress(), station);
		}
	}

	@Override
	public Element createDOM() {
		JE802StatEval statEval = theStations.get(0).getStatEval();
		JE802PhyMCSEval phyModeEval = statEval.getPhyMCSEval();
		JE802RouteEval routeEval = statEval.getRouteEval();
		List<Element> routeList = new ArrayList<Element>();

		Map<Integer, Map<Integer, List<JE802RouteInfo>>> routes = routeEval.getRouteMap();
		for (Integer source : routes.keySet()) {
			Map<Integer, List<JE802RouteInfo>> destMap = routes.get(source);
			for (Integer destination : destMap.keySet()) {
				// for each Route, consider all changing paths
				List<Element> routeElements = new ArrayList<Element>();
				List<JE802RouteInfo> routeInfos = destMap.get(destination);
				for (JE802RouteInfo routeInfo : routeInfos) {
					JETime startTime = routeInfo.getStartTime();
					JETime endTime = routeInfo.getStopTime();
					List<JE802HopInfo> hopInfos = routeInfo.getHops();
					// for all hops on the path
					JE802HopInfo sourceHop = hopInfos.get(0);
					for (int i = 1; i < hopInfos.size(); i++) {
						JE802HopInfo destinationHop = hopInfos.get(i);
						JETime interval = stationMap.get(sourceHop.getAddress()).getMobility().getInterpolationInterval_ms();
						int timeSteps = (int) (endTime.minus(startTime).dividedby(interval));
						for (int j = 0; j < timeSteps; j++) {
							JETime currentTime = new JETime(startTime.getTimeMs() + j * interval.getTimeMs());
							JE802PhyMCS mode = phyModeEval.getMostUsedPhymode(sourceHop.getAddress(),
									destinationHop.getAddress(), currentTime);
							if (mode != null) {
								Color color = JE802GuiTimePanel.phyMode2Color(mode.getModeId());
								Element link = createLink(color, currentTime, currentTime.plus(interval), 0, 1,
										stationMap.get(sourceHop.getAddress()), stationMap.get(destinationHop.getAddress()));
								routeElements.add(link);
							}
						}
						sourceHop = destinationHop;
					}
				}
				Element routePhyMCSs = createFolder(routeElements, "From " + source + " To " + destination, true);
				routeList.add(routePhyMCSs);
			}
		}

		return createFolder(routeList, "PhyMCSLinks", false);
	}

}
