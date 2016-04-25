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
package IEEE11ac.layer2;

import IEEE11ac.layer1.acPhy;
import IEEE11ac.layer1.acRateAdaptation;
import IEEE11ac.layer2.acMPDU.JE80211MpduType;
import IEEE11ac.station.acStation;
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
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JETimer;
import zzInfra.layer1.JE802PhyMCS;
import zzInfra.layer3_network.JE802IPPacket;

public final class acBackoffEntity extends JEEventHandler {

    private long overallTPCtr;
    private long specificTPCtr;

    private final acMac theMac;

    private final acVCollisionHandler theVch;

    private final JE802Gui theUniqueGui;

    private int theCW;

    private int theAC;

    private int dot11EDCACWmin;

    private int dot11EDCACWmax;

    private double dot11EDCAPF;

    private int dot11EDCAAIFSN;

    private JETime dot11EDCAMMSDULifeTime;

    private int dot11EDCATXOPLimit;

    private int theShortRetryCnt;

    private int theLongRetryCnt;

    private int discardedCounter = 0;

    private int maxRetryCountShort;

    private int maxRetryCountLong;

    private Vector<acMPDU> theQueue;

    private int theQueueSize;

    private acTimerNav theNavTimer;

    private JETimer theTxTimeoutTimer;

    private JETimer theInterFrameSpaceTimer;

    private acTimerBackoff theBackoffTimer;

    private acMPDU theMpduRx;

    private acMPDU theMpduData;

    private acMPDU theMpduRts;

    private acMPDU theMpduCtrl;

    private JETime theSlotTime;

    private JETime theSIFS;

    private JETime theAIFS;

    private int collisionCount = 0;

//	private List<JE802PhyMode> phyList; not in our concept
    private int noAckCount = 0;
    int debugCount = 0;

    private enum txState {

        idle, txRts, txAck, txCts, txData
    }

    private txState theTxState;

    private acRateAdaptation rate;
    
    private int faultToleranceThreshold;

    private long previousDiscardedPacket = -1;
    private long previousReceivedAck = -1;
    private int successfullyTxedCount;

    public acBackoffEntity(JEEventScheduler aScheduler,
            Random aGenerator, JE802Gui aGui, Node aTopLevelNode,
            acMac aMac, acVCollisionHandler aVch) throws XPathExpressionException {

        super(aScheduler, aGenerator);
        theUniqueGui = aGui;
        this.theMac = aMac;
        this.theVch = aVch;

        overallTPCtr = 0;
        specificTPCtr = 0;

        /* 		this.phyList = new ArrayList<JE802PhyMode>();

         for (JE802PhyMode aPhyMode : phyList)
         aPhyMode.display_status();
         */
        Element backoffElem = (Element) aTopLevelNode;
        if (backoffElem.getNodeName().equals("JE802BackoffEntity")) {
            this.theAC = new Integer(backoffElem.getAttribute("AC"));
            this.theQueue = new Vector<acMPDU>();
            this.theQueueSize = new Integer(
                    backoffElem.getAttribute("queuesize"));

            XPath xpath = XPathFactory.newInstance().newXPath();
            Element mibElem = (Element) xpath.evaluate("MIB802.11e",
                    backoffElem, XPathConstants.NODE);
            if (mibElem != null) {
                this.dot11EDCACWmin = new Integer(
                        mibElem.getAttribute("dot11EDCACWmin"));
                this.dot11EDCACWmax = new Integer(
                        mibElem.getAttribute("dot11EDCACWmax"));
                this.dot11EDCAPF = new Double(
                        mibElem.getAttribute("dot11EDCAPF"));
                this.dot11EDCAAIFSN = new Integer(
                        mibElem.getAttribute("dot11EDCAAIFSN"));
                this.dot11EDCAMMSDULifeTime = new JETime(new Double(
                        mibElem.getAttribute("dot11EDCAMSDULifetime")));
                this.dot11EDCATXOPLimit = new Integer(
                        mibElem.getAttribute("dot11EDCATXOPLimit"));
            } else {
                this.error("Station " + this.theMac.getMacAddress()
                        + " no MIB parameters found.");
            }

            this.theSIFS = this.theMac.getPhy().getSIFS();
            this.theSlotTime = this.theMac.getPhy().getSlotTime();
            this.theAIFS = this.theSIFS.plus(this.theSlotTime
                    .times(this.dot11EDCAAIFSN));

            // contention window
            this.theCW = this.dot11EDCACWmin;

            // counters
            this.theShortRetryCnt = 0;
            this.theLongRetryCnt = 0;

            this.theNavTimer = new acTimerNav(theUniqueEventScheduler,
                    theUniqueRandomGenerator, aGui, "nav_expired_ind",
                    this.getHandlerId());
            this.theTxTimeoutTimer = new JETimer(theUniqueEventScheduler,
                    theUniqueRandomGenerator, "tx_timeout_ind",
                    this.getHandlerId());
            this.theInterFrameSpaceTimer = new JETimer(theUniqueEventScheduler,
                    theUniqueRandomGenerator, "interframespace_expired_ind",
                    this.getHandlerId());
            this.theBackoffTimer = new acTimerBackoff(
                    theUniqueEventScheduler, theUniqueRandomGenerator,
                    theUniqueGui, this.theMac.getMacAddress(), this.theAC,
                    "backoff_expired_ind", this.getHandlerId(),
                    this.theSlotTime, this);

            this.theTxState = txState.idle;
            this.updateBackoffTimer();

            this.theMpduRx = null;
            this.theMpduData = null;
            this.theMpduRts = null;
            this.theMpduCtrl = null;

        } else {
            this.warning("Station " + this.theMac.getMacAddress()
                    + " messed up xml, dude.");
        }

        this.faultToleranceThreshold = 3;

    }

