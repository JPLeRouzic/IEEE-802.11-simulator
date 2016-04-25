/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IEEE11af.station.ARC;

import zzInfra.ARC.JE802Station;
import zzInfra.ARC.Mac48Address;


/**
 * @author jean-Pierre Le Rouzic
 */
public class AP {
    Mac48Address mac_address;
    Mac48Address ssid;
    Mac48Address bssid;
    JE802Station myself;
    JE802Station mysta[];

    void Set_SSID( String pssid) {
        ssid.mac_addr = pssid.getBytes();
    }

    void Set_BSSID(Mac48Address address) {
        bssid = address;
    }

    String Get_SSID() {
        return ssid.toString();
    }

    Mac48Address Get_BSSID() {
        return bssid;
    }

}
