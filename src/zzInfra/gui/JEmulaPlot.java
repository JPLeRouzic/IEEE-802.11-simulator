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

package zzInfra.gui;

import java.util.ArrayList;
import java.util.List;
import zzInfra.layer3_network.JE802HopInfo;
import zzInfra.layer5_application.JE802TrafficGen;
import org.w3c.dom.Document;
import zzInfra.plot.JEMultiPlotter;
import zzInfra.plot.JEPlotter;
import zzInfra.plot.JESynchroPlotter;
import zzInfra.ARC.JE802Station;



/**
 * A little demo application for the plotter classes.
 * Set the DEMO variable to 1, 2, 3 to choose the plot type and
 * see the out commented lines for different plot options.
 * 
 * @author Laurent Zimmerli - laurentz@student.ethz.ch
 *
 */
public class JEmulaPlot {

	/**
	 * Entry point of the application.
	 */
    public static void EnterPlot(List<JE802Station> stations) {
    	// Some locals:
    	double data;
    	JESynchroPlotter synchroPlotter = null;
    	Double sleepTime;
    	int time = 0;
        
        synchroPlotter = new JESynchroPlotter("Live Plotters", 3, "Time", 60000);
            
        // open one new display
        synchroPlotter.addSubPlot("Throughput", "Throughput [Mb/s]", 50, 200);
        synchroPlotter.display();


                data = CalculateMeanThrp(stations) ;
                synchroPlotter.plot(time - 500, data, 0);
    }
    
private static double CalculateMeanThrp(List<JE802Station> stations) 
    {
    double data = 0 ;

    // put throughput data of each link into a separate station record,
    // which is then accounted to the correct Station
    for (JE802Station station : stations) 
        {
            List<JE802TrafficGen> gens = station.getTrafficGenList();
            for (JE802TrafficGen gen : gens) 
                {
                boolean bnull = (gen != null) ;
                boolean evaltrp = gen.isEvaluatingThrp() ;
                boolean activ = gen.is_active() ;    
                if (bnull && evaltrp && activ) 
                    {
                    ArrayList<JE802HopInfo> hops = gen.getHopAddresses();
                    int start = gen.getEvaluationStartTimeStep();
                    int stop = gen.getEvaluationStopTimeStep();
                    // go though all hops of a route
                    for (int i = 0; i < hops.size(); i++) 
                        {
                        for (int j = start; j < stop; j++) 
                            {
                            int numPackets = (Integer) gen.getThrpResults().get(i).getEvalList3().get(j - start);
                            double avgPacketSize = (Double) gen.getThrpResults().get(i).getEvalList5().get(j - start);
                            double factor = 1000.0 / gen.getStatEval().getEvaluationInterval().getTimeMs(); // samples
                            double rateMb = (numPackets * avgPacketSize * factor) / 125000.0; // bytes
                            data = data + rateMb;
                            }
                        }
                    }
                }
        }
    return data ;
    }

}
