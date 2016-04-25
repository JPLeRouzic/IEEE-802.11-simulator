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


/**
 * @author Stefan Mangold
 */
public final class JETime implements Comparable<JETime> {
	
	private long time;

	public JETime() {
		this.time = 0;
	}

	/**
	 * @param aTime
	 */
	public JETime(JETime aTime) {
		this.time = aTime.time;
	}

	/**
	 * @param aTime
	 * @param aUnit
	 */
	public JETime(double aTime) {
		this.time = (long)(aTime*1000);
	}
	
	public JETime(long aTime) {
		this.time = aTime;
	}

	/**
	 * @param aTime
	 * @return time
	 */
	public boolean isEarlierThan(JETime aTime) {
		return this.time < aTime.getTime();
	}
	
	public boolean isEarlierEqualThan(JETime aTime) {
		return this.time <= aTime.getTime();
	}

	/**
	 * @param aTime
	 * @return boolean
	 */
	public boolean isLaterThan(JETime aTime) {
		return this.time > aTime.getTime();
	}
	
	public boolean isLaterEqualThan(JETime aTime) {
		return this.time >= aTime.getTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEmula#get(java.lang.String)
	 */
	
	public double getTimeMs() {
		return this.time/1000.0;
	}
	
	public long getTime(){
		return this.time;
	}
	
	public double getTimeS() {
		return this.time / 1000000.0;
	}
	
	public void setTime(Double time) {
		this.time = (long)(time*1000);
	}
	
	/**
	 * @param aTime1
	 * @return time
	 */
	public JETime plus(JETime aTime1) {
		return new JETime(this.time + aTime1.getTime());
	}

	/**
	 * @param aFactor
	 * @return time
	 */
	public JETime times(long aFactor) {
		return new JETime(this.time*aFactor);
	}

	/**
	 * @param aTime1
	 * @return time
	 */
	public JETime minus(JETime aTime1) {
		return new JETime(this.time-aTime1.getTime());
	}

	public long dividedby(JETime aTime1) {
		return this.time/aTime1.getTime();
	}

	/**
	 * @param aTime1
	 * @param aTime2
	 * @return time
	 */
	public static JETime add(JETime aTime1, JETime aTime2) {
		return new JETime(aTime1.getTime()+aTime2.getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%.3f",this.getTimeMs())+" ms";
	}

	@Override
	public int compareTo(JETime aTime) {
		if (this.time > aTime.time) {
			return 1;
		} else if(this.time == aTime.time) {
			return 0;
		} else {
			return -1;
		}
	}

}