/*
 * We make an IBSS STA a bit like an AP, with the BSSID for frames
 * transmitted by each STA set to that STA's address.
 */
package zzInfra.ARC;

import java.util.List;
import java.util.Random;
import javax.xml.xpath.XPathExpressionException;
import zzInfra.layer0.WirelessMedium;
import java.util.logging.Level;
import java.util.logging.Logger;
import zzInfra.layer1.JE802PhyMCS;
import org.w3c.dom.Node;
import zzInfra.ARC.JE802Station;
import zzInfra.emulator.JE802StatEval;
import zzInfra.gui.JE802Gui;
import zzInfra.kernel.JEEventScheduler;

/**
 * @author jean-Pierre Le Rouzic
 */
public class IBSS {

    public JE802Station sta ;
    
    public IBSS(JEEventScheduler aScheduler, WirelessMedium aChannel, 
            Random aGenerator, JE802Gui aGui, JE802StatEval aStatEval, 
            Node topLevelNode, List<JE802PhyMCS> phyModes, double longitude, 
            double latitude)
        {
            sta = new JE802Station(aScheduler, aChannel, aGenerator, aGui,
                    aStatEval, topLevelNode) ;
        }
}
