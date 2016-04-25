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
package IEEE11ac.station;

import IEEE11ac.layer2.acMac;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import zzInfra.ARC.Mac48Address;
import zzInfra.ARC.JE802Station;
import zzInfra.emulator.JE802StatEval;
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.layer0.WirelessMedium;
import zzInfra.layer1.JE802Mobility;
import zzInfra.layer2.JE802MacAlgorithm;
import zzInfra.layer3_network.JE802RouteManager;
import zzInfra.layer4_transport.JE802TCPManager;
import zzInfra.layer5_application.JE802TrafficGen;

public class acStation extends JE802Station {

    private int address;

    public Mac48Address ssid;
    public Mac48Address bssid;

    public int propag = 0;

    private Map<Integer, acMac> dot11MacMap;

    private JE802RouteManager ipLayer;

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    public acStation(JEEventScheduler aScheduler, WirelessMedium aChannel, Random aGenerator, JE802Gui aGui,
            JE802StatEval aStatEval, Node topLevelNode, String std)
            throws XPathExpressionException {
        boolean isAP = false;
        Element aTopLevelNode = (Element) topLevelNode;
        this.theUniqueEventScheduler = aScheduler;
        this.statEval = aStatEval;
        if (aTopLevelNode.getNodeName().equals("JE802Station")) {
            this.message("XML definition " + aTopLevelNode.getNodeName() + " found.", 1);

            // get station address
            this.address = Integer.parseInt(aTopLevelNode.getAttribute("address"));
            String wiredStationsString = aTopLevelNode.getAttribute("wiredTo");
            if (wiredStationsString.isEmpty()) {
                this.wiredAddresses = null;
            } else {
                String[] addresses = wiredStationsString.split(",");
                this.wiredAddresses = new ArrayList<Integer>(addresses.length);
                for (String wired : addresses) {
                    Integer addr = new Integer(wired);
                    this.wiredAddresses.add(addr);
                }
            }

            this.dot11MacMap = new HashMap<Integer, acMac>();
            this.trafficGenerators = new ArrayList<JE802TrafficGen>();

            if (aTopLevelNode.hasChildNodes()) {

                // manage APs
                Node apNode = (Node) xpath.evaluate("isAP", aTopLevelNode, XPathConstants.NODE);
                Element apElem = (Element) apNode;

                String ety = apElem.getTextContent();
                if (ety.equals("true")) {
                    isAP = true;
                } else {
                    isAP = false;
                }

                // -- create SME (Station Management Entity):
                // ------------------------------------------------------------------------------------------------
                Node smeNode = (Node) xpath.evaluate("JE802SME", aTopLevelNode, XPathConstants.NODE);
                this.message("allocating " + smeNode.getNodeName(), 10);
                this.sme = new acSme(aScheduler, aGenerator, smeNode, this);

                // -- create mobility:
                // ----------------------------------------------------------------------------------------
                Node mobNode = (Node) xpath.evaluate("JE802Mobility", aTopLevelNode, XPathConstants.NODE);
                this.message("allocating " + mobNode.getNodeName(), 10);
                this.mobility = new JE802Mobility(mobNode);

                // -- create MACS:
                // ----------------------------------------------------------------------------------------
                // create 802_11 Macs, if any
                NodeList macList = (NodeList) xpath.evaluate("JE80211MAC", aTopLevelNode, XPathConstants.NODESET);
                if (macList.getLength() > 0) {
                    for (int i = 0; i < macList.getLength(); i++) {
                        Node macNode = macList.item(i);
                        this.message("allocating " + macNode.getNodeName(), 10);

                        acMac theMacLoc = new acMac(this, aScheduler, aStatEval, aGenerator, 
                                aGui, aChannel, macNode, this.sme.getHandlerId(), std);

                        this.dot11MacMap.put(thePhy.getCurrentChannelNumberRX(), theMacLoc);
                        theMacLoc.setMACAddress(this.address);

                        JE802MacAlgorithm totto = theMacLoc.getMlme().getTheAlgorithm();
                        totto.compute() ;
                        this.getPhy().setMobility(this.mobility);
                        this.getPhy().send(new JEEvent("start_req", this.getPhy(), theUniqueEventScheduler.now()));
                        this.theMac = theMacLoc;
                    }

                    ArrayList toto = new ArrayList<acMac>(dot11MacMap.values()) ;
                    sme.setMacs(toto);

                    this.ipLayer = new JE802RouteManager(aScheduler, aGenerator, this.sme, statEval, this);
                    this.sme.setIpHandlerId(this.ipLayer.getHandlerId());
                } else {
                    this.message("No JE80211Mac definition found", 10);
                }

                // -- create TCP Manager:
                // ------------------------------------------------------------------------------------------------
                Node tcpNode = (Node) xpath.evaluate("JE802TCP", aTopLevelNode, XPathConstants.NODE);
                this.tcp = new JE802TCPManager(aScheduler, aGenerator, tcpNode, this.ipLayer, this.statEval);
                this.ipLayer.setTcpHandlerId(tcp.getHandlerId());

                /*
                 * for (Iterator<JE802_11Mac> iterator =
                 * macMap.values().iterator(); iterator.hasNext();) {
                 * acMac currentMac = (acMac) iterator.next(); }
                 * mac.setTcpHandlerId(this.tcp.getHandlerId());
                 */
                // -- create traffic generators:
                // ----------------------------------------------------------------------------------------
                NodeList tgList = (NodeList) xpath.evaluate("JE802TrafficGen", aTopLevelNode, XPathConstants.NODESET);
                // APs do not generate traffic, but they relay stations' traffic to destination in a BSS
//                                    if(isAP == false)
//                                        {
                for (int i = 0; i < tgList.getLength(); i++) {
                    Node tgNode = tgList.item(i);
                    this.message("allocating " + tgNode.getNodeName(), 10);
                    JE802TrafficGen aNewTrafficGen = new JE802TrafficGen(aScheduler, aGenerator, tgNode, this.address, aStatEval,
                            tcp);
                    this.trafficGenerators.add(aNewTrafficGen);
                    // stop generating traffic, usually not used:
                    aNewTrafficGen.send(new JEEvent("stop_req", aNewTrafficGen, aNewTrafficGen.getStopTime()));
                    // start generating traffic:
                    aNewTrafficGen.send(new JEEvent("start_req", aNewTrafficGen, aNewTrafficGen.getStartTime()));
                }
//                                        }

            } else {
                this.message("XML definition " + aTopLevelNode.getNodeName() + " has no child nodes!", 10);
            }
        } else {
            this.message("XML definition " + aTopLevelNode.getNodeName() + " found, but JE802Station expected!", 10);
        }

        if (aGui != null) {
            aGui.setupStation(this.address);
        }

    }

