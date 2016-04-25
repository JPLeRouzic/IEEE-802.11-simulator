package zzInfra.emulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.kernel.JETime;
import zzInfra.layer3_network.JE802HopInfo;;

public class JE802RouteEval {

	// keys are source, destination
	private Map<Integer, Map<Integer, List<JE802RouteInfo>>> routeMap;

	private JETime endOfEmulation;

	public JE802RouteEval(JETime endOfEmulation) {
		this.endOfEmulation = endOfEmulation;
		this.routeMap = new HashMap<Integer, Map<Integer, List<JE802RouteInfo>>>();
	}

	public void linkBroken(int hopSource, int hopDestination, JETime when) {
		for (Integer source : routeMap.keySet()) {
			Map<Integer, List<JE802RouteInfo>> routesFromSource = routeMap.get(source);
			for (Integer destination : routesFromSource.keySet()) {
				List<JE802RouteInfo> infos = routesFromSource.get(destination);
				for (JE802RouteInfo info : infos) {
					if (info.getStartTime().isEarlierThan(when) && info.getStopTime().isLaterThan(when)) {
						if (info.hasHop(hopSource, hopDestination)) {
							info.setStopTime(when);
						}
					}
				}
			}
		}
	}

	public void addRoute(int source, int da, JETime when, List<JE802HopInfo> hops) {
		Map<Integer, List<JE802RouteInfo>> routesOfStation = routeMap.get(source);
		if (routesOfStation == null) {
			routesOfStation = new HashMap<Integer, List<JE802RouteInfo>>();
			routeMap.put(source, routesOfStation);
		}
		List<JE802RouteInfo> routeOverTime = routesOfStation.get(da);
		if (routeOverTime == null) {
			routeOverTime = new ArrayList<JE802RouteInfo>();
			routesOfStation.put(da, routeOverTime);
		}
		// if the route is different from the last route, add the new route to
		// the list with start time "when"
		// and set the end time of the last route to "when"
		if (hops != null) {
			JE802RouteInfo currentRoute = new JE802RouteInfo(hops, when);
			currentRoute.setStopTime(endOfEmulation);
			if (routeOverTime.isEmpty()) {
				routeOverTime.add(currentRoute);
			} else if (!routeOverTime.get(routeOverTime.size() - 1).isEqualRoute(currentRoute)) {
				routeOverTime.get(routeOverTime.size() - 1).setStopTime(currentRoute.getStartTime());
				routeOverTime.add(currentRoute);
			}
			// when the hop list is null, this indicates that the previous route
			// is broken
		} else {
			if (!routeOverTime.isEmpty()) {
				routeOverTime.get(routeOverTime.size() - 1).setStopTime(when);
			}
		}
	}

	public void evaluate() {
		int routeCount = 0;
		int routeLengthSum = 0;
		for (Integer sa : routeMap.keySet()) {
			Map<Integer, List<JE802RouteInfo>> routes = routeMap.get(sa);
			for (Integer da : routes.keySet()) {
				for (JE802RouteInfo route : routes.get(da)) {
					routeCount++;
					routeLengthSum += route.getHops().size() - 1;
				}
			}
		}
		// System.out.println((double)routeLengthSum/routeCount);
	}

	// returns map of Routes stored by source / destination pairs
	public Map<Integer, Map<Integer, List<JE802RouteInfo>>> getRouteMap() {
		return routeMap;
	}

	public class JE802RouteInfo {

		private JETime startTime;

		private JETime stopTime;

		private final List<JE802HopInfo> hops;

		public JE802RouteInfo(List<JE802HopInfo> hops, JETime startTime) {
			this.hops = new ArrayList<JE802HopInfo>(hops);
			this.startTime = startTime;
		}

		public boolean hasHop(int hopSource, int hopDestination) {
			if (hops.isEmpty()) {
				return false;
			}
			JE802HopInfo lastHop = hops.get(0);
			int size = hops.size();
			for (int i = 1; i < size; i++) {
				JE802HopInfo hop = hops.get(i);
				if (lastHop.getAddress() == hopSource && hopDestination == hop.getAddress()) {
					return true;
				}
			}
			return false;
		}

		public List<JE802HopInfo> getHops() {
			return hops;
		}

		public JETime getStartTime() {
			return startTime;
		}

		public void setStopTime(JETime stopTime) {
			this.stopTime = stopTime;
		}

		public JETime getStopTime() {
			return stopTime;
		}

		public boolean isEqualRoute(JE802RouteInfo otherRoute) {
			if (otherRoute.getHops().size() != hops.size()) {
				return false;
			}
			for (int i = 0; i < hops.size(); i++) {
				JE802HopInfo ourHop = hops.get(i);
				JE802HopInfo otherHop = otherRoute.getHops().get(i);
				if (!ourHop.equals(otherHop)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			return "Start " + startTime + " Stop " + stopTime + " " + hops;
		}
	}

}
