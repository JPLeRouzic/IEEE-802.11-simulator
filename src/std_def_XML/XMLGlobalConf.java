package std_def_XML;
////////////////////////////////////////////////////////////////////////////////////////////////////////
//This class reads the parameters from the GUI or the network configuration file and makes an xml file
//which is used by the Simulator class.
////////////////////////////////////////////////////////////////////////////////////////////////////////

import std_def_XML.af.XML80211af_PHY;
import std_def_XML.ac.XML80211ac_PHY;
import gui.conf_screen.MainScreen;
import gui.conf_screen.getters_setters;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import std_def_XML.ax.XML80211ax_PHY;

public class XMLGlobalConf {
        
        Date dt = new Date() ;
        String txt ;

        public getters_setters gs = new getters_setters() ;   
                
    public XMLGlobalConf(MainScreen msc) {
        
    }

    //This method is called when configuration from GUI is chosen
    public void configureGlobally() {
        Element s = null;
        Element level_1 = null;
        Element level_2 = null;
        Element level_3 = null;
        Element level_4 = null;
        Node n = null;

        Document xmldoc = new DocumentImpl();

        Element root = xmldoc.createElement("JE802");
        
        // Number of APs
        level_1 = xmldoc.createElementNS(null, "Nb_APs");
        n = xmldoc.createTextNode(Integer.toString(gs.i_Nb_AP));
        level_1.appendChild(n);
        root.appendChild(level_1);

        // Number of stations
        level_1 = xmldoc.createElementNS(null, "Nb_STAs");
        n = xmldoc.createTextNode(Integer.toString(gs.i_Nb_STA));
        level_1.appendChild(n);
        root.appendChild(level_1);

        // premise's shape (round, square, rectangle, etc..)
        // The STAs placement is dependent on the shape
        level_1 = xmldoc.createElementNS(null, "PremiseModel");
        n = xmldoc.createTextNode(Integer.toString(gs.i_prem_mod));
        level_1.appendChild(n);
        root.appendChild(level_1);

        // This tells how the media attenuates the radio waves
        level_1 = xmldoc.createElementNS(null, "ProPag");
        n = xmldoc.createTextNode(Integer.toString(gs.i_progmod));
        level_1.appendChild(n);
        root.appendChild(level_1);
        
        /*
        <!--mesh -->
	<JE802Control showGui="true" EmulationDuration_ms="10000"
		resume="false" resumeFile="results\hibernation.zip">
                        ...
                        ...
		
	</JE802Control>
        */
        level_1 = xmldoc.createElementNS(null, "JE802Control");
        // The gui shows each station frame, NAV, etc... on a separate screen 
        // at the en of the simulation
        // FIXME: how to end the program when showGui is false?
        level_1.setAttributeNS(null, "showGui", "true");
        
        // Emulation duration in milli sseconds
        level_1.setAttributeNS(null, "EmulationDuration_ms", "1000");
        
        // It is possible to put the simulation in "pause" and resume later
        level_1.setAttributeNS(null, "resume", "false");
        level_1.setAttributeNS(null, "resumeFile", "results\\hibernation.zip");
        
        /*
         * <JE802StatEval seed="1" EvalThrpPerAC="true"
		EvalDelayPerAC="true" EvalOfferPerAC="true" EvaluationStarttime_ms="0"
		EvaluationInterval_ms="100" Path2Results="results"
		EvalTotalOffer="true" EvalTotalThrp="true" EvalTotalDelay="true"
		HistogramMax_ms="100" HistogramNumOfBins="2000">
		<!--JE802StatEval defines the parameters needed for statistical analysis 
			and evaluation (for example when measuring the overall throughputs etc.) -->
	</JE802StatEval>

         */
        level_2 = xmldoc.createElementNS(null, "JE802StatEval");

        // A seed to make each simulation unique
        level_2.setAttributeNS(null, "seed", gs.seed.toString());

        // Statistics
        level_2.setAttributeNS(null, "EvalThrpPerAC", "true");
        level_2.setAttributeNS(null, "EvalDelayPerAC", "true");
        level_2.setAttributeNS(null, "EvalOfferPerAC", "true");

        level_2.setAttributeNS(null, "EvaluationStarttime_ms", "0");
        level_2.setAttributeNS(null, "EvaluationInterval_ms", "100");
        level_2.setAttributeNS(null, "Path2Results", "results");
        level_2.setAttributeNS(null, "EvalTotalOffer", "true");
        level_2.setAttributeNS(null, "EvalTotalThrp", "true");
        level_2.setAttributeNS(null, "EvalTotalDelay", "true");
        level_2.setAttributeNS(null, "HistogramMax_ms", "100");
        level_2.setAttributeNS(null, "HistogramNumOfBins", "2000");
        level_1.appendChild(level_2);   
            
        /*
         * <JE802Animation attenuationFactor="3.5" generateGEarth="true" maxDelay="5"
		maxThrp="3" maxTxdBm="20" mbPerBlock="0.2" minTxdBm="-60"
		overlayAccuracy="2" generateOfferBlocks="true" generatePowerOverlay="true" />
         */
        level_2 = xmldoc.createElementNS(null, "JE802Animation");
        level_2.setAttributeNS(null, "attenuationFactor", "3.5");
        level_2.setAttributeNS(null, "generateGEarth", "true");
        level_2.setAttributeNS(null, "maxDelay", "5");
        level_2.setAttributeNS(null, "maxThrp", "3");
        level_2.setAttributeNS(null, "maxTxdBm", "20");
        level_2.setAttributeNS(null, "mbPerBlock", "0.2");
        level_2.setAttributeNS(null, "minTxdBm", "-60");
        level_2.setAttributeNS(null, "overlayAccuracy", "2");
        level_2.setAttributeNS(null, "generateOfferBlocks", "true");
        level_2.setAttributeNS(null, "generatePowerOverlay", "true");
        level_1.appendChild(level_2);   
            
        root.appendChild(level_1);
        
        /*
        <JE802RoutingParameters routingEnabled="false"
		channelSwitchingEnabled="false" multiChannelPathMetricEnabled="false"
		activeRouteTimeout_ms="3000" ipHeaderByte="20" brokenLinkAfterLoss="10"
		helloInterval_ms="5000" channelSwitchingDelay_ms="1" />
                * */
        level_1 = xmldoc.createElementNS(null, "JE802RoutingParameters");
        level_1.setAttributeNS(null, "routingEnabled", "false");
        level_1.setAttributeNS(null, "channelSwitchingEnabled", "false");
        level_1.setAttributeNS(null, "multiChannelPathMetricEnabled", "false");
        level_1.setAttributeNS(null, "activeRouteTimeout_ms", "3000");
        level_1.setAttributeNS(null, "ipHeaderByte", "20");
        level_1.setAttributeNS(null, "brokenLinkAfterLoss", "10");
        level_1.setAttributeNS(null, "helloInterval_ms", "5000");
        level_1.setAttributeNS(null, "channelSwitchingDelay_ms", "1");
        root.appendChild(level_1);
        
        //For each station...
        XMLStationsConf stloop = null ;
        stloop = new XMLStationsConf() ;
        stloop.stations_loop(
                root,
                s, 
                level_1,
                level_2,
                level_3,
                level_4,
                xmldoc,
                gs) ;
        // stations loop end
        
        // define PHY
        String std = gs.getStandard() ;
            switch (std) {
                case "11ac":
                    {
                        XML80211ac_PHY amendphy = null ;
                        amendphy = new XML80211ac_PHY() ;
                        amendphy.phy_glob(
                                root,
                                s,
                                level_1,
                                level_2,
                                level_3,
                                level_4,
                                xmldoc,
                                gs) ;   break;
                    }
                case "11af":
                    {
                        XML80211af_PHY amendphy = null ;
                        amendphy = new XML80211af_PHY() ;
                        amendphy.phy_glob(
                                root,
                                s,
                                level_1,
                                level_2,
                                level_3,
                                level_4,
                                xmldoc,
                                gs) ;   break;
                    }
                case "11ax":
                    {
                        XML80211ax_PHY amendphy = null ;
                        amendphy = new XML80211ax_PHY() ;
                        amendphy.phy_glob(
                                root,
                                s,
                                level_1,
                                level_2,
                                level_3,
                                level_4,
                                xmldoc,
                                gs) ;   break;
                    }
                default:
                    System.out.print("\n error, not a known wireless standard") ;
                    break;
            }
        
        xmldoc.appendChild(root);

        try {
            FileOutputStream fos = new FileOutputStream("./scenarios/scenario.xml");

            OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
            of.setIndent(1);
            of.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(fos, of);

            serializer.asDOMSerializer();
            serializer.serialize(xmldoc.getDocumentElement());
            fos.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}