    // APs to which the STA is associated
    void Set_SSID(String pssid) {
        ssid.mac_addr = pssid.getBytes();
    }

    // APs to which the STA is associated
    String Get_SSID() {
        return ssid.toString();
    }

    /**
     * Calculates the Horizontal distance of the specific node from another
     * node.
     *
     * @param fromx The mobility.xLocation-distance from the other node, e.g.
     * specificNode.mobility.xLocation-anotnerNode.mobility.xLocation
     * @param fromy The mobility.yLocation-distance from the other node, e.g.
     * specificNode.mobility.yLocation-anotnerNode.mobility.yLocation
     * @return The distance from the other node in meters.
     */
    private double Horizontal_DistFrom_AP(double fromx, double fromy) {
        double xdist = 0;
        double ydist = 0;

        if (fromx > mobility.xLocation) {
            xdist = fromx - mobility.xLocation;
        } else {
            xdist = mobility.xLocation - fromx;
        }

        if (fromy > mobility.yLocation) {
            ydist = fromy - mobility.yLocation;
        } else {
            ydist = mobility.yLocation - fromy;
        }

        return (int) Math.sqrt(Math.pow(xdist, 2) + Math.pow(ydist, 2));
    }

    /**
     * Calculates the Vertical distance of the specific node from another node.
     *
     * @param fromx The mobility.xLocation-distance from the other node, e.g.
     * specificNode.mobility.xLocation-anotnerNode.mobility.xLocation
     * @param fromy The mobility.yLocation-distance from the other node, e.g.
     * specificNode.mobility.yLocation-anotnerNode.mobility.yLocation
     * @return The distance from the other node in meters.
     */
    private double Vertical_DistFrom_AP(double fromz) {
        double zdist = 0;

        if (fromz > mobility.zLocation) {
            zdist = fromz - mobility.zLocation;
        } else {
            zdist = mobility.zLocation - fromz;
        }

        return (int) zdist;
    }

    /**
     * Calculates the distance of the specific node from its AP.
     *
     * @param fromx The mobility.xLocation-distance from the other node, e.g.
     * specificNode.mobility.xLocation-anotnerNode.mobility.xLocation
     * @param fromy The mobility.yLocation-distance from the other node, e.g.
     * specificNode.mobility.yLocation-anotnerNode.mobility.yLocation
     * @return The distance from the other node in meters.
     */
    public double DistFromAP() {
        acStation n = this;
        double horiz, vertic;

        horiz = Horizontal_DistFrom_AP(n.mobility.xLocation, n.mobility.yLocation);
        vertic = Vertical_DistFrom_AP(n.mobility.zLocation);

        return (int) Math.sqrt(Math.pow(horiz, 2) + Math.pow(vertic, 2));
    }

    /*
     * Distance between two STAs
     */
    public int DistBtwSTAs(acStation sta1, acStation sta2) {
        double locSTAxLocation = sta1.mobility.xLocation;
        double locSTAyLocation = sta1.mobility.yLocation;
        double locSTAzLocation = sta1.mobility.zLocation;
        double distSTAxLocation = sta2.mobility.xLocation;
        double distSTAyLocation = sta2.mobility.yLocation;
        double distSTAzLocation = sta2.mobility.zLocation;

        return (int) DistBtwPoints(
                locSTAxLocation, distSTAxLocation,
                locSTAyLocation, distSTAyLocation,
                locSTAzLocation, distSTAzLocation
        );
    }

    /*
     * Distance between two points
     */
    public double DistBtwPoints(
            double locSTAxLocation, double distSTAxLocation,
            double locSTAyLocation, double distSTAyLocation,
            double locSTAzLocation, double distSTAzLocation
    ) {
        double xdist, ydist, zdist;

        if (locSTAxLocation > distSTAxLocation) {
            xdist = locSTAxLocation - distSTAxLocation;
        } else {
            xdist = distSTAxLocation - locSTAxLocation;
        }

        if (locSTAyLocation > distSTAyLocation) {
            ydist = locSTAyLocation - distSTAyLocation;
        } else {
            ydist = distSTAyLocation - locSTAyLocation;
        }

        if (locSTAzLocation > distSTAzLocation) {
            zdist = locSTAzLocation - distSTAzLocation;
        } else {
            zdist = distSTAzLocation - locSTAzLocation;
        }

        return (Math.sqrt(Math.pow(xdist, 2) + Math.pow(ydist, 2) + Math.pow(zdist, 2)));
    }

    /*
     * Vertical distance
     */
    @Override
    public String toString() {
        return Integer.toString(theMac.getMacAddress());
    }

}
