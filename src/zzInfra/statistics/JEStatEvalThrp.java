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

package zzInfra.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * @author Stefan Mangold
 *
 */
public class JEStatEvalThrp extends JEStatEval {

	/**
	 * 
	 */
	public JEStatEvalThrp(String aPath, String aFileName, String aHeaderLine) {
		super(aPath, aFileName, aHeaderLine);
		this.message(this.getClass().getSimpleName().toString() + " constructor done.", 0);
	}

	public void evaluation(double anEvalTime_ms) {

		int aNumberOfSamples = this.theSampleList1.size();

		this.theSum1 = this.theSum1 + aNumberOfSamples;


		double anAverage1 = 0.0;
		int cnt = 0;
		for (cnt = 0; cnt < aNumberOfSamples; cnt++) {
			if (this.theSampleList1.get(cnt) != null) {
				anAverage1 = anAverage1 + (Integer) this.theSampleList1.get(cnt);
			}
		}
		anAverage1 = anAverage1 / cnt;

		double anAverage2 = 0.0;
		for (cnt = 0; cnt < aNumberOfSamples; cnt++) {
			if (this.theSampleList2.get(cnt) != null) {
				anAverage2 = anAverage2 + (Long) this.theSampleList2.get(cnt);
			}
		}
		anAverage2 = anAverage2 / cnt;

		for (cnt = 0; cnt < aNumberOfSamples; cnt++) {
			if (this.theSampleList2.get(cnt) != null) {
				this.theSum2 = this.theSum2 + (Long) this.theSampleList2.get(cnt);
			}
		}

		double anAverage3 = 0.0;
		for (cnt = 0; cnt < this.theSampleList3.size(); cnt++) {
			if (this.theSampleList3.get(cnt) != null) {
				anAverage3 = anAverage3 + (Double) this.theSampleList3.get(cnt);
			}
		}
		anAverage3 = anAverage3 / cnt;

		for (cnt = 0; cnt < this.theSampleList3.size(); cnt++) {
			if (this.theSampleList3.get(cnt) != null) {
				this.theSum3 = this.theSum3 + (Double) this.theSampleList3.get(cnt);
			}
		}

		double aTotalEvalTime_ms = 0.0;
		if (this.theEvalList1.size() > 0) {
			aTotalEvalTime_ms = anEvalTime_ms - (Double) this.theEvalList1.elementAt(0);
		} 

		double aTotalMbps = this.theSum3 / aTotalEvalTime_ms * 1000 * 8 / 1e6;

		double intervall;
		if(!theEvalList1.isEmpty()){
			intervall = anEvalTime_ms-(Double)theEvalList1.get(theEvalList1.size()-1);
		} else {
			intervall = 0;
		}
		
		this.theEvalList1.addElement(anEvalTime_ms);

//		if (this.theSampleList1.size() > 0) {
//			this.theEvalList2.addElement(this.theSampleList1.get(this.theSampleList1.size() - 1));
//		} else {
//			this.theEvalList2.addElement(Double.NaN);
//		}
		
		
		this.theEvalList3.addElement(aNumberOfSamples);
		this.theEvalList4.addElement(this.theSum1);
		this.theEvalList5.addElement(anAverage3);
		this.theEvalList6.addElement(this.theSum3);
		this.theEvalList7.addElement(aTotalMbps);
		double bytes = aNumberOfSamples*anAverage3;
		double mBit = bytes/1e6*8;
		double factor = (1/(intervall/1000));
		double currentTP = mBit*factor;
		try {
			this.theOutWriter = new PrintWriter(new FileWriter(this.theFullFileName, true));
			this.theOutWriter.println(
					this.theEvalList1.elementAt(this.theEvalList1.size() - 1).toString() + "\t" +
					//this.theEvalList2.elementAt(this.theEvalList2.size() - 1).toString() + "\t" +
					this.theEvalList3.elementAt(this.theEvalList3.size() - 1).toString() + "\t" +
					this.theEvalList4.elementAt(this.theEvalList4.size() - 1).toString() + "\t" +
					this.theEvalList5.elementAt(this.theEvalList5.size() - 1).toString() + "\t" +
					this.theEvalList6.elementAt(this.theEvalList6.size() - 1).toString() + "\t" +
					this.theEvalList7.elementAt(this.theEvalList7.size() - 1).toString() + "\t" + 
					currentTP);
			this.theOutWriter.close();
		} catch (IOException e) {
			this.error("Error opening file " + this.theFullFileName + ". Reason: " + e.toString());
		}
		this.theSampleList1.clear();
		this.theSampleList2.clear();
		this.theSampleList3.clear();
		super.sampleMap = new HashMap<Long,Object>();
	}	
}