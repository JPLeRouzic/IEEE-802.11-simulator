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
package zzInfra.layer1;

import IEEE11ac.layer1.acMCS;
import IEEE11af.layer1.afMCS;
import IEEE11ax.layer1.axMCS;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import zzInfra.ARC.JE802Station;
import zzInfra.emulator.JE802StatEval;
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer0.WirelessChannel;
import zzInfra.layer0.WirelessMedium;
import zzInfra.layer2.JE802Mac;

public abstract class JE802Phy extends JEEventHandler {

//	protected acMac theMac;
    protected final JE802Gui theUniqueGui;

    protected final WirelessMedium theUniqueRadioChannel;

    protected JE802Mobility mobility;

    protected boolean useBasicModes;

    protected JETime currentTxEnd = new JETime(-1);

    protected JETime currentRxEnd = new JETime(-1);

    public JE802PhyMCS theCurrentPhyMCS;
    public List<JE802PhyMCS> thePhyMCSList;
    public JE802PhyMCS theDefaultPhyMCS;

    private List<JE802PhyMCS> phyModes;
    
    private JE802Mac theMac;

    public JE802Mac getMac() {
        return theMac;
    }

    protected enum state {

        idle, active, active_sync
    }

    protected state locState;

    protected int concurrentRx = 0;

    protected int maxConcurrentRx = 0;

    protected final JE802StatEval statEval;

    protected Boolean useGnuRadio;

    protected JE802Radio theRealPhy;

    protected JEAntenna theAntenna;

    protected int currentChannelNumberRX;

    protected int currentChannelNumberTX;

    protected double currentTransmitPowerLevel_dBm;

    protected double currentTransmitPower_mW;

    protected boolean onceTx = false;

    public JE802Phy(JEEventScheduler aScheduler, JE802StatEval statEval, Random aGenerator, WirelessMedium aChannel,
            JE802Gui aGui, Node aTopLevelNode, JE802Mac theMac) throws XPathExpressionException {

        super(aScheduler, aGenerator);

        this.theUniqueGui = aGui;
        this.locState = state.idle;
        this.theUniqueRadioChannel = aChannel;
        this.statEval = statEval;
        this.phyModes = new ArrayList<JE802PhyMCS>();
	this.theMac = theMac;

        Element phyElem = (Element) aTopLevelNode;

        if (phyElem.getTagName().startsWith("JE802") && phyElem.getTagName().endsWith("PHY")) {

            XPath xpath = XPathFactory.newInstance().newXPath();

            String basicModes = phyElem.getAttribute("useBasicModesForCtrl");
            if (basicModes.equals("")) {
                this.useBasicModes = false;
            } else {
                this.useBasicModes = new Boolean(basicModes);
            }

            // setup antenna (like specified in XML if "JEAntenna" element
            // exists, uniform/omnidirectional otherwise
            Element antennaElem = (Element) xpath.evaluate("JEAntenna", phyElem, XPathConstants.NODE);
            if (antennaElem != null) {
                double gain = new Double(antennaElem.getAttribute("gain_dBi"));

                if (gain <= 0) {
                    this.warning("Station " + theMac.getMacAddress()
                            + ": Antenna gain must be positive, using an omnidirectional antenna instead.");
                }

                /*
                 * double gainLow = new
                 * Double(antennaElem.getAttribute("gainLow_dBi")); if(gainLow
                 * >= 0) { this.warning("Station " + this.theMac.getMacAddress()
                 * +
                 * ": Antenna gainLow must be negative, using an omnidirectional antenna instead."
                 * ); }
                 */
                this.theAntenna = new JEAntenna(new Double(antennaElem.getAttribute("xDirection")), new Double(
                        antennaElem.getAttribute("yDirection")), new Double(antennaElem.getAttribute("zDirection")), gain,
                        new Double(antennaElem.getAttribute("angle_degrees")));
            } else {
                this.warning("Station " + theMac.getMacAddress()
                        + ": No JEAntenna XML tag found. Assuming an omnidirectional antenna.");
                this.useOmnidirectionalAntenna();
            }
        } else {
            this.error("Construction of JE802Phy did not receive JE802Phy xml node");
        }
    }

    public abstract void event_handler(JE802Station sta, final JEEvent anEvent);

    /*
     * (non-Javadoc)
     * 
     * @see jemula.kernel.JEEventHandler#event_handler(jemula.kernel.JEEvent)
     * when outside circle of influence
     */
    public double getReuseDistance() {
        return this.theUniqueRadioChannel.getReuseDistance();
    }

    public JE802Mobility getMobility() {
        return mobility;
    }

    public List<WirelessChannel> getAvailableChannels() {
        return theUniqueRadioChannel.getAvailableChannels();
    }

