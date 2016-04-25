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
package IEEE11ac.layer1;

import IEEE11ac.layer2.acMPDU;
import IEEE11ac.layer2.acMac;
import IEEE11ac.station.acStation;
import IEEE11af.layer1.afMCS;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
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
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer0.WirelessChannel;
import zzInfra.layer0.WirelessMedium;
import zzInfra.layer1.JE802Radio;
import zzInfra.layer1.JE802Phy;
import zzInfra.layer1.JE802PhyMCS;
import zzInfra.layer1.JE802Ppdu;
import zzInfra.layer2.JE802Mac;

public class acPhy extends JE802Phy {

    private JETime aSlotTime;

    private JETime SIFS;

    private JETime SymbolDuration;

    private int PLCPTail_bit;

    private int PLCPServiceField_bit;

    private JETime PLCPHeaderWithoutServiceField;

    private JETime PLCPPreamble;

    private JETime PLCPHeaderDuration;

    private JETime emulationEnd, halfDuration;

    private final acMac myMac;

    public acPhy(JEEventScheduler aScheduler, JE802StatEval statEval, Random aGenerator, WirelessMedium aChannel,
            JE802Gui aGui, Node aTopLevelNode, acStation sta, String std) throws XPathExpressionException {

        super(aScheduler, statEval, aGenerator, aChannel, aGui, aTopLevelNode, sta.getMac());

        myMac = (acMac) sta.getMac();

        Element phyElem = (Element) aTopLevelNode;

        if (phyElem.getTagName().equals("JE80211PHY")) {

            XPath xpath = XPathFactory.newInstance().newXPath();

            String GnuRadio = phyElem.getAttribute("useGnuRadio");
            if (GnuRadio.equals("")) {
                this.useGnuRadio = false;
            } else {
                this.useGnuRadio = new Boolean(phyElem.getAttribute("useGnuRadio"));
            }

            if (this.useGnuRadio) {
                this.message("Station " + sta.getMac().getMacAddress() + " connecting to GnuRadio now.");
                this.theRealPhy = new JE802Radio(sta.getMac().getMacAddress());
            }

            this.SymbolDuration = new JETime(new Double(phyElem.getAttribute("SymbolDuration_ms")));
            this.PLCPTail_bit = new Integer(phyElem.getAttribute("PLCPTail_bit"));
            this.PLCPServiceField_bit = new Integer(phyElem.getAttribute("PLCPServiceField_bit"));
            this.PLCPHeaderWithoutServiceField = new JETime(new Double(phyElem.getAttribute("PLCPHeaderWithoutServiceField_ms")));
            this.PLCPPreamble = new JETime(new Double(phyElem.getAttribute("PLCPPreamble_ms")));
            this.PLCPHeaderDuration = this.PLCPPreamble.plus(PLCPHeaderWithoutServiceField);

            createPhyMCSs(phyElem, xpath, std);

            // mib
            Element mibElem = (Element) xpath.evaluate("MIB802.11ac", phyElem, XPathConstants.NODE);
            if (mibElem != null) {
                this.aSlotTime = new JETime(new Double(mibElem.getAttribute("aSlotTime")));
                this.SIFS = new JETime(new Double(mibElem.getAttribute("SIFS")));
                this.currentTransmitPowerLevel_dBm = new Double(mibElem.getAttribute("dot11CurrentTransmitPowerLevel_dBm"));
                this.currentTransmitPower_mW = Math.pow(10, (currentTransmitPowerLevel_dBm - 30) / 10);
                this.currentChannelNumberRX = new Integer(mibElem.getAttribute("dot11CurrentChannelNumberRX"));
                this.currentChannelNumberTX = new Integer(mibElem.getAttribute("dot11CurrentChannelNumberTX"));
            } else {
                this.error("No MIB802.11ac definition found !!!");
            }

        } else {
            this.error("Construction of JE802Phy did not receive JE802Phy xml node");
        }

        this.emulationEnd = new JETime(10000);
        this.halfDuration = this.emulationEnd.times(1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jemula.kernel.JEEventHandler#event_handler(jemula.kernel.JEEvent)
     */
    @Override
    public void event_handler(JE802Station sta, JEEvent anEvent) {

        JETime now = anEvent.getScheduledTime();
        String anEventName = anEvent.getName();

        if (this.locState == state.idle) {

            if (anEventName.equals("stop_req")) {
                // ignore;

            } else if (anEventName.equals("start_req")) {
                this.message("PHY received event start_req while in idle state", 1);

                this.parameterlist.clear();
                this.parameterlist.add(this);
                this.send(new JEEvent("register_req", this.theUniqueRadioChannel.getHandlerId(), now, this.parameterlist));
                if (this.currentChannelNumberRX != this.currentChannelNumberTX) {
                    // register TX channel scanning
                    this.send(new JEEvent("registerCcaTX_req", this.theUniqueRadioChannel.getHandlerId(), now, this.parameterlist));
                }
                if (mobility.isMobile()) {
                    this.send(new JEEvent("location_update", this, mobility.getTraceStartTime()));
                }

                this.locState = state.active;

            } else {
                this.error("undefined event '" + anEventName + "' in state " + this.locState);
            }

        } else if (this.locState == state.active || this.locState == state.active_sync) {

            if (anEventName.equals("PHY_SyncEnd_ind")) { // Pietro: we need to
                // check which MAC
                // is present in the
                // packet
                this.parameterlist.clear();
                this.parameterlist = anEvent.getParameterList();
                acPPDU aPpdu = (acPPDU) this.parameterlist.elementAt(0);
                if (!aPpdu.isJammed() && currentTxEnd.isEarlierThan(now) && concurrentRx == 1) { // Pietro:
                    // get
                    // the
                    // mac
                    // Mpdu
                    acMPDU aMpdu = aPpdu.getMpdu();
                    this.parameterlist = new Vector<Object>();
                    this.parameterlist.add(aMpdu.getDA());
                    this.parameterlist.add(aMpdu.getAC());
                    this.parameterlist.add(aMpdu.getNav());
                    // this.parameterlist.addAll(aMpdu.getStartParameterList());
                    this.send(new JEEvent("PHY_RxStart_ind", this.getMac(), now, this.parameterlist));
                }
                this.locState = state.active;

            } else if (anEventName.equals("MEDIUM_RxStart_ind")) {
                this.parameterlist.clear();
                this.parameterlist = anEvent.getParameterList();
                concurrentRx++;
                if (concurrentRx > maxConcurrentRx) {
                    maxConcurrentRx = concurrentRx;
                }
                JE802Ppdu aPpdu = (JE802Ppdu) this.parameterlist.elementAt(0);
                if (!aPpdu.isJammed() && this.currentTxEnd.isEarlierThan(now) && this.currentRxEnd.isEarlierThan(now)) {
                    this.send(new JEEvent("PHY_SyncStart_ind", this.getMac(), now));
                    this.send(new JEEvent("PHY_SyncEnd_ind", this, now.plus(PLCPHeaderDuration), this.parameterlist));
                    // this.locState = state.active_sync; // for a short amount
                    // of time, change state to sync, until SyncEnt
                }
                JETime receptionDuration = aPpdu.getMpdu().getTxTime();
                statEval.recordPowerRx(sta.getMac().getMacAddress(), this.getHandlerId(), now, receptionDuration);
                JETime newRxEnd = now.plus(aPpdu.getMpdu().getTxTime());
                if (newRxEnd.isLaterThan(currentRxEnd)) {
                    this.currentRxEnd = newRxEnd;
                }

            } else if (anEventName.equals("MEDIUM_RxEnd_ind")) {
                concurrentRx--;
                if (now.isLaterThan(currentRxEnd)) {
                    maxConcurrentRx = 0;
                }
                Vector<Object> parameterList = new Vector<Object>();
                JE802Ppdu aPpdu = (JE802Ppdu) anEvent.getParameterList().get(0);
                // if the current rxStart was later than the txEnd otherwise,
                // don't even report because its garbage anyway and we are not
                // in reception state
                if (now.minus(aPpdu.getMpdu().getTxTime()).isLaterThan(currentTxEnd)) {
                    if (aPpdu.isJammed() || maxConcurrentRx > 1) {
                        parameterList.add(null);
                    } else {
                        parameterList.add(aPpdu.getMpdu());
                    }
                    this.send(new JEEvent("PHY_RxEnd_ind", this.getMac().getHandlerId(), theUniqueEventScheduler.now(),
                            parameterList));
                }
                if (now.getTimeMs() == currentRxEnd.getTimeMs()) {
                    maxConcurrentRx = 0;
                }

            } else if (anEventName.equals("PHY_ChannelSwitch_req")) {

                int from = this.currentChannelNumberTX;
                this.currentChannelNumberRX = (Integer) anEvent.getParameterList().elementAt(0);
                this.currentChannelNumberTX = this.currentChannelNumberRX;
                this.concurrentRx = 0;
                this.currentRxEnd = new JETime(-1);
                this.currentTxEnd = new JETime(-1);
                this.parameterlist.clear();
                this.parameterlist.add(this);
                this.parameterlist.add(from);
                this.parameterlist.add(currentChannelNumberTX);
                // do not switch channel while transmitting a packet
                this.send(new JEEvent("channel_switch_req", this.theUniqueRadioChannel.getHandlerId(), now, this.parameterlist));

            } else if (anEventName.equals("PHY_TxStart_req")) {

                this.parameterlist.clear();
                this.parameterlist = anEvent.getParameterList();

                // Pietro: here we need to know if either a 802.11 or 802.15
                // frame shall be transmitted
                acMPDU aMpdu = (acMPDU) this.parameterlist.elementAt(0);
                statEval.addPacketForCounts(aMpdu);
                acPPDU aPpdu = new acPPDU(aMpdu, this.currentTransmitPowerLevel_dBm, this.currentChannelNumberTX);
                JETime txDuration = aMpdu.getTxTime();
                statEval.recordPowerTx(sta.getMac().getMacAddress(), this.getHandlerId(), now, txDuration);
                currentTxEnd = now.plus(aMpdu.getTxTime());
                this.parameterlist.clear();
                this.parameterlist.addElement(this);
                this.parameterlist.addElement(aPpdu);

                // show frame in GUI
                if (theUniqueGui != null) {
                    String type = aMpdu.getType() + " " + aMpdu.getSeqNo();
                    if (aMpdu.isData()) {
                        String ipPacketType = aMpdu.getPayload().getClass().getName();
                        if (ipPacketType.contains("RREQ")) {
                            type = type + " RREQ";
                        } else if (ipPacketType.contains("RREP")) {
                            type = type + " RREP" + aMpdu.getPayload().getSA();
                        } else if (ipPacketType.contains("RRER")) {
                            type = type + " RERR";
                        }
                    }
                    theUniqueGui.addFrame(theUniqueEventScheduler.now(), aMpdu.getTxTime(), aPpdu.getChannelNumber(),
                            aMpdu.getSA(), type, (aMpdu.getPhyMcs().toString()), "DA: " + aMpdu.getDA(),
                            this.currentChannelNumberTX);
                }

                if (this.useGnuRadio) {
                    // request radio to transmit frame
                    this.theRealPhy.tx(aPpdu);

                } else {
                    // forward MPDU as PPDU to channel:
                    this.send(new JEEvent("MEDIUM_TxStart_req", this.theUniqueRadioChannel.getHandlerId(), now,
                            this.parameterlist));

                    this.parameterlist.clear();
                    this.parameterlist.addElement(aPpdu.getMpdu().getAC());
                    this.send(new JEEvent("PHY_TxEnd_ind", this.getMac(), now.plus(aMpdu.getTxTime()), this.parameterlist));

                }

            } else if (anEventName.equals("location_update")) {
                this.parameterlist = new Vector<Object>();
                this.parameterlist.add(this);
                this.send(new JEEvent("location_update_req", this.theUniqueRadioChannel.getHandlerId(), now, parameterlist));
                if (sta.getMac().getMacAddress() == 2) {
                    if (now.isEarlierEqualThan(this.halfDuration)) {
                        this.mobility.setXLocation(this.mobility.getXLocation() + 0.005);
                    } else {
                        this.mobility.setXLocation(this.mobility.getXLocation() - 0.005);
                    }
                }
                this.send(new JEEvent("location_update", this, now.plus(mobility.getInterpolationInterval_ms())));

            } else if (anEventName.equals("start_req")) {
                // ignore

            } else if (anEventName.equals("stop_req")) {
                this.locState = state.idle;

            } else {
                this.error("undefined event '" + anEventName + "' in state " + this.locState);
            }

        } else {
            this.error("undefined event handler state.");
        }
    }

    public int getPLCPServiceField_bit() {
        return PLCPServiceField_bit;
    }

    public JETime getSlotTime() {
        return aSlotTime;
    }

    public JETime getSIFS() {
        return SIFS;
    }

    public JETime getPLCPHeaderWithoutServiceField() {
        return PLCPHeaderWithoutServiceField;
    }

    public JETime getSymbolDuration() {
        return SymbolDuration;
    }

    public JETime getPLCPPreamble() {
        return PLCPPreamble;
    }

    @Override
    public JE802Mac getMac() {
        return myMac;
    }

    public double getReuseDistance() {
        return this.theUniqueRadioChannel.getReuseDistance();
    }

    public Integer getPLCPTail_bit() {
        return PLCPTail_bit;
    }

    public List<WirelessChannel> getAvailableChannels() {
        return theUniqueRadioChannel.getAvailableChannels();
    }

//    @Override
    public String toString(acStation sta) {
        return ("Phy" + sta.getMac().getMacAddress() + "_Rx" + this.currentChannelNumberRX + "_Tx" + this.currentChannelNumberTX);
    }

    public JETime getPLCPHeaderDuration() {
        return this.PLCPHeaderDuration;
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

}
