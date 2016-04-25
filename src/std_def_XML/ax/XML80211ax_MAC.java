/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package std_def_XML.ax;

import gui.conf_screen.getters_setters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class XML80211ax_MAC {
    
      /**
     *
     */
    public Element mac_sta(
            Element root,
            Element s, 
            Element level_1,
            Document xmldoc,
            getters_setters gs
            )   
    {
            Element level_2;
            Element level_3;
            Element level_4;
            
            /* This tag encloses other tags
             * <JE80211MAC dot11MacAddress4_byte="6" dot11MacFCS_byte="4"
			dot11MacHeaderACK_byte="10" dot11MacHeaderCTS_byte="10"
			dot11MacHeaderDATA_byte="24" dot11MacHeaderRTS_byte="16"
			dot11WepEncryption="false">
             */
            level_2 = xmldoc.createElementNS(null, "JE80211MAC");
            level_2.setAttributeNS(null, "dot11MacAddress4_byte", "6");
            level_2.setAttributeNS(null, "dot11MacFCS_byte", "4");
            level_2.setAttributeNS(null, "dot11MacHeaderACK_byte", "10");
            level_2.setAttributeNS(null, "dot11MacHeaderCTS_byte", "10");
            level_2.setAttributeNS(null, "dot11MacHeaderDATA_byte", "24");
            level_2.setAttributeNS(null, "dot11MacHeaderRTS_byte", "16");
            level_2.setAttributeNS(null, "dot11WepEncryption", "false");
            level_1.appendChild(level_2);               
            /*
            <JE802Mlme MCSalgorithm="phymode_54Mbps"
                    ComputingInterval_ms="100" ShowPlot="false">
            </JE802Mlme>
            */
            level_3 = xmldoc.createElementNS(null, "JE802Mlme");
            level_3.setAttributeNS(null, "ComputingInterval_ms", "100");
            level_3.setAttributeNS(null, "ShowPlot", "false");
            level_2.appendChild(level_3);   

            /*
            <JE802BackoffEntity AC="1" queuesize="10">
                    <MIB802.11e dot11EDCAAIFSN="2" dot11EDCACWmax="1023"
                    dot11EDCACWmin="15" dot11EDCAMSDULifetime="1000" dot11EDCAPF="2.0"
                    dot11EDCATXOPLimit="3008" />
            </JE802BackoffEntity>
            */
            level_3 = xmldoc.createElementNS(null, "JE802BackoffEntity");
            level_3.setAttributeNS(null, "AC", "1");
            level_3.setAttributeNS(null, "queuesize", "10");
            level_2.appendChild(level_3);   
            
            level_4 = xmldoc.createElementNS(null, "MIB802.11e");
            level_4.setAttributeNS(null, "dot11EDCAAIFSN", "2");
            level_4.setAttributeNS(null, "dot11EDCACWmax", "1023");
            level_4.setAttributeNS(null, "dot11EDCACWmin", "15");
            level_4.setAttributeNS(null, "dot11EDCAMSDULifetime", "1000");
            level_4.setAttributeNS(null, "dot11EDCAPF", "2.0");
            level_4.setAttributeNS(null, "dot11EDCATXOPLimit", "3008");
            level_3.appendChild(level_4);   
            level_2.appendChild(level_3);            
            /*
            <JE802BackoffEntity AC="2" queuesize="10">
                    <MIB802.11e dot11EDCAAIFSN="4" dot11EDCACWmax="1023"
                            dot11EDCACWmin="15" dot11EDCAMSDULifetime="1000" dot11EDCAPF="2.0"
                            dot11EDCATXOPLimit="3008" />
            </JE802BackoffEntity>
            */
            level_3 = xmldoc.createElementNS(null, "JE802BackoffEntity");
            level_3.setAttributeNS(null, "AC", "2");
            level_3.setAttributeNS(null, "queuesize", "10");
            level_2.appendChild(level_3);   
            
            level_4 = xmldoc.createElementNS(null, "MIB802.11e");
            level_4.setAttributeNS(null, "dot11EDCAAIFSN", "2");
            level_4.setAttributeNS(null, "dot11EDCACWmax", "1023");
            level_4.setAttributeNS(null, "dot11EDCACWmin", "15");
            level_4.setAttributeNS(null, "dot11EDCAMSDULifetime", "1000");
            level_4.setAttributeNS(null, "dot11EDCAPF", "2.0");
            level_4.setAttributeNS(null, "dot11EDCATXOPLimit", "3008");
            level_3.appendChild(level_4);   
            level_2.appendChild(level_3);            

            /*
            <JE802BackoffEntity AC="3" queuesize="10">
                    <MIB802.11e dot11EDCAAIFSN="2" dot11EDCACWmax="1023"
                            dot11EDCACWmin="15" dot11EDCAMSDULifetime="1000" dot11EDCAPF="2.0"
                            dot11EDCATXOPLimit="3008" />
            </JE802BackoffEntity>
            */
            level_3 = xmldoc.createElementNS(null, "JE802BackoffEntity");
            level_3.setAttributeNS(null, "AC", "3");
            level_3.setAttributeNS(null, "queuesize", "10");
            level_2.appendChild(level_3);   
            
            level_4 = xmldoc.createElementNS(null, "MIB802.11e");
            level_4.setAttributeNS(null, "dot11EDCAAIFSN", "2");
            level_4.setAttributeNS(null, "dot11EDCACWmax", "1023");
            level_4.setAttributeNS(null, "dot11EDCACWmin", "15");
            level_4.setAttributeNS(null, "dot11EDCAMSDULifetime", "1000");
            level_4.setAttributeNS(null, "dot11EDCAPF", "2.0");
            level_4.setAttributeNS(null, "dot11EDCATXOPLimit", "3008");
            level_3.appendChild(level_4);   
            level_2.appendChild(level_3);            
            
            /*
            <JE802BackoffEntity AC="4" queuesize="10">
                    <MIB802.11e dot11EDCAAIFSN="2" dot11EDCACWmax="1023"
                            dot11EDCACWmin="15" dot11EDCAMSDULifetime="1000" dot11EDCAPF="2.0"
                            dot11EDCATXOPLimit="3008" />
            </JE802BackoffEntity>
            */
            level_3 = xmldoc.createElementNS(null, "JE802BackoffEntity");
            level_3.setAttributeNS(null, "AC", "4");
            level_3.setAttributeNS(null, "queuesize", "10");
            level_2.appendChild(level_3);   
            
            level_4 = xmldoc.createElementNS(null, "MIB802.11e");
            level_4.setAttributeNS(null, "dot11EDCAAIFSN", "2");
            level_4.setAttributeNS(null, "dot11EDCACWmax", "1023");
            level_4.setAttributeNS(null, "dot11EDCACWmin", "15");
            level_4.setAttributeNS(null, "dot11EDCAMSDULifetime", "1000");
            level_4.setAttributeNS(null, "dot11EDCAPF", "2.0");
            level_4.setAttributeNS(null, "dot11EDCATXOPLimit", "3008");
            level_3.appendChild(level_4);   
            level_2.appendChild(level_3);            
            
            /*

            <MIB802.11-1999 dot11BroadcastAddress="255"
                    dot11FragmentationThreshold="512" dot11LongRetryLimit="4"
                    dot11MACAddress="1" dot11MaxReceiveLifetime="1000"
                    dot11MaxTransmitMSDULifetime="1000" dot11RTSThreshold="500"
                    dot11ShortRetryLimit="7" />
            */
            level_3 = xmldoc.createElementNS(null, "MIB802.11-1999");
            level_3.setAttributeNS(null, "dot11BroadcastAddress", "255");
            level_3.setAttributeNS(null, "dot11FragmentationThreshold", "512");
            level_3.setAttributeNS(null, "dot11LongRetryLimit", "4");
            level_3.setAttributeNS(null, "dot11MACAddress", "1");
            level_3.setAttributeNS(null, "dot11MaxReceiveLifetime", "1000");
            level_3.setAttributeNS(null, "dot11MaxTransmitMSDULifetime", "1000");
            level_3.setAttributeNS(null, "dot11RTSThreshold", "500");
            level_3.setAttributeNS(null, "dot11ShortRetryLimit", "7");
            level_2.appendChild(level_3);   
            
            return level_2 ;
    }
}
