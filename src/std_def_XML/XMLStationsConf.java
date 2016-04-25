/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package std_def_XML;

import std_def_XML.af.XML80211af_MAC;
import std_def_XML.ac.XML80211ac_PHY;
import std_def_XML.ac.XML80211ac_MAC;
import gui.conf_screen.getters_setters;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import std_def_XML.ax.XML80211ax_MAC;
import std_def_XML.ax.XML80211ax_PHY;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class XMLStationsConf {
  
            Node n = null;

      /**
     *
     */
    public void stations_loop(
            Element root,
            Element s, 
            Element level_1,
            Element level_2,
            Element level_3,
            Element level_4,
            Document xmldoc,
            getters_setters gs
            )
        {
        Iterator iter_x = gs.X_v.iterator();
        Iterator iter_y = gs.Y_v.iterator();
        Iterator iter_z = gs.Z_v.iterator();

        // upper loop bound
        int upperbnd = gs.i_Nb_STA + gs.i_Nb_AP ;
        
        for (int i = 0; i < upperbnd; i++) {
            level_1 = xmldoc.createElementNS(null, "JE802Station");
            level_1.setAttributeNS(null, "address", Integer.toString(i + 1));

            /* max rate for this station
            level_2 = xmldoc.createElementNS(null, "rate");
            n = (Node) xmldoc.createTextNode(gs.rate);
            level_2.appendChild((Node) n);
            level_1.appendChild(level_2);
*/
            
            // If global configation is used, the coverage and position parameters have no meaning.
            level_2 = xmldoc.createElementNS(null, "coverage");
            n = xmldoc.createTextNode("3000");
            level_2.appendChild(n);
            level_1.appendChild(level_2);

            // indicates if true, if the station is an AP, a station otherwise
            level_2 = xmldoc.createElementNS(null, "isAP");
            if (gs.getAPrank() == i) {
                n = xmldoc.createTextNode("true");
            } else {
                n = xmldoc.createTextNode("false");
            }
            level_2.appendChild(n);
            level_1.appendChild(level_2);
            
            /* 		
             * <JE802TrafficGen AC="1" DA="1" EvalDelay="true"
               EvalThrp="true" EvalThrpOffer="true" HistogramMax_ms="60"
               HistogramNumOfBins="600" isTcpTraffic="false" max_packet_size_byte="200"
               mean_load_Mbps="0.9" port="47768196" starttime_ms="0" stoptime_ms="0"
               type="data" />
            */
            level_2 = xmldoc.createElementNS(null, "JE802TrafficGen");
            level_2.setAttributeNS(null, "AC", "1");
            level_2.setAttributeNS(null, "DA", "1");
            level_2.setAttributeNS(null, "EvalDelay", "true");
            level_2.setAttributeNS(null, "EvalThrp", "true");
            level_2.setAttributeNS(null, "EvalThrpOffer", "true");
            level_2.setAttributeNS(null, "HistogramMax_ms", "60");
            level_2.setAttributeNS(null, "HistogramNumOfBins", "600");
            level_2.setAttributeNS(null, "isTcpTraffic", "false");
            level_2.setAttributeNS(null, "max_packet_size_byte", gs.pktLngth);
            level_2.setAttributeNS(null, "mean_load_Mbps", "3000");
            level_2.setAttributeNS(null, "port", "47768196");
            level_2.setAttributeNS(null, "starttime_ms", "0");
            level_2.setAttributeNS(null, "stoptime_ms", "0");
            level_2.setAttributeNS(null, "type", gs.i_PktDistributionRate.toString());
            level_1.appendChild(level_2);
            
            /*
		<JE802TrafficGen AC="2" DA="1" EvalDelay="true"
			EvalThrp="true" EvalThrpOffer="true" HistogramMax_ms="60"
			HistogramNumOfBins="600" isTcpTraffic="false" max_packet_size_byte="2000"
			mean_load_Mbps="0.9" port="47768196" starttime_ms="0" stoptime_ms="0"
			type="data" />
            */
            level_2 = xmldoc.createElementNS(null, "JE802TrafficGen");
            level_2.setAttributeNS(null, "AC", "2");
            level_2.setAttributeNS(null, "DA", "1");
            level_2.setAttributeNS(null, "EvalDelay", "true");
            level_2.setAttributeNS(null, "EvalThrp", "true");
            level_2.setAttributeNS(null, "EvalThrpOffer", "true");
            level_2.setAttributeNS(null, "HistogramMax_ms", "60");
            level_2.setAttributeNS(null, "HistogramNumOfBins", "600");
            level_2.setAttributeNS(null, "isTcpTraffic", "true");
            level_2.setAttributeNS(null, "max_packet_size_byte", gs.pktLngth);
            level_2.setAttributeNS(null, "mean_load_Mbps", "3000");
            level_2.setAttributeNS(null, "port", "47768196");
            level_2.setAttributeNS(null, "starttime_ms", "0");
            level_2.setAttributeNS(null, "stoptime_ms", "0");
            level_2.setAttributeNS(null, "type", gs.i_PktDistributionRate) ;
            level_1.appendChild(level_2);

            /*
             * Traffic model (Packet Generation Rate)  
             * two sub fields
             *      Packet generation rate distributiion
             *      Packet generation rate on average
             */
            level_2 = xmldoc.createElementNS(null, "JETrafficModel");
            level_2.setAttributeNS(null, "RateDistribution", gs.i_PktDistributionRate.toString());
//            level_2.setAttributeNS(null, "AverageRate", gs.i_PktAverageRate.toString());

            level_1.appendChild(level_2);
            
            /*
             * <JE802TCP b="1" bufferSizePackets="10" minimumTimeoutMs="50"
			slowStartThreshold="10" />            
             */
            level_2 = xmldoc.createElementNS(null, "JE802TCP");
            level_2.setAttributeNS(null, "b", "1");
            level_2.setAttributeNS(null, "bufferSizePackets", "10");
            level_2.setAttributeNS(null, "minimumTimeoutMs", "50");
            level_2.setAttributeNS(null, "slowStartThreshold", "10");
            level_1.appendChild(level_2);   
            
            /* position of the station (a small variation on "animation geographical position)
            <JE802Mobility isMobile="false" 
                xLocation="0" yLocation="0" zLocation="0" 
                baseLatitude="47.37655200000001"
		baseLongitude="8.548604999999982"  />
            */
            level_2 = xmldoc.createElementNS(null, "JE802Mobility");
            level_2.setAttributeNS(null, "isMobile", "false");
            level_2.setAttributeNS(null, "xLocation", iter_x.next().toString());
            level_2.setAttributeNS(null, "yLocation", iter_y.next().toString());
            level_2.setAttributeNS(null, "zLocation", iter_z.next().toString());
            level_2.setAttributeNS(null, "baseLatitude", "48.37655200000001");
            level_2.setAttributeNS(null, "baseLongitude", "-1.548604999999982");
            level_2.setAttributeNS(null, "baseHeight", "1.548604999999982");           
            level_1.appendChild(level_2);
            
            /*
             *  <JE802SME>
		<!--not much defined so far -->
		</JE802SME>
             */
            level_2 = xmldoc.createElementNS(null, "JE802SME");
            n = xmldoc.createTextNode(" <!--not much defined so far --> ");
            level_2.appendChild(n);
            level_1.appendChild(level_2);   

        // choose which amendment you want .11af, .11ac, etc..
        // only for station related details
        String std = gs.getStandard() ;
        switch (std) 
            {
            case "11af":
                {
            
                XML80211af_MAC amendmac = null ;
                amendmac = new XML80211af_MAC() ;
                amendmac.mac(
                        root,
                        s, 
                        level_1,
                        level_2,
                        level_3,
                        level_4,
                        xmldoc,
                        gs) ;
                break ;
                }
            case "11ac":
                {             

                XML80211ac_MAC amendmac = null ;
                amendmac = new XML80211ac_MAC() ;
                level_2 = amendmac.mac_sta(
                        root,
                        s, 
                        level_1,
                        xmldoc,
                        gs) ;
                level_1.appendChild(level_2);                

                XML80211ac_PHY amendphy = null ;
                amendphy = new XML80211ac_PHY() ;
                level_2 = amendphy.phy_sta(
                        root,
                        s, 
                        level_1,
                        level_2,
                        xmldoc,
                        gs) ;

                break ;
                }
            case "11ax":
                {             

                XML80211ax_MAC amendmac = null ;
                amendmac = new XML80211ax_MAC() ;
                level_2 = amendmac.mac_sta(
                        root,
                        s, 
                        level_1,
                        xmldoc,
                        gs) ;
                level_1.appendChild(level_2);                

                XML80211ax_PHY amendphy = null ;
                amendphy = new XML80211ax_PHY() ;
                level_2 = amendphy.phy_sta(
                        root,
                        s, 
                        level_1,
                        level_2,
                        xmldoc,
                        gs) ;

                break ;
                }
                default:
                    System.out.print("/n Incorrect amendment in XMLStationsConf.java.java");
            }
        root.appendChild(level_1); 
}

}  

    void stations_loop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
