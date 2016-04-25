/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.conf_screen;

import zzInfra.emulator.JE802Starter;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import std_def_XML.XMLGlobalConf;

/**
 * @author Jean-Pierre Le Rouzic
 */
public class MainScreen_buttons {

    
    public JPanel jCPaneRun = null;
    
    public JTextArea jTxtArRun = null;
    
    public JLabel jLabel = null;
    
    public JProgressBar jProgressBar = null;
    
    public JLabel jLabel2 = null;
    
    public JButton jBtRun = null;
    
    public JButton jBtPause = null;
    
    public JPanel jPanelNtConf = null;
    
    public JPanel JPanelWstd = null;
    
    public JMenuBar jJMenuBar = null;
    
    public JMenu jMenuFile = null;
    
    public JMenuItem jMenuItemExit = null;
    
    public JRadioButton jRbXml = null;
    
    public JLabel jLblNtConf = null;
    
    public JRadioButton jRbGlobal = null;
    
    public JButton jBtnRun = null;
    
    private JButton jButton3 = null;  
    
    private ButtonGroup ntGroup = new ButtonGroup();  //  @jve:decl-index=0:
    
    public JButton jBtConf = null;
    
    private ConfScrn_main cf = null;
    

    /* PHY management */
    
    private JRadioButton jRBPhyLTEU = null; // LTE-U

    private JRadioButton jRBPhy11ac = null; // .11ac

    private JRadioButton jRBPhy11af = null; // .11af

    private JRadioButton jRBPhy11ah = null; // .11ah

    private JRadioButton jRBPhy11ax = null; // .11ax

    public ButtonGroup phyGroup = new ButtonGroup();
    
    public Btn_events ms_bt_ev;


    // "Run Simulation" button
    public JButton getJBtnRun( final MainScreen msc) {
        if (jBtnRun == null) {
            jBtnRun = new JButton();
            jBtnRun.setBounds(new Rectangle(15, 110, 119, 20));
            jBtnRun.setFont(new Font("Dialog", Font.PLAIN, 12));
            jBtnRun.setText("Run Simulation");
            jBtnRun.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    msc.CheckAndStart();
                    try {
                        // Let's run the simulation
                        JE802Starter.RunSimul() ;
                    } catch (IOException ex) {
                        Logger.getLogger(MainScreen_buttons.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            });
        }
        return jBtnRun;
    }

