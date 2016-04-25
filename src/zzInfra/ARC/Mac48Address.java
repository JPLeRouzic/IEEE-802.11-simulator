/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzInfra.ARC;

/**
 * @author jean-Pierre Le Rouzic
 */
public class Mac48Address {
    public byte[] mac_addr;

    public Mac48Address() {
        mac_addr = new byte[6];
    }

    
    public static Mac48Address intToMac(int value) {
        Mac48Address b = new Mac48Address();

        for (int i = 0; i < 6; i++) {
            int offset = (b.mac_addr.length - 1 - i) * 8;
            b.mac_addr[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static Mac48Address StringToMac(String value) {
        // for unformatted addresses (not as in EA.12.A5.8F.D6.5F)
        int res ;
        
        res = Integer.parseInt(value) ;
        return intToMac(res) ;
    }

    public static int MacToint(Mac48Address value) {
        int res = 0 ;

        for (int i = 0; i < 4; i++) 
            {
          //  int offset = i * 8;
            res = value.mac_addr[0]  ;
            res = res + value.mac_addr[1] <<  8 ;
            res = res + value.mac_addr[2] << 16 ;
            res = res + value.mac_addr[3] << 24 ;
            res = res + value.mac_addr[4] << 32 ;
            res = res + value.mac_addr[5] << 48 ;
          }
        return res ;
    }

    public static boolean equal( Mac48Address a,  Mac48Address b) {
        for (int i = 0; i < 4; i++) {
            if (a.mac_addr[i] != b.mac_addr[i]) {
                return false;
            }
        }
        return true;
    }
}
