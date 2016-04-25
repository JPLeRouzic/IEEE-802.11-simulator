/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zzInfra.ARC;

import zzInfra.layer0.WirelessChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.layer2.JE802Mac;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class JE802Sme extends JEEventHandler {

    public JE802Station station;

    public List<JE802Station> wiredStations;

    public HashMap<Integer, JE802Mac> macDot11Map;

    public int ipHandlerId;

    public int channelHandlerId;

    public JE802Sme(JEEventScheduler aScheduler, Random aGenerator) {
        super(aScheduler, aGenerator);
    }

    /*
     * public double getETT(int channel) { acMac mac =
     * macMap.get(channel); if (mac != null) { int phyMode =
     * mac.getPhy().getCurrentPhyMCS().getRateMbps(); double bytePerSecond =
     * phyMode / 8.0 * 1E6; double etx = mac.getETX(); double ett = etx * (1024
     * / bytePerSecond); return ett; } else { return 0; } }
     */
    public void setIpHandlerId(int tcpHandlerId) {
        this.ipHandlerId = tcpHandlerId;
    }

    public void setChannelHandlerId(int channelHandlerId) {
        this.channelHandlerId = channelHandlerId;
    }

    public void setMacs(ArrayList<JE802Mac> macs) {
        this.macDot11Map = new HashMap<Integer, JE802Mac>();
        for (JE802Mac mac : macs) {
            macDot11Map.put(station.thePhy.getCurrentChannelNumberRX(), mac);
        }
    }

    public void checkQueueSize(int size) {
        if (this.macDot11Map != null) {
            for (JE802Mac mac : macDot11Map.values()) {
                mac.checkQueueSize(size);
            }
        }
    }

    public int getAddress() {
        if (macDot11Map != null) {
            return this.macDot11Map.values().iterator().next().getMacAddress();
        }
        return 0;
    }

    public List<WirelessChannel> getAvailableChannels() {
        if (this.macDot11Map != null) {
            message("80211Mac channels", 10);
            return station.getPhy().getAvailableChannels();
        } else {
            return null;
        }
    }

    public List<Integer> getChannelsInUse() {
        List<Integer> channels = new ArrayList<Integer>();
        if (this.macDot11Map != null) {
            for (JE802Mac mac : macDot11Map.values()) {
                channels.add(station.thePhy.getCurrentChannelNumberRX());
            }
        }
        return channels;
    }

    public void setWiredStations(List<JE802Station> wiredStations) {
        if (wiredStations != null && wiredStations.isEmpty()) {
            this.wiredStations = null;
        } else {
            this.wiredStations = wiredStations;
        }
    }

}
