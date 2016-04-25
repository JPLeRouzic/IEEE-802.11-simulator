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

import java.util.Vector;

/**
 * @author Stefan Mangold
 */
public final class JEEvent extends JEmula implements Comparable<JEEvent> {

	private String name;

	private Integer theTargetHandlerId;

	private Integer theSourceHandlerId;

	private JETime theScheduledTime;

	private Vector<Object> theParameterList;

	/**
	 * @param aName
	 * @param aHandlerId
	 * @param aScheduledTime
	 * @param aParameterList
	 */
	@SuppressWarnings("unchecked")
	public JEEvent(String aName, Integer aHandlerId, JETime aScheduledTime, Vector<Object> aParameterList) {
		this.name = aName; // name of the event
		this.theTargetHandlerId = aHandlerId;
		this.theSourceHandlerId =0; // handler from which the event is sent
		this.theScheduledTime = aScheduledTime; // time when the event occurs
		if (aParameterList.size() > 0) {
			this.theParameterList = new Vector(aParameterList); // an event usually comes with some parameters
		} else {
			this.theParameterList = null; // parameter list must not be defined, default is then an empty list
		}
	}

	/**
	 * @param aName
	 * @param aHandler
	 * @param aScheduledTime
	 * @param aParameterList
	 */
	@SuppressWarnings("unchecked")
	public JEEvent(String aName, JEEventHandler aHandler, JETime aScheduledTime, Vector aParameterList) {
		Integer aHandlerId = aHandler.getHandlerId();
		this.name = aName; // name of the event
		this.theTargetHandlerId = aHandlerId;
		this.theSourceHandlerId = 0;
		this.theScheduledTime = aScheduledTime; // time when the
		// event occurs
		if (aParameterList.size() > 0) {
			this.theParameterList = new Vector(aParameterList); // an event usually comes with some parameters
		} else {
			this.theParameterList = null; // paramterlist must not be defined, default is then an  empty list
		}
	}

	/**
	 * @param aName
	 * @param aHandlerId
	 * @param aScheduledTime
	 */
	public JEEvent(String aName, Integer aHandlerId, JETime aScheduledTime) {
		this.name = aName; // name of the event
		this.theTargetHandlerId = aHandlerId;
		this.theSourceHandlerId = 0;
		this.theScheduledTime = aScheduledTime; // time when the
		// event occurs
		this.theParameterList = null; // parameter list must not be defined,
		// default is then an empty list
	}

	/**
	 * @param aName
	 * @param aHandler
	 * @param aScheduledTime
	 */
	public JEEvent(String aName, JEEventHandler aHandler, JETime aScheduledTime) {
		Integer aHandlerId = aHandler.getHandlerId();
		this.name = aName; // name of the event
		this.theTargetHandlerId = aHandlerId;
		this.theSourceHandlerId = 0;
		this.theScheduledTime = aScheduledTime; // time when the
		// event occurs
		this.theParameterList = null; // paramterlist must not be defined, default is then an empty list

	}

	/**
	 * @param anEvent
	 */
	@SuppressWarnings("unchecked")
	public JEEvent(JEEvent anEvent) {
		this.name = anEvent.getName();
		this.theTargetHandlerId = anEvent.getTargetHandlerId();
		this.theSourceHandlerId = anEvent.getSourceHandlerId();
		this.theScheduledTime = anEvent.getScheduledTime();
		if (anEvent.getParameterList()!= null) {
			this.theParameterList = new Vector((anEvent.getParameterList()));
		} else {
			this.theParameterList = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEmula#display_status()
	 */
	@Override
	public void display_status() {

		System.out.println("=========== JEmula object (" + this.getClass() + ") ==========");
		System.out.println("  - name:                " + this.name);
		System.out.println("  - target handler id:   " + this.theTargetHandlerId);
		System.out.println("  - source handler id:   " + this.theSourceHandlerId);
		System.out.println("  - scheduled time:      " + this.theScheduledTime);

		if (this.theParameterList != null) {
			Vector list = this.theParameterList;
			for (int cnt = 0; cnt < list.size(); cnt++) {
				System.out.println("  - parameter (" + new Integer(cnt).toString() + "):       " + list.elementAt(cnt).getClass().toString());
			}
		}

		super.display_status();
		System.out.println("=======================================================");
	}

	public String getName() {
		return this.name;
	}
	
	public JETime getScheduledTime() {
		return this.theScheduledTime;
	}
	
	public Integer getSourceHandlerId() {
		return this.theSourceHandlerId;
	}
	
	public Integer getTargetHandlerId() {
		return this.theTargetHandlerId;
	}
	
	public Vector<Object> getParameterList() {
		return theParameterList;
	}
	
	public void setSourceHandlerId(Integer theSourceHandlerId) {
		this.theSourceHandlerId = theSourceHandlerId;
	}
	
	public void setTargetHandlerId(Integer theTargetHandlerId) {
		this.theTargetHandlerId = theTargetHandlerId;
	}
	
	protected void setTheScheduledTime(JETime theScheduledTime) {
		this.theScheduledTime = theScheduledTime;
	}

	@Override
	public int compareTo(JEEvent anEvent) {
		return this.getScheduledTime().compareTo(anEvent.getScheduledTime());
	}
	
	@Override
	public String toString() {
		return name +" at " + theScheduledTime.getTimeMs()  + "ms\t" + theParameterList ;
	}
}
