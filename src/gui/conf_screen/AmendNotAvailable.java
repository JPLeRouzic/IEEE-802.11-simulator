package gui.conf_screen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;

public class AmendNotAvailable extends JFrame {

    
   private Frame mainFrame;
   private TextArea statusLabel1;
//   private Label statusLabel2;
//   private Label statusLabel3;

   public AmendNotAvailable(){
      prepareGUI();
   }

   private void prepareGUI(){
      mainFrame = new Frame("Not available in community version");
      mainFrame.setSize(400,200);

      mainFrame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent windowEvent){
            System.exit(0);
         }        
      });    

      statusLabel1 = new TextArea();        
      statusLabel1.setSize(350,50);
/*
      statusLabel2 = new Label();        
      statusLabel2.setSize(350,10);

      statusLabel3 = new Label();        
      statusLabel3.setSize(350,10);
*/
      statusLabel1.setText("Not available in current implementation" +
      "\nplease contact jeanpierre.lerouzic@yahoo.no" +
      "\nfor more information") ;
      
      mainFrame.add(statusLabel1);
   }

   public void showPopupMenuDemo()
    {
    statusLabel1.setLocation(10, 20);
//    mainFrame.setEditable(false);   //Prevents editing  
//    mainFrame.setEnabled(false);   //Stops cutting and pasting
    mainFrame.setVisible(true);
    }
}

