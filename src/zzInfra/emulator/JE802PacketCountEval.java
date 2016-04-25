package zzInfra.emulator;

import IEEE11ac.layer2.acMPDU;
import IEEE11ac.layer2.acMPDU.JE80211MpduType;
import zzInfra.layer2.JE802Mpdu;
import zzInfra.layer3_network.JE802IPPacket;
import zzInfra.layer3_network.JE802RERRPacket;
import zzInfra.layer3_network.JE802RREPPacket;
import zzInfra.layer3_network.JE802RREQPacket;
import zzInfra.layer4_transport.JE802TCPPacket;

public class JE802PacketCountEval {

	private long[] mpduCounts;

	private long[] ipCounts;

	private long[] tcpCounts;

	public JE802PacketCountEval() {
		mpduCounts = new long[4];
		ipCounts = new long[4];
		tcpCounts = new long[2];
	}

	@SuppressWarnings("unused")
	public void endOfEmulation() {

		if (true) {
			// TODO: organize this method with XML scnenario file, write out
			// into file, no system.out !
			return;
		} else {
			long totalPackets = 0;
			for (int i = 0; i < mpduCounts.length; i++) {
				totalPackets += mpduCounts[i];
			}
			long ipSum = 0;
			for (int i = 0; i < ipCounts.length; i++) {
				ipSum += ipCounts[i];
			}
			long tcpSum = 0;
			for (int i = 0; i < tcpCounts.length; i++) {
				tcpSum += tcpCounts[i];
			}
			System.out.println("MAC LAYER:");
			System.out.println("\tDATA: " + mpduCounts[3] + " (" + ((double) mpduCounts[3] / totalPackets) * 100 + "%)");
			System.out.println("\tACK: " + mpduCounts[0] + " (" + ((double) mpduCounts[0] / totalPackets) * 100 + "%)");
			System.out.println("\tRTS: " + mpduCounts[2] + " (" + ((double) mpduCounts[2] / totalPackets) * 100 + "%)");
			System.out.println("\tCTS: " + mpduCounts[1] + " (" + ((double) mpduCounts[1] / totalPackets) * 100 + "%)");
			System.out.println("IP LAYER:");
			System.out.println("\tDATA: " + ipCounts[3] + " (" + ((double) ipCounts[3] / ipSum) * 100 + "%)");
			System.out.println("\tRREQ: " + ipCounts[0] + " (" + ((double) ipCounts[0] / ipSum) * 100 + "%)");
			System.out.println("\tRREP: " + ipCounts[1] + " (" + ((double) ipCounts[1] / ipSum) * 100 + "%)");
			System.out.println("\tRERR: " + ipCounts[2] + " (" + ((double) ipCounts[2] / ipSum) * 100 + "%)");
			System.out.println("TCP LAYER");
			System.out.println("\tDATA: " + tcpCounts[1] + " (" + ((double) tcpCounts[1] / tcpSum) * 100 + "%)");
			System.out.println("\tACK: " + tcpCounts[0] + " (" + ((double) tcpCounts[0] / tcpSum) * 100 + "%)");
		}
	}

	public void addTransmittedPacket(JE802Mpdu aMpdu) {
		JE80211MpduType type = (JE80211MpduType) aMpdu.getType();
		switch (type) {
		case ack:
			mpduCounts[0]++;
			break;
		case cts:
			mpduCounts[1]++;
			break;

		case rts:
			mpduCounts[2]++;
			break;
		case data:
			mpduCounts[3]++;
			JE802IPPacket ipPacket = aMpdu.getPayload();
			if (ipPacket != null) {
				addIpCounts(ipPacket);
			}
			break;
		default:
			break;
		}
	}

	private void addIpCounts(JE802IPPacket packet) {
		if (packet instanceof JE802RREQPacket) {
			ipCounts[0]++;
		} else if (packet instanceof JE802RREPPacket) {
			ipCounts[1]++;
		} else if (packet instanceof JE802RERRPacket) {
			ipCounts[2]++;
		} else {
			ipCounts[3]++;
			JE802TCPPacket tcpPacket = packet.getPayload();
			if (tcpPacket != null) {
				addTcpCounts(tcpPacket);
			}
		}
	}

	private void addTcpCounts(JE802TCPPacket tcpPacket) {
		if (tcpPacket.isAck()) {
			tcpCounts[0]++;
		} else {
			tcpCounts[1]++;
		}
	}
}