    public void event_handler(acStation sta, JEEvent anEvent) {
        String anEventName = anEvent.getName();
        this.theLastRxEvent = anEvent;

        this.message("Station " + this.theMac.getMacAddress() + " on Channel " + this.theMac.getPhy().getCurrentChannelNumberTX() + " AC " + this.theAC
                + " received event '" + anEventName + "'", 30);

        if (anEventName.contains("update_backoff_timer_req")) {
            this.updateBackoffTimer();
        } else if (anEventName.contains("MLME")) {
            this.event_MlmeRequests(anEvent);
        } else if (anEventName.equals("MSDUDeliv_req")) {
            this.event_MSDUDeliv_req(anEvent);
        } else if (anEventName.equals("MPDUDeliv_req")) {
            this.event_MPDUDeliv_req(sta, anEvent);
        } else if (anEventName.equals("backoff_expired_ind")) {
            this.event_backoff_expired_ind(sta, anEvent);
        } else if (anEventName.equals("interframespace_expired_ind")) {
            this.event_interframespace_expired_ind(sta, anEvent);
        } else if (anEventName.equals("nav_expired_ind")) {
            this.event_nav_expired_ind(anEvent);
        } else if (anEventName.equals("tx_timeout_ind")) {
            this.event_tx_timeout_ind(anEvent);
        } else if (anEventName.equals("PHY_RxEnd_ind")) {
            this.event_PHY_RxEnd_ind(sta, anEvent);
        } else if (anEventName.equals("PHY_SyncStart_ind")) {
            this.event_PHY_SyncStart_ind(anEvent);
        } else if (anEventName.equals("MPDUReceive_ind")) {
            this.event_MPDUReceive_ind(anEvent);
        } else if (anEventName.equals("virtual_collision_ind")) {
            this.event_virtual_collision_ind(anEvent);
        } else if (anEventName.equals("PHY_TxEnd_ind")) {
            this.event_PHY_TxEnd_ind(anEvent);
        } else if (anEventName.equals("decrease_phy_mode")) {
            this.rate.event_reduce_phy_mode_ind(anEvent, this.theMac.getPhy());
        } else if (anEventName.equals("increase_phy_mode")) {
            this.rate.event_increase_phy_mode_ind(anEvent, this.theMac.getPhy());
        } else {
            this.error("Station " + this.theMac.getMacAddress() + " undefined event '" + anEventName + "'");
        }
    }

    private void event_virtual_collision_ind(JEEvent anEvent) {
        this.theTxState = txState.idle;
        this.updateBackoffTimer();
    }

    private void event_PHY_TxEnd_ind(JEEvent anEvent) {
        if (this.theTxState.equals(txState.txAck)
                || this.theTxState.equals(txState.txCts)) {
            this.theTxState = txState.idle;
            this.theMpduCtrl = null;
            this.keep_going(); // now let us continue our own backoff, because
            // the backoff timer was paused before. we do
            // this even after CTS, so in case data is not
            // following, we just go ahead with our own
            // stuff.
        } else if (this.theMpduData != null
                && this.theMpduData.getDA() == this.theMac
                .getDot11BroadcastAddress()) {
            this.theMpduData = null;
            this.theTxState = txState.idle;
            this.theMpduCtrl = null;
            this.send(new JEEvent("broadcast_sent", this.theMac.getHandlerId(),
                    anEvent.getScheduledTime()));
            this.keep_going();
        } else {
            // nothing
        }
    }

    private void increaseCw() {
        this.theCW = (int) Math.round((this.theCW + 1) * this.dot11EDCAPF) - 1;
        if (this.theCW < 0) {
            this.theCW = 0;
        } else if (this.theCW > this.dot11EDCACWmax) {
            this.theCW = this.dot11EDCACWmax;
        }
    }

    private void event_tx_timeout_ind(JEEvent anEvent) {
        if (this.theTxState == txState.txRts) { // RTS was sent, but station did
            // not receive CTS frame
            this.theTxState = txState.idle;
            collisionCount++;
            this.retryRts();
        } else if (this.theTxState == txState.txData) { // data was sent, but
            // station did not
            // receive an ack frame
            this.theTxState = txState.idle;
            collisionCount++;
            this.retryData();
        } else if (this.theTxState == txState.txCts) {
            this.theTxState = txState.idle;
            this.theMpduCtrl = null;
        } else if (this.theTxState == txState.txAck) {
            this.theTxState = txState.idle;
            this.theMpduCtrl = null;
        }
        this.send("tx_timeout_ind", this.theVch);
        this.keep_going();
    }

    private void retryData() {

        if (this.theMpduData == null) {
            this.error("Station " + this.theMac.getMacAddress()
                    + " retryData: no pending RTS/DATA frame to transmit");
        }

        if (this.theMpduData.getFrameBodySize() < this.theMac
                .getDot11RTSThreshold()) {
            this.theShortRetryCnt++; // station Short Retry Count

            if (this.theShortRetryCnt > this.theMac.getDot11ShortRetryLimit()) { // retransmissions
                // exceeded
                // the
                // limit,
                // now
                // discard
                // the
                // frame

                acMPDU data = this.theMpduData;
                this.theMpduData = null;
                this.theCW = this.dot11EDCACWmin; // reset contention window
                this.parameterlist.clear();
                this.parameterlist.add(data);
                this.parameterlist.add(false);
                this.parameterlist.add(this.theShortRetryCnt);
                setMaxRetryCountShort(Math.max(getMaxRetryCountShort(),
                        theShortRetryCnt));
                this.discardedCounter++;
                this.theShortRetryCnt = 0;
                this.send(new JEEvent("MSDU_discarded_ind", this.theMac,
                        theUniqueEventScheduler.now(), this.parameterlist));
            } else {
                this.increaseCw(); // increase the CW and ...
                this.deferForIFS(); // ... retry after the backoff
            }
        } else {
            this.theLongRetryCnt++; // station Long Retry Count
            if (this.theLongRetryCnt > this.theMac.getDot11LongRetryLimit()) {
                acMPDU data = this.theMpduData;
                this.theMpduData = null;
                this.theCW = this.dot11EDCACWmin; // reset contention window
                this.parameterlist.clear();
                this.parameterlist.add(data);
                this.parameterlist.add(false);
                this.parameterlist.add(this.theLongRetryCnt);
                maxRetryCountLong = Math
                        .max(maxRetryCountLong, theLongRetryCnt);
                this.discardedCounter++;
                this.theLongRetryCnt = 0;
                this.send(new JEEvent("MSDU_discarded_ind", this.theMac,
                        theUniqueEventScheduler.now(), this.parameterlist));
            } else {
                this.increaseCw(); // increase the CW and ...
                this.deferForIFS(); // ... retry after the backoff
            }

        }
    }

