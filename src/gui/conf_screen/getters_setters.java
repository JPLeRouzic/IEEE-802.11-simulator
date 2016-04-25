/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.conf_screen;

import java.util.Vector;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class getters_setters {
    
    public String i_standard;
    public int i_mimo_mod ;
    public int i_prem_mod;
    public int i_nb_spatial_streams;
    public int i_channel_aggr;
    public int i_channel_width ;
    public int i_Nb_AP;
    public int i_Nb_STA;
    public int i_dist ;
    public int i_AmbNoise;
    public int i_mcs;
    
    public int i_progmod;
    public int i_APrank;
    public String i_PktDistributionRate ;
    public int zRoof_Height = 15 ;
    public int zCeiling_nb = 3 ;

    // X and Y vectors for stations (stations)
    
    public Vector X_v = new Vector();    
    public Vector Y_v = new Vector();
    public Vector Z_v = new Vector();
    
    //These variables are initialized with set methods from the GUI. Their values are written to the xml file
    long in ;
    public String seed = Long.toString(in).toString() ;
//    public String stations = "10";
    public String pktLngth = "8000";
    public String rtsThr = "99999";
    public String ctsToSelf = "n";

    

    //Set method follow...

    public void setCtsToSelf(String ctsToSelf) {
        this.ctsToSelf = ctsToSelf;
    }

    public void setRtsThr(String rtsThr) {
        this.rtsThr = rtsThr;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public void setPktLngth(String pktLngth) {
        this.pktLngth = pktLngth;
    }

    public void setDistNodes(int layer) {
        i_dist = layer;
    }

    public int getDistNodes() {
        return i_dist;
    }

    public int getRoof_height() {
        return zRoof_Height ;
    }
    
    public int getCeiling_height() {
        return zCeiling_nb ;
    }
    
    public int setRoof_height(int roof) {
        return zRoof_Height ;
    }
    
    public int setCeiling_height(int ceiling) {
        return zCeiling_nb ;
    }
    
    public void setStandard(String standard) {
        i_standard = standard;
    }

    public String getStandard() {
        return i_standard ;
    }

    public void setMimoMod(int mimo_mod) {
        i_mimo_mod = mimo_mod;
    }

    public void setPremMod(int prem_mod) {
        i_prem_mod = prem_mod;
    }

    public void setSpatialStreams(int nb_spatial_streams) {
        i_nb_spatial_streams = nb_spatial_streams;
    }

    public void setChannelAggreg(int channel_aggr) {
        i_channel_aggr = channel_aggr;
    }

    public void setChannelWidth(int channel_width) {
        i_channel_width = channel_width;
    }

    public void setNb_AP(int parseInt) {
        i_Nb_AP = parseInt;
    }

    public void setNb_STA(int parseInt) {
        i_Nb_STA = parseInt;
    }

    public int getNb_STA() {
        return i_Nb_STA;
    }

    public int getNb_AP() {
        return i_Nb_STA;
    }

    // Packet generation rate distributiion
    public void setPktDistributionRate(String parseInt) {
        i_PktDistributionRate = parseInt;
    }

    public void setAmbNoise(int parseInt) {
        i_AmbNoise = parseInt;
    }

    public void setMCS(int smcs) {
        i_mcs = smcs;
    }

    public void setProgMod(int smcs) {
        i_progmod = smcs;
    }

    public void setAPrank(int smcs) {
        i_APrank = smcs;
    }

    public int getAPrank() {
        return i_APrank;
    }

    public void set_XYZ(int x, int y, int z) {
        // this is the AP coordinates
        X_v.add(x);
        Y_v.add(y);
        Z_v.add(z);
    }
    
}
