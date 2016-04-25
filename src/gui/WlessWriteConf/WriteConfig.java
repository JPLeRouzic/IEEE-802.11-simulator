
package gui.WlessWriteConf;


import gui.conf_screen.ConfScrn_main;
import gui.conf_screen.MainScreen_buttons;
import std_def_XML.XMLGlobalConf;

/**
 * @author Jean-Pierre Le Rouzic
 */
public class WriteConfig {
    
XMLGlobalConf mk = null;
public Wc11ac c11ac ; 
public Wc11af c11af ; 
public Wc11ax c11ax ; 
public Wc11ah c11ah ; 
public Wclteu clteu ; 

    public WriteConfig(XMLGlobalConf mkt) {
        mk = mkt;
    }


    public void writeToNtConf( MainScreen_buttons msb, ConfScrn_main cfgm ) {
        String is, std;
        Integer issta, isap ;

        // set wireless standard
        std = msb.phyGroup.getSelection().getActionCommand() ;
        mk.gs.setStandard(std);
        
        // MCS
        mk.gs.setMCS(cfgm.bt.cs_bt_ev.smcs);

        // Propagation model
        mk.gs.setProgMod(Integer.parseInt(cfgm.bt.jTxt_prog_heur.getText()));

        // set distance between stations
        is = cfgm.bt.jTxtDistNodes.getText();
        mk.gs.setDistNodes(Integer.parseInt(is));

        // set noise level
        is = cfgm.bt.jTxtAmbNoise.getText();
        mk.gs.setAmbNoise(Integer.parseInt(is));

        // set nb of stations (non AP)
        is = cfgm.bt.jTxt_Nb_STA.getText();
        issta = Integer.parseInt(is) ;
        mk.gs.setNb_STA(issta);

        // set nb of APs
        is = cfgm.bt.jTxt_Nb_AP_Nodes.getText();
        isap = Integer.parseInt(is) ;
        mk.gs.setNb_AP(isap);

        // set traffic variation rate
        is = cfgm.bt.jCmbDstr.getSelectedItem().toString();
        mk.gs.setPktDistributionRate(is);

        // set ceiling, roof height
        is = cfgm.bt.jTxt_Roof.getText() ;
        mk.gs.setRoof_height(Integer.parseInt(is));
        
//        mk.gs.setCeiling_height(cfgm.bt.jTxt_Ceiling.getText());
        mk.gs.setCeiling_height(3); // fixme

        // set mimo mode (Spatial Streamsor (ss) or BeamForming (bf))
        mk.gs.setMimoMod(cfgm.bt.cs_bt_ev.mimo_mod);

        // set channel aggregation (1, 2, 3 or 4 times)
        mk.gs.setChannelAggreg(cfgm.bt.cs_bt_ev.chan_aggregate);

        // set channel width (6, 7 or 8 MHz)
        mk.gs.setChannelWidth(cfgm.bt.cs_bt_ev.chan_width);

        // set MIMO
        mk.gs.setSpatialStreams(cfgm.bt.cs_bt_ev.nb_spatial_streams);

        // set premise physical model
        mk.gs.setPremMod(cfgm.bt.cs_bt_ev.prem_mod);
        
        // set stations coordinates
        int na = 0, nb = 0, nc = 0;

        // CIRCLE
        if (cfgm.bt.cs_bt_ev.prem_mod == 1) 
            { // This is square will be changed in a circle below
            // we know the total number of stations, the square root gives the number 'nb' stations per side.
            nb = (int) Math.sqrt(mk.gs.getNb_STA());

            // the length "na" of one side is "nb - 1" multiplied by the distance between stations.
            // but because of rounding errors it's better that way
            na = (nb + 1) * mk.gs.getDistNodes();
            nc = na;
            } 
        // RECTANGLE
        else if (cfgm.bt.cs_bt_ev.prem_mod == 2) 
            {
            // mk.gs.getNb_STA() = nb*nb*1.6 =>
            //	nb = (int) Math.sqrt(mk.gs.getNb_STA()/1.6)
            //	nc = na * 1.6
            nb = (int) Math.sqrt(mk.gs.getNb_STA() / 1.6);

            // the length "na" of one side is "nb - 1" multiplied by the distance between stations.
            // but because of rounding errors it's better that way
            na = (nb) * mk.gs.getDistNodes();
            nc = (int) (nb * 1.6) * mk.gs.getDistNodes();
            } 
        // Narrow RECTANGLE (like in a bus or a train wagon)
        else if (cfgm.bt.cs_bt_ev.prem_mod == 3) 
            {
            // mk.gs.getNb_STA() = nb*nb*5 =>
            //	nb = (int) Math.sqrt(mk.gs.getNb_STA()/5)
            //	nc = na * 1.6
            nb = (int) Math.sqrt(mk.gs.getNb_STA() / 5);

            // the length "na" of one side is "nb - 1" multiplied by the distance between stations.
            // but because of rounding errors it's better that way
            na = (nb + 1) * mk.gs.getDistNodes();
            nc = (nb * 5) * mk.gs.getDistNodes();
            } 
        // STADIUM
        else if (cfgm.bt.cs_bt_ev.prem_mod == 4) 
            {
            // we know the total number of stations, the square root gives the number 'nb' stations per side.
            nb = (int) Math.sqrt(mk.gs.getNb_STA());

            // the length "na" of one side is "nb - 1" multiplied by the distance between stations.
            // but because of rounding errors it's better that way
            na = (nb + 1) * mk.gs.getDistNodes();
            nc = na;
            }

        // The goal is to find the X,Y, Z coordinates of the stations.
        // A special station is the AP which is at the center of the area
        int xc = 0, yc = 0, zc = 0;
        zc = 0 ;
        for (int kd = 0; kd < mk.gs.getRoof_height(); kd += mk.gs.getCeiling_height()) 
            {
            xc = 0 ;
            for (int id = 0; id < na; id += mk.gs.getDistNodes()) 
                {
                yc = 0;
                for (int jd = 0; jd < nc; jd += mk.gs.getDistNodes()) 
                    {
// -->
                    // To change the square in a rough circle
                    if((cfgm.bt.cs_bt_ev.prem_mod == 1) && (xc > na/4) && (xc < (na * 0.75)) && (yc > nc/4) && (yc < (nc*0.75)))
                            {
                            mk.gs.set_XYZ(id, jd, kd) ;
                            }
                    else
//                    if(cfgm.cs_bt_ev.prem_mod != 1)
                            {
                            mk.gs.set_XYZ(id, jd, kd) ;
                            }
                    // FIXME What to do with other stations?  
                        
// <--                         
                    yc++;
                    }
                xc++;
                }
            zc++ ;
            }
        // now tell the AP rank
        mk.gs.setAPrank(((xc / 2) * (yc / 2) * (zc / 2)) - 1);
    }

}