    private void retryRts() {
        if ((this.theMpduData == null) | (this.theMpduRts == null)) {
            this.error("Station " + this.theMac.getMacAddress()
                    + " retryRTS: no pending RTS/DATA frame to transmit");
        }
        this.theShortRetryCnt++; // station Short Retry Count
        if (this.theShortRetryCnt > this.theMac.getDot11ShortRetryLimit()) {
            acMPDU data = this.theMpduData;
            this.theMpduRts = null;
            this.theMpduData = null;
            maxRetryCountLong = Math.max(maxRetryCountLong, theLongRetryCnt);
            setMaxRetryCountShort(Math.max(getMaxRetryCountShort(),
                    theShortRetryCnt));
            this.theShortRetryCnt = 0;
            this.theLongRetryCnt = 0;
            this.discardedCounter++;
            this.theCW = this.dot11EDCACWmin; // reset contention window
            this.parameterlist.clear();
            this.parameterlist.add(data);
            this.parameterlist.add(false);
            this.send(new JEEvent("MSDU_discarded_ind", this.theMac,
                    theUniqueEventScheduler.now(), this.parameterlist));
        } else {
            this.increaseCw(); // increase the CW and ...
            this.deferForIFS(); // ... retry after the backoff
        }
    }

    private void event_backoff_expired_ind(acStation sta, JEEvent anEvent) {
        if (this.theMpduCtrl != null) {
            if (this.theBackoffTimer.is_idle() & this.theInterFrameSpaceTimer.is_idle()) {
                this.warning("Station " + this.theMac.getMacAddress() + " backoff error?");
                return;
            }
        }
        if (!this.checkAndTxRTS(sta, false)) {
            if (!this.checkAndTxDATA(sta, false)) {
                // no more rts and no more data.
                this.keep_going();
            }
        }
    }

    private void event_interframespace_expired_ind(acStation sta, JEEvent anEvent) {
//		if ((this.theMpduCtrl == null) && (this.theMpduRts == null)
//				&& (this.theMpduData == null)) {
//			//this.keep_going();
//		} else {
        if (this.checkAndTxCTRL()) {
        } else {
            if (this.theBackoffTimer.is_active()) {
                this.message("Station " + this.theMac.getMacAddress()
                        + " defer problem (possibly due to hidden station): backoff active while AIFS or any other IFS", 10);
            } else {
                if (this.checkAndTxRTS(sta, true)) {
//						theUniqueGui.addLine(theUniqueEventScheduler.now(),
//								this.theMac.getMacAddress(), this.theAC+3, "magenta",
//								1);
                } else {
                    if (this.checkAndTxDATA(sta, true)) {
//							theUniqueGui.addLine(theUniqueEventScheduler.now(),
//									this.theMac.getMacAddress(), this.theAC+2, "magenta",
//									1);
                    }
                }
//					theUniqueGui.addLine(theUniqueEventScheduler.now(),
//							this.theMac.getMacAddress(), this.theAC, "blue",
//							1);
            }
        }
//		}
    }

    private void event_nav_expired_ind(JEEvent anEvent) {
        keep_going();
    }

    private void keep_going() {
        if (this.theBackoffTimer.is_paused() && !this.busy()) { // now let us continue our own backoff
            this.deferForIFS();
        } else { // or check next Mpdu
            this.nextMpdu();
        }
    }

    private void event_PHY_RxEnd_ind(acStation sta, JEEvent anEvent) {
        // received packet completely or collision occurred
        this.parameterlist = anEvent.getParameterList();
        if (this.parameterlist.elementAt(0) == null) { // bad luck: no packet at
            // all, just garbage
            this.theMpduRx = null;
            this.keep_going();
        } else { // we successfully received a packet, which is now given to us
            // as event parameter.
            this.theMpduRx = (acMPDU) this.parameterlist.elementAt(0);
            if (this.theMpduRx.getDA() != theMac.getDot11BroadcastAddress()) {
                if ((this.theMpduRx.getDA() != this.theMac.getMacAddress())
                        || (this.theMpduRx.getAC() != this.theAC)) {
                    this.theMpduRx = null;
                    return; // this is all for this backoff entity. The frame is
                    // not for us.
                }
            }
            JE80211MpduType type = this.theMpduRx.getType();
            switch (type) {
                case data:
                    receiveData(sta);
                    break;
                case ack:
                    this.message("Station " + this.theMac.getMacAddress()
                            + " received ACK " + this.theMpduRx.getSeqNo()
                            + "from Station " + this.theMpduRx.getSA()
                            + " on channel "
                            + this.theMac.getPhy().getCurrentChannelNumberRX(), 10);
                    receiveAck();
                    break;
                case rts:
                    this.message("Station " + this.theMac.getMacAddress()
                            + " received RTS " + this.theMpduRx.getSeqNo()
                            + "from Station " + this.theMpduRx.getSA()
                            + " on channel "
                            + this.theMac.getPhy().getCurrentChannelNumberRX(), 10);
                    receiveRts(sta);
                    break;
                case cts:
                    this.message("Station " + this.theMac.getMacAddress()
                            + " received CTS " + this.theMpduRx.getSeqNo()
                            + "from Station " + this.theMpduRx.getSA()
                            + " on channel "
                            + this.theMac.getPhy().getCurrentChannelNumberRX(), 10);
                    receiveCts();
                    break;
                default:
                    this.error("Undefined MpduType");
            }
            this.theMpduRx = null;
        }
    }

    private void event_PHY_SyncStart_ind(JEEvent anEvent) {
        this.updateBackoffTimer();
    }

    private void updateBackoffTimer() {
        boolean busy = this.busy();
        if (this.theBackoffTimer.is_paused()) {
            if (busy) { // if busy then come back in 5 microseconds again and
                // see if channel is idle then
                this.send(new JEEvent("update_backoff_timer_req", this
                        .getHandlerId(), theUniqueEventScheduler.now().plus(
                                this.theSlotTime)));
            } else {
                //this.deferForIFS();
                this.theBackoffTimer.resume();
            }
        } else if (this.theBackoffTimer.is_active() && busy) {
            this.theBackoffTimer.pause();
        }
    }

