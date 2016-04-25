package zzInfra.layer3_network;

import zzInfra.kernel.JETime;

public final class JE802RoutingConstants {

	// function switches

	public static boolean routingEnabled = false;

	public static boolean channelSwitchingEnabled = false;

	public static boolean MCRMetricEnabled = false;

	// AODV CONSTANTS

	public static int maxTTL = 5;

	public static JETime ACTIVE_ROUTE_TIMEOUT = new JETime(3000);

	public static JETime PATH_DISCOVERY_TIME = new JETime(3000);

	// period after wich an invalid route is deleted from the routing table
	public static JETime DELETE_PERIOD = ACTIVE_ROUTE_TIMEOUT.times(5);

	// length of ip header
	public static int IP_HEADER_BYTE = 20;

	// after how many lost packets a link is considered as broken
	public static int LINK_BREAK_AFTER_LOSS = 10;

	public static JETime HELLO_INTERVAL_MS = new JETime(2000);

	// Multi channel routing parameters

	// delay needed for switching an interface to a new channel
	public static JETime CHANNEL_SWITCHING_DELAY = new JETime(1);

	public static JETime MAX_SWITCHING_INTERVAL = new JETime(50);// CHANNEL_SWITCHING_DELAY.times(10);

	public static int RREQ_RATE_LIMIT = 20;

	// weight of the two different metrics
	public static double beta = 0.5;

	// fixed channel switch probability
	public static double switchProbability = 0.4;

	// time for which the channel usages should be logged
	public static int USAGE_WINDOW = 100;

}
