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
public class TrafficGenFtp {

    TrafficGenFtp(JE802TrafficGen TrafficGen, Random aGenerator) throws XPathExpressionException 
        {

        // FTP source according 3GPP TR 25.892
        // The Log Normal distribution is widely used when most 
        // of the values occur near minimum value,
        TrafficGen.var_data_size_byte = new JERandomVar(aGenerator, "LogNormal", 0.0, TrafficGen.max_packet_size_byte / 2.0, 722000*8);
        // EXPONENTIAL_DISTR
        TrafficGen.var_interarr_ms = new JERandomVar(aGenerator, "NegExp", 0.0, (TrafficGen.max_packet_size_byte / 2.0 * 8.0)
                            / TrafficGen.mean_load_Mbps * 1e3 / 1e6, 722000*8);
        // FIXME 
        // According to 3GPP, 74% of packets use 1500Bytes MSDU while 24% use 576Butes MSDU.
        //If the user did not change the default packet size, set the packet size randomly, according to the above.

	}

    public void event_start_req(JE802TrafficGen TrafficGen, JETime now
                        ) throws XPathExpressionException 
        {
        // random data model
        JETime aNextPacketArrivalTime ;
        
        aNextPacketArrivalTime = JETime.add(now, new JETime(TrafficGen.var_interarr_ms.nextvalue()));
        TrafficGen.send(new JEEvent("newpacket_ind", TrafficGen.getHandlerId(), aNextPacketArrivalTime));  
        }

    public void event_newpacket_ind(JE802TrafficGen TrafficGen, JETime now
    ) throws XPathExpressionException {
        // 
        JETime aNextPacketArrivalTime;

        aNextPacketArrivalTime = JETime.add(now, new JETime(TrafficGen.var_interarr_ms.nextvalue()));
        TrafficGen.send(new JEEvent("newpacket_ind", TrafficGen.getHandlerId(), aNextPacketArrivalTime));
        if (!TrafficGen.stopped) 
            {
            TrafficGen.tcpDelivery(now);
            }
    }
}