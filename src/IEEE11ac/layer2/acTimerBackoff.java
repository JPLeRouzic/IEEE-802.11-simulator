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

package IEEE11ac.layer2;

import java.util.Random;
import IEEE11ac.station.acStation;

import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JETimer;
import zzInfra.statistics.JERandomVar;
import zzInfra.gui.JE802Gui;

public final class acTimerBackoff extends JETimer {

	private int theCW;

	private int theAC;

	private JETime aSlotTime;

	private int theStationId;

	private JE802Gui theUniqueGui;

	private JERandomVar theRandomVar;

	private acBackoffEntity be;

	/**
	 * @param aScheduler
	 * @param aGenerator
	 * @param aGui
	 * @param aStaId
	 * @param anEventName
	 * @param aHandlerId
	 * @param aSlotTime
	 */
	public acTimerBackoff(JEEventScheduler aScheduler, Random aGenerator, JE802Gui aGui, int aStaId, int anAC,
			String anEventName, int aHandlerId, JETime aSlotTime, acBackoffEntity be) {

		super(aScheduler, aGenerator, anEventName, aHandlerId);
		this.theAC = anAC;
		this.theStationId = aStaId; // needed for Gui
		theUniqueGui = aGui;
		this.aSlotTime = aSlotTime;
		this.theRandomVar = null;
		this.theRemainingInterval = new JETime(0.0);
		// only for gui
		this.be = be;
	}

	public void start(acStation sta, int aCW, boolean cca_busy) {
		this.theCW = aCW;
		this.theStartTime = theUniqueEventScheduler.now();
		// calculate backoff
		this.theRandomVar = new JERandomVar(theUniqueRandomGenerator, "Uniform", 0.0, this.theCW / 2.0, this.theCW / 4);

		this.theRemainingInterval = this.aSlotTime.times(Math.round(this.theRandomVar.nextvalue()));
		int aRemainingWindow = (int) this.theRemainingInterval.dividedby(this.aSlotTime);
		this.theRemainingInterval = this.aSlotTime.times(aRemainingWindow);
		JETime aCWInterval = this.aSlotTime.times(this.theCW);

		// start backoff and schedule end gui
		super.start(this.theRemainingInterval);
		// however, if medium is busy (indicated by cca_busy, cca = clear
		// channel assessment), immediately pause the backoff
		if (cca_busy) {
			this.pause();
		} else {
			if (theUniqueGui != null)
				theUniqueGui.addBackoff(theUniqueEventScheduler.now(), this.theRemainingInterval, this.theStationId, this.theAC,
						aRemainingWindow + "", new Integer(this.theCW), aCWInterval, sta.getPhy().getCurrentChannelNumberRX());
		}
	}

	public void resume(acStation sta) {
		if (!this.is_paused()) {
			this.error("backoff timer must be paused to be resumed.");
		}
		this.theRemainingInterval = this.theRemainingInterval.minus(this.aSlotTime);
		if (this.theRemainingInterval.isEarlierThan(new JETime(0.0))) {
			this.theRemainingInterval.setTime(0.0);
		}
		int aRemainingWindow = (int) this.theRemainingInterval.dividedby(this.aSlotTime);
		JETime aCWInterval = this.aSlotTime.times(this.theCW);
		if (theUniqueGui != null)
			theUniqueGui.addBackoff(theUniqueEventScheduler.now(), this.theRemainingInterval, this.theStationId, this.theAC,
					aRemainingWindow + "", this.theCW, aCWInterval, sta.getPhy().getCurrentChannelNumberRX());
		super.resume();
	}
}