    private void receiveRts(acStation sta) {
        if (this.theMpduRx.getDA() - this.theMac.getMacAddress() == 0) {
            if ((this.theMpduRx.getAC() - this.theAC) != 0) {
                this.error("Station " + this.theMac.getMacAddress()
                        + " generating CTS for wrong AC: AC[theMpduRx]="
                        + this.theMpduRx.getAC() + " and AC[this]="
                        + this.theAC);
            }
            this.generateCts(sta);
            if (this.theInterFrameSpaceTimer.is_active()) {
                this.theInterFrameSpaceTimer.stop();
            }
            this.deferForIFS();

        } else {
            // the received RTS-frame is for another station
        }
    }

    private void receiveAck() {

        this.noAckCount = 0;
        if (this.theTxState == txState.txData) {
            if (this.theMpduData == null) {
                this.error("Station " + this.theMac.getMacAddress()
                        + " receiveAck: station has no data frame");
            }
            acMPDU receivedData = this.theMpduData; // store for
            // forwarding
            this.parameterlist.clear();
            this.parameterlist.add(receivedData);

            if (this.getMac().getMlme().isARFEnabled()) {
                if (receivedData.getSeqNo() == this.previousReceivedAck + 1) {
                    this.successfullyTxedCount++;
                    this.previousReceivedAck = receivedData.getSeqNo();

                    if (this.successfullyTxedCount == 10) {
                        this.successfullyTxedCount = 0;
                    }
                }
            }

            if (this.theMpduData.getFrameBodySize() < this.theMac
                    .getDot11RTSThreshold()) {
                this.parameterlist.add(this.theShortRetryCnt);
                setMaxRetryCountShort(Math.max(getMaxRetryCountShort(),
                        theShortRetryCnt));
                this.theShortRetryCnt = 0;
            } else {
                this.parameterlist.add(this.theLongRetryCnt);
                maxRetryCountLong = Math
                        .max(maxRetryCountLong, theLongRetryCnt);
                this.theLongRetryCnt = 0;
            }

            this.theMpduData = null;
            this.theNavTimer.stop();
            this.theCW = this.dot11EDCACWmin; // reset contention window
            this.theTxState = txState.idle;
            this.theTxTimeoutTimer.stop();
            this.send(new JEEvent("MSDU_delivered_ind", this.theMac
                    .getHandlerId(), theUniqueEventScheduler.now(),
                    this.parameterlist));
            this.deferForIFS();
        } else {
            this.keep_going();
        }
    }

    private void receiveCts() {

        if (this.theTxState != txState.txRts) { // do nothing in case the
            // station did not transmit rts
            // frame
        } else {
            this.noAckCount = 0; // received CTS, so the packet is acknowledged,
            // now restart the counter.
            if (this.theMpduRts == null) {
                this.message("Station " + this.theMac.getMacAddress()
                        + " receiveCts: station has no pending rts frame", 30);
            }
            this.theMpduRts = null; // we sent the rts, and received cts. So
            // let's assume rts has done its job.
            if (this.theMpduData == null) {
                this.message("Station " + this.theMac.getMacAddress()
                        + " receiveCts: station has no pending data frame", 30);
            }
            this.theTxTimeoutTimer.stop();
            this.theShortRetryCnt = 0;
            setMaxRetryCountShort(Math.max(theShortRetryCnt,
                    getMaxRetryCountShort()));
            this.theCW = this.dot11EDCACWmin; // reset contention window
            this.deferForIFS();
        }
    }

    private void receiveData(acStation sta) {
        if (this.theMpduRx.getDA() == this.theMac.getMacAddress()) {
            if (this.theMpduRx.getAC() != this.theAC) {
                this.error("Station "
                        + this.theMac.getMacAddress()
                        + " this backoff entity generates an ACK for a data frame of different AC.");
            }
            this.theMpduCtrl = null;
            this.theTxTimeoutTimer.stop();
            // send received packet back to its source, for evaluation:
            this.parameterlist.clear();
            this.parameterlist.add(this.theMpduRx.clone()); // make a copy since
            // arrival time will
            // be changed until
            // mpdu gets
            // evaluated
            this.theMpduRx.setLastArrivalTime(theUniqueEventScheduler.now());
            this.parameterlist.add(theUniqueEventScheduler.now());
            this.parameterlist.add(this.theAC);
            if (this.theMpduRx.getHopAddresses() != null
                    && !this.theMpduRx.getHopAddresses().isEmpty()) {
                this.send(new JEEvent("packet_forward", this.theMac,
                        theUniqueEventScheduler.now(), this.parameterlist));
            } else {
                this.send(new JEEvent("packet_exiting_system_ind", this.theMac
                        .getHandlerId(), theUniqueEventScheduler.now(),
                        this.parameterlist));
            }
            this.theMpduRx.setLastArrivalTime(theUniqueEventScheduler.now());
            this.generateAck(sta); // generate an ACK
            this.deferForIFS();
            // the packet is a broadcast packet
        } else if (this.theMpduRx.getDA() == theMac.getDot11BroadcastAddress()) {
            this.theMpduCtrl = null;
            this.theTxTimeoutTimer.stop();
            // packet was send by ourselves
            if (this.theMpduRx.getSA() != this.theMac.getMacAddress()) {
                this.parameterlist.clear();
                this.parameterlist.add(this.theMpduRx.clone());
                this.parameterlist.add(theUniqueEventScheduler.now());
                this.parameterlist.add(this.theAC);
                this.send(new JEEvent("packet_exiting_system_ind", this.theMac
                        .getHandlerId(), theUniqueEventScheduler.now(),
                        this.parameterlist));
            }
            this.deferForIFS();
        } else {
            this.error("Station "
                    + this.theMac.getMacAddress()
                    + " the received MPDU should be ours, but it is for another station. Not good.");
        }
    }