    // exit button
    public JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setBounds(new Rectangle(365, 110, 120, 20));
            jButton3.setFont(new Font("Dialog", Font.PLAIN, 12));
            jButton3.setText("Exit");
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return jButton3;
    }

    // configuration screen button
    private JButton getJBtConf( final MainScreen msc, final XMLGlobalConf mk) {
        if (jBtConf == null) {
            jBtConf = new JButton();
            jBtConf.setText("Configure");
            jBtConf.setFont(new Font("Dialog", Font.PLAIN, 12));
            jBtConf.setBounds(new Rectangle(350, 25, 120, 22));
            jBtConf.addActionListener(new java.awt.event.ActionListener() 
                {
                public void actionPerformed(java.awt.event.ActionEvent e) 
                    {
                    AmendNotAvailable amd ;    
                    ButtonModel initPhy = phyGroup.getSelection(); //Get the selected item 
                    String tuty = initPhy.getActionCommand();
                    switch (tuty) {
                        case "11ac":
                            // int aggwidth, int nbagg, int widtha, int widthb, int widthc
                            cf = new ConfScrn_main(msc, mk, 4, 20, 0, 0);
                            break;
                        case "11ax":
                            // int aggwidth, int nbagg, int widtha, int widthb, int widthc
                            cf = new ConfScrn_main(msc, mk, 8, 20, 0, 0);
                            break;
                        case "11af":
                            // int aggwidth, int nbagg, int widtha, int widthb, int widthc
                            cf = new ConfScrn_main(msc, mk, 4, 6, 7, 8);
                            break;
                        case "11ah":
                            amd = new AmendNotAvailable() ;
                            amd.showPopupMenuDemo();
                            break;
                        case "LTE-U":
                            amd = new AmendNotAvailable() ;
                            amd.showPopupMenuDemo();
                            break;
                        }
                    if(cf != null)
                        {
                        cf.setVisible(true);
                        cf.cfg_setInitParams( tuty); 
                        }
                    msc.setEnabled(false);
                    }
                });
        }
        return jBtConf;
    }

    // "use XML file Configuration" button
            // Using XML Configuration file
    public JLabel getJBtXML( final MainScreen msc) {
        if (jLblNtConf == null) {
           jLblNtConf = new JLabel();
           jLblNtConf.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
           jLblNtConf.setBounds(new Rectangle(280, 58, 201, 20));
           jLblNtConf.setFont(new Font("Dialog", Font.PLAIN, 12));
           jLblNtConf.setText("Using XML Configuration file");
        }
        return jLblNtConf ;
    }
    
    // Configure Nodes with the GUI
    private JRadioButton getJRbGlobal() {
        if (jRbGlobal == null) {
            jRbGlobal = new JRadioButton();
            jRbGlobal.setSelected(true);
            jRbGlobal.setBounds(new Rectangle(15, 25, 210, 20));
            jRbGlobal.setFont(new Font("Dialog", Font.PLAIN, 12));
            jRbGlobal.setText("Configure Nodes with the GUI");
            jRbGlobal.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
//					jBtnNtConf.setEnabled(false);
                    // enable configuration screen
                    jBtConf.setEnabled(true);
                }
            });
            ntGroup.add(jRbGlobal);
        }
        return jRbGlobal;
    }
   
    // Configuration as in the XML File
    private JRadioButton getJRbXml() {
        if (jRbXml == null) {
            jRbXml = new JRadioButton();
            jRbXml.setBounds(new Rectangle(15, 55, 229, 16));
            jRbXml.setFont(new Font("Dialog", Font.PLAIN, 12));
            jRbXml.setText("Configuration as in the XML File");
            jRbXml.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // disable configuration screen                    
                    jBtConf.setEnabled(false);
//					jBtnNtConf.setEnabled(false);
                    jLblNtConf.setEnabled(false);
                }
            });
            ntGroup.add(jRbXml);
        }
        return jRbXml;
    }

     // 3GPP LTE-U
    public JRadioButton getJRBPhyLTEU( ) {
        if (jRBPhyLTEU == null) {
            jRBPhyLTEU = new JRadioButton();
            jRBPhyLTEU.setSelected(false);
            jRBPhyLTEU.setActionCommand("LTE-U");
            jRBPhyLTEU.setBounds(new Rectangle(8, 20, 113, 20));
            jRBPhyLTEU.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRBPhyLTEU.setText("LTE-U");
//            jRBPhyLTEU.addActionListener(ms_bt_ev.phyListen);
            phyGroup.add(jRBPhyLTEU);
        }
        return jRBPhyLTEU;
    }

    // IEEE 802.11ac
    public JRadioButton getJRBPhy11ac( ) {
        if (jRBPhy11ac == null) {
            jRBPhy11ac = new JRadioButton();
            jRBPhy11ac.setSelected(true);
            jRBPhy11ac.setActionCommand("11ac");
            jRBPhy11ac.setBounds(new Rectangle(8, 40, 105, 20));
            jRBPhy11ac.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRBPhy11ac.setText("IEEE 802.11ac");
//            jRBPhy11ac.addActionListener(ms_bt_ev.phyListen);
            phyGroup.add(jRBPhy11ac);
        }
        return jRBPhy11ac;
    }

    // IEEE 802.11af    
    public JRadioButton getJRBPhy11af( ) {
        if (jRBPhy11af == null) {
            jRBPhy11af = new JRadioButton();
            jRBPhy11af.setSelected(false);
            jRBPhy11af.setActionCommand("11af");
            jRBPhy11af.setBounds(new Rectangle(8, 60, 105, 20));
            jRBPhy11af.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRBPhy11af.setText("IEEE 802.11af");
//            jRBPhy11af.addActionListener(ms_bt_ev.phyListen);
            phyGroup.add(jRBPhy11af);
        }
        return jRBPhy11af;
    }

    // IEEE 802.11ah
    public JRadioButton getJRBPhy11ah( ) {
        if (jRBPhy11ah == null) {
            jRBPhy11ah = new JRadioButton();
            jRBPhy11ah.setSelected(false);
            jRBPhy11ah.setActionCommand("11ah");
            jRBPhy11ah.setBounds(new Rectangle(8, 80, 105, 20));
            jRBPhy11ah.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRBPhy11ah.setText("IEEE 802.11ah");
//            jRBPhy11ah.addActionListener(ms_bt_ev.phyListen);
            phyGroup.add(jRBPhy11ah);
        }
        return jRBPhy11ah;
    }
   
    // IEEE 802.11ah
    public JRadioButton getJRBPhy11ax( ) {
        if (jRBPhy11ax == null) {
            jRBPhy11ax = new JRadioButton();
            jRBPhy11ax.setSelected(false);
            jRBPhy11ax.setActionCommand("11ax");
            jRBPhy11ax.setBounds(new Rectangle(120, 20, 105, 20));
            jRBPhy11ax.setFont(new Font("Dialog", Font.PLAIN, 10));
            jRBPhy11ax.setText("IEEE 802.11ax");
//            jRBPhy11ax.addActionListener(ms_bt_ev.phyListen);
            phyGroup.add(jRBPhy11ax);
        }
        return jRBPhy11ax;
    }
   
    // panel containing the "Nodes Configuration" radio buttons and conf screen
    public JPanel getJPanelWstd( MainScreen msc, XMLGlobalConf mk) {
        if (JPanelWstd == null) 
            {
            JPanelWstd = new JPanel();
            JPanelWstd.setLayout(null);
            JPanelWstd.setBounds(new Rectangle(15, 150, 502, 120));
            JPanelWstd.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Radio Configuration", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            ms_bt_ev = new Btn_events(msc.msb);

            JPanelWstd.add(getJRBPhyLTEU(), null);
            JPanelWstd.add(getJRBPhy11ac(), null);
            JPanelWstd.add(getJRBPhy11af(), null);
            JPanelWstd.add(getJRBPhy11ah(), null);
            JPanelWstd.add(getJRBPhy11ax(), null);
            }            
       return JPanelWstd;
    }

    // panel containing the "Nodes Configuration radio buttons and conf screen
    public JPanel getJPanelNtConf( MainScreen msc, XMLGlobalConf mk) {
        if (jPanelNtConf == null) {
            jPanelNtConf = new JPanel();
            jPanelNtConf.setLayout(null);
            jPanelNtConf.setBounds(new Rectangle(15, 10, 502, 85));
            jPanelNtConf.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Nodes Configuration", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));
            
            jPanelNtConf.add(getJBtConf(msc, mk), null);
            jPanelNtConf.add(getJRbGlobal(), null);
            jPanelNtConf.add(getJRbXml(), null);
        }
        return jPanelNtConf;
    }

    public JMenuBar getJJMenuBar(MainScreen msc) {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getJMenuFile(msc));
