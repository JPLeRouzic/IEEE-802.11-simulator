/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */
package zzInfra.emulator;

import IEEE11ac.layer0.acMediumInterferenceModel;
import IEEE11ac.layer0.acMediumUnitDisk;
import IEEE11ac.station.acStation;
import IEEE11af.station.afStation;
import IEEE11ax.station.axStation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import zzInfra.ARC.JE802Station;
import zzInfra.animation.JE802KmlWriter;
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;
import zzInfra.layer0.WirelessMedium;
import zzInfra.layer1.JE802PhyMCS;
import zzInfra.layer3_network.JE802RoutingConstants;

/**
 * @author Stefan Mangold
 *
 */
public class JE802Control extends JEmula {

    public List<JE802Station> stations;

    private boolean showGui;

    private boolean useInterferenceModel;

    private String path2Results;

    private JEEventScheduler theUniqueEventScheduler;

    private WirelessMedium theUniqueWirelessMedium;

    private Random theUniqueRandomBaseGenerator;

    private JE802Gui theUniqueGui;

    private JE802StatEval statEval;

    private JE802KmlWriter kmlWriter;

    public Document STAsGeoConf;

    public Document amend;

    private Object phyStd;

    public JE802Control(Document aDocument, Document amendment, boolean showGui) {
        this.STAsGeoConf = aDocument;
        this.amend = amendment;
        this.theUniqueEventScheduler = new JEEventScheduler();
        this.showGui = showGui;
        this.kmlWriter = null;
        if (this.showGui) {
            theUniqueGui = new JE802Gui(aDocument.getBaseURI());
            theUniqueGui.setVisible(true);
            theUniqueGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            theUniqueGui = null;
        }
        this.stations = new ArrayList<JE802Station>(); // queue of stations
        this.parse_xml_and_create_entities();
        this.path2Results = statEval.getPath2Results();
        this.theUniqueEventScheduler.setPath2Results(this.path2Results);
        // we
        // give
        // this
        // path
        // to
        // the
        // scheduler
        // to
        // allow
        // kernel
        // file
        // outputs
        // such
        // as
    }