    public void setMobility(JE802Mobility mobility) {
        this.mobility = mobility;
    }

    public void setOnceTx(boolean onceTx) {
        this.onceTx = onceTx;
    }

    public boolean isOnceTx() {
        return onceTx;
    }

    @Override
    public int hashCode() {
        return this.getHandlerId();
    }

    public boolean isCcaBusy() {
        if (this.useGnuRadio) {
            return theGnuRadioisCcaBusy();
        }
        double powerLevel_mW = this.theUniqueRadioChannel.getRxPowerLevel_mW(this);
        boolean busy = powerLevel_mW > this.theUniqueRadioChannel.getBusyPowerLevel_mW();
        return busy;
    }

    public boolean theGnuRadioisCcaBusy() {
        double powerLevel_mW = this.theRealPhy.getCca_mW();
        boolean busy = powerLevel_mW > this.theUniqueRadioChannel.getBusyPowerLevel_mW();
        return busy;
    }

    @Override
    public String toString() {
        return this.currentChannelNumberRX + "/" + this.currentChannelNumberTX + "/" + this.currentRxEnd + "/"
                + this.currentTxEnd + super.toString();
    }

    public JEAntenna getAntenna() {
        return theAntenna;
    }

    public void useOmnidirectionalAntenna() {
        this.theAntenna = new JEAntenna(1, 0, 0, 0, 180);
    }

    /**
     * @return the currentChannelNumberRX
     */
    public int getCurrentChannelNumberRX() {
        return currentChannelNumberRX;
    }

    /**
     * @param currentChannelNumberRX the currentChannelNumberRX to set
     */
    public void setCurrentChannelNumberRX(int currentChannelNumberRX) {
        this.currentChannelNumberRX = currentChannelNumberRX;
    }

    /**
     * @return the currentChannelNumberTX
     */
    public int getCurrentChannelNumberTX() {
        return currentChannelNumberTX;
    }

    /**
     * @param currentChannelNumberTX the currentChannelNumberTX to set
     */
    public void setCurrentChannelNumberTX(int currentChannelNumberTX) {
        this.currentChannelNumberTX = currentChannelNumberTX;
    }

    /**
     * @return the currentTransmitPowerLevel_dBm
     */
    public double getCurrentTransmitPowerLevel_dBm() {
        return currentTransmitPowerLevel_dBm;
    }

    /**
     * @param currentTransmitPowerLevel_dBm the currentTransmitPowerLevel_dBm to
     * set
     */
    public void setCurrentTransmitPowerLevel_dBm(double currentTransmitPowerLevel_dBm) {
        this.currentTransmitPowerLevel_dBm = currentTransmitPowerLevel_dBm;
    }

    /**
     * @return the currentTransmitPower_mW
     */
    public double getCurrentTransmitPower_mW() {
        return currentTransmitPower_mW;
    }

    /**
     * @param currentTransmitPower_mW the currentTransmitPower_mW to set
     */
    public void setCurrentTransmitPower_mW(double currentTransmitPower_mW) {
        this.currentTransmitPower_mW = currentTransmitPower_mW;
    }

    public void createPhyMCSs(Element Node, XPath xpath, String std) throws XPathExpressionException {
        // phymodes
        // Node phyModesNode = (Node) xpath.evaluate("JE802PhyMCSs", Node, XPathConstants.NODE);
        //phyStd = 
        if (Node != null) {
            NodeList phyModeList = (NodeList) xpath.evaluate("aPhyMCS", Node, XPathConstants.NODESET);
            for (int i = 0; i < phyModeList.getLength(); i++) {
                Node phyNode = phyModeList.item(i);
                if (std.equals("11ac")) {
                    phyModes.add(new acMCS(phyNode));
                } else if (std.equals("11af")) {
                    phyModes.add(new afMCS(phyNode));
                } else if (std.equals("11ax")) {
                    phyModes.add(new axMCS(phyNode));
                }

            }
            statEval.setPhyMCSs(phyModes);
        } else {
            this.error("No MCS node specified in xml");
        }
    }

    public JE802PhyMCS getCurrentPhyMCS() {
        return theCurrentPhyMCS;
    }

    public JE802PhyMCS getDefaultPhyMCS() {
        return theDefaultPhyMCS;
    }

    public void setCurrentPhyMCS(String aName) {
        if (aName.equals("default")) {
            theCurrentPhyMCS = this.theDefaultPhyMCS;
        } else if (aName.equals(theCurrentPhyMCS.getName())) {
            // do nothing
        } else {
            for (int cnt = 0; cnt < thePhyMCSList.size(); cnt++) {
                if (aName.equals(thePhyMCSList.get(cnt).getName())) {
                    theCurrentPhyMCS = thePhyMCSList.get(cnt);
                    break;
                }
            }
        }
    }
}
