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
import java.util.Vector;

/**
 * @author Stefan Mangold
 */
public abstract class JEEventHandler extends JEmula {

	private static int theUniqueHandlerId = 1;

	private Integer HandlerId;

	protected JEEvent theLastRxEvent;

	protected JEEvent theLastTxEvent;

	public state theState;

	protected Vector<Object> parameterlist;

	protected enum state {
		idle, active
	}

	/**
	 * @param aScheduler
	 * @param aGenerator
	 */
	public JEEventHandler(JEEventScheduler aScheduler, Random aGenerator) {
		super();
		theUniqueHandlerId++;
		this.HandlerId = theUniqueHandlerId;
		this.theState = state.idle; // state machines are helpful for event handlers
		this.theLastRxEvent = null;
		this.theLastTxEvent = null;
		this.parameterlist = new Vector<Object>(); // for exchanging events
		this.theUniqueRandomGenerator = aGenerator;
		this.theUniqueEventScheduler = aScheduler;
		this.theUniqueEventScheduler.register_handler(this); // handler and scheduler now know each other
	}
	
	public state getState() {
		return theState;
	}
	
	public Integer getHandlerId() {
		return HandlerId;
	}
	
	/**
	 * @return boolean
	 */
	public boolean is_active() {
		return this.theState.equals(state.active);
	}

	/**
	 * @return boolean
	 */
	public boolean is_idle() {
		return this.theState.equals(state.idle);
	}

	public void end_of_emulation() {
		// the scheduler calls all handlers at the end of emulation.
		this.message("shutting down.", 0);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEmula#message(java.lang.String, int)
	 */
	@Override
	protected void message(String aLine, int aLevel) {
		super.message(aLine, aLevel);
	}

	/**
	 * @param anEvent
	 */
	public void event_handler(JEEvent anEvent) {

		this.message("WARNING: operation `event_handler` not yet defined for this class, dude!", 1);

		String anEventName = anEvent.getName();

		if (anEventName.equalsIgnoreCase("Test")) {
			this.message(this.toString() + " received test event @ time " + theUniqueEventScheduler.now().toString(), 10);
		}
	}

	/**
	 * @param anEvent
	 */
	public void send(JEEvent anEvent) {
		anEvent.setSourceHandlerId(this.HandlerId);
		this.theLastTxEvent = anEvent;
		theUniqueEventScheduler.queue_event(anEvent);
	}

	/**
	 * @param anEventName
	 * @param anEventHandler
	 */
	public void send(String anEventName, JEEventHandler anEventHandler) {
		JEEvent anEvent = new JEEvent(anEventName, anEventHandler.getHandlerId(), theUniqueEventScheduler.now());
		anEvent.setSourceHandlerId(this.HandlerId);
		this.theLastTxEvent = anEvent;
		theUniqueEventScheduler.queue_event(anEvent);
	}

	/**
	 * @param anEvent
	 * @param anEventHandler
	 */
	public void send(JEEvent anEvent, JEEventHandler anEventHandler) {
		anEvent.setSourceHandlerId(this.HandlerId);
		anEvent.setTargetHandlerId(anEventHandler.getHandlerId());
		this.theLastTxEvent = anEvent;
		theUniqueEventScheduler.queue_event(anEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEmula#display_status()
	 */
	@Override
	public void display_status() {
		this.message("=========== JEmula object (" + this.getClass().toString() + ") ==========");
		this.message("  - HandlerId:                 " + this.HandlerId);
		this.message("  - state:                     " + this.theState);
		if (this.theLastRxEvent != null)
			this.message("  - theLastReceivedEvent:      " + this.theLastRxEvent.getName());
		if (this.theLastTxEvent != null)
			this.message("  - theLastTransmittedEvent:   " + this.theLastTxEvent.getName());
		super.display_status();
		this.message("=======================================================");
	}
}
