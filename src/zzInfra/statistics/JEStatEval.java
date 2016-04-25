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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import zzInfra.kernel.JEmula;

/**
 * @author Stefan Mangold
 */
public abstract class JEStatEval extends JEmula {

	protected String thePath2File;

	protected String theFileName;

	protected File theFullFileName;

	protected PrintWriter theOutWriter;

	protected List<Number> theSampleList1;

	protected List<Number> theSampleList2;

	protected List<Number> theSampleList3;

	protected Vector<Number> theEvalList1;

	protected Vector<Number> theEvalList2;

	protected Vector<Number> theEvalList3;

	protected Vector<Number> theEvalList4;

	protected Vector<Number> theEvalList5;

	protected Vector<Number> theEvalList6;

	protected Vector<Number> theEvalList7;

	protected Vector<Number> theEvalList8;

	protected int theSum1;

	protected double theSum2;

	protected double theSum3;
	
	protected Map<Long,Object> sampleMap = new HashMap<Long,Object>();

	protected JEStatEval(String aPath, String aFileName, String aHeaderLine) {

		this.theSampleList1 = new ArrayList<Number>();
		this.theSampleList2 = new ArrayList<Number>();
		this.theSampleList3 = new ArrayList<Number>();
		this.theEvalList1 = new Vector<Number>();
		this.theEvalList2 = new Vector<Number>();
		this.theEvalList3 = new Vector<Number>();
		this.theEvalList4 = new Vector<Number>();
		this.theEvalList5 = new Vector<Number>();
		this.theEvalList6 = new Vector<Number>();
		this.theEvalList7 = new Vector<Number>();
		this.theEvalList8 = new Vector<Number>();
		this.theSum1 = 0;
		this.theSum2 = 0.0;
		this.theSum3 = 0.0;

		this.reset();

		this.thePath2File = new String(aPath);
		File file = new File(this.thePath2File);
		if (!file.exists()) {
			// directory does not exist
			try {
				this.message("creating new folder \"" + this.thePath2File + "\"", 100);
				file.mkdirs();
			} catch (Exception e) {
				this.error("could not create the result directory " + this.thePath2File);
			}
		} else {
			// directory exists already, do nothing
		}

		this.theFileName = new String(aFileName);
		this.theFullFileName = new File(this.thePath2File + File.separatorChar + this.theFileName + ".m");
		if (this.theFullFileName.exists()) {
			// remove the file if it exists already from previous runs.
			if (!this.theFullFileName.delete())
				this.error("could not delete existing file " + this.theFullFileName.getName() + " in " + this.theFullFileName.getAbsolutePath());
		}

		try {
			this.theOutWriter = new PrintWriter(new FileWriter(this.theFullFileName, true));
			this.theOutWriter
					.println("%=============================================================================================================================");
			this.theOutWriter.println("% Result \"" + this.theFileName.toString() + "\". " + new Date().toString() + ".");
			this.theOutWriter
					.println("%-----------------------------------------------------------------------------------------------------------------------------");
			this.theOutWriter.println(aHeaderLine);
			this.theOutWriter
					.println("%=============================================================================================================================");
			this.theOutWriter.println("result_" + aFileName + " = [");
			this.theOutWriter.close();

		} catch (Exception e) {
			this.error("Error opening file " + this.theFullFileName + ". Reason: " + e.toString());
		}
	}
	
	public boolean sampleNoDuplicate(double aTime_ms, int aValue1, long aValue2, double aValue3) {
		boolean toSample = (sampleMap.get(aValue2) == null);
		if(toSample) {
			this.theSampleList1.add(aValue1);
			this.theSampleList2.add(aValue2);
			this.theSampleList3.add(aValue3);	
		}
		return toSample;
	}

	public void sample(double aTime_ms, int aValue1, long aValue2, double aValue3) {
		sampleMap.put(aValue2, new Object());
		this.theSampleList1.add(aValue1);
		this.theSampleList2.add(aValue2);
		this.theSampleList3.add(aValue3);
	}

	public void reset() {
		this.theEvalList1.clear();
		this.theEvalList2.clear();
		this.theEvalList3.clear();
		this.theEvalList4.clear();
		this.theEvalList5.clear();
		this.theEvalList6.clear();
		this.theEvalList7.clear();
		this.theEvalList8.clear();

		this.theSum1 = 0;
		this.theSum2 = 0.0;
		this.theSum3 = 0.0;

		this.theSampleList1.clear();
		this.theSampleList2.clear();
		this.theSampleList3.clear();
	}

	public void end_of_emulation() {
		try {
			this.theOutWriter = new PrintWriter(new FileWriter(this.theFullFileName, true));
			this.theOutWriter.println("]; % " + new Date().toString());
			this.theOutWriter.close();
			this.theOutWriter = null;
		} catch (IOException e) {
			this.error("Error closing file " + this.theFullFileName + ". Reason: " + e.toString());
		}
	}
	
	public int getSampleCount() {
		return this.theEvalList1.size();
	}
	
	public Vector<Number> getEvalList1() {
		return theEvalList1;
	}
	public Vector<Number> getEvalList2() {
		return theEvalList2;
	}
	
	public Vector<Number> getEvalList3() {
		return theEvalList3;
	}
	
	public Vector<Number> getEvalList4() {
		return theEvalList4;
	}

	public Vector<Number> getEvalList5() {
		return theEvalList5;
	}
	
	public Vector<Number> getEvalList6() {
		return theEvalList6;
	}
	
	public Vector<Number> getEvalList7() {
		return theEvalList7;
	}
	
	public Vector<Number> getEvalList8() {
		return theEvalList8;
	}
	
	@Override
	public String toString() {	
		return this.theFileName;
	}
}