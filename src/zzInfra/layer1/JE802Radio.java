package zzInfra.layer1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import zzInfra.kernel.JEmula;

public class JE802Radio extends JEmula {

	private BufferedReader TxInStream;

	private BufferedWriter TxOutStream;

	private int theMacAddress;

	private JE802GnuRadioRxThread theRxReader;

	public JE802Radio(int aMacAddress) {

		this.theMacAddress = aMacAddress;

		try {
			Socket TxSocket = new Socket("localhost", 12345);
			this.TxInStream = new BufferedReader(new InputStreamReader(TxSocket.getInputStream()));
			this.TxOutStream = new BufferedWriter(new OutputStreamWriter(TxSocket.getOutputStream()));

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.theRxReader = new JE802GnuRadioRxThread(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		message("connection established.");
	}

	public double getCca_mW() {

		double cca_mW = 0.0;

		try {
			this.TxOutStream.write("sense\n");
			this.TxOutStream.flush();
			message("cca sensing", 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			String cca = this.TxInStream.readLine();
			cca_mW = new Double(cca);
			message("cca[mW]: " + cca + " || cca[dBm]: " + (10 * Math.log10(cca_mW)), 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cca_mW;
	}

	public void tx(JE802Ppdu aPpdu) {
		String aFrameBody = new String("JEMULA802FRAME:" + "|TxTime=" + aPpdu.getMpdu().getTxTime().toString() + "|ChannelNum="
				+ aPpdu.getChannelNumber().toString() + "|SA=" + aPpdu.getMpdu().getSA() + "|Type="
				+ aPpdu.getMpdu().getType().toString() + "|PhyMCS=" + aPpdu.getMpdu().getPhyMcs().toString() + "|DA="
				+ aPpdu.getMpdu().getDA());
		try {
			this.TxOutStream.write(aFrameBody);
			this.TxOutStream.flush();
			message(aFrameBody, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class JE802GnuRadioRxThread extends Thread {

	private final BufferedReader RxInStream;

	public JE802GnuRadioRxThread(JE802Radio theRealPhy) throws IOException {
		Socket RxSocket = new Socket("localhost", 54321);
		this.RxInStream = new BufferedReader(new InputStreamReader(RxSocket.getInputStream()));
	}

	@Override
	public void run() {

		String line;
		while (true) {
			try {
				line = this.RxInStream.readLine();
				System.out.println(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}