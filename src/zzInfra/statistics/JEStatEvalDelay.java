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
import java.util.Vector;

/**
 * @author Stefan Mangold
 * 
 */
public class JEStatEvalDelay extends JEStatEval {

	private final int theHistogramNumOfBins;
	private final double theHistogramMax_ms;
	private Vector<Integer> theBins;

	public JEStatEvalDelay(String aPath, String aFileName, String aHeaderLine) {
		super(aPath, aFileName, aHeaderLine);
		this.theHistogramNumOfBins = 50; // arbitrary default value
		this.theHistogramMax_ms = 100.0; // arbitrary default value
		this.reset_histogram();
	}

	public JEStatEvalDelay(String aPath, String aFileName, String aHeaderLine, int aNumOfBins, double aHistogramMax_ms) {
		super(aPath, aFileName, aHeaderLine);
		this.theHistogramNumOfBins = aNumOfBins;
		this.theHistogramMax_ms = aHistogramMax_ms;
		this.reset_histogram();
	}
	
	@Override
	public void reset() {
		super.reset();
		reset_histogram();
	}

	private void reset_histogram() {
		this.theBins = new Vector<Integer>();
		this.theBins.setSize(this.theHistogramNumOfBins);
		for (int cnt = 1; cnt <= this.theHistogramNumOfBins; cnt++) {
			this.theBins.add(cnt, 0);
		}
	}

	public void evaluation(double anEvalTime_ms) {

		int aNumOfSamples = this.theSampleList1.size();
		this.theSum1 = this.theSum1 + aNumOfSamples;

		double anAverage1 = 0.0;
		int cnt = 0;
		for (cnt = 0; cnt < aNumOfSamples; cnt++) {
			if (this.theSampleList1.get(cnt) != null) {
				anAverage1 = anAverage1 + (Integer) this.theSampleList1.get(cnt);
			}
		}
		anAverage1 = anAverage1 / cnt;

		double anAverage2 = 0.0;
		for (cnt = 0; cnt < aNumOfSamples; cnt++) {
			if (this.theSampleList2.get(cnt) != null) {
				anAverage2 = anAverage2 + (Long) this.theSampleList2.get(cnt);
			}
		}
		anAverage2 = anAverage2 / cnt;

		for (cnt = 0; cnt < aNumOfSamples; cnt++) {
			if (this.theSampleList2.get(cnt) != null) {
				this.theSum2 = this.theSum2 + (Long) this.theSampleList2.get(cnt);
			}
		}

		double anAverage3 = 0.0;
		double aMax3 = 0.0;
		for (cnt = 0; cnt < this.theSampleList3.size(); cnt++) {
			if (this.theSampleList3.get(cnt) != null) {
				anAverage3 = anAverage3 + (Double) this.theSampleList3.get(cnt);
				if ((Double) this.theSampleList3.get(cnt) > aMax3) {
					aMax3 = (Double) this.theSampleList3.get(cnt);
				}
			}
		}
		anAverage3 = anAverage3 / cnt;

		for (cnt = 0; cnt < this.theSampleList3.size(); cnt++) {
			if (this.theSampleList3.get(cnt) != null) {
				this.theSum3 = this.theSum3 + (Double) this.theSampleList3.get(cnt);
			}
		}

		this.theEvalList1.addElement(anEvalTime_ms);

//		if (this.theSampleList1.size() > 0) {
//			this.theEvalList2.addElement(this.theSampleList1.get(this.theSampleList1.size() - 1));
//		} else {
//			this.theEvalList2.addElement(Double.NaN);
//		}
//
		this.theEvalList3.addElement(aNumOfSamples);
		this.theEvalList4.addElement(this.theSum1);
		this.theEvalList5.addElement(anAverage3);
		this.theEvalList6.addElement(aMax3);

		double aSumOfWeightedAverage_ms = 0.0;
		double aSumOfWeightedMax_ms = 0.0;
		for (cnt = 1; cnt < this.theEvalList3.size(); cnt++) {
			if (this.theEvalList3.elementAt(cnt) != null) {
				double aWeight = new Double(((Integer) this.theEvalList3.elementAt(cnt)).doubleValue());
				aSumOfWeightedAverage_ms = aSumOfWeightedAverage_ms + aWeight * (Double) this.theEvalList5.elementAt(cnt);
				aSumOfWeightedMax_ms = aSumOfWeightedMax_ms + aWeight * (Double) this.theEvalList6.elementAt(cnt);
			}
		}
		double anOverallAverage_ms = aSumOfWeightedAverage_ms / this.theSum1;
		this.theEvalList7.addElement(anOverallAverage_ms);
		double anOverallMax_ms = aSumOfWeightedMax_ms / this.theSum1;
		this.theEvalList8.addElement(anOverallMax_ms);

			try {
			this.theOutWriter = new PrintWriter(new FileWriter(this.theFullFileName, true));
			this.theOutWriter.print(this.theEvalList1.elementAt(this.theEvalList1.size() - 1).toString() + "\t\t"
//					+ this.theEvalList2.elementAt(this.theEvalList2.size() - 1).toString() + "\t"
					+ this.theEvalList3.elementAt(this.theEvalList3.size() - 1).toString() + "\t"
					+ this.theEvalList4.elementAt(this.theEvalList4.size() - 1).toString() + "\t"
					+ this.theEvalList5.elementAt(this.theEvalList5.size() - 1).toString() + "\t"
					+ this.theEvalList6.elementAt(this.theEvalList6.size() - 1).toString() + "\t"
					+ this.theEvalList7.elementAt(this.theEvalList7.size() - 1).toString() + "\t"
					+ this.theEvalList8.elementAt(this.theEvalList8.size() - 1).toString() + "\t"
			// + "% " + new Date().toString()
					);
			StringBuilder strBu = new StringBuilder();
			strBu.append(this.theHistogramMax_ms / this.theHistogramNumOfBins + " ");
			for (int hist_cnt = 1; hist_cnt <= this.theHistogramNumOfBins; hist_cnt++) {
				strBu.append(this.theBins.elementAt(hist_cnt)).append(" ");
			}
			this.theOutWriter.println(strBu.toString());

			this.theOutWriter.close();
		} catch (IOException e) {
			this.error("Error opening file " + this.theFullFileName + ". Reason: " + e.toString());
		}
		this.theSampleList1.clear();
		this.theSampleList2.clear();
		this.theSampleList3.clear();
		super.sampleMap = new HashMap<Long,Object>();
	}
	
	@Override
	public void sample(double aTime_ms, int aValue1, long aValue2, double aValue3) {
		
			super.sample(aTime_ms, aValue1, aValue2, aValue3);
			// now increment the bin value of the histogram. For this, calculate the right bin.
			int aBin = (int)Math.round(0.5 + aValue3/this.theHistogramMax_ms * this.theHistogramNumOfBins);
			aBin = Math.min(aBin, this.theHistogramNumOfBins);
			this.theBins.setElementAt(((Integer) this.theBins.elementAt(aBin)) + 1, aBin);	
		
	}
	
	@Override
	public boolean sampleNoDuplicate(double aTimeMs, int aValue1, long aValue2,	double aValue3) {
		boolean toSample = (sampleMap.get(aValue2) == null);
		if(toSample) {
		   sample(aTimeMs, aValue1, aValue2, aValue3);
		}
		return toSample;
	}
}