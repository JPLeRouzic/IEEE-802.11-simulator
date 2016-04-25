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
package IEEE11ax.layer2;

import java.util.Random;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import IEEE11ax.layer1.axPhy;

public final class axVCollisionHandler extends JEEventHandler {

    private axMPDU theTxMpdu;

    private axPhy thePhy = null;

    private axMac theMac = null;

    private enum VCollisionHandlerState {

        idle, active, checkcollision
    }

    private VCollisionHandlerState theVchState;

    private Integer theTransmittingEntityId;

    public axVCollisionHandler(JEEventScheduler aScheduler, Random aGenerator, axPhy aPhy, axMac aMac) {

        super(aScheduler, aGenerator);

        this.theVchState = VCollisionHandlerState.idle;
        this.thePhy = aPhy;
        this.theMac = aMac;
        this.theTxMpdu = new axMPDU();
        this.theTransmittingEntityId = null;
    }

    @Override
    public void event_handler(JEEvent anEvent) {

        JETime now = anEvent.getScheduledTime();
        String anEventName = anEvent.getName();

        // an event arrived
        this.message("VCH at Station " + this.theMac.getMacAddress() + " received event '" + anEventName + "'", 10);

        if (this.theVchState == VCollisionHandlerState.idle) {

            if (anEventName.equals("stop_req")) { // ----------------------------------------------
                // ignore;

            } else if (anEventName.equals("start_req")) { // ----------------------------------------------
                this.theVchState = VCollisionHandlerState.active;

            } else {
                this.error("undefined event '" + anEventName + "' in state " + this.theVchState.toString());
            }

        } else if (this.theVchState == VCollisionHandlerState.active) {

            if (anEventName.equals("start_req")) { // ----------------------------------------------
                // ignore

            } else if (anEventName.equals("stop_req")) { // --------------------------------------------
                this.theVchState = VCollisionHandlerState.idle;

            } else if (anEventName.equals("txattempt_req")) { // ----------------------------------------
                this.parameterlist = anEvent.getParameterList();
                this.theTxMpdu = (axMPDU) this.parameterlist.elementAt(0);
                this.theTransmittingEntityId = anEvent.getSourceHandlerId();
                this.send(new JEEvent("checkdone_ind", this, theUniqueEventScheduler.now().plus(new JETime(Double.MIN_VALUE))));
                this.theVchState = VCollisionHandlerState.checkcollision;

            } else if (anEventName.equals("tx_timeout_ind")) { // ----------------------------------------
                if (this.theTransmittingEntityId != null) {
                    if (!anEvent.getSourceHandlerId().equals(this.theTransmittingEntityId)) {
                    } else {
                        this.theTransmittingEntityId = 0;
                    }
                }
            } else {
                this.error("undefined event '" + anEventName + "' in state " + this.theVchState.toString());
            }

        } else if (this.theVchState == VCollisionHandlerState.checkcollision) {

            if (anEventName.equals("txattempt_req")) { // ----------------------------------------------
                this.parameterlist = anEvent.getParameterList();
                axMPDU anMpdu = (axMPDU) this.parameterlist.elementAt(0);
                if (anMpdu.getAC() < this.theTxMpdu.getAC()) {
                    this.theTxMpdu = anMpdu;
                    this.theTransmittingEntityId = anEvent.getSourceHandlerId();
                }
            } else if (anEventName.equals("checkdone_ind")) { // --------------------------------------------
                this.parameterlist.clear();
                this.parameterlist.addElement(this.theTxMpdu);

                // forward MPDU to Phy:
                this.send(new JEEvent("PHY_TxStart_req", this.thePhy, now, this.parameterlist));
                this.theVchState = VCollisionHandlerState.active;

				// inform other backoff entities. Send the winning frame up to
                // the Mac. The mac will then treat this frame as if it was
                // received from the outside.
                this.send(new JEEvent("VCH_TxStart_ind", this.theMac, now, this.parameterlist));

            } else {
                this.error("undefined event '" + anEventName + "' in state " + this.theVchState.toString());
            }
        }
    }

    public Integer getTransmitterId() {
        return this.theTransmittingEntityId;
    }

    public void setTransmitterId(Integer theTransmittingEntityId) {
        this.theTransmittingEntityId = theTransmittingEntityId;
    }
}
