package IEEE11ac.layer1;

import zzInfra.layer1.JE802Ppdu;
import IEEE11ac.layer2.acMPDU;

public class acPPDU extends JE802Ppdu {

	public acPPDU(acMPDU aMpdu, double aTxPower, int aChannel) {
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
	public acMPDU getMpdu() {
		return (acMPDU) theMpdu;
	}

	public void setMpdu(acMPDU theMpdu) {
		this.theMpdu = theMpdu;
	}

	@Override
	public acPPDU clone() {
		acPPDU aCopy = new acPPDU(getMpdu(), theTxPower_mW, theChannelNumber);
		aCopy.isJammed = isJammed;
		return aCopy;
	}
}
