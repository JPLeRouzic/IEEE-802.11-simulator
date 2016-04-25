package gui.conf_screen;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class Panels extends JFrame {

    
//    private final JPanel jPanelPhy = null; // PHY type
    
    private JPanel jPanelRte = null; // MCS and Data Rate
    
    private JPanel jPanelNodes = null; // number of nodes
    
    private JPanel jPanelIntArr = null; // Packet Generation Rate
    
    private JPanel jPanelProp = null; // Propagation model

    /*	802.11ac specific	*/
    
    private JPanel jPanelAnt = null; // number of antennas
    
    private JPanel jPanelMIMO = null; // number of antennas
    
    private JPanel jPanelChan = null; // channel aggregation
    
    private JPanel jPanelChanWidth = null ; // width of channels
    
    private JPanel jPanelMod = null; // type of station implementation in premise

    private JPanel jContentPane = null;
    
    private JLabel jLbl_dist_sta = null;
    
    private JLabel jLblIntArDstr = null;
    
    private JLabel jLblNodes = null; // text part
    
//    private JLabel jLblRoof = null; // text part

    private JLabel jLblMixNodes = null;


    
    public JPanel getJContentPane(MainScreen msc, ConfScrn_main cfgm, int nbagg, int widtha, int widthb, int widthc) {
        if (jContentPane == null) {

            jLbl_dist_sta = new JLabel();
            jLbl_dist_sta.setText("Distance between STAs:");
            jLbl_dist_sta.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLbl_dist_sta.setBounds(new Rectangle(10, 50, 240, 19));

            jLblIntArDstr = new JLabel();
            jLblIntArDstr.setText("Packet Generation Rate Distribution:");
            jLblIntArDstr.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLblIntArDstr.setBounds(new Rectangle(15, 19, 209, 21));

            jLblMixNodes = new JLabel();
            jLblMixNodes.setEnabled(true);
            jLblMixNodes.setBounds(new Rectangle(220, 18, 162, 21));
            jLblMixNodes.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLblMixNodes.setText("Number of APs:");

            jLblNodes = new JLabel();
            jLblNodes.setText("Number of STAs:");
            jLblNodes.setFont(new Font("Dialog", Font.PLAIN, 12));
            jLblNodes.setBounds(new Rectangle(10, 15, 107, 23));

            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(cfgm.bt.getJBtConfOK(msc, cfgm), null);
            jContentPane.add(cfgm.bt.getJBtConfCancel(msc, cfgm), null);

            jContentPane.add(getJPanel_MIMOs(msc.msb, cfgm), null);
            jContentPane.add(getJPanelRte(msc.msb, cfgm), null);
            jContentPane.add(getJPanelNodes(cfgm), null);
            jContentPane.add(getJPanelIntArr(cfgm), null);
            jContentPane.add(getJPanel_propag(cfgm), null);

            jContentPane.add(getJPanel_model(msc.msb, cfgm), null);
            jContentPane.add(getJPanel_channel(msc.msb, cfgm, nbagg, widtha, widthb, widthc), null);
            jContentPane.add(getJPanel_chanWidth(msc.msb, cfgm, widtha, widthb, widthc), null);
            jContentPane.add(getJPanel_antennas(msc.msb, cfgm), null);
        }
        return jContentPane;
    }
    
    // channel aggregation properties
    private JPanel getJPanel_channel( MainScreen_buttons ms, ConfScrn_main cfgm, int nbagg, int widtha, int widthb, int widthc) 
    {
        String chan ;
        
        if (jPanelChan == null) {
            jPanelChan = new JPanel();
            jPanelChan.setLayout(null);
            jPanelChan.setBounds(new Rectangle(175, 110, 150, 65));
            jPanelChan.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Aggregation width", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            if(nbagg < 4)
                System.out.println("error aggregation number") ;
            
            // compute aggregated channel width
            chan = Integer.toString(widtha * 4) ;
            jPanelChan.add(cfgm.bt.getJAggChanA(ms, chan), null); // channel width 80MHz
            chan = Integer.toString(widtha * 8) ;
            jPanelChan.add(cfgm.bt.getJAggChanB(ms, chan), null); // channel width 160MHz
            chan = Integer.toString(widtha * 1) ;
            jPanelChan.add(cfgm.bt.getJAggChanC(ms, chan), null); // channel width 20MHz
            chan = Integer.toString(widtha * 2) ;
            jPanelChan.add(cfgm.bt.getJAggChanD(ms, chan), null); // channel width 40MHz
        }
        return jPanelChan;
    }

     // channel width properties
    private JPanel getJPanel_chanWidth( MainScreen_buttons msb, ConfScrn_main cfgm, int widtha, int widthb, int widthc) 
    {
        String chan ;
        
        if (jPanelChanWidth == null) 
            {
            jPanelChanWidth = new JPanel();
            jPanelChanWidth.setLayout(null);
            jPanelChanWidth.setBounds(new Rectangle(175, 175, 150, 65));
            jPanelChanWidth.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Channel width", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            String tuty =  msb.phyGroup.getSelection().getActionCommand();
            switch (tuty) 
                {
                case "11ac":
                    // compute channel width
                    chan = Integer.toString(widtha) ;
                    jPanelChanWidth.add(cfgm.bt.getJchannelWidthA(msb, chan), null); 
                    break;
                case "11af":
                    // compute channel width
                    chan = Integer.toString(widtha) ;
                    jPanelChanWidth.add(cfgm.bt.getJchannelWidthA(msb, chan), null); // channel width 6MHz
                    // compute channel width
                    chan = Integer.toString(widthb) ;
                    jPanelChanWidth.add(cfgm.bt.getJchannelWidthB(msb, chan), null); // channel width 7MHz
                    // compute channel width
                    chan = Integer.toString(widthc) ;
                    jPanelChanWidth.add(cfgm.bt.getJchannelWidthC(msb, chan), null); // channel width 8MHz
                    break;
                case "11ah":
                    break;
                case "11ax":
                    // compute channel width
                    chan = Integer.toString(widtha) ;
                    jPanelChanWidth.add(cfgm.bt.getJchannelWidthA(msb, chan), null); 
                    break;
                case "LTE-U":
                    break;
                default:
                    System.out.print("/n Incorrect amendment in Panels.java");
                }
            }
        return jPanelChanWidth;
    }

   // propagation model
    private JPanel getJPanel_propag( ConfScrn_main cfgm) {
        if (jPanelProp == null) {
            jPanelProp = new JPanel();
            jPanelProp.setLayout(null);
            jPanelProp.setBounds(new Rectangle(175, 5, 150, 105));
            jPanelProp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Propag. Model", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            jPanelProp.add(cfgm.bt.getLbl_lvl_noise(), null);
            jPanelProp.add(cfgm.bt.getJTxt_amb_noise(), null);
            jPanelProp.add(cfgm.bt.getLbl_prog_heur(), null);
            jPanelProp.add(cfgm.bt.getJTxt_prog_heur(), null);
        }
        return jPanelProp;
    }

    // physical premise properties
    private JPanel getJPanel_model( MainScreen_buttons msb, ConfScrn_main cfgm) {
        if (jPanelMod == null) {
            jPanelMod = new JPanel();
            jPanelMod.setLayout(null);
            jPanelMod.setBounds(new Rectangle(18, 5, 150, 105));
            jPanelMod.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Layout Model", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            jPanelMod.add(cfgm.bt.getJmodelA(msb), null);
            jPanelMod.add(cfgm.bt.getJmodelB(msb), null);
            jPanelMod.add(cfgm.bt.getJmodelC(msb), null);
            jPanelMod.add(cfgm.bt.getJmodelD(msb), null);
        }
        return jPanelMod;
    }

    // number of antennas
    private JPanel getJPanel_antennas( MainScreen_buttons msb, ConfScrn_main cfgm) {
        if (jPanelAnt == null) {
            jPanelAnt = new JPanel();
            jPanelAnt.setLayout(null);
            jPanelAnt.setBounds(new Rectangle(18, 175, 150, 110));
            jPanelAnt.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "MIMO mode", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            jPanelAnt.add(cfgm.bt.getjantenna1(msb), null); // number of antennas
            jPanelAnt.add(cfgm.bt.getjantenna2(msb), null);
            jPanelAnt.add(cfgm.bt.getjantenna4(msb), null);
            jPanelAnt.add(cfgm.bt.getjantenna8(msb), null);

        }
        return jPanelAnt;
    }

    // Kind of MIMO
    private JPanel getJPanel_MIMOs(MainScreen_buttons msb, ConfScrn_main cfgm) {
        if (jPanelMIMO == null) {
            jPanelMIMO = new JPanel();
            jPanelMIMO.setLayout(null);
            jPanelMIMO.setBounds(new Rectangle(18, 110, 150, 65));
            jPanelMIMO.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "MIMO mode", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));

            jPanelMIMO.add(cfgm.bt.getJMIMO_SS(msb), null); 
            jPanelMIMO.add(cfgm.bt.getJMIMO_BF(msb), null);

        }
        return jPanelMIMO;
    }

    // MCS and Data Rate
    private JPanel getJPanelRte( MainScreen_buttons msb, ConfScrn_main cfgm) {
        if (jPanelRte == null) {
            jPanelRte = new JPanel();
            jPanelRte.setLayout(null);
            jPanelRte.setBounds(new Rectangle(330, 195, 412, 86));
            jPanelRte.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray, 1), "Data Rate", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));
            jPanelRte.add(cfgm.bt.getJRbRate0(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate1(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate2(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate3(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate4(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate5(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate6(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate7(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate8(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate9(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate10(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate11(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate12(msb), null);
            jPanelRte.add(cfgm.bt.getJRbRate13(msb), null);
        }
        return jPanelRte;
    }

    
    private JPanel getJPanelNodes( ConfScrn_main cfgm) {
        if (jPanelNodes == null) {
            jPanelNodes = new JPanel();
            jPanelNodes.setLayout(null);
            jPanelNodes.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1), "Nodes", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));
            jPanelNodes.setSize(new Dimension(412, 85));
            jPanelNodes.setLocation(new Point(330, 110));
            jPanelNodes.setFont(new Font("Dialog", Font.PLAIN, 12));
            jPanelNodes.add(jLblMixNodes, null);
            jPanelNodes.add(jLblNodes, null); // text part for nb of STA
            jPanelNodes.add(cfgm.bt.getJTxt_nb_STA_Nodes(), null); // Input box for nb of STA
            jPanelNodes.add(cfgm.bt.getJTxt_nb_AP_Nodes(), null);
            jPanelNodes.add(cfgm.bt.getJTxt_Roof(), null); // Input box for roof
            jPanelNodes.add(jLbl_dist_sta, null); // text part for distance between STAs
            jPanelNodes.add(cfgm.bt.getJTxt_dist_STA(), null); // input box for distance between STAs
        }
        return jPanelNodes;
    }

    // Packet Generation Rate
    private JPanel getJPanelIntArr( ConfScrn_main cfgm) {
        if (jPanelIntArr == null) {
            jPanelIntArr = new JPanel();
            jPanelIntArr.setLayout(null);
            jPanelIntArr.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray, 1), "Packet Generation Rate", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), SystemColor.controlDkShadow));
            jPanelIntArr.setSize(new Dimension(408, 74));
            jPanelIntArr.setLocation(new Point(330, 285));
            jPanelIntArr.add(cfgm.bt.getJCmbDstr(), null);
            jPanelIntArr.add(jLblIntArDstr, null);
        }
        return jPanelIntArr;
    }


}
