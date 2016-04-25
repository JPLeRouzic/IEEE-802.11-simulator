/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package zzInfra.ARC;

import java.util.List;
import java.util.Map;
import java.util.Random;
import org.w3c.dom.Node;
import zzInfra.emulator.JE802StatEval;
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;
import zzInfra.layer0.WirelessMedium;
import zzInfra.layer1.JE802Mobility;
import zzInfra.layer1.JE802Phy;
import zzInfra.layer2.JE802Mac;
import zzInfra.layer4_transport.JE802TCPManager;
import zzInfra.layer5_application.JE802TrafficGen;

/**
 *
 * @author Gengis
 */
public class JE802Station extends JEmula {
    
    public JE802Sme sme;

    public JE802Mac theMac;

    public JE802Phy thePhy;

    public JE802Mobility mobility;

    public JE802StatEval statEval;
    
    private Map<Integer, JE802Mac> dot11MacMap;

    public List<JE802TrafficGen> trafficGenerators;

    public JE802TCPManager tcp;

    public List<Integer> wiredAddresses;
    public Integer address;

    public JE802Station(JEEventScheduler theUniqueEventScheduler, WirelessMedium theUniqueWirelessMedium, 
            Random theUniqueRandomBaseGenerator, JE802Gui theUniqueGui, JE802StatEval statEval, 
            Node stationNode) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public JE802Station() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public JE802Phy getPhy() {
        return (JE802Phy) thePhy;
    }

    public Integer getMacAddress() {
        return this.address;
    }

    public JE802Sme getSme() {
        return this.sme;
    }

    public JE802Mac getMac() {
        return theMac;
    }

    public List<Integer> getWiredAddresses() {
        return this.wiredAddresses;
    }

    public long getTransmittedPackets() {
        return tcp.getTransmittedPackets();
    }

    public long getLostPackets() {
        return tcp.getLostPackets();
    }

    public void displayLossrate() {
        tcp.retransmissionRate();
    }

    public List<JE802TrafficGen> getTrafficGenList() {
        return this.trafficGenerators;
    }

    public int getFixedChannel() {
        for (JE802Mac aMac : dot11MacMap.values()) {
            if (aMac.isFixedChannel()) {
                return getPhy().getCurrentChannelNumberRX();
            }
        }
        return this.getPhy().getCurrentChannelNumberTX();
    }

    public JE802StatEval getStatEval() {
        return statEval;
    }

    public boolean isMobile() {
        return this.mobility.isMobile();
    }

    public JE802Mobility getMobility() {
        return mobility;
    }

    public double getXLocation(JETime time) {
        return this.mobility.getXLocation(time);
    }

    public double getYLocation(JETime time) {
        return this.mobility.getYLocation(time);
    }

    public double getZLocation(JETime time) {
        return this.mobility.getZLocation(time);
    }

    public double getTransmitPowerLeveldBm() {
        // returns power level of first mac
        return this.getPhy().getCurrentTransmitPowerLevel_dBm();
    }
    
}