    private void event_MPDUReceive_ind(JEEvent anEvent) {
        this.parameterlist = anEvent.getParameterList();
        Integer aMacId = new Integer((Integer) this.parameterlist.elementAt(0));
        Integer anAc = new Integer((Integer) this.parameterlist.elementAt(1));
        JETime aNav = new JETime((JETime) this.parameterlist.elementAt(2));

        if (this.theBackoffTimer.is_active()) { // the classical case: while
            // down-counting, another
            // station initiated a frame
            // exchange
            this.theBackoffTimer.pause();
        }
        this.theMpduRx = new acMPDU();
        this.theMpduRx.setDA(aMacId); // destination MAC address
        this.theMpduRx.setAC(anAc); // access category
        this.theMpduRx.setNAV(aNav); // NAV value
        if (!(aMacId == this.theMac.getMacAddress() && anAc == this.theAC)
                || aMacId == theMac.getDot11BroadcastAddress()) {
            this.theNavTimer.start(aNav, this.theMac.getMacAddress(),
                    this.theAC, this.theMac.getPhy().getCurrentChannelNumberRX());
        }
    }

    @SuppressWarnings("unchecked")
    private void event_MSDUDeliv_req(JEEvent anEvent) {
        this.parameterlist = anEvent.getParameterList();
        JE802IPPacket packet = (JE802IPPacket) this.parameterlist.elementAt(3);
        int size = packet.getLength();
        long seqno = (Long) anEvent.getParameterList().get(4); // MAC Seq No,
        // not TCP
        int DA = ((acHopInfo) this.parameterlist.elementAt(0)).getAddress();
        ArrayList<acHopInfo> hopAdresses = (ArrayList<acHopInfo>) this.parameterlist
                .elementAt(2);
        int sourceHandler = (Integer) anEvent.getParameterList().get(5);
        int headersize = this.theMac.getDot11MacHeaderDATA_byte();
        acMPDU aMpdu = new acMPDU(this.theMac.getMacAddress(), DA,
                JE80211MpduType.data, seqno, size, headersize, this.theAC,
                sourceHandler, hopAdresses, theUniqueEventScheduler.now(),
                packet);

		// no queue handling needed here: this is done in event_MPDUDeliv_req
        // ... . Just request MPDUDeliv_req and thats it.
        this.parameterlist.clear();
        this.parameterlist.add(aMpdu);
        this.send(new JEEvent("MPDUDeliv_req", this, theUniqueEventScheduler
                .now(), this.parameterlist));
    }

    private void event_MPDUDeliv_req(acStation sta, JEEvent anEvent) {

        specificTPCtr++;
        overallTPCtr++;

        this.parameterlist = anEvent.getParameterList();
        acMPDU aMpdu = (acMPDU) this.parameterlist.elementAt(0);
        aMpdu.setPhyMCS(this.theMac.getPhy().getCurrentPhyMCS());
        aMpdu.setTxTime(this.calcTxTime(sta, aMpdu)); // here we actually
        // store the txtime in the Mpdu as well now set the NAV to
        // FRAMEDURATION(the tx time)-SYNCDUR (usually 20us)+SIFS+ACK frame:
        aMpdu.setNAV(this
                .calcTxTime(sta, aMpdu)
                .minus(this.theMac.getPhy().getPLCPHeaderDuration())
                .plus(this.theSIFS.plus(this.calcTxTime(sta, 0, this.theMac.getDot11MacHeaderACK_byte(), this.theMac.getPhy()
                                        .getDefaultPhyMCS()))));

        if ((this.theMpduData == null)
                && (!this.theInterFrameSpaceTimer.is_active())) {
            this.theMpduData = aMpdu;
            this.theMpduData.setType(JE80211MpduType.data);
            this.theMpduData.setSA(this.theMac.getMacAddress());
            this.theMpduData.setTxTime(this.calcTxTime(sta, this.theMpduData));

            this.theMpduData.setNAV(this
                    .calcTxTime(sta, aMpdu)
                    .minus(this.theMac.getPhy().getPLCPHeaderDuration())
                    .plus(this.theSIFS.plus(this.calcTxTime(sta, 0, this.theMac.getDot11MacHeaderACK_byte(),
                                            this.theMac.getPhy()
                                            .getDefaultPhyMCS()))));

            this.generateRts(sta);
            boolean busy = this.busy();
            if (!this.theBackoffTimer.is_active()) {
                if (!busy) {
                    if (!this.theInterFrameSpaceTimer.is_active()) {
                        this.deferForIFS();
                    } else {
                        this.error("Station " + this.theMac.getMacAddress()
                                + " already deferring");
                    }
                } else {
                    if (this.theBackoffTimer.is_idle()) {
                        this.theBackoffTimer.start(sta, this.theCW, busy); // new
                        // backoff
                        this.updateBackoffTimer();
                    }
                }
            } else {
				// we have to wait until backoff expires. Happens often during
                // post backoff.
            }
        } else { // queue the MPDU or discard if queue full - in this case
            // inform TrafficGen
            if (this.theQueue.size() >= this.theQueueSize) {
                this.parameterlist.clear();
                this.parameterlist.add(this.theMpduData);
                this.parameterlist.add(true);
                this.send(new JEEvent("MSDU_discarded_ind", this.theMac,
                        theUniqueEventScheduler.now(), this.parameterlist));
            } else {
                this.theQueue.add(aMpdu);
            }
        }
    }

    private void nextMpdu() {
        if (!this.theQueue.isEmpty()) {
            if (this.theMpduData == null) {
                this.theLongRetryCnt = 0;
                this.theShortRetryCnt = 0;
                acMPDU aMpdu = this.theQueue.remove(0); // we pull out a
                // new mpdu from
                // the queue
                this.parameterlist.clear();
                this.parameterlist.add(aMpdu);
                this.send(new JEEvent("MPDUDeliv_req", this,
                        theUniqueEventScheduler.now(), this.parameterlist));
            } else { // backoff was stopped before. Resume it by starting IFS
                // defer.
                this.deferForIFS();
            }
        } else {
            if (this.theMpduData != null) {
                this.deferForIFS();
            }
			// the queue is empty. So we don't have anything to do - well this
            // is not entirely true:
            // If we are using the "saturation" traffic model, now is the time
            // to ask the traffic
            // gen for another MPDU. Why? Because we ALWAYS have to pump data.
            // So let us ask for another MPDU:
            this.parameterlist.clear();
            this.parameterlist.add(this.theAC);
            this.send(new JEEvent("empty_queue_ind", this.theMac,
                    theUniqueEventScheduler.now(), this.parameterlist));
        }
    }

