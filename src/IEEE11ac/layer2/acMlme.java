/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
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
package IEEE11ac.layer2;

import IEEE11ac.station.acStation;
import zzInfra.layer2.JE802MacAlgorithm;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.Vector;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stefan Mangold
 */
public class acMlme extends JEEventHandler {

    private Vector<acBackoffEntity> theBackoffEntityList;

    private JETime theIterationPeriod;

    private acMacAlgorithm theAlgorithm;

    private boolean ARFEnabled;
    private boolean showPlot = false;

    public acMlme(JEEventScheduler aScheduler,
            Random aGenerator,
            Vector<acBackoffEntity> aListofBackoffEntities,
            acStation sta,
            acMac aMac,
            Node aTopLevelNode) {
        super(aScheduler, aGenerator);

        Element mlmeElem = (Element) aTopLevelNode;
        if (mlmeElem.getNodeName().equals("JE802Mlme")) {
            this.message("XML definition " + mlmeElem.getNodeName() + " found.", 1);
            this.theBackoffEntityList = aListofBackoffEntities;
            this.showPlot = Boolean.valueOf(mlmeElem.getAttribute("ShowPlot"));

            this.theAlgorithm = new acMacAlgorithm(sta, aScheduler);

            this.theIterationPeriod = new JETime(new Double(mlmeElem.getAttribute("ComputingInterval_ms")));
            this.theState = state.idle;
            this.ARFEnabled = Boolean.valueOf(mlmeElem.getAttribute("EnableARF"));
            // this.message("ARF Enabled? "+this.ARFEnabled);

        }
    }

    @Override
    public void event_handler(JEEvent anEvent) {
        JETime now = anEvent.getScheduledTime();
        String anEventName = anEvent.getName();
        // an event arrived
        this.message("MLME id " + this.getHandlerId() + " received event " + anEventName, 10);
        if (this.theState == state.idle) {
            if (anEventName.equals("stop_req")) {
                // ignore;
            } else if (anEventName.equals("start_req")) {
                this.theState = state.active;
                this.send(new JEEvent("mobilecomputing_req", this, now.plus(this.theIterationPeriod)));
                this.send(new JEEvent("location_update", anEvent.getSourceHandlerId(), now));
                // this.theState = state.active;

            } else {
                this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
            }

        } else if (this.theState == state.active) {

            if (anEventName.equals("start_req")) {
                // ignore
            } else if (anEventName.equals("mobilecomputing_req")) {
                // algorithm is called periodically
                if (!ARFEnabled) {
                    this.theAlgorithm.compute();
                    if (this.showPlot) {
                        this.theAlgorithm.plot();
                    }
					// this.send(new JEEvent("mobilecomputing_req", this,
                    // now.plus(this.theIterationPeriod)));
                }

                this.send(new JEEvent("mobilecomputing_req", this, now.plus(this.theIterationPeriod)));

            } else if (anEventName.equals("stop_req")) {
                this.theState = state.idle;

            } else if (anEventName.equals("decrease_phy_mode_request")) {
				// TODO: find way later to make a more comprehensive
                // decision,taking into account the environment
                // this.message("Recieved request to decrease phy ");
                int sourceHandlerId = anEvent.getSourceHandlerId();
                for (acBackoffEntity be : this.theBackoffEntityList) {
                    if (be.getHandlerId() == sourceHandlerId) {
                        this.send(new JEEvent("decrease_phy_mode", anEvent.getSourceHandlerId(), theUniqueEventScheduler.now()),
                                be);
                    }
                }

            } else if (anEventName.equals("increase_phy_mode_request")) {
                int sourceHandlerId = anEvent.getSourceHandlerId();
                for (acBackoffEntity be : this.theBackoffEntityList) {
                    if (be.getHandlerId() == sourceHandlerId) {
                        this.send(new JEEvent("increase_phy_mode", anEvent.getSourceHandlerId(), theUniqueEventScheduler.now()),
                                be);// this.theBackoffEntityList.get(1));
                    }
                }

            } else {

                this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
            }
        } else {
            this.error("undefined event handler state.");
        }
    }

    public boolean isARFEnabled() {
        return ARFEnabled;
    }

    public JE802MacAlgorithm getTheAlgorithm() {
        return theAlgorithm;
    }

    public JETime getTheIterationPeriod() {
        return theIterationPeriod;
    }

}
