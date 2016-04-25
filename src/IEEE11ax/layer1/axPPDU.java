package IEEE11ax.layer1;

import zzInfra.layer1.JE802Ppdu;
import IEEE11ax.layer2.axMPDU;

public class axPPDU extends JE802Ppdu {

	public axPPDU(axMPDU aMpdu, double aTxPower, int aChannel) {
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
	public axMPDU getMpdu() {
		return (axMPDU) theMpdu;
	}

	public void setMpdu(axMPDU theMpdu) {
		this.theMpdu = theMpdu;
	}

	@Override
	public axPPDU clone() {
		axPPDU aCopy = new axPPDU(getMpdu(), theTxPower_mW, theChannelNumber);
		aCopy.isJammed = isJammed;
		return aCopy;
	}
}
