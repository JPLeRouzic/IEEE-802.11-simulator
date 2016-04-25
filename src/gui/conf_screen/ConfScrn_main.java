package gui.conf_screen;

import gui.WlessWriteConf.Wclteu;
import gui.WlessWriteConf.Wc11ah;
import gui.WlessWriteConf.Wc11af;
import gui.WlessWriteConf.Wc11ac;
import gui.WlessWriteConf.Wc11ax;
import gui.WlessWriteConf.WriteConfig;
import javax.swing.*;
import java.awt.*;
import std_def_XML.XMLGlobalConf;

public class ConfScrn_main extends JFrame {

    public ConfScrnButtons bt;
    
    public Panels pnl;
    
    public WriteConfig wcfg;

    private MainScreen pm = null; //For enabling the parent window

    int PHY_type = 1;
    
    public XMLGlobalConf mk = null; //For writing to the network configuration file
    
    public ButtonModel initRate = null; //Used for the cancel button
    
    public ButtonModel initPhy = null; //Used for the cancel button
    
    public String initNodes = null; //Used for the cancel button
    
    public String initMixNodes = null; //Used for the cancel button
    
    public String initGen = null; //Used for the cancel button
    public int initGenDstr = 0; //Used for the cancel button
    public static final long serialVersionUID = 1L;

    public ConfScrn_main(MainScreen pmarg, XMLGlobalConf mkarg, int nbagg, int widtha, int widthb, int widthc) {
        super();
        this.pm = pmarg; //Pass the argument of the parent window
        this.mk = mkarg; //Pass the STAsGeoConf arguement of the parent window

        bt = new ConfScrnButtons();
        initPhy = pmarg.msb.phyGroup.getSelection(); //Get the selected item and store it to a ComboModel
        String tuty = initPhy.getActionCommand();
        
        switch (tuty) {
            // add code/parameters specific to amendments
            case "11ac":
                wcfg = new WriteConfig(mk);
                wcfg.c11ac = new Wc11ac(mk) ;
                break;
            case "11af":
                wcfg = new WriteConfig(mk);
                wcfg.c11af = new Wc11af(mk) ;
                break;
            case "11ax":
                wcfg = new WriteConfig(mk);
                wcfg.c11ax = new Wc11ax(mk) ;
                break;
            case "11ah":
                wcfg = new WriteConfig(mk);
                wcfg.c11ah = new Wc11ah(mk) ;
                break;
            case "LTE-U":
                wcfg = new WriteConfig(mk);
                wcfg.clteu = new Wclteu(mk) ;
                break;
            default:
                System.out.println("Unknown standard in ConfScrn_main");
        }
        
        pnl = new Panels();
        initialize(pm, pnl, tuty, nbagg, widtha, widthb, widthc);
    }

    private void initialize(MainScreen msc,  Panels pnl, String tuty, int nbagg, int widtha, int widthb, int widthc) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(764, 456);
        this.setResizable(false);
        this.setLocation(new Point((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2));

        bt.cs_bt_ev = new Btn_events(msc.msb);
        
        Container cnt = pnl.getJContentPane(msc, this, nbagg, widtha, widthb, widthc) ;
        this.setContentPane(cnt);
        
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); //A listener is defined
        this.setTitle("Wireless Simulation Configuration");
                
        //Listener for the X button
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                getInitParams(); //Set the values that the window had when it was opened
                setVisible(false); //Close the window
                pm.setEnabled(true); //Open the parent window
                pm.setVisible(true); //Added because otherwise the parent window opens minimized
            }
        });
    }

    //Sets the elements to the values that they had when the window was opened. Used for the cancel button
    public void getInitParams() {
        initRate.setSelected(true); //Sets the selected value for the rate. Init rate is the selected item of the rateGroup
        initPhy.setSelected(true); //Sets the selected value for the physical. Init phy is the selected item of the phyGroup
        bt.jTxt_Nb_STA.setText(initNodes);
        bt.jTxt_Nb_AP_Nodes.setText(initMixNodes);
        bt.jTxt_Roof.setText("15");
        bt.jCmbDstr.setSelectedIndex(initGenDstr);
    }

    //This is used in the listener of the parent object. 
    // Gets the values of all elements and stores it to some 'buffer' objects
    public void cfg_setInitParams( String tuty) {

        checkRates(tuty); // Enables/disables the rates according to the initial value of phy.
        /* ajust rate */
        initRate = bt.rateGroup.getSelection();

        initNodes = bt.jTxt_Nb_STA.getText(); //Get the text...
        initMixNodes = bt.jTxt_Nb_AP_Nodes.getText();
        initGenDstr = bt.jCmbDstr.getSelectedIndex();
    }

    //Enables or disables the rates according to the value of the physical layer
    public void checkRates( String str) {
        // 802.11a
        switch (str) {
            case "11a":
                // 802.11a
//			disableRates(false,false,false,false,false,false,false,false,true,true);
                disableRates(true, true, true, true, true, true, true, true, false, false, false, false, false, false);
                PHY_type = 1;
                break;
            case "11ac":
                // 802.11ac
                disableRates(true, true, true, true, true, true, true, true, true, true, false, false, false, false);
                PHY_type = 2;
                break;
            case "11af":
                // 802.11af
                disableRates(true, true, true, true, true, true, true, true, false, false, false, false, false, false);
                PHY_type = 2;
                break;
            case "11ah":
                // 802.11ah FIXME
                disableRates(true, true, true, true, true, true, true, true, false, false, false, false, false, false);
                PHY_type = 2;
                break;
            case "11ax":
                // 802.11ac
                disableRates(true, true, true, true, true, true, true, true, true, true, true, true, true, true);
                PHY_type = 2;
                break;
            case "LTE-U":
                // FIXME
                disableRates(true, true, true, true, true, true, true, true, false, false, false, false, false, false);
                PHY_type = 2;
                break;
            default:
                disableRates(false, false, false, false, false, false, false, false, false, false, false, false, false, false);
                break;
        }
    }

    //Used in the CheckRates above
    private void disableRates(boolean r1, boolean r2, boolean r5, boolean r6, 
            boolean r9, boolean r11, boolean r12, boolean r18, 
            boolean r22, boolean r24,
            boolean r30, boolean r31, boolean r32, boolean r33
    ) {
        bt.jRb_MCS_0.setEnabled(r1);
        bt.jRb_MCS_1.setEnabled(r2);
        bt.jRb_MCS_2.setEnabled(r5);
        bt.jRb_MCS_3.setEnabled(r6);
        bt.jRb_MCS_4.setEnabled(r9);
        bt.jRb_MCS_5.setEnabled(r11);
        bt.jRb_MCS_6.setEnabled(r12);
        bt.jRb_MCS_7.setEnabled(r18);
        bt.jRb_MCS_8.setEnabled(r22);
        bt.jRb_MCS_9.setEnabled(r24);
        bt.jRb_MCS_10.setEnabled(r30);
        bt.jRb_MCS_11.setEnabled(r31);
        bt.jRb_MCS_12.setEnabled(r32);
        bt.jRb_MCS_13.setEnabled(r33);
    }

}