    public void startSimulation() {
		// long before = System.currentTimeMillis(); TODO: Roman: variable not
        // used, delete
        this.theUniqueEventScheduler.start(); // the event scheduler will now
        // start, and then control the
        // entire emulation
        try {
            FileWriter writer = new FileWriter(new File(this.path2Results + "/retransmissionRates.txt"));
            writer.write("ID\tLost\tTransmitted\tRate\n");
            long lostSum = 0;
            long transmittedSum = 0;
            for (JE802Station station : stations) {
                station.displayLossrate();
                long lost = station.getLostPackets();
                lostSum += lost;
                long transmitted = station.getTransmittedPackets();
                transmittedSum += transmitted;
                double r = (double) lost / transmitted;
                writer.write("Station " + station.getMacAddress() + "\t" + lost + "\t" + transmitted + "\t" + r + "\n");

            }
            double rate = (double) lostSum / transmittedSum;
            writer.write("Overall: " + lostSum + "\t" + transmittedSum + "\t" + rate);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (kmlWriter != null) {
            this.message("Creating animation now. This might take some time ...", 100);
            long beforeAnim = System.currentTimeMillis();
            kmlWriter.createDOM();
            kmlWriter.writeDOMtoFile();
            kmlWriter.createKMZArchive();
            this.message("Done. Creating animation took " + (System.currentTimeMillis() - beforeAnim) / 60 + " seconds.", 100);
        }

        this.STAsGeoConf = null;
    }

    public JETime getSimulationTime() {
        return this.theUniqueEventScheduler.now();
    }

    public String getPath2Results() {
        return this.path2Results;
    }

    public void setSimulationEnd(JETime time) {
        this.theUniqueEventScheduler.setEmulationEnd(time);
    }

    public void setRandomSeed(long newSeed) {
        this.theUniqueRandomBaseGenerator.setSeed(newSeed);
    }

    private void parse_xml_and_create_entities() {
        Node theTopLevelNode = STAsGeoConf.getFirstChild();
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {

            // parseControl element
            createControlElement(theTopLevelNode, xpath);
            // wireless channel definitions
            createWirelessChannels(theTopLevelNode, xpath);
            createAnimation(theTopLevelNode, xpath);
            createRoutingConstants(theTopLevelNode, xpath);
            createStations(theTopLevelNode, xpath);
            this.setWiredStations();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private void setWiredStations() {
        for (JE802Station station : stations) {
            List<Integer> wiredStations = station.getWiredAddresses();
            if (wiredStations != null) {
                List<JE802Station> wiredToSation = new ArrayList<JE802Station>(wiredStations.size());
                for (Integer addr : wiredStations) {
                    for (JE802Station station2 : stations) {
                        if (station2.getMacAddress() == addr) {
                            wiredToSation.add(station2);
                        }
                    }
                }
                station.sme.setWiredStations(wiredToSation);
            }
        }
    }

    private void createRoutingConstants(Node theTopLevelNode, XPath xpath) throws XPathExpressionException {
        Element routingParameters = (Element) xpath.evaluate("JE802RoutingParameters", theTopLevelNode, XPathConstants.NODE);
        if (routingParameters == null) {
            this.warning("No <JE802RoutingParameters> in XML, using default values");
            return;
        }
        String enabledStr = routingParameters.getAttribute("routingEnabled");
        if (!enabledStr.isEmpty()) {
            JE802RoutingConstants.routingEnabled = new Boolean(enabledStr);
        } else {
            JE802RoutingConstants.routingEnabled = false;
        }

        // dynamicChannelSwitching enabled
        String switchingStr = routingParameters.getAttribute("channelSwitchingEnabled");
        if (!switchingStr.isEmpty()) {
            JE802RoutingConstants.channelSwitchingEnabled = new Boolean(switchingStr);
        } else {
            JE802RoutingConstants.channelSwitchingEnabled = false;
        }

        String mcrMetricStr = routingParameters.getAttribute("multiChannelPathMetricEnabled");
        if (!mcrMetricStr.isEmpty()) {
            JE802RoutingConstants.MCRMetricEnabled = new Boolean(mcrMetricStr);
        } else {
            JE802RoutingConstants.MCRMetricEnabled = false;
        }

        String routeTimeout = routingParameters.getAttribute("activeRouteTimeout_ms");
        if (!routeTimeout.isEmpty()) {
            double timeOut = new Double(routeTimeout);
            JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT = new JETime(timeOut);
        } else {
            JE802RoutingConstants.ACTIVE_ROUTE_TIMEOUT = new JETime(3000.0);
        }

        String ipHeaderLengthStr = routingParameters.getAttribute("ipHeaderByte");
        if (!ipHeaderLengthStr.isEmpty()) {
            int headerLength = new Integer(ipHeaderLengthStr);
            JE802RoutingConstants.IP_HEADER_BYTE = headerLength;
        } else {
            JE802RoutingConstants.IP_HEADER_BYTE = 20;
        }

        String brokenStr = routingParameters.getAttribute("brokenLinkAfterLoss");
        if (!brokenStr.isEmpty()) {
            int brokenAfter = new Integer(brokenStr);
            JE802RoutingConstants.LINK_BREAK_AFTER_LOSS = brokenAfter;
        } else {
            JE802RoutingConstants.LINK_BREAK_AFTER_LOSS = 3;
        }

        String ttlStr = routingParameters.getAttribute("maxTTL");
        if (!ttlStr.isEmpty()) {
            int maxTTL = new Integer(ttlStr);
            JE802RoutingConstants.maxTTL = maxTTL;
        } else {
            JE802RoutingConstants.maxTTL = 5;
        }

        String helloIntervalStr = routingParameters.getAttribute("helloInterval_ms");
        if (!helloIntervalStr.isEmpty()) {
            double interval = new Integer(helloIntervalStr);
            JE802RoutingConstants.HELLO_INTERVAL_MS = new JETime(interval);
        } else {
            JE802RoutingConstants.HELLO_INTERVAL_MS = new JETime(2000);
        }

        String channelDelayStr = routingParameters.getAttribute("channelSwitchingDelay_ms");
        if (!channelDelayStr.isEmpty()) {
            double delay = new Double(channelDelayStr);
            JE802RoutingConstants.CHANNEL_SWITCHING_DELAY = new JETime(delay);
        } else {
            JE802RoutingConstants.CHANNEL_SWITCHING_DELAY = new JETime(1);
        }
    }

    private void createStations(Node theTopLevelNode, XPath xpath) throws XPathExpressionException {
        // stations
        NodeList stationNodeList = (NodeList) xpath.evaluate("JE802Station", theTopLevelNode, XPathConstants.NODESET);
        for (int i = 0; i < stationNodeList.getLength(); i++) {
            // find which amendment is used
            Node phyModesNode = (Node) xpath.evaluate("JEWirelessChannels", theTopLevelNode, XPathConstants.NODE);
            if (phyModesNode != null) {
		phyStd = new String(((Element) phyModesNode).getAttribute("standard"));

                Node stationNode = stationNodeList.item(i);
                if (phyStd.equals("11ac")) {
                    acStation station = null;
                    station = new acStation(theUniqueEventScheduler, theUniqueWirelessMedium,
                            theUniqueRandomBaseGenerator, theUniqueGui, statEval, stationNode, phyStd.toString());
                    this.stations.add(station);
                } 
                else if (phyStd.equals("11ax")) {
                    axStation station = null;
                    station = new axStation(theUniqueEventScheduler, theUniqueWirelessMedium,
                            theUniqueRandomBaseGenerator, theUniqueGui, statEval, stationNode, phyStd.toString());
                    this.stations.add(station);
                } 
                else if (phyStd.equals("11af")) {
                    afStation station = null;
                    station = new afStation(theUniqueEventScheduler, theUniqueWirelessMedium,
                            theUniqueRandomBaseGenerator, theUniqueGui, statEval, stationNode, phyStd.toString());
                    this.stations.add(station);
                } else {
                    this.error("Unknow standard");
                }
            }
        }
    }

    private void createAnimation(Node theTopLevelNode, XPath xpath) throws XPathExpressionException {
        // animation
        String resultPath = statEval.getPath2Results();
        Element animationNode = (Element) xpath.evaluate("//JE802Animation", theTopLevelNode, XPathConstants.NODE);
        if (new Boolean(animationNode.getAttribute("generateGEarth"))) {
			kmlWriter = new JE802KmlWriter(animationNode, resultPath, this.STAsGeoConf.getDocumentURI(), this.stations,
					this.theUniqueWirelessMedium.getReuseDistance());
        }

    }

    private void createControlElement(Node theTopLevelNode, XPath xpath) throws XPathExpressionException {
        // find control element
        Element controlElem = (Element) xpath.evaluate("JE802Control", theTopLevelNode, XPathConstants.NODE);
        if (controlElem != null) {
            // set emulation duration
            JETime anEmulationEnd = new JETime(Double.parseDouble((controlElem).getAttribute("EmulationDuration_ms")));
            theUniqueEventScheduler.setEmulationEnd(anEmulationEnd);

            // stat eval
            Node statEvalNode = (Node) xpath.evaluate("JE802StatEval", controlElem, XPathConstants.NODE);
            if (statEvalNode != null) {
                long aSeed = new Long(((Element) statEvalNode).getAttribute("seed"));
                theUniqueRandomBaseGenerator = new Random(aSeed);
                statEval = new JE802StatEval(theUniqueEventScheduler, theUniqueRandomBaseGenerator, statEvalNode);
                statEval.send(new JEEvent("start_req", statEval, theUniqueEventScheduler.now()));
            } else {
                this.warning("No JE802StatEval node specified in xml");
            }
        } else {
            this.warning("No JE802Control node specified in xml");
        }
    }

    private void createCommuniCore(Node theTopLevelNode, XPath xpath) throws XPathExpressionException {
        Node communiCoreNode = null;

        communiCoreNode = (Node) xpath.evaluate("JE802CommuniCore", theTopLevelNode, XPathConstants.NODE);
        if (communiCoreNode != null) {
        } else {
            this.warning("No JE802CommuniCore node specified in xml");
        }
    }

    private void createWirelessChannels(Node theTopLevelNode, XPath xpath) throws XPathExpressionException {
        Node channelNode = null;
        boolean channel_is_defined = false;

        channelNode = (Node) xpath.evaluate("JEWirelessChannels", theTopLevelNode, XPathConstants.NODE);
        if (channelNode != null) {
            channel_is_defined = true;
            Node interference = (Node) xpath
                    .evaluate("JEWirelessChannels/@useInterference", theTopLevelNode, XPathConstants.NODE);
            if (interference != null) {
                useInterferenceModel = new Boolean(interference.getNodeValue());
                if (useInterferenceModel) {
                    theUniqueWirelessMedium = new acMediumInterferenceModel(theUniqueEventScheduler,
                            theUniqueRandomBaseGenerator, channelNode);
                } else {
                    theUniqueWirelessMedium = new acMediumUnitDisk(theUniqueGui, theUniqueEventScheduler,
                            theUniqueRandomBaseGenerator, channelNode);
                }
            } else {
                theUniqueWirelessMedium = new acMediumUnitDisk(theUniqueGui, theUniqueEventScheduler,
                        theUniqueRandomBaseGenerator, channelNode);
            }

        } else {
            this.error("No JEWirelessChannels node specified in xml");
        }
        if (channel_is_defined == false) {
            this.error("No JEWirelessChannels specified in xml");
        }
    }

}