//            jJMenuBar.add(getJMenuHelp());
        }
        return jJMenuBar;
    }

    
    private JMenu getJMenuFile(MainScreen msc) {
        if (jMenuFile == null) {
            jMenuFile = new JMenu();
            jMenuFile.setPreferredSize(new Dimension(60, 20));
            jMenuFile.setText("File");
            jMenuFile.setFont(new Font("Dialog", Font.PLAIN, 12));
            jMenuFile.setMnemonic(KeyEvent.VK_UNDEFINED);
            jMenuFile.setBounds(new Rectangle(0, 0, 40, 20));
            jMenuFile.addSeparator();
            jMenuFile.add(getJMenuItemExit());
        }
        return jMenuFile;
    }

    private JMenuItem getJMenuItemExit() {
        if (jMenuItemExit == null) {
            jMenuItemExit = new JMenuItem();
            jMenuItemExit.setPreferredSize(new Dimension(40, 20));
            jMenuItemExit.setFont(new Font("Dialog", Font.PLAIN, 12));
            jMenuItemExit.setText("Exit");
            jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return jMenuItemExit;
    }

    public JPanel getJCPaneRun( MainScreen msc) {
        jLabel2 = new JLabel();
        jLabel2.setBounds(new Rectangle(58, 208, 124, 16));
        jLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
        jLabel2.setText("Simulation Progress");

        jCPaneRun = new JPanel();
        jCPaneRun.setLayout(null);
        jCPaneRun.setSize(new Dimension(192, 357));
        jCPaneRun.add(getJTxtArRun(), null);
        jCPaneRun.add(jLabel, null);
        jCPaneRun.add(getJProgressBar(), null);
        jCPaneRun.add(jLabel2, null);
        jCPaneRun.add(getJBtRun(msc), null);
        return jCPaneRun;
    }

    
    private JTextArea getJTxtArRun() {
        jTxtArRun = new JTextArea();
        jTxtArRun.setBounds(new Rectangle(35, 46, 190, 145));
        jTxtArRun.setWrapStyleWord(true);
        jTxtArRun.setFont(new Font("Dialog", Font.PLAIN, 12));
        jTxtArRun.setEditable(false);
        jTxtArRun.setForeground(Color.blue);
        jTxtArRun.setText("");
        jTxtArRun.setLineWrap(true);
        return jTxtArRun;
    }

// !!! re-enable me
    private JProgressBar getJProgressBar() {
        jProgressBar = new JProgressBar();
        jProgressBar.setBounds(new Rectangle(9, 231, 233, 21));
        jProgressBar.setString("ProgressBar");
        jProgressBar.setFont(new Font("Dialog", Font.PLAIN, 12));
        jProgressBar.setStringPainted(true);
        return jProgressBar;
    }

    
    private JButton getJBtRun( final MainScreen msc ) {
        jBtRun = new JButton();
        jBtRun.setBounds(new Rectangle(154, 321, 87, 25));
        jBtRun.setFont(new Font("Dialog", Font.PLAIN, 12));
        jBtRun.setText("Stop");
        jBtRun.addActionListener(new java.awt.event.ActionListener() {

            				@SuppressWarnings("deprecation")
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (jBtRun.getText().equals("Stop")) {
//						task.stop(); // Sun is mad
//						st.stop();
//                    task = null;
//                    st = null;
                    jBtRun.setText("Close");
                    jTxtArRun.setText(jTxtArRun.getText() + "Simulation interrupted.\n");
//                    jBtPause.setEnabled(false);
                } else {
//                    rn.setVisible(false);
                    msc.setVisible(true);
                }
            }
        });
        return jBtRun;
    }
}
