/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package IEEE11ac.layer1;

import zzInfra.kernel.JEEvent;

/**
 *
 * @author jyul5586
 */
public class acRateAdaptation {

    public void event_increase_phy_mode_ind(JEEvent anEvent, acPhy phy) {
        if (phy.equals("256QAM34")) 
        {
            phy.setCurrentPhyMCS("256QAM56");
        } else if (phy.equals("64QAM56")) {
            phy.setCurrentPhyMCS("256QAM34");
        } else if (phy.equals("64QAM34")) {
            phy.setCurrentPhyMCS("64QAM56");
        } else if (phy.equals("64QAM23")) {
            phy.setCurrentPhyMCS("64QAM34");
        } else if (phy.equals("16QAM34")) {
            phy.setCurrentPhyMCS("64QAM23");
        } else if (phy.equals("16QAM12")) {
            phy.setCurrentPhyMCS("16QAM34");
        } else if (phy.equals("QPSK34")) {
            phy.setCurrentPhyMCS("16QAM12");
        } else if (phy.equals("QPSK12")) {
            phy.setCurrentPhyMCS("QPSK34");
        } else if (phy.equals("BPSK12")) {
            phy.setCurrentPhyMCS("QPSK12");
        } else {
            System.out.print("\n Incorrect PhyMCS");
        }
    }

    public void event_reduce_phy_mode_ind(JEEvent anEvent, acPhy phy) {
        switch (phy.toString()) {
            case "256QAM56":
                phy.setCurrentPhyMCS("256QAM34");
                break;
            case "256QAM34":
                phy.setCurrentPhyMCS("64QAM56");
                break;
            case "64QAM56":
                phy.setCurrentPhyMCS("64QAM34");
                break;
            case "64QAM34":
                phy.setCurrentPhyMCS("64QAM23");
                break;
            case "64QAM23":
                phy.setCurrentPhyMCS("16QAM34");
                break;
            case "16QAM34":
                phy.setCurrentPhyMCS("16QAM12");
                break;
            case "16QAM12":
                phy.setCurrentPhyMCS("QPSK34");
                break;
            case "QPSK34":
                phy.setCurrentPhyMCS("QPSK12");
                break;
            case "QPSK12":
                phy.setCurrentPhyMCS("BPSK12");
                break;
            default:
                System.out.print("\n Incorrect PhyMCS");
                break;
        }
    }
    
}
