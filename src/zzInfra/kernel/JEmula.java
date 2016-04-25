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

import java.util.Random;


/** @author Stefan Mangold */
public abstract class JEmula {

	private int debuglevel = 70;

	//private static boolean verbose = true; // Pietro
	private static boolean verbose = false;

	private static String messageSelector = "";

	protected JEEventScheduler theUniqueEventScheduler;

	protected Random theUniqueRandomGenerator;


	/**
	 * 
	 */
	public void display_status() {
		this.message("debuglevel: (" + debuglevel + ") verbose: (" + verbose + ")");
	}


	public boolean isVerbose() {
		return verbose;
	}


	public int getDebuglevel() {
		return debuglevel;
	}

	protected void setDebuglevel(int aLevel) {
		this.debuglevel = aLevel;
	}


	/** @param aLine
	 * @param aLevel */
	protected void message(String aLine, int aLevel) {
		if ((aLevel >= debuglevel) || verbose) {
			if (aLine.contains(messageSelector)) {
				StringBuilder line = new StringBuilder();
				if (theUniqueEventScheduler != null) {
					line.append(theUniqueEventScheduler.now().toString());
				}
				line.append(" " + this.getClass().getSimpleName());
				line.append(": " + aLine);
				System.out.println(line);
			}
		}
	}


	protected void message(String aLine) {
		if (aLine.contains(messageSelector)) {
			StringBuilder line = new StringBuilder();
			if (theUniqueEventScheduler != null) {
				line.append(theUniqueEventScheduler.now().toString());
			}
			line.append(" " + this.getClass().getSimpleName());
			line.append(": " + aLine);
			System.out.println(line);
		}
	}


	/** @param aLine */
	protected void warning(String aLine) {
		StringBuilder line = new StringBuilder();
		if (theUniqueEventScheduler != null) {
			line.append(theUniqueEventScheduler.now().toString());
		}
		line.append(" WARNING: ");
		line.append(this.getClass().getSimpleName());
		line.append(": " + aLine);
		System.err.println(line);
	}


	/** @param aLine */
	public void error(String aLine) {
		StringBuilder line = new StringBuilder();
		if (theUniqueEventScheduler != null) {
			line.append(theUniqueEventScheduler.now().toString());
		}
		line.append(" ERROR: ");
		line.append(this.getClass().getSimpleName());
		line.append(": " + aLine);
		System.err.println(line);
		System.exit(0);
	}

}
