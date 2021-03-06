/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package std_def_XML.ac;

import gui.conf_screen.getters_setters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class XML80211ac_PHY {

    /**
     *
     */
    public void phy_glob(
            Element root,
            Element s,
            Element level_1,
            Element level_2,
            Element level_3,
            Element level_4,
            Document xmldoc,
            getters_setters gs
    ) {
        /*
         * 	<JE802WirelessChannels theReuseDistance_m="200"
         useInterference="false" orthogonalChannelDistance="1"
         channelBusyThreshold_dBm="-82" noiseLevel_dBm="-95">
         <aChannel aChannelNumber="1" dot11FreqBandWidth_MHz="22.0"
         dot11CenterFreq_MHz="2412" dot11MaximumTransmitPowerLevel_dBm="20.0" />
         <aChannel aChannelNumber="6" dot11FreqBandWidth_MHz="22.0"
         dot11CenterFreq_MHz="2437" dot11MaximumTransmitPowerLevel_dBm="20.0" />
         <aChannel aChannelNumber="11" dot11FreqBandWidth_MHz="22.0"
         dot11CenterFreq_MHz="2462" dot11MaximumTransmitPowerLevel_dBm="20.0" />
         </JE802WirelessChannels>
         */
        level_1 = xmldoc.createElementNS(null, "JEWirelessChannels");

        // Distance at which two BSS can use the same channel safely, 
        // otherwise if they use the same channel, they are in OBSS
        // however RTS/CTS could protect transmission
        level_1.setAttributeNS(null, "theReuseDistance_m", "200");

        level_1.setAttributeNS(null, "MIMOmod", Integer.toString(gs.i_mimo_mod));

        // this should represent interchannel interference but is poorly implemented
        level_1.setAttributeNS(null, "orthogonalChannelDistance", "1");

        // Manage CCA level
        level_1.setAttributeNS(null, "channelBusyThreshold_dBm", "-82");

        // ambient noise
        level_1.setAttributeNS(null, "noiseLevel_dBm", Integer.toString(gs.i_AmbNoise));

        // 802.11 amendment
        level_1.setAttributeNS(null, "standard", "11ac");

        root.appendChild(level_1);

        if (gs.i_channel_aggr == 1) {
            /*     20 MHz channel definition
             <aChannel aChannelNumber="1" dot11FreqBandWidth_MHz="22.0"
             dot11CenterFreq_MHz="2412" dot11MaximumTransmitPowerLevel_dBm="20.0" />
             */
            level_2 = xmldoc.createElementNS(null, "aChannel");
            level_2.setAttributeNS(null, "aChannelNumber", "666");// FIXME rdm.toString() + chan ; 
            level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
            level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
            level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
            level_1.appendChild(level_2);
            /*  sélectionner le bon canal
             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "40020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "44020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "48020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "52020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "56020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "60020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "64020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "100020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "104020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "108020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "112020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "116020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "120020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "124020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "128020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "132020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "136020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "140020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "149020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "153020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "157020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "161020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "165020");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   
             */
        } else if (gs.i_channel_aggr == 2) {
            /*     40 MHz channel definition
             <aChannel aChannelNumber="1" dot11FreqBandWidth_MHz="22.0"
             dot11CenterFreq_MHz="2412" dot11MaximumTransmitPowerLevel_dBm="20.0" />
             */
            level_2 = xmldoc.createElementNS(null, "aChannel");
            level_2.setAttributeNS(null, "aChannelNumber", "666"); // FIXME rdm.toString() + chan ;
            level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "40.0");
            level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
            level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
            level_1.appendChild(level_2);
            /* sélectionner le bon canal
             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "44040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "52040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "60040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "100040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "108040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "116040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "124040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "132040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "149040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "157040");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel", "30");
             level_1.appendChild(level_2);   
             */
        } else if (gs.i_channel_aggr == 4) {
            /*     80 MHz channel definition
             <aChannel aChannelNumber="1" dot11FreqBandWidth_MHz="22.0"
             dot11CenterFreq_MHz="2412" dot11MaximumTransmitPowerLevel_dBm="20.0" />
             */
            level_2 = xmldoc.createElementNS(null, "aChannel");
            level_2.setAttributeNS(null, "aChannelNumber", "666"); // FIXME rdm.toString() + chan ;
            level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "80.0");
            level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
            level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
            level_1.appendChild(level_2);
            /* sélectionner le bon canal
             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "52080");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "100080");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "23");
             level_1.appendChild(level_2);   

             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "116080");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20.0");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "23");
             level_1.appendChild(level_2);   
             * */
        } else if (gs.i_channel_aggr == 8) {
            /*     160 MHz channel definition
             <aChannel aChannelNumber="1" dot11FreqBandWidth_MHz="22.0"
             dot11CenterFreq_MHz="2412" dot11MaximumTransmitPowerLevel_dBm="20.0" />
             */
            level_2 = xmldoc.createElementNS(null, "aChannel");
            level_2.setAttributeNS(null, "aChannelNumber", "666"); // FIXME rdm.toString() + chan ;
            level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "160.0");
            level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
            level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "16");
            level_1.appendChild(level_2);

            /*
             level_2 = xmldoc.createElementNS(null, "aChannel");
             level_2.setAttributeNS(null, "aChannelNumber", "100160");
             level_2.setAttributeNS(null, "dot11FreqBandWidth_MHz", "20");
             level_2.setAttributeNS(null, "dot11CenterFreq_MHz", "5000");
             level_2.setAttributeNS(null, "dot11MaximumTransmitPowerLevel_dBm", "23");
             level_1.appendChild(level_2);   
             */
        }
        root.appendChild(level_1);
    }

    /**
     *
     */
    public Element phy_sta(
            Element root,
            Element s,
            Element level_1,
            Element level_2,
            Document xmldoc,
            getters_setters gs
    ) {
//    Element level_2 = null;
        Element level_3;
        Element level_4;
        
        // Because a .11ac symbol encompasses 4 micro seconds
        int symbol_dur = 4 ;

        level_3 = xmldoc.createElementNS(null, "JE80211PHY");
        level_3.setAttributeNS(null, "PLCPHeaderWithoutServiceField_ms", "0.004");
        level_3.setAttributeNS(null, "PLCPPreamble_ms", "0.016");
        level_3.setAttributeNS(null, "PLCPServiceField_bit", "16");
        level_3.setAttributeNS(null, "PLCPTail_bit", "6");
        level_3.setAttributeNS(null, "SymbolDuration_ms", Integer.toString(symbol_dur/1000));

        /*        sélectionner le bon MCS
         <JE802PhyMCSs>
         <aPhyMCS Mbps="06" Name="MCS0" bit_per_symbol="24" id="1" />
         <aPhyMCS Mbps="09" Name="MCS1" bit_per_symbol="36" id="2" />
         <aPhyMCS Mbps="12" Name="MCS2" bit_per_symbol="48" id="3" />
         <aPhyMCS Mbps="18" Name="MCS3" bit_per_symbol="72" id="4" />
         <aPhyMCS Mbps="24" Name="MCS4" bit_per_symbol="96" id="5" />
         <aPhyMCS Mbps="36" Name="MCS5" bit_per_symbol="144" id="6" />
         <aPhyMCS Mbps="48" Name="MCS6" bit_per_symbol="192" id="7" />
         <aPhyMCS Mbps="54" Name="MCS7" bit_per_symbol="216" id="8" />
         <aPhyMCS Mbps="65" Name="MCS8" bit_per_symbol="192" id="7" />
         <aPhyMCS Mbps="78" Name="MCS9" bit_per_symbol="216" id="8" />
         </JE802PhyMCSs>        
         </JE802>
         */
        int thrpbase = 6;

        level_4 = xmldoc.createElementNS(null, "aPhyMCS");

        level_4.setAttributeNS(null, "standard", "11ac");

        if (gs.i_mcs == 0) {
            thrpbase = 6;
        } else if (gs.i_mcs == 1) {
            thrpbase = 13;
        } else if (gs.i_mcs == 2) {
            thrpbase = 19;
        } else if (gs.i_mcs == 3) {
            thrpbase = 26;
        } else if (gs.i_mcs == 4) {
            thrpbase = 39;
        } else if (gs.i_mcs == 5) {
            thrpbase = 52;
        } else if (gs.i_mcs == 6) {
            thrpbase = 58;
        } else if (gs.i_mcs == 7) {
            thrpbase = 65;
        } else if (gs.i_mcs == 8) {
            thrpbase = 78;
        } else if (gs.i_mcs == 9) {
            thrpbase = 86;
        }

        level_4.setAttributeNS(null, "Name", "MCS" + Integer.toString(gs.i_mcs));
        level_4.setAttributeNS(null, "id", Integer.toString(gs.i_mcs));

        int thrput = gs.i_nb_spatial_streams * thrpbase * gs.i_channel_aggr;

        level_4.setAttributeNS(null, "Mbps", Integer.toString(thrput));

        Integer bps = thrpbase * symbol_dur ;
        level_4.setAttributeNS(null, "bit_per_symbol", bps.toString());

        level_3.appendChild(level_4);
        level_2.appendChild(level_3);

        String CurrentChannelRX = "666"; // FIXME rdm.toString() + chan ;
        String CurrentChannelTX = "666"; // FIXME rdm.toString() + chan ;

        level_4 = xmldoc.createElementNS(null, "MIB802.11ac");
        level_4.setAttributeNS(null, "SIFS", "0.016");
        level_4.setAttributeNS(null, "aSlotTime", "0.009");
        level_4.setAttributeNS(null, "dot11CurrentChannelNumberRX", CurrentChannelRX);
        level_4.setAttributeNS(null, "dot11CurrentChannelNumberTX", CurrentChannelTX);
        level_4.setAttributeNS(null, "dot11CurrentTransmitPowerLevel_dBm", "20");
        level_3.appendChild(level_4);
        level_2.appendChild(level_3);

        level_4 = xmldoc.createElementNS(null, "JEAntenna");
        level_4.setAttributeNS(null, "xDirection", "0");
        level_4.setAttributeNS(null, "yDirection", "0");
        level_4.setAttributeNS(null, "zDirection", "0");
        level_4.setAttributeNS(null, "angle_degrees", "30");
        level_4.setAttributeNS(null, "gain_dBi", "50");
        level_3.appendChild(level_4);
        level_2.appendChild(level_3);

        return level_2;
    }
}
