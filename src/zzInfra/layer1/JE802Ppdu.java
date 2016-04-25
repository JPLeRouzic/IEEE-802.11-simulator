/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */

package zzInfra.layer1;

import zzInfra.kernel.JEmula;
import zzInfra.layer2.JE802Mpdu;

public abstract class JE802Ppdu extends JEmula {

	protected JE802Mpdu theMpdu;

	protected final double theTxPower_mW;

	protected final int theChannelNumber;

	protected boolean isJammed;

	public JE802Ppdu(double aTxPower, int aChannel) {
		this.theTxPower_mW = aTxPower;
		this.theChannelNumber = aChannel;
		this.isJammed = false;
	}

	public void jam() {
		this.isJammed = true;
	}

	public boolean isJammed() {
		return this.isJammed;
	}

	@Override
	public abstract String toString();

	@Override
	public abstract JE802Ppdu clone();

	public Integer getChannelNumber() {
		return theChannelNumber;
	}

	@Override
	public void display_status() {
		System.out.println("=========== JEmula object (" + this.getClass().toString() + ") ==========");
		System.out.println("  - TxPower [mW]:     " + this.theTxPower_mW);
		System.out.println("  - ChannelNumber:    " + this.theChannelNumber);
		System.out.println("  - is jammed:        " + this.isJammed);
		System.out.println("  - PAYLOAD-----:     ");
		this.theMpdu.display_status();
		System.out.println("=======================================================");
	}

	public JE802Mpdu getMpdu() {
		return theMpdu;
	}

}
