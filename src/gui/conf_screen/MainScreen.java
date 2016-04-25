package gui.conf_screen;

import javax.swing.*;
import java.awt.*;
import std_def_XML.XMLGlobalConf;

public class MainScreen extends JFrame {
    
    public MainScreen_buttons msb;

    private static final long serialVersionUID = 1L;
    
    private XMLGlobalConf mk = null;
    
    private JPanel jMainScreenPane = null;


    public MainScreen() {
        super();

        msb = new MainScreen_buttons();
        mk = new XMLGlobalConf(this);
        initialize(mk);
    }

    public void initialize(final XMLGlobalConf mk) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(650, 400);
        this.setResizable(false);
        this.setLocation(new Point((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(this.msb.getJJMenuBar(this));
        this.setContentPane(this.getJContentPane(mk));
        this.setTitle("WLAN Simulator");
    }

    public void CheckAndStart() {
            if (this.msb.jBtConf.isEnabled()) {
                this.mk.configureGlobally();
            }
            MainScreen.this.setVisible(false);
        
    }

    public JPanel getJContentPane( final XMLGlobalConf mk) {
        if (jMainScreenPane == null) {
            // 
            jMainScreenPane = new JPanel();
            jMainScreenPane.setLayout(null);
            jMainScreenPane.setName("");
            
            // "Run Simulation" button
            jMainScreenPane.add(msb.getJBtnRun(this), null);
            
            // exit button
            jMainScreenPane.add(msb.getJButton3(), null);
            
            // panel for choosing which wireless standard
            jMainScreenPane.add(msb.getJPanelWstd( this, mk), null);
            
            // panel containing the "Nodes Configuration radio buttons and conf screen
            jMainScreenPane.add(msb.getJPanelNtConf(this, mk), null);
            
            // "Use XML file" button
            jMainScreenPane.add(msb.getJBtXML(this), null);
        }
        return jMainScreenPane;
    }
        
}  
