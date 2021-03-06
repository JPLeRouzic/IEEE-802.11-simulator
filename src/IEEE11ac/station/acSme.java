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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import zzInfra.ARC.JE802Sme;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer0.WirelessChannel;
import IEEE11ac.layer2.acHopInfo;
import IEEE11ac.layer2.acMac;
import IEEE11ac.layer2.acMPDU;
import zzInfra.layer3_network.JE802IPPacket;
import org.w3c.dom.Node;
import zzInfra.ARC.JE802Station;
import zzInfra.layer2.JE802Mac;
import zzInfra.layer3_network.JE802HopInfo;

/**
 * @author Stefan Mangold
 */
public class acSme extends JE802Sme { 
// SME = Station Management Entity

    private long seqNo = 0;

    public acSme(JEEventScheduler aScheduler, Random aGenerator, Node aTopLevelNode, acStation myStation) {
        super(aScheduler, aGenerator);
        this.station = myStation;

        this.theState = state.active;
    }

    @Override
    public void event_handler(JEEvent anEvent) {
        JETime now = anEvent.getScheduledTime();
        String anEventName = anEvent.getName();

            // an event arrived
        // this.message("Sme at Station " + this.getAddress() +
        // " received event " + anEventName);
        switch (anEventName) {
            case "packet_forward": {
                acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                int nextChannel = aMpdu.getHopAddresses().get(0).getChannel();
                acHopInfo nextHop = aMpdu.getHopAddresses().get(0);
                JE802Mac mac = macDot11Map.get(nextChannel);
                if (mac != null) {
                    parameterlist = new Vector<Object>();
                    parameterlist.add(nextHop); // Destination Address of MPDU
                    parameterlist.add(aMpdu.getAC());
                    parameterlist.add(aMpdu.getHopAddresses());
                    parameterlist.add(aMpdu.getPayload());
                    parameterlist.add(aMpdu.getSeqNo());
                    parameterlist.add(aMpdu.getSourceHandler());
                    JEEvent groupForwardEvent = new JEEvent("groupForwardEvent", mac.getHandlerId(), theUniqueEventScheduler.now(),
                            parameterlist);
                    this.send(groupForwardEvent);
                } else {
                    this.error("Station " + this.getAddress() + " does not know channel " + nextChannel);
                }
                break;
            }
            case "start_req":
                this.parameterlist.clear();
                    // this.send(new JEEvent("listen_for_pages_req",
                // this.macDot15Map.values().iterator().next().getHandlerId(),
                // theUniqueEventScheduler.now()));
                break;
            case "Channel_Switch_req":
                Integer switchFrom = (Integer) anEvent.getParameterList().get(0);
                Integer switchTo = (Integer) anEvent.getParameterList().get(1);
                JE802Mac macThatSwitches = macDot11Map.get(switchFrom);
                if (macDot11Map.get(switchTo) != null) {
                    System.err.println("Switching to channel at station" + this.getAddress());
                }
                    // change assigned channel of mac
                // this.message("Switching from " + switchFrom + " to " + switchTo +
                // " at Station" + this.getAddress());
                macDot11Map.remove(switchFrom);
                macDot11Map.put(switchTo, macThatSwitches);
                this.parameterlist = new Vector<Object>();
                this.parameterlist.add(switchTo);
                this.send(new JEEvent("Channel_Switch_req", macThatSwitches.getHandlerId(), theUniqueEventScheduler.now(),
                        this.parameterlist));
                break;
            case "IP_Deliv_req": {
                JE802HopInfo nextHop = (JE802HopInfo) anEvent.getParameterList().get(0);
                boolean sent = false;
                if (wiredStations != null) {
                    for (JE802Station station : wiredStations) {
                        if (station.getMacAddress() == nextHop.getAddress() || nextHop.getAddress() == 255) {
                            seqNo++;
                            anEvent.getParameterList().add(seqNo);
                            anEvent.getParameterList().add(this.getAddress());
                            this.send(new JEEvent("wiredForward", station.getSme().getHandlerId(), now, anEvent.getParameterList()));
                            sent = true;
                        }
                    }
                }
                if (!sent || nextHop.getAddress() == 255) {
                    int channel = nextHop.getChannel();
                    JE802Mac macOnChannel = macDot11Map.get(channel);
                    anEvent.getParameterList().add(seqNo);
                    anEvent.getParameterList().add(this.getHandlerId());
                    seqNo++;
                    if (macOnChannel != null) {
                        this.send(new JEEvent("MSDUDeliv_req", macOnChannel.getHandlerId(), now, anEvent.getParameterList()));
                    } else {
                        this.warning("Station " + this.getAddress() + " does not know channel " + channel);
                    }
                }
                break;
            }
            case "wiredForward":
                JE802IPPacket packet = (JE802IPPacket) anEvent.getParameterList().get(3);
                Integer ac = (Integer) anEvent.getParameterList().get(1);
                acHopInfo hop = (acHopInfo) anEvent.getParameterList().get(0);
                Long sequenceNo = (Long) anEvent.getParameterList().get(4);
                Integer sa = (Integer) anEvent.getParameterList().get(5);
                Vector<Object> parameterList = new Vector<Object>();
                parameterList.add(packet);
                parameterList.add(now);
                parameterList.add(ac);
                parameterList.add(hop.getChannel());
                parameterList.add(sequenceNo);
                parameterList.add(sa);
                this.send(new JEEvent("packet_exiting_system_ind", this.ipHandlerId, now, parameterList));
                break;
            case "broadcast_sent":
                this.send(new JEEvent("broadcast_sent", channelHandlerId, anEvent.getScheduledTime(), anEvent.getParameterList()));
                break;
            case "hop_evaluation": {
                acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                anEvent.getParameterList().setElementAt(aMpdu.getPayload(), 0);
                anEvent.getParameterList().setElementAt(aMpdu.getSeqNo(), 2);
                anEvent.getParameterList().add(this.getAddress());
                this.send(new JEEvent("hop_evaluation", this.ipHandlerId, anEvent.getScheduledTime(), anEvent.getParameterList()));
                break;
            }
            case "empty_queue_ind":
                this.send(new JEEvent("empty_queue_ind", this.ipHandlerId, anEvent.getScheduledTime(), anEvent.getParameterList()));
                break;
            case "packet_exiting_system_ind": {
                acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                anEvent.getParameterList().setElementAt(aMpdu.getPayload(), 0);
                // mac sequence number
                anEvent.getParameterList().add(aMpdu.getSeqNo());
                anEvent.getParameterList().add(aMpdu.getSA());
                this.send(new JEEvent("packet_exiting_system_ind", this.ipHandlerId, now, anEvent.getParameterList()));
                break;
            }
            case "MSDU_discarded_ind": {
                acMPDU aMPDU = (acMPDU) anEvent.getParameterList().get(0);
                Integer channel = (Integer) anEvent.getParameterList().get(2);
                Integer retries = (Integer) anEvent.getParameterList().get(1);
                this.parameterlist = new Vector<Object>();
                this.parameterlist.add(aMPDU.getPayload());
                this.parameterlist.add(retries);
                this.parameterlist.add(aMPDU.getDA());
                this.parameterlist.add(channel);
                this.send(new JEEvent("IPPacket_discarded_ind", ipHandlerId, theUniqueEventScheduler.now(), this.parameterlist));
                break;
            }
            case "MSDU_delivered_ind":
                this.send(new JEEvent("IPPacket_delivered_ind", ipHandlerId, now, anEvent.getParameterList()));
                break;
            case "push_back_packet": {
                acMPDU aMpdu = (acMPDU) anEvent.getParameterList().get(0);
                Integer channel = (Integer) anEvent.getParameterList().get(1);
                Vector<Object> params = new Vector<Object>();
                params.add(aMpdu.getPayload());
                params.add(channel);
                params.add(aMpdu.getDA());
                this.send(new JEEvent("push_back_packet", channelHandlerId, now, params));
                break;
            }
            default:
                this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
                break;
        }
    }

    public int getAddress() { 
        if (macDot11Map != null) {
            return this.macDot11Map.values().iterator().next().getMacAddress();
        }
        return 0;
    }

    public List<WirelessChannel> getAvailableChannels() { // TODO Pietro: this
        // method should be
        // generalized to
        // all MACs
        if (this.macDot11Map != null) {
            message("80211Mac channels", 10);
            return station.getPhy().getAvailableChannels();
        } else {
            return null;
        }
    }

    /*
     * public Map<Integer, Double> getETT() { for (acMac mac :
     * macMap.values()) { int phyMode =
     * mac.getPhy().getCurrentPhyMCS().getRateMbps(); double bytePerSecond =
     * phyMode / 8.0 * 1E6; double etx = mac.getETX(); double ett = etx * (1024
     * / bytePerSecond); ettMap.put(mac.getChannel(), ett); } return ettMap; }
     */

    @Override
    public String toString() {
        return "Sme at station " + this.getAddress();
    }

}
