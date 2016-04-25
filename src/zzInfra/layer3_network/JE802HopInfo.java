/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package zzInfra.layer3_network;

/**
 *
 * @author Gengis
 */
public class JE802HopInfo {

 	public final int address;

	public final int channel;

	public JE802HopInfo(int address, int channel) {
		this.address = address;
		this.channel = channel;
	}

	public int getAddress() {
		return address;
	}

	public int getChannel() {
		return channel;
	}

	public boolean equalsAddr(JE802HopInfo otherInfo) {
		return (this.address == otherInfo.getAddress());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JE802HopInfo) {
			JE802HopInfo info = (JE802HopInfo) obj;
			return (info.getAddress() == address && info.getChannel() == channel);
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + address + "," + channel + ")";
	}   
}