    private void event_MlmeRequests(JEEvent anEvent) {
        String anEventName = anEvent.getName();
        if (anEventName.equals("undefinded")) { // nothing defined in MLME so
            // far
        } else {
            this.error("Station " + this.theMac.getMacAddress()
                    + " undefined MLME request event '" + anEventName + "'");
        }
    }

    private void tx(acMPDU aTxPdu, JETime aTimeout) {

        if (this.theMac.getMlme().isARFEnabled()) {
            aTxPdu.setPhyMCS(theMac.getPhy().getCurrentPhyMCS());
            if (aTxPdu.getSeqNo() == previousDiscardedPacket) {
                // this.noAckCount++;
                if (noAckCount == 3) {
                    noAckCount = 0;
                }
                this.noAckCount++;
            } else {
                previousDiscardedPacket = aTxPdu.getSeqNo();
            }
        }

        this.parameterlist.clear();
        this.parameterlist.add(aTxPdu);
        this.send(new JEEvent("txattempt_req", this.theVch,
                theUniqueEventScheduler.now(), this.parameterlist));
        if (aTxPdu.isData() || aTxPdu.isRts()) {
			// only RTS and DATA know the timeout, because they expect something
            // back: CTS or ACK, resp.
            this.theTxTimeoutTimer.start(aTimeout);
        }
    }

    private void deferForIFS() {
        if (!this.theInterFrameSpaceTimer.is_active()) { // ignore during
            // interframe space defering interval
            if (this.theMpduCtrl != null) { // CTS or ACK required
                this.theInterFrameSpaceTimer.start(this.theSIFS);
            } else if (this.theMpduRts != null) {
                if (!this.theBackoffTimer.is_active()) {
                    this.theInterFrameSpaceTimer.start(this.theAIFS);
                }
            } else if (this.theMpduData != null) {
                if (!this.theBackoffTimer.is_active()) {
                    if (this.theTxState == txState.txRts) {
                        this.theInterFrameSpaceTimer.start(this.theSIFS);
						// we sent RTS before and just received CTS.
                        // Now SIFS is used before DATA, not AIFS
                    } else {
                        this.theInterFrameSpaceTimer.start(this.theAIFS);
                    }
                } else { // backoff is busy. Do nothing though data is pending.
                    this.message("Station " + this.theMac.getMacAddress() + " deferForIFS: doing nothing though data is pending: backoff timer already busy.", 10);
                }
            } else { // the transmission was successful.
                this.theTxState = txState.idle;
                this.nextMpdu(); // now check for next MPDU
            }
        } else {
        }
    }

    private boolean busy() { // returns true if NAV or medium is busy, or
        // interframe space is ongoing (SIFS, AIFS)

        if (this.theNavTimer.getExpiryTime().isLaterThan(
                theUniqueEventScheduler.now())
                || this.theMac.getPhy().isCcaBusy()
                || this.theInterFrameSpaceTimer.is_active()) {
            return true;
        } else {
            return false;
        }
    }

    private void generateRts(acStation sta) {
        if (this.theMpduData != null) {
            if (this.theMpduRts == null) {
                if ((this.theMpduData.getFrameBodySize() < this.theMac
                        .getDot11RTSThreshold())) {
                    this.theMpduRts = null;
                } else {
                    Integer headersize = this.theMac
                            .getDot11MacHeaderRTS_byte();
                    this.theMpduRts = new acMPDU(
                            this.theMpduData.getSA(), this.theMpduData.getDA(),
                            JE80211MpduType.rts, 0, 0, headersize, this.theAC,
                            -1, null, null, null);
                    this.theMpduRts.setPhyMCS(this.theMac.getPhy().getDefaultPhyMCS());

                    // calculate transmission time, with RTS framebody size 0
                    this.theMpduRts.setTxTime(this.calcTxTime(sta, 0, this.theMac.getDot11MacHeaderRTS_byte(), this.theMac.getPhy()
                            .getDefaultPhyMCS()));
                    // set seqno
                    this.theMpduRts.setSeqNo(this.theMpduData.getSeqNo());
                    // calculate RTS NAV duration field
                    this.theMpduRts
                            .setNAV(this.theMpduRts
                                    .getTxTime()
                                    .minus(this.theMac.getPhy()
                                            .getPLCPHeaderDuration())
                                    .plus(this.theSIFS.plus(this
                                                    .calcTxTime(
                                                            sta,
                                                            0,
                                                            this.theMac
                                                            .getDot11MacHeaderCTS_byte(),
                                                            this.theMac
                                                            .getPhy().getDefaultPhyMCS())
                                                    .plus(this.theSIFS
                                                            .plus(this.theMpduData
                                                                    .getTxTime())
                                                            .plus(this.theSIFS)
                                                            .plus(this
                                                                    .calcTxTime(
                                                                            sta,
                                                                            0,
                                                                            this.theMac
                                                                            .getDot11MacHeaderACK_byte(),
                                                                            this.theMac
                                                                            .getPhy().getDefaultPhyMCS()))))));
                }
            } else {
                this.error("Station " + this.theMac.getMacAddress()
                        + " backoff entity has ongoing RTS frame.");
            }
        } else {
            this.error("Station " + this.theMac.getMacAddress()
                    + " backoff entity has ongoing MPDU.");
        }
    }

    private void generateCts(acStation sta) {
        int aDA = this.theMpduRx.getSA();
        JETime anRtsNav = this.theMpduRx.getNav();
        if (this.theMpduCtrl == null) {
            int headersize = this.theMac.getDot11MacHeaderCTS_byte();
            this.theMpduCtrl = new acMPDU(0, aDA, JE80211MpduType.cts, 0,
                    0, headersize, this.theAC, -1, null, null, null);
            this.theMpduCtrl.setSA(this.theMac.getMacAddress());
            // calculate CTS transmission time
            this.theMpduCtrl.setPhyMCS(this.theMac.getPhy()
                    .getCurrentPhyMCS());
            this.theMpduCtrl.setTxTime(this.calcTxTime(sta, 0, this.theMac
                    .getDot11MacHeaderCTS_byte(), this.theMac.getPhy()
                    .getCurrentPhyMCS()));
            // set seqno
            this.theMpduCtrl.setSeqNo(this.theMpduRx.getSeqNo());
            // calculate CTS's nav value:
            this.theMpduCtrl.setNAV(anRtsNav.minus(this.theSIFS)
                    .minus(this.theMpduCtrl.getTxTime())
                    .minus(this.theMac.getPhy().getPLCPHeaderDuration()));
        } else {
            this.message("Station " + this.theMac.getMacAddress()
                    + "generateCTS: backoff entity has pending CTS/ACK frame",
                    10); // this
            // can
            // happen
            // in
            // hidden
            // node
            // scenarios
        }
    }

