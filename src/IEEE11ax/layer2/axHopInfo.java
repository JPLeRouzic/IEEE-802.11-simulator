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

package IEEE11ax.layer2;

import zzInfra.layer3_network.JE802HopInfo;

public final class axHopInfo extends JE802HopInfo {

    public axHopInfo(int address, int channel) {
        super(address, channel);
    }

    @Override
	public int getAddress() {
		return address;
	}

    @Override
	public int getChannel() {
		return channel;
	}

	public boolean equalsAddr(axHopInfo otherInfo) {
		return (this.address == otherInfo.getAddress());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof axHopInfo) {
			axHopInfo info = (axHopInfo) obj;
			return (info.getAddress() == address && info.getChannel() == channel);
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + address + "," + channel + ")";
	}

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

}
