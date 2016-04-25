package zzInfra.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class JEStatEval80215Delay extends JEStatEval {

	public JEStatEval80215Delay(String aPath, String aFileName, String aHeaderLine) {
		super(aPath, aFileName, aHeaderLine);
		// TODO Auto-generated constructor stub
	}
	
	
	public void evaluation(int scannerAddr, int guestAddr, double StartTime, double EndTime, int cycle) {

		this.theEvalList1.addElement(scannerAddr);
		this.theEvalList2.addElement(guestAddr);
		this.theEvalList3.addElement(StartTime);
		this.theEvalList4.addElement(EndTime);
		this.theEvalList5.addElement(cycle);
		

			try {
			this.theOutWriter = new PrintWriter(new FileWriter(this.theFullFileName, true));
			this.theOutWriter.print(this.theEvalList1.elementAt(this.theEvalList1.size() - 1).toString() + "\t"
					+ this.theEvalList2.elementAt(this.theEvalList2.size() - 1).toString() + "\t"
					+ this.theEvalList3.elementAt(this.theEvalList3.size() - 1).toString() + "\t"
					+ this.theEvalList4.elementAt(this.theEvalList4.size() - 1).toString() + "\t"
					+ this.theEvalList5.elementAt(this.theEvalList5.size() - 1).toString() + "\t"
					);
			StringBuilder strBu = new StringBuilder();
			this.theOutWriter.println(strBu.toString());
			this.theOutWriter.close();
		} catch (IOException e) {
			this.error("Error opening file " + this.theFullFileName + ". Reason: " + e.toString());
		}
	}
}