    private void generateAck(acStation sta) {
        if (this.theMpduCtrl == null) {
            int headersize = this.theMac.getDot11MacHeaderACK_byte();
            this.theMpduCtrl = new acMPDU(0, this.theMpduRx.getSA(), JE80211MpduType.ack, 0, 0, headersize, this.theAC, -1,
                    null, null, null);
            this.theMpduCtrl.setTxTime(this.calcTxTime(sta, 0, this.theMac.getDot11MacHeaderACK_byte(), this.theMac.getPhy()
                    .getDefaultPhyMCS()));
            this.theMpduCtrl.setNAV(this.theMpduCtrl.getTxTime().minus(this.theMac.getPhy().getPLCPHeaderDuration()));
            this.theMpduCtrl.setSA(this.theMac.getMacAddress());
            this.theMpduCtrl.setPhyMCS(this.theMac.getPhy().getDefaultPhyMCS());
            // set seqno
            this.theMpduCtrl.setSeqNo(this.theMpduRx.getSeqNo());
            this.message("Station " + this.theMac.getMacAddress() + " sent ACK " + this.theMpduCtrl.getSeqNo() + " to Station "
                    + this.theMpduCtrl.getDA() + " on channel " + this.theMac.getPhy().getCurrentChannelNumberTX(), 10);
        } else {
            this.error("Station " + this.theMac.getMacAddress() + " generateACK: backoff entity has pending CTS/ACK frame");
        }
    }

    private boolean checkAndTxRTS(acStation sta, boolean IfsExpired) {
        if (this.theBackoffTimer.is_active()) {
            this.warning("Station " + this.theMac.getMacAddress() + " backoff still busy"); // this
            // should
            // not
            // happen.
            return false;
        }
        if (this.theMpduRts == null) {
            return false; // no RTS to send
        }
        if (this.theMpduRts.isRts()) {
            if (IfsExpired) { // we are now at the end of the IFS
                boolean busy = this.busy();
                if (this.theBackoffTimer.is_paused() && !busy) {
                    this.theBackoffTimer.resume(); // continue downcounting
                }
                if (this.theBackoffTimer.is_idle()) {
                    // new backoff
                    this.theBackoffTimer.start(sta, this.theCW, busy);
                    this.updateBackoffTimer();
                }
                return true;
            } // we are now at the end of the backoff
            if (!this.busy()) {
                this.theTxState = txState.txRts;
                this.updateBackoffTimer();
                JETime aTimeout = new JETime(this.calcTxTime(sta, 0, this.theMac.getDot11MacHeaderRTS_byte(),
                        this.theMpduRts.getPhyMcs()));
                aTimeout = aTimeout.plus(this.theSIFS);
                aTimeout = aTimeout
                        .plus(this.calcTxTime(sta, 0, this.theMac.getDot11MacHeaderCTS_byte(), this.theMpduRts.getPhyMcs()));
                aTimeout = aTimeout.plus(this.theSlotTime);
                this.tx(this.theMpduRts, aTimeout);
                return true; // RTS was sent (if no error occurred. But then we
                // are in trouble anyway.)
            } else {
                return false;
            }
        } else {
            this.error("Station " + this.theMac.getMacAddress() + " has Mpdu of wrong subtype, expected " + JE80211MpduType.rts);
            return false;
        }
    }

    private boolean checkAndTxCTRL() {
        if (this.theMpduCtrl == null) {
            return false; // no CTS or ACK to send
        }
        if (this.theMpduCtrl.isCts()) {
            this.theTxState = txState.txCts;
        } else if (this.theMpduCtrl.isAck()) {
            this.theTxState = txState.txAck;
        } else {
            this.error("Station " + this.theMac.getMacAddress()
                    + " has Mpdu of wrong subtype, expected "
                    + JE80211MpduType.ack + " or " + JE80211MpduType.cts);
        }
        this.tx(this.theMpduCtrl, /* no timeout for CTS or ACK: */ null);
        this.theMpduCtrl = null;
        return true;
    }

    private boolean checkAndTxDATA(acStation sta, boolean IfsExpired) {
        if (this.theBackoffTimer.is_active()) {
            this.warning("Station " + this.theMac.getMacAddress()
                    + " backoff still busy");
            return false;
        }
        if (this.theMpduData == null) {
            return false; // no DATA to send
        }
        if (this.theMpduData.isData()) {
            if (IfsExpired && !this.theTxState.equals(txState.txRts)) {
                boolean busy = this.busy();
                if (this.theBackoffTimer.is_paused() && !busy) {
                    this.theBackoffTimer.resume(); // continue down counting
                }
                if (this.theBackoffTimer.is_idle()) { // new backoff
                    this.theBackoffTimer.start(sta, this.theCW, busy);
                    this.updateBackoffTimer();
                }
                return true;
            }
            if (!this.busy()) {
                this.theTxState = txState.txData;
                this.updateBackoffTimer();
                JETime aTimeout = new JETime(this.theMpduData.getTxTime());
                aTimeout = aTimeout.plus(this.theSIFS);
                aTimeout = aTimeout.plus(this.calcTxTime(sta, 0, this.theMac
                        .getDot11MacHeaderACK_byte(), this.theMac.getPhy()
                        .getDefaultPhyMCS()));
                aTimeout = aTimeout.plus(this.theSlotTime);
                this.tx(this.theMpduData, aTimeout);
                return true;
            } else {
                return false;
            }
        } else {
            this.error("Station " + this.theMac.getMacAddress()
                    + " has Mpdu of wrong subtype, expected "
                    + JE80211MpduType.data);
            return false;
        }
    }

