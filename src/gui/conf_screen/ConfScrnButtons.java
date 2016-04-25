package gui.conf_screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ConfScrnButtons extends JFrame {

    public Btn_events cs_bt_ev;    
        
    public String srate;
    long loc_rate;
    public long static_irate;

    private static final long serialVersionUID = 1L;
    
    private JButton jBtConfOK = null;
    
    private JButton jBtConfCancel = null;

    /* MCS management */
    
    public JRadioButton jRb_MCS_0 = null;
    
    public JRadioButton jRb_MCS_1 = null;
    
    public JRadioButton jRb_MCS_2 = null;
    
    public JRadioButton jRb_MCS_5 = null;
    
    public JRadioButton jRb_MCS_3 = null;
    
    public JRadioButton jRb_MCS_6 = null;
    
    public JRadioButton jRb_MCS_8 = null;
    
    public JRadioButton jRb_MCS_4 = null;
    
    public JRadioButton jRb_MCS_7 = null;
    
    public JRadioButton jRb_MCS_9 = null;
    
    public JRadioButton jRb_MCS_10 = null;
    
    public JRadioButton jRb_MCS_11 = null;
    
    public JRadioButton jRb_MCS_12 = null;
    
    public JRadioButton jRb_MCS_13 = null;
    
    // # of APs and STAs
    
    public JTextField jTxt_Nb_STA = null;
    
    public JTextField jTxt_Nb_AP_Nodes = null;

    public JTextField jTxt_Roof = null;
    

   // distance between STAs and ambient noise
    
    public JTextField jTxtDistNodes = null;
    
    public JTextField jTxtAmbNoise = null;
 
    public JComboBox jCmbPkt = null; // packet size
    
    public JComboBox jCmbDstr = null; // packet generation rate
    
    public JTextField jTxtPktGenRateMean = null;
    
    private JLabel jLbl_lvl_noise = null;
    
    private JLabel jLbl_prog_heur = null;
    
    public JTextField jTxt_prog_heur = null;
    
    private JRadioButton jmodelA = null;
    
    private JRadioButton jmodelB = null;
    
    private JRadioButton jmodelC = null;
    
    private JRadioButton jmodelD = null;
    
    public JRadioButton jchannelA = null;
    public JRadioButton jchannelB = null;
    public JRadioButton jchannelC = null;
    public JRadioButton jchannelD = null;
    
    private JRadioButton jmimobf = null;
    private JRadioButton jmimoss = null;

    private JRadioButton jchannelW6 = null;
    private JRadioButton jchannelW7 = null;
    private JRadioButton jchannelW8 = null;

    private JRadioButton jantenna1 = null;
    private JRadioButton jantenna2 = null;
    private JRadioButton jantenna4 = null;
    private JRadioButton jantenna8 = null;

    // MCS choice
    public ButtonGroup rateGroup = new ButtonGroup();

    // choice of kind of MIMO 
    private ButtonGroup MIMOGroup = new ButtonGroup();

    // number of antennas choice
    private ButtonGroup antGroup = new ButtonGroup();

    // channel aggregation choice   
    private ButtonGroup chanGroup = new ButtonGroup();

    // channel width choice   
    private ButtonGroup chanWidthGroup = new ButtonGroup();

    // model premise
    private ButtonGroup modGroup = new ButtonGroup();


    // the OK button of the configuration screen
    public JButton getJBtConfOK( final MainScreen pm,  final ConfScrn_main cfgm) {
        if (jBtConfOK == null) {
            jBtConfOK = new JButton();
            jBtConfOK.setText("OK");
            jBtConfOK.setSize(new Dimension(95, 20));
            jBtConfOK.setFont(new Font("Dialog", Font.PLAIN, 12));
            jBtConfOK.setLocation(new Point(19, 390));
            jBtConfOK.addActionListener(new java.awt.event.ActionListener() 
                {
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    cfgm.wcfg.writeToNtConf(pm.msb, cfgm); // write conf in memory variables
                    cfgm.setVisible(false);
                    pm.setEnabled(true);
                    pm.setVisible(true);
                    }
                });
        }
        return jBtConfOK;
    }
     // the Cancel button of the configuration screen   
    public JButton getJBtConfCancel( final MainScreen pm,  final ConfScrn_main cfgm) {
        if (jBtConfCancel == null) {
            jBtConfCancel = new JButton();
            jBtConfCancel.setText("Cancel");
            //Same listener as this for the X button
            jBtConfCancel.setBounds(new Rectangle(645, 390, 95, 20));
            jBtConfCancel.setFont(new Font("Dialog", Font.PLAIN, 12));
            jBtConfCancel.setMnemonic(KeyEvent.VK_UNDEFINED);
            jBtConfCancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    cfgm.getInitParams();
                    cfgm.setVisible(false);
                    pm.setEnabled(true);
                    pm.setVisible(true);
                }
            });
        }
        return jBtConfCancel;
    }

    // Channel aggregated width 80MHz
    public JRadioButton getJAggChanA( MainScreen_buttons ms, String aggchan ) {
        if (jchannelA == null) {
            jchannelA = new JRadioButton();
//            jchannelA.setSelected(true);
            jchannelA.setActionCommand(aggchan);
            jchannelA.setBounds(new Rectangle(8, 20, 70, 20));
            jchannelA.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelA.setText(aggchan);
            jchannelA.addActionListener(cs_bt_ev.chanListen);
            chanGroup.add(jchannelA);
        }
        return jchannelA;
    }

    // Channel aggregated width 160MHz
    public JRadioButton getJAggChanB( MainScreen_buttons ms, String aggchan ) {
        if (jchannelB == null) {
            jchannelB = new JRadioButton();
            jchannelB.setActionCommand(aggchan);
            jchannelB.setBounds(new Rectangle(8, 40, 70, 20));
            jchannelB.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelB.setText(aggchan);
            jchannelB.addActionListener(cs_bt_ev.chanListen);
            chanGroup.add(jchannelB);
        }
        return jchannelB;
    }

    // Channel aggregated width 20MHz
    public JRadioButton getJAggChanC( MainScreen_buttons ms, String aggchan) {
        if (jchannelC == null) {
            jchannelC = new JRadioButton();
            jchannelC.setSelected(true);
            jchannelC.setActionCommand(aggchan);
            jchannelC.setBounds(new Rectangle(80, 20, 65, 20));
            jchannelC.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelC.setText(aggchan);
            jchannelC.addActionListener(cs_bt_ev.chanListen);
            chanGroup.add(jchannelC);
        }
        return jchannelC;
    }

    // Channel aggregated width 40MHz
    public JRadioButton getJAggChanD(MainScreen_buttons ms, String aggchan) {
        if (jchannelD == null) {
            jchannelD = new JRadioButton();
            jchannelD.setActionCommand(aggchan);
            jchannelD.setBounds(new Rectangle(80, 40, 65, 20));
            jchannelD.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelD.setText(aggchan);
            jchannelD.addActionListener(cs_bt_ev.chanListen);
            chanGroup.add(jchannelD);
        }
        return jchannelD;
    }

    // Spatial Stream MIMO
    public JRadioButton getJMIMO_SS( MainScreen_buttons ms) {
        if (jmimoss == null) {
            jmimoss = new JRadioButton();
            jmimoss.setSelected(true);
            jmimoss.setActionCommand("ss");
            jmimoss.setBounds(new Rectangle(8, 20, 130, 20));
            jmimoss.setFont(new Font("Dialog", Font.PLAIN, 10));
            jmimoss.setText("Spatial Streams");
            jmimoss.addActionListener(cs_bt_ev.mimoListen);
            MIMOGroup.add(jmimoss);
        }
        return jmimoss;
    }

    // Beam forming MIMO
    public JRadioButton getJMIMO_BF( MainScreen_buttons ms) {
        if (jmimobf == null) {
            jmimobf = new JRadioButton();
            jmimobf.setActionCommand("bf");
            jmimobf.setBounds(new Rectangle(8, 40, 130, 20));
            jmimobf.setFont(new Font("Dialog", Font.PLAIN, 10));
            jmimobf.setText("Beam Forming");
            jmimobf.addActionListener(cs_bt_ev.mimoListen);
            MIMOGroup.add(jmimobf);
        }
        return jmimobf;
    }

    // Channel width A ex: 6MHz
    public JRadioButton getJchannelWidthA( MainScreen_buttons ms, String width) {
        if (jchannelW6 == null) {
            jchannelW6 = new JRadioButton();
            jchannelW6.setSelected(true);
            jchannelW6.setActionCommand(width);
            jchannelW6.setBounds(new Rectangle(8, 20, 70, 20));
            jchannelW6.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelW6.setText(width);
            jchannelW6.addActionListener(cs_bt_ev.aggrListen);
            chanWidthGroup.add(jchannelW6);
        }
        return jchannelW6;
    }

    // Channel width B ex: 7MHz
    public JRadioButton getJchannelWidthB( MainScreen_buttons ms, String width) {
        if (jchannelW7 == null) {
            jchannelW7 = new JRadioButton();
            jchannelW7.setActionCommand(width);
            jchannelW7.setBounds(new Rectangle(8, 40, 70, 20));
            jchannelW7.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelW7.setText(width);
            jchannelW7.addActionListener(cs_bt_ev.aggrListen);
            chanWidthGroup.add(jchannelW7);
        }
        return jchannelW7;
    }

    // Channel width C, ex:8MHz
    public JRadioButton getJchannelWidthC( MainScreen_buttons ms, String width) {
        if (jchannelW8 == null) {
            jchannelW8 = new JRadioButton();
//            jchannelC.setSelected(true);
            jchannelW8.setActionCommand(width);
            jchannelW8.setBounds(new Rectangle(80, 20, 65, 20));
            jchannelW8.setFont(new Font("Dialog", Font.PLAIN, 10));
            jchannelW8.setText(width);
            jchannelW8.addActionListener(cs_bt_ev.aggrListen);
            chanWidthGroup.add(jchannelW8);
        }
        return jchannelW8;
    }

    // Number of antennas
    public JRadioButton getjantenna1( MainScreen_buttons msb) {
        if (jantenna1 == null) {
            jantenna1 = new JRadioButton();
            jantenna1.setSelected(true);
            jantenna1.setActionCommand("ant1");
            jantenna1.setBounds(new Rectangle(8, 20, 106, 20));
            jantenna1.setFont(new Font("Dialog", Font.PLAIN, 10));
            jantenna1.setText("1 X 1 antenna");
            jantenna1.addActionListener(cs_bt_ev.antListen);
            antGroup.add(jantenna1);
        }
        return jantenna1;
    }


    
    public JRadioButton getjantenna2( MainScreen_buttons msb) {
        if (jantenna2 == null) {
            jantenna2 = new JRadioButton();
            jantenna2.setActionCommand("ant2");
            jantenna2.setBounds(new Rectangle(8, 40, 113, 20));
            jantenna2.setFont(new Font("Dialog", Font.PLAIN, 10));
            jantenna2.setText("2 X 2 antennas");
            jantenna2.addActionListener(cs_bt_ev.antListen);
            antGroup.add(jantenna2);
        }
        return jantenna2;
    }

    
    public JRadioButton getjantenna4( MainScreen_buttons msb) {
        if (jantenna4 == null) {
            jantenna4 = new JRadioButton();
            jantenna4.setActionCommand("ant4");
            jantenna4.setBounds(new Rectangle(8, 60, 106, 20));
            jantenna4.setFont(new Font("Dialog", Font.PLAIN, 10));
            jantenna4.setText("4 X 2 antennas");
            jantenna4.addActionListener(cs_bt_ev.antListen);
            antGroup.add(jantenna4);
        }
        return jantenna4;
    }


    
    public JRadioButton getjantenna8( MainScreen_buttons msb) {
        if (jantenna8 == null) {
            jantenna8 = new JRadioButton();
            jantenna8.setActionCommand("ant8");
            jantenna8.setBounds(new Rectangle(8, 80, 113, 20));
            jantenna8.setFont(new Font("Dialog", Font.PLAIN, 10));
            jantenna8.setText("8 X 1 antennas");
            jantenna8.addActionListener(cs_bt_ev.antListen);
            antGroup.add(jantenna8);
        }
        return jantenna8;
    }

    // do not modify values without aligning with mcsRadioListener in ConfScrnBtn_events.java
    
    public JRadioButton getJRbRate0( MainScreen_buttons msb) {
        if (jRb_MCS_0 == null) {
            jRb_MCS_0 = new JRadioButton();
            // smcs = "MCS 0" ;
            jRb_MCS_0.setSelected(true);
            jRb_MCS_0.setBounds(new Rectangle(15, 19, 70, 16));
            jRb_MCS_0.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_0.setText("MCS 0");
            jRb_MCS_0.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_0);
        }
        return jRb_MCS_0;
    }

    
    public JRadioButton getJRbRate1( MainScreen_buttons msb) {
        if (jRb_MCS_1 == null) {
            jRb_MCS_1 = new JRadioButton();
            // smcs = "MCS 1";
            jRb_MCS_1.setBounds(new Rectangle(15, 39, 70, 15));
            jRb_MCS_1.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_1.setText("MCS 1");
            jRb_MCS_1.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_1);
        }
        return jRb_MCS_1;
    }

    
    public JRadioButton getJRbRate2( MainScreen_buttons msb) {
        if (jRb_MCS_2 == null) {
            jRb_MCS_2 = new JRadioButton();
            // smcs = "MCS 2" ;
            jRb_MCS_2.setEnabled(false);
            jRb_MCS_2.setBounds(new Rectangle(15, 59, 82, 15));
            jRb_MCS_2.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_2.setText("MCS 2");
            jRb_MCS_2.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_2);
        }
        return jRb_MCS_2;
    }

    
    public JRadioButton getJRbRate3( MainScreen_buttons msb) {
        if (jRb_MCS_3 == null) {
            jRb_MCS_3 = new JRadioButton();
            // smcs = "MCS 3" ;
            jRb_MCS_3.setEnabled(false);
            jRb_MCS_3.setBounds(new Rectangle(95, 19, 70, 15));
            jRb_MCS_3.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_3.setText("MCS 3");
            jRb_MCS_3.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_3);
        }
        return jRb_MCS_3;
    }

    
    public JRadioButton getJRbRate4( MainScreen_buttons msb) {
        if (jRb_MCS_4 == null) {
            jRb_MCS_4 = new JRadioButton();
            // smcs = "MCS 4" ;
            jRb_MCS_4.setEnabled(false);
            jRb_MCS_4.setBounds(new Rectangle(95, 39, 70, 15));
            jRb_MCS_4.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_4.setText("MCS 4");
            jRb_MCS_4.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_4);
        }
        return jRb_MCS_4;
    }

    
    public JRadioButton getJRbRate5( MainScreen_buttons msb) {
        if (jRb_MCS_5 == null) {
            jRb_MCS_5 = new JRadioButton();
            // smcs = "MCS 5" ;
            jRb_MCS_5.setEnabled(false);
            jRb_MCS_5.setBounds(new Rectangle(95, 59, 82, 15));
            jRb_MCS_5.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_5.setText("MCS 5");
            jRb_MCS_5.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_5);
        }
        return jRb_MCS_5;
    }

    
    public JRadioButton getJRbRate6( MainScreen_buttons msb) {
        if (jRb_MCS_6 == null) {
            jRb_MCS_6 = new JRadioButton();
            // smcs = "MCS 6" ;
            jRb_MCS_6.setEnabled(false);
            jRb_MCS_6.setBounds(new Rectangle(175, 19, 85, 15));
            jRb_MCS_6.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_6.setText("MCS 6");
            jRb_MCS_6.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_6);
        }
        return jRb_MCS_6;
    }

    
    public JRadioButton getJRbRate7( MainScreen_buttons msb) {
        if (jRb_MCS_7 == null) {
            jRb_MCS_7 = new JRadioButton();
            // smcs = "MCS 7" ;
            jRb_MCS_7.setEnabled(false);
            jRb_MCS_7.setBounds(new Rectangle(175, 39, 84, 15));
            jRb_MCS_7.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_7.setText("MCS 7");
            jRb_MCS_7.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_7);
        }
        return jRb_MCS_7;
    }

    
    public JRadioButton getJRbRate8( MainScreen_buttons msb) {
        if (jRb_MCS_8 == null) {
            jRb_MCS_8 = new JRadioButton();
            // smcs = "MCS 8" ;
            jRb_MCS_8.setEnabled(false);
            jRb_MCS_8.setBounds(new Rectangle(175, 59, 83, 15));
            jRb_MCS_8.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_8.setText("MCS 8");
            jRb_MCS_8.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_8);
        }
        return jRb_MCS_8;
    }

    public JRadioButton getJRbRate9( MainScreen_buttons msb) {
        if (jRb_MCS_9 == null) {
            jRb_MCS_9 = new JRadioButton();
            // smcs = "MCS 9" ;
            jRb_MCS_9.setEnabled(false);
            jRb_MCS_9.setBounds(new Rectangle(255, 19, 81, 15));
            jRb_MCS_9.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_9.setText("MCS 9");
            jRb_MCS_9.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_9);
        }
        return jRb_MCS_9;
    }

    public JRadioButton getJRbRate10( MainScreen_buttons msb) {
        if (jRb_MCS_10 == null) {
            jRb_MCS_10 = new JRadioButton();
            // smcs = "MCS 10" ;
            jRb_MCS_10.setEnabled(false);
            jRb_MCS_10.setBounds(new Rectangle(255, 39, 70, 15));
            jRb_MCS_10.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_10.setText("MCS 10");
            jRb_MCS_10.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_10);
        }
        return jRb_MCS_10;
    }

    public JRadioButton getJRbRate11( MainScreen_buttons msb) {
        if (jRb_MCS_11 == null) {
            jRb_MCS_11 = new JRadioButton();
            // smcs = "MCS 11" ;
            jRb_MCS_11.setEnabled(false);
            jRb_MCS_11.setBounds(new Rectangle(255, 59, 81, 15));
            jRb_MCS_11.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_11.setText("MCS 11");
            jRb_MCS_11.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_11);
        }
        return jRb_MCS_11;
    }

    public JRadioButton getJRbRate12( MainScreen_buttons msb) {
        if (jRb_MCS_12 == null) {
            jRb_MCS_12 = new JRadioButton();
            // smcs = "MCS 12" ;
            jRb_MCS_12.setEnabled(false);
            jRb_MCS_12.setBounds(new Rectangle(330, 19, 81, 15));
            jRb_MCS_12.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_12.setText("MCS 12");
            jRb_MCS_12.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_12);
        }
        return jRb_MCS_12;
    }

    public JRadioButton getJRbRate13( MainScreen_buttons msb) {
        if (jRb_MCS_13 == null) {
            jRb_MCS_13 = new JRadioButton();
            // smcs = "MCS 13" ;
            jRb_MCS_13.setEnabled(false);
            jRb_MCS_13.setBounds(new Rectangle(330, 39, 81, 15));
            jRb_MCS_13.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRb_MCS_13.setText("MCS 13");
            jRb_MCS_13.addActionListener(cs_bt_ev.mcsListen);
            rateGroup.add(jRb_MCS_13);
        }
        return jRb_MCS_13;
    }

   
    public JRadioButton getJmodelA( MainScreen_buttons msb) {
        if (jmodelA == null) {
            jmodelA = new JRadioButton();
            jmodelA.setActionCommand("mod_a");
            jmodelA.setBounds(new Rectangle(8, 20, 113, 20));
            jmodelA.setFont(new Font("Dialog", Font.PLAIN, 10));
            jmodelA.addActionListener(cs_bt_ev.modListen);
            jmodelA.setText("Circle model");
            modGroup.add(jmodelA);
        }
        return jmodelA;
    }

    
    public JRadioButton getJmodelB( MainScreen_buttons msb) {
        if (jmodelB == null) {
            jmodelB = new JRadioButton();
            jmodelB.setSelected(true);
            jmodelB.setActionCommand("mod_b");
            jmodelB.setBounds(new Rectangle(8, 40, 113, 20));
            jmodelB.setFont(new Font("Dialog", Font.PLAIN, 10));
            jmodelB.addActionListener(cs_bt_ev.modListen);
            jmodelB.setText("Room model");
            modGroup.add(jmodelB);
        }
        return jmodelB;
    }

    
    public JRadioButton getJmodelC( MainScreen_buttons msb) {
        if (jmodelC == null) {
            jmodelC = new JRadioButton();
            jmodelC.setActionCommand("mod_c");
            jmodelC.setBounds(new Rectangle(8, 60, 113, 20));
            jmodelC.setFont(new Font("Dialog", Font.PLAIN, 10));
            jmodelC.addActionListener(cs_bt_ev.modListen);
            jmodelC.setText("Bus model");
            modGroup.add(jmodelC);
        }
        return jmodelC;
    }

    
    public JRadioButton getJmodelD( MainScreen_buttons msb) {
        if (jmodelD == null) {
            jmodelD = new JRadioButton();
            jmodelD.setActionCommand("mod_d");
            jmodelD.setBounds(new Rectangle(8, 80, 113, 20));
            jmodelD.setFont(new Font("Dialog", Font.PLAIN, 10));
            jmodelD.addActionListener(cs_bt_ev.modListen);
            jmodelD.setText("Stadium model");
            modGroup.add(jmodelD);
        }
        return jmodelD;
    }

    public JLabel getLbl_prog_heur() { // Text field for progagation model heuristic
        if (jLbl_prog_heur == null) {
            jLbl_prog_heur = new JLabel();
            jLbl_prog_heur.setText("Propag. heuristic:");
            jLbl_prog_heur.setBounds(new Rectangle(18, 13, 250, 19));
            jLbl_prog_heur.setFont(new Font("Dialog", Font.PLAIN, 10));
        }
        return jLbl_prog_heur;
    }

    
    public JTextField getJTxt_prog_heur() { // Input box (Text field) for progagation model heuristic
        if (jTxt_prog_heur == null) {
            jTxt_prog_heur = new JTextField();
            jTxt_prog_heur.setText("900");
            jTxt_prog_heur.setFont(new Font("Dialog", Font.PLAIN, 12));
            jTxt_prog_heur.setBounds(new Rectangle(18, 33, 46, 23));
        }
        return jTxt_prog_heur;
    }

    
    public JLabel getLbl_lvl_noise() { // Text field for ambient noise
        if (jLbl_lvl_noise == null) {
            jLbl_lvl_noise = new JLabel();
            jLbl_lvl_noise.setText("Level of noise:");
            jLbl_lvl_noise.setBounds(new Rectangle(18, 53, 250, 19));
            jLbl_lvl_noise.setFont(new Font("Dialog", Font.PLAIN, 10));
        }
        return jLbl_lvl_noise;
    }

    
    public JTextField getJTxt_amb_noise() { // Input box for ambient noise
        if (jTxtAmbNoise == null) {
            jTxtAmbNoise = new JTextField();
            jTxtAmbNoise.setText("-90");
            jTxtAmbNoise.setFont(new Font("Dialog", Font.PLAIN, 12));
            jTxtAmbNoise.setBounds(new Rectangle(18, 73, 46, 23));
        }
        return jTxtAmbNoise;
    }

    
    public JTextField getJTxt_nb_STA_Nodes() { // Input box for nb of STAs
        if (jTxt_Nb_STA == null) {
            jTxt_Nb_STA = new JTextField();
            jTxt_Nb_STA.setText("10");
            jTxt_Nb_STA.setFont(new Font("Dialog", Font.PLAIN, 12));
            jTxt_Nb_STA.setBounds(new Rectangle(165, 18, 46, 23));
        }
        return jTxt_Nb_STA;
    }

    
    // fixme the new value is not used when the conf window is reopened and the new value set
    
    public JTextField getJTxt_dist_STA() { // Input box for distance between STAs
        if (jTxtDistNodes == null) {
            jTxtDistNodes = new JTextField();
            jTxtDistNodes.setText("6");
            jTxtDistNodes.setFont(new Font("Dialog", Font.PLAIN, 12));
            jTxtDistNodes.setBounds(new Rectangle(165, 50, 46, 23));
//			jTxtDistNodes.addActionListener(envListen);
        }
        return jTxtDistNodes;
    }

    
    public JTextField getJTxt_nb_AP_Nodes() {
        if (jTxt_Nb_AP_Nodes == null) {
            jTxt_Nb_AP_Nodes = new JTextField();
            jTxt_Nb_AP_Nodes.setEnabled(true);
            jTxt_Nb_AP_Nodes.setBounds(new Rectangle(351, 18, 41, 21));
            jTxt_Nb_AP_Nodes.setFont(new Font("Dialog", Font.PLAIN, 12));
            jTxt_Nb_AP_Nodes.setText("1");
        }
        return jTxt_Nb_AP_Nodes;
    }

    
    public JTextField getJTxt_Roof() {
        if (jTxt_Roof == null) {
            jTxt_Roof = new JTextField();
            jTxt_Roof.setEnabled(true);
            jTxt_Roof.setBounds(new Rectangle(351, 50, 41, 21));
            jTxt_Roof.setFont(new Font("Dialog", Font.PLAIN, 12));
            jTxt_Roof.setText("15");
        }
        return jTxt_Roof;
    }

    public JComboBox getJCmbDstr() {
        if (jCmbDstr == null) {
            jCmbDstr = new JComboBox();
            jCmbDstr.setBounds(new Rectangle(320, 19, 79, 21));
            jCmbDstr.setFont(new Font("Dialog", Font.PLAIN, 12));
            jCmbDstr.addItem("data");
            jCmbDstr.addItem("saturation");
            jCmbDstr.addItem("const bit rate");
            jCmbDstr.addItem("saturation_fixed");
            jCmbDstr.addItem("ftp (3gpp)");
            jCmbDstr.addItem("http (3gpp)");
            jCmbDstr.addItem("video (3gpp)");
            jCmbDstr.addItem("disabled");
        }
        return jCmbDstr;
    }
}

