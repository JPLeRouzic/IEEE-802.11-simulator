/*
 * 
 * This is jemula.
 *
 *    Copyright (c) 2006-2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
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

package zzInfra.kernel;

import java.util.Random;

/**
 * @author Stefan Mangold
 * 
 */
public class JETimer extends JEEventHandler {

	protected JETime theStartTime;

	protected JETime theResumeTime;

	protected JETime theElapsedInterval;

	protected JETime theRemainingInterval;

	protected String theFireEventName;

	protected JEEvent theExpiryIndicator;

	protected Integer theTargetHandlerId;
	
	private JEEvent fireEvent;

	protected enum timerstate {
		idle, active, paused
	}

	protected timerstate theTimerState;

	public JETimer(JEEventScheduler aScheduler, Random aGenerator, String anEventName, Integer aHandlerId) {
		super(aScheduler, aGenerator);
		this.theFireEventName = anEventName;
		this.reset();
		this.theTargetHandlerId = aHandlerId;
		this.fireEvent = null;
	}
	
	public JETimer(JEEventScheduler aScheduler, Random aGenerator, JEEvent anEvent, Integer aHandlerId) {
		super(aScheduler, aGenerator);
		this.theFireEventName = anEvent.getName();
		this.fireEvent = anEvent;
		this.reset();
		this.theTargetHandlerId = aHandlerId;
	}

	/**
	 * @param aRemainingTime
	 */
	
	public void start(JETime aRemainingTime) {
		JETime now = this.theUniqueEventScheduler.now();
		switch (this.theTimerState) {
		case active:
			theUniqueEventScheduler.cancel_event(this.theExpiryIndicator); // because this event will be updated
			//$FALL-THROUGH$
		case idle:				
			this.theStartTime = now;
			this.theResumeTime = now;
			this.theRemainingInterval = aRemainingTime;
			this.theElapsedInterval = new JETime(0.0);
			this.theExpiryIndicator = new JEEvent("timer_expired_ind", this.getHandlerId(), now.plus(aRemainingTime));
			this.send(this.theExpiryIndicator);
			this.theTimerState=timerstate.active;
			break;
		case paused:
			this.error("timer cannot be started while paused. Use 'resume_req' to restart.");
			break;
		default:
			break;
		}
	}

	public void stop() {
		theUniqueEventScheduler.cancel_event(this.theExpiryIndicator);
		this.reset();
	}

	public void pause() {
		JETime now = this.theUniqueEventScheduler.now();
		switch (this.theTimerState) {
		case active:
			theUniqueEventScheduler.cancel_event(this.theExpiryIndicator);
			this.theElapsedInterval = this.theElapsedInterval.plus(now.minus(this.theResumeTime));
			this.theRemainingInterval = this.theRemainingInterval.minus(now.minus(this.theResumeTime));
			this.theTimerState = timerstate.paused;
			break;
		case idle:
			this.error("cannot pause idle timer.");
			break;
		default:
			break;
		}
	}
	
	public void resume() {
		JETime now = this.theUniqueEventScheduler.now();
		theUniqueEventScheduler.cancel_event(this.theExpiryIndicator);
		this.theResumeTime = now;
		this.theExpiryIndicator = new JEEvent("timer_expired_ind", this.getHandlerId(), now.plus(this.theRemainingInterval));
		this.send(this.theExpiryIndicator);
		this.theTimerState = timerstate.active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEEventHandler#event_handler(jemula.kernel.JEEvent)
	 */
	@Override
	public void event_handler(JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		String anEventName = anEvent.getName();
		
		if (this.theTimerState == timerstate.active && anEventName.equals("timer_expired_ind")){
			if (fireEvent == null) {
				this.send(new JEEvent(this.theFireEventName, this.theTargetHandlerId, now));
			} else {
				fireEvent.setTheScheduledTime(now);
				this.send(fireEvent);
			}				
			this.reset();
			this.theTimerState = timerstate.idle;
		} else {
			this.error("undefined event '" + anEventName + "' in state " + this.theTimerState.toString());
		}
	}

	/**
	 * 
	 */
	private void reset() {
		this.theStartTime = new JETime(0.0);
		this.theResumeTime = new JETime(0.0);
		this.theRemainingInterval = new JETime(0.0);
		this.theElapsedInterval = new JETime(0.0);
		this.theTimerState = timerstate.idle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEEventHandler#is_active()
	 */
	@Override
	public boolean is_active() {
		return this.theTimerState.equals(timerstate.active);
	}

	/**
	 * @return boolean
	 */
	public boolean is_paused() {
		return this.theTimerState == timerstate.paused;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEEventHandler#is_idle()
	 */
	@Override
	public boolean is_idle() {
		return this.theTimerState == timerstate.idle;
	}

	public JETime getRemainingInterval() {
		this.warning("JETimer.getRemainingInterval() does ***not*** return time until firing. For this, use JETime.getExpiryTime()");
		return this.theRemainingInterval;
	}

	public JETime getExpiryTime() {
		if (this.theExpiryIndicator != null) {
			return this.theExpiryIndicator.getScheduledTime();
		}
		return new JETime(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEmula#display_status()
	 */
	@Override
	public void display_status() {

		System.out.println("=========== JEmula object (" + this.getClass().toString() + ") ==========");
		System.out.println("  - now:            " + theUniqueEventScheduler.now().toString());
		System.out.println("  - start:          " + this.theStartTime.toString());
		System.out.println("  - elapsed:        " + this.theElapsedInterval.toString());
		System.out.println("  - resume:        " + this.theResumeTime.toString());
		System.out.println("  - remaining:      " + this.theRemainingInterval.toString());
		System.out.println("  - fire event:     " + this.theFireEventName.toString());
		System.out.println("  - target handler: " + this.theTargetHandlerId.toString());
		System.out.println("  - timer state:    " + this.theTimerState.toString());
		if (this.theExpiryIndicator != null) {
			System.out.println("  - expiry:         " + this.theExpiryIndicator.toString());
		}
		super.display_status();
		System.out.println("=======================================================");
	}
}