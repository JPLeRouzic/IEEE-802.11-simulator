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
 *      TrafficGen list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      TrafficGen list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from TrafficGen software without
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

package zzInfra.layer5_application;

import java.util.Random;

import javax.xml.xpath.XPathExpressionException;

import zzInfra.statistics.JERandomVar;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JETime;

/**
 * @author Stefan Mangold, Jean-Pierre Le Rouzic (2014)
 * 
 */
public class TrafficGenSatFix {

    TrafficGenSatFix(JE802TrafficGen TrafficGen, Random aGenerator) throws XPathExpressionException 
        {
        TrafficGen.var_data_size_byte = new JERandomVar(aGenerator,"cbr", 0.0, new Double(TrafficGen.max_packet_size_byte), 0);
        TrafficGen.var_interarr_ms = null;
	}

    public void event_start_req(JE802TrafficGen TrafficGen, JETime now
                        ) throws XPathExpressionException 
        {
        TrafficGen.send(new JEEvent("newpacket_ind", TrafficGen.getHandlerId(), now));   
        }
                
    public void event_newpacket_ind(JE802TrafficGen TrafficGen, JETime now
    ) throws XPathExpressionException {
        // 
        if (!TrafficGen.stopped) 
            {
            // do not create event for next packet. Wait for indication
            // from lower layer instead.
            TrafficGen.tcpDelivery( now);
            }
    }
}
