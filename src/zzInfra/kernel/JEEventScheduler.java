/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) Stefan Mangold
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;


/** @author Stefan Mangold */
public final class JEEventScheduler extends JEmula {

	private JETime now;

	private final JEEventList eventList;

	private final Vector<JEEventHandler> HandlerList;

	private JETime theEmulationEnd;

	@SuppressWarnings("unused")
	private String thePath2Results;

	private long DateOfStart;

	private final static boolean statsEnabled = false;

	private Map<String, Integer> eventStats = null;


	public JEEventScheduler() {
		this.now = new JETime(0.0); // start time reset to zero.
		this.eventList = new JEEventList();
		this.HandlerList = new Vector<JEEventHandler>(); // list of event handlers
		this.HandlerList.setSize(500);
		this.HandlerList.clear();
		this.theEmulationEnd = new JETime(1000.0);
		if (statsEnabled) {
			eventStats = new HashMap<String, Integer>();
		}
	}


	/** @param aNewEvent */

	public void queue_event(JEEvent aNewEvent) {
		JETime time = aNewEvent.getScheduledTime();
		if (time.compareTo(this.now) < 0) { // check if time of event is not already in the past
			this.error("emulation time(" + this.now.toString() + ") already ahead of event time(" + aNewEvent.getScheduledTime().toString() + "). Event "
					+ aNewEvent + " is outdated and will not be processed.");
		} else {
			eventList.add((long) time.getTime(), aNewEvent);
		}
	}


	/** @param anObsoleteEvent */
	public void cancel_event(JEEvent anObsoleteEvent) {
		this.eventList.remove(anObsoleteEvent);
	}


	/** @param aNewEventHandler
	 * @return this */
	public void register_handler(JEEventHandler aNewEventHandler) {
		// the unique object id determines the position
		Integer aHandlerId = aNewEventHandler.getHandlerId();
		if (this.HandlerList.size() < aHandlerId.intValue() - 1) {
			this.HandlerList.setSize(aHandlerId.intValue() - 1);
		}
		if (this.HandlerList.size() > aHandlerId.intValue() - 1) {
			this.HandlerList.removeElementAt(aHandlerId.intValue() - 1);
		}
		this.HandlerList.insertElementAt(aNewEventHandler, aHandlerId.intValue() - 1); // now stored in vector
	}


	/**
	 * 
	 */
	public void start() {
		if (!this.eventList.isEmpty()) {
			this.schedule();
		}
	}


	/** @param aScheduledEmulationEnd */
	public void start(JETime aScheduledEmulationEnd) {

		if (!this.eventList.isEmpty()) {
			this.theEmulationEnd = aScheduledEmulationEnd;
			this.schedule();
		}
	}


	/** @return aNextEvent */
	private JEEvent peek() {
		return this.eventList.removeFirst();
	}


