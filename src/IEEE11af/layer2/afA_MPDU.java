/*
/*
	 A-MPDU frames require the use of the "block acknowledge" that was introduced in 802.11e and has been optimized in 802.11n.
	 These protocol extensions enable to increase the physical layer (PHY) data rate, but can't be used to communicate with other pre-802.11n stations. With pre-802.11n stations the classic ACK should be used to confirm an A-afMSDU, but block acknowledge should be used to confirm reception of the aggregated MPDU.
	 MPDU aggregation does not require that all frames that construct up the aggregated frame, should have the same destination address. However, this result in the same behavior as in afMSDU aggregation, since the destination of all frames sent by the station are to the access point of this station, where frames are then forwarded to the final destination.
	 With MPDU aggregation, it is possible to encrypt each frame independently of others, using the security association SA for each destination MAC address. There is no effective difference in encryption with respect to afMSDU aggregation, because all frames sent by a station are encrypted by using the access point security association, and all frames sent by the access point are encrypted by using the security association to the station that will receive the frame.
	 In a similar manner to an afMSDU aggregation, the MPDU aggregation requires that all constituent frames have the same level of QoS.
	 The effectiveness of the MPDU aggregation method is less than the afMSDU aggregation method for short and medium size clusters, due to the additional number of headers of each MAC frame embedded in the aggregated MPDU. Efficiency is further reduced if frames are encrypted. Encryption adds an overlay to each MPDU of the aggregated MPDU then an aggregated afMSDU which is encrypted has only one of those overlays.
	 However, MPDU aggregation is the preferred schema when large amounts of data are available for the aggregation.
	 In 802.11ac all physical frames must be aggregated, even if there is only a single physical frame. This is necessary because the PHY layer does not any more contains the size but only the number of OFDM symbols, and one MPDU frame contains only the duration and not the size.

	 3.3.2.3 format A-MPDU 
	 The format A-MPDU in 802.11ac is an extension of the 802.11n A-MPDU. A-MPDU maximum length in an 802.11ac PPDU is 1.048.576 bytes.	
*/
package IEEE11af.layer2;

import java.util.Vector;

/**
 * @author jean-Pierre Le Rouzic
 */
public class afA_MPDU {

    private int m_currentNbMpdu;
    private int m_maxSize = 1000000; // FIXME should not be declared here
    private int m_maxNumberOfMpdu = 700; // FIXME should not be declared here
    private boolean m_maxTxop;
    private int m_currentAMpduSize;

    public boolean Aggregate(Vector<afMSDU> msdu) {
        boolean ok = true;

        return ok;
    }


    public void Deaggregate(Vector<afMSDU> aggregatedPacket) {

    }

    private void AddPadding(Vector paddingSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class AmpduSubframeHeader {

        public AmpduSubframeHeader() {
        }
    }

}
