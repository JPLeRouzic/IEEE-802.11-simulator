package IEEE11af.layer1;

import zzInfra.layer1.JE802Ppdu;
import IEEE11af.layer2.afMPDU;

public class afPPDU extends JE802Ppdu {

	public afPPDU(afMPDU aMpdu, double aTxPower, int aChannel) {
		super(aTxPower, aChannel);
		this.theMpdu = aMpdu;
	}

	@Override
	public String toString() {
		// PPDU(SA:00->DA:00/AC:00/NO:/type)
		return (theUniqueEventScheduler.now().toString() + " TX PPDU(" + (this.theMpdu.getTxTime()).toString() + " until:"
				+ (theUniqueEventScheduler.now().plus(this.theMpdu.getTxTime())).toString() + "/Channel:" + this.theChannelNumber
				+ "/Power:" + this.theTxPower_mW + "/" + this.theMpdu.toString() + ")");
	}

	@Override
	public afMPDU getMpdu() {
		return (afMPDU) theMpdu;
	}

	public void setMpdu(afMPDU theMpdu) {
		this.theMpdu = theMpdu;
	}

	@Override
	public afPPDU clone() {
		afPPDU aCopy = new afPPDU(getMpdu(), theTxPower_mW, theChannelNumber);
		aCopy.isJammed = isJammed;
		return aCopy;
	}
}