    private JETime calcTxTime(acStation sta, int framebodysize, int headersize, JE802PhyMCS aPhyMCS) {

        // first the preamble:
        JETime aTxTime = this.theMac.getPhy().getPLCPPreamble();
        // now add the phy header:
        aTxTime = aTxTime.plus(this.theMac.getPhy().getPLCPHeaderWithoutServiceField());
        // now calc the payload duration incl MAC Header in byte:
        int framesize = framebodysize + headersize + this.theMac.getDot11MacFCS_byte();
        if (this.theMac.isDot11WepEncr()) {
            framesize = framesize + 8 * 32;// this.theMac.get("MACCONFIG.WEP_byte");
        }
        // now calc the duration of this payload:
        int aNumOfPayload_bit = this.theMac.getPhy().getPLCPServiceField_bit() + framesize * 8;
        int aNumOfSymbols = (aNumOfPayload_bit + aPhyMCS.getBitsPerSymbol() - 1) / aPhyMCS.getBitsPerSymbol();
        JETime aPayloadDuration = this.theMac.getPhy().getSymbolDuration().times(aNumOfSymbols);
        // now calculate the final duration and return it:
        aTxTime = aTxTime.plus(aPayloadDuration);
        return aTxTime;
    }

    private JETime calcTxTime(acStation sta, acMPDU aMpdu) {
        int framebodysize = aMpdu.getFrameBodySize();
        int headersize = aMpdu.getHeaderSize();
        JE802PhyMCS aPhyMCS = aMpdu.getPhyMcs();
        return this.calcTxTime(sta, framebodysize, headersize, aPhyMCS);
    }

    public Integer getAC() {
        return theAC;
    }

    public JETime getAIFS() {
        return theAIFS;
    }

    public int getDot11EDCAAIFSN() {
        return dot11EDCAAIFSN;
    }

    public int getDot11EDCACWmax() {
        return dot11EDCACWmax;
    }

    public int getDot11EDCACWmin() {
        return dot11EDCACWmin;
    }

    public JETime getDot11EDCAMMSDULifeTime() {
        return dot11EDCAMMSDULifeTime;
    }

    public int getDot11EDCATXOPLimit() {
        return dot11EDCATXOPLimit;
    }

    public double getDot11EDCAPF() {
        return dot11EDCAPF;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public int getQueueSize() {
        return this.theQueueSize;
    }

    public int getCurrentQueueSize() {
        return this.theQueue.size();
    }

    public acMac getMac() {
        return theMac;
    }

    public void setMaxRetryCountShort(int maxRetryCountShort) {
        this.maxRetryCountShort = maxRetryCountShort;
    }

    public int getMaxRetryCountShort() {
        return maxRetryCountShort;
    }

    public void setMaxRetryCountLong(int maxRetryCountLong) {
        this.maxRetryCountLong = maxRetryCountLong;
    }

    public int getMaxRetryCountLong() {
        return maxRetryCountLong;
    }

    public void setAC(Integer theAC) {
        this.theAC = theAC;
    }

    public void setDot11EDCAAIFSN(Integer dot11edcaaifsn) {
        if (dot11edcaaifsn < 1) {
            this.warning("Station " + this.theMac.getMacAddress()
                    + " AIFSN < 1: " + dot11edcaaifsn);
            dot11edcaaifsn = 1;
        }
        this.dot11EDCAAIFSN = dot11edcaaifsn;
        this.theAIFS = this.theSIFS.plus(this.theSlotTime
                .times(this.dot11EDCAAIFSN));
    }

    public void setDot11EDCACWmax(Integer dot11edcacWmax) {
        dot11EDCACWmax = dot11edcacWmax;
    }

    public void setDot11EDCACWmin(Integer dot11edcacWmin) {
        if (dot11edcacWmin < 1) {
            this.warning("Station " + this.theMac.getMacAddress()
                    + " CWmin < 1: " + dot11edcacWmin);
            dot11edcacWmin = 1;
        }
        dot11EDCACWmin = dot11edcacWmin;
    }

    public void setDot11EDCAPF(Double dot11edcapf) {
        dot11EDCAPF = dot11edcapf;
    }

    public void setDot11EDCATXOPLimit(Integer dot11edcatxopLimit) {
        dot11EDCATXOPLimit = dot11edcatxopLimit;
    }

    public void setDot11EDCAMMSDULifeTime(JETime dot11edcammsduLifeTime) {
        dot11EDCAMMSDULifeTime = dot11edcammsduLifeTime;
    }

    @Override
    public String toString() {
        return "BE in Station " + this.theMac.getMacAddress() + " AC: "
                + this.theAC;
    }

    public void discardQueue() {
        this.discardedCounter += theQueue.size();
        for (int i = this.theQueue.size() - 1; i >= 0; i--) {
            Vector<Object> params = new Vector<Object>();
            params.add(this.theQueue.get(i));
            params.add(this.theMac.getPhy().getCurrentChannelNumberRX());
            this.send(new JEEvent("push_back_packet", this.theMac
                    .getHandlerId(), theUniqueEventScheduler.now(), params));
        }
        this.theQueue.clear();
        this.parameterlist.clear();
        this.parameterlist.add(this.theAC);
        this.send(new JEEvent("empty_queue_ind", this.theMac,
                theUniqueEventScheduler.now(), this.parameterlist));
    }

    /**
     * @param discardedCounter the discardedCounter to set
     */
    public void setDiscardedCounter(int discardedCounter) {
        this.discardedCounter = discardedCounter;
    }

    /**
     * @return the discardedCounter
     */
    public int getDiscardedCounter() {
        return discardedCounter;
    }

    // additional methods for provokable_nice_guy algorithm
    public JETime getTime() {
        return this.theUniqueEventScheduler.now();
    }

    public JEEventScheduler getTheUniqueEventScheduler() {
        return this.theUniqueEventScheduler;
    }

    public Integer getLongRetryCount() {
        return this.theLongRetryCnt;
    }

    public Integer getShortRetryCount() {
        return this.theShortRetryCnt;
    }

    public long getOverallTPCtr() {
        return overallTPCtr;
    }

    public long getSpecificTPCtr() {
        return specificTPCtr;
    }

    public int getFaultToleranceThreshold() {
        return faultToleranceThreshold;
    }

}