	/**
	 * 
	 */
	private void schedule() {
		this.DateOfStart = new Date().getTime();
		this.message("*** jemula started (" + new Date().toString() + ") ***", 100);
		JEEvent aNextEvent;
		JETime anEventTime;
		Integer aTargetHandlerId;
		Double aProgressMessageInterval = 10.0; // percent
		Double aNextMessage = aProgressMessageInterval;
		while (!this.eventList.isEmpty() && this.now.isEarlierThan(this.theEmulationEnd)) {
			aNextEvent = this.peek(); // get next event from event list
			if (aNextEvent != null) {
				if (statsEnabled) {
					Integer count = eventStats.get(aNextEvent.getName());
					if (count != null) {
						count++;
					} else {
						count = new Integer(1);
					}
					eventStats.put(aNextEvent.getName(), count);
				}
//				double aProgress = this.now.dividedby(this.theEmulationEnd) * 100.0;
                                double aProgress = (this.now.getTimeMs() / this.theEmulationEnd.getTimeMs()) * 100.0;                                
				if (aProgress >= aNextMessage) {
					Date date = new Date();
					long elapsedTime = date.getTime() - this.DateOfStart;
					Integer elapsedTime_h = Math.round(elapsedTime / 1000 / 60 / 60);
					Integer elapsedTime_min = -elapsedTime_h * 60 + Math.round(elapsedTime / 1000 / 60);
					String elapse_message = elapsedTime_h + "h" + elapsedTime_min + "min";
					this.message("*** jemula " + (Math.round(aProgress)) + "% done at (" + new Date().toString() + ") (" + elapse_message + ") ***",
							100);
					aNextMessage = aNextMessage + aProgressMessageInterval;
				}
				//eventCount++;
				anEventTime = aNextEvent.getScheduledTime();
				if (anEventTime.isEarlierThan(this.now)) { // check if time of event is in the past
					this.error("Emulation time(" + this.now.toString() + ") already ahead of event time(" + anEventTime.toString() + "). Event "
							+ aNextEvent.toString() + "  is outdated and will not be processed.");
				} else {
					if (anEventTime.isEarlierThan(this.theEmulationEnd)) {
						this.now = anEventTime; // update simulation time
						aTargetHandlerId = aNextEvent.getTargetHandlerId();
						JEEventHandler handler = this.HandlerList.elementAt(aTargetHandlerId - 1);
						handler.event_handler(aNextEvent);
					} else {
						// we reached the emulation end. Ignore event and artificially jump to the exact end time as defined in the xml file
						this.now = this.theEmulationEnd;
					}
				}
			}
		}
		// no more events to proceed, shutdown
		if (statsEnabled) {
			long sum = 0;
			for (Integer eventCount : eventStats.values()) {
				sum += eventCount;
			}
			List<Entry<String,Integer>> entryList = new ArrayList<Entry<String,Integer>>(eventStats.entrySet());			
			Comparator<Entry<String,Integer>> comp = new Comparator<Entry<String,Integer>>() {

				@Override
				public int compare(Entry<String, Integer> o1,
						Entry<String, Integer> o2) {
				    if(o1.getValue()>o2.getValue()){
				    	return 1;
				    } else if(o1.getValue()<o2.getValue()){
				    	return -1;
				    } else {
				    	return 0;
				    }					
				}
			};
			Collections.sort(entryList, comp);
			for(Entry<String,Integer> entry : entryList){
				this.message(" Event:" + entry.getKey() + "\t\tCount:\t" + eventStats.get(entry.getKey()) + " Percentage: \t" + (double)eventStats.get(entry.getKey())/sum*100 + "%");
			}
		}
		for (int id = 0; id < this.HandlerList.size(); id++) {
			if (this.HandlerList.elementAt(id) != null) {
				JEEventHandler aHandler = this.HandlerList.elementAt(id);
				aHandler.end_of_emulation();
			}
		}
		this.message("*** jemula done (" + new Date().toString() + ") ***", 100);
	}


	/** @return this.now */
	public JETime now() {
		return this.now;
	}


	public void setPath2Results(String thePath2Results) {
		this.thePath2Results = thePath2Results;
	}


	public void setEmulationEnd(JETime theEmulationEnd) {
		this.theEmulationEnd = theEmulationEnd;
	}


	public JETime getEmulationEnd() {
		return this.theEmulationEnd;
	}

	@Override
	public String toString() {
		return now.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEmula#display_status()
	 */
	@Override
	public void display_status() {
		System.out.println("=========== JEmula object (" + this.getClass().toString() + ") ==========");
		System.out.println("  - now:                 " + this.now.toString());
		System.out.println("  - emulation end:       " + this.theEmulationEnd.toString());
		System.out.println("  - number of events:    " + this.eventList.getSize());
		System.out.println("  - number of handlers:  " + this.HandlerList.size());

		if (this.eventList != null) {
			int counter = 0;
			for (JEEvent event : this.eventList.getList()) {
				System.out.println("  - event (" + counter + "):  " + ((event).getName()) + " (#" + (event.getSourceHandlerId()) + "->"
						+ (event.getTargetHandlerId()) + " @" + (event.getScheduledTime()) + ")");
				counter++;
			}
		}
		super.display_status();
		System.out.println("=======================================================");
	}
}
