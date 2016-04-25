/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.conf_screen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class Btn_events extends JFrame {

    public boolean rate_flag = false;
    public int chan_aggregate = 1 ; // As this is the default value for the button getJchannelA

    // used only in .11af where there are at the same time aggregation and several widths
    public int chan_width = 20 ; // 20 MHz by default
    public boolean chan_width_chgd = false ;
    
    public int nb_spatial_streams = 1;
    public int prem_mod = 1;
    public int mimo_mod = 1;
    
    private MainScreen_buttons loc_ms ;

    public int smcs = 0 ; // same as default button value for MCS
    
//    public MainScreeListener mainScreenListen = new MainScreeListener();
    
    public MimoListener mimoListen = new MimoListener();
    
    public ChanRadioListener chanListen = new ChanRadioListener();
    
    public WidthRadioListener aggrListen = new WidthRadioListener() ;
    
    public ModRadioListener modListen = new ModRadioListener();
    
    public AntRadioListener antListen = new AntRadioListener();
    
    public mcsRadioListener mcsListen = new mcsRadioListener();


    public Btn_events(MainScreen_buttons msc) {
        loc_ms = msc ;
    }

    //
    class MimoListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e) {
            String str = e.getActionCommand(); 
            switch (str) {
                case "ss":
                    mimo_mod = 1;
                    break;
                case "bf":
                    mimo_mod = 2;
                    break;
            }
        }
    }

    // physical premise characteristics
    class ModRadioListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e) {
            String str = e.getActionCommand(); // get MIMO mode
            switch (str) {
                case "mod_a":
                    prem_mod = 1;
                    break;
                case "mod_b":
                    prem_mod = 2;
                    break;
                case "mod_c":
                    prem_mod = 3;
                    break;
                case "mod_d":
                    prem_mod = 4;
                    break;
            }
        }
    }

    // Channel aggregation
    class ChanRadioListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e) {
            String str = e.getActionCommand(); // get channel
            switch (str) {
                case "80":
                    chan_aggregate = 4;
                    break;
                case "160":
                    chan_aggregate = 8;
                    break;
                case "20":
                    chan_aggregate = 1;
                    break;
                case "40":
                    chan_aggregate = 2;
                    break;
            }
        }
    }

    // Channel aggregation
    class WidthRadioListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e) {
            String str = e.getActionCommand(); // get text corresponding to click
//            String aggchan ;
            switch (str) {
                case "6MHz":
                    chan_width = 6;
                    break;
                case "7MHz":
                    chan_width = 7;
                    break;
                case "8MHz":
                    chan_width = 8;
                    break;
            }
            
            // show there was a change in channel width
            chan_width_chgd = true ;
            
            /* FIXME how to change the aggregated channel width
             * 
            aggchan = Integer.toString(chan_width * 4) ;
            JRadioButton jchannelA = loc_cfgm.bt.getJAggChanA(loc_ms, aggchan); // 80 MHz
            jchannelA.setText(aggchan); 
            aggchan = Integer.toString(chan_width * 8) ;
            JRadioButton jchannelB = loc_cfgm.bt.getJAggChanB(loc_ms, aggchan); // 160 MHz
            jchannelB.setText(aggchan); 
            aggchan = Integer.toString(chan_width * 1) ;
            JRadioButton jchannelC = loc_cfgm.bt.getJAggChanC(loc_ms, aggchan); // 20 MHz
            jchannelC.setText(aggchan); 
            aggchan = Integer.toString(chan_width * 2) ;
            JRadioButton jchannelD = loc_cfgm.bt.getJAggChanD(loc_ms, aggchan); // 40 MHz
            jchannelD.setText(aggchan); 
            */
        }
    }

    // Number of antennas
    class AntRadioListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e) {
            String str = e.getActionCommand(); // get MIMO mode
            switch (str) {
                case "ant1":
                    nb_spatial_streams = 1;
                    break;
                case "ant2":
                    nb_spatial_streams = 4;
                    break;
                case "ant4":
                    nb_spatial_streams = 8;
                    break;
                case "ant8":
                    nb_spatial_streams = 8;
                    break;
            }
        }
    }

    // do not modify values without aligning with getJRbRate9x in Buttons.java
    class mcsRadioListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e) {
            String str = e.getActionCommand(); // get MCS mode
            switch (str) {
                case "MCS 0":
                    smcs = 0;
                    break;
                case "MCS 1":
                    smcs = 1;
                    break;
                case "MCS 2":
                    smcs = 2;
                    break;
                case "MCS 3":
                    smcs = 3;
                    break;
                case "MCS 4":
                    smcs = 4;
                    break;
                case "MCS 5":
                    smcs = 5;
                    break;
                case "MCS 6":
                    smcs = 6;
                    break;
                case "MCS 7":
                    smcs = 7;
                    break;
                case "MCS 8":
                    smcs = 8;
                    break;
                case "MCS 9":
                    smcs = 9;
                    break;
            }
        }
    }
}
