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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JEEventList {
	
	private Map<Long, LinkedList<JEEvent>> bucketMap;
	private List<Long> bucketOrder;
	private LinkedList<JEEvent> current;
	private long last;

	public JEEventList() {
        this.last = -1;
		bucketMap = new HashMap<Long, LinkedList<JEEvent>>();
		bucketOrder = new ArrayList<Long>();
		current = null;
	}
	
	public boolean isEmpty() {
		return bucketMap.isEmpty();
	}
	
	public void add(long time, JEEvent event) {
		LinkedList<JEEvent> bucket = bucketMap.get(time); 
		if (bucket!=null){
			bucket.add(event);
		} else {
			bucket = new LinkedList<JEEvent>();
			bucket.add(event);
			bucketMap.put(time, bucket);
			int index = Collections.binarySearch(bucketOrder, time);
			if (index < 0) {
				index = -(index+1);
			}
			bucketOrder.add(index, time);
			if (current == null) {
				current = bucket;
				last = bucketOrder.remove(0);
			}
		}
	}
	
	public JEEvent removeFirst() {
		if (current.size()==0) {
			if (!bucketOrder.isEmpty()) {
				last = bucketOrder.remove(0);
				current = bucketMap.get(last);
			} else {
				return null;
			}
		} 
		JEEvent res =  current.removeFirst();
		if (current.size()==0) {
			bucketMap.remove(res.getScheduledTime().getTime());
		}
		return res;
	}
	
	public void remove(JEEvent event) {
		if (event!=null) {
			double time = event.getScheduledTime().getTime();
			LinkedList<JEEvent> bucket = bucketMap.get(time);
			if (bucket != null) {
				bucket.remove(event);
				if (bucket.size()==0) {
					bucketOrder.remove(time);
					bucketMap.remove(time);
				}
			}
		}
	}
	
	
	public int getSize() {
		return this.getList().size();
	}
	
	public List<JEEvent> getList() {
		List<JEEvent> result = new ArrayList<JEEvent>();
		if (last!=-1) {
			LinkedList<JEEvent> currentList = bucketMap.get(last);
			if (currentList!=null) {
				for (JEEvent jeEvent : currentList) {
					result.add(jeEvent);
				}
			}
			for (int i = 0; i < bucketOrder.size(); i++) {
				currentList = bucketMap.get(i);
				if (currentList!=null) {
					for (JEEvent jeEvent : currentList) {
						result.add(jeEvent);
					}
				}
			}
		}	
		return result;
	}
	
	
}
