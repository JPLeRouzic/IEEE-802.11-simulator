/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzInfra.layer5_application;

import zzInfra.statistics.JERandomVar;
import java.util.Random;
import javax.xml.xpath.XPathExpressionException;
import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JETime;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class WeatherRadar {
    
    WeatherRadar(JE802TrafficGen TrafficGen, Random aGenerator) throws XPathExpressionException 
    {
        /*
        WeatherRadar model. It is the same as DATA, except that at random time all traffic become forbidden on a given channel.
        */

        TrafficGen.var_data_size_byte = new JERandomVar(aGenerator, "Uniform", 0.0, TrafficGen.max_packet_size_byte / 2.0, 0);
        TrafficGen.var_interarr_ms = new JERandomVar(aGenerator, "NegExp", 0.0, (TrafficGen.max_packet_size_byte / 2.0 * 8.0)
                                                        / TrafficGen.mean_load_Mbps * 1e3 / 1e6, 0);
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
            // TrafficGen, now, dataSizeByte, port, hopAddresses, isTcpStream, seqNo
            TrafficGen.tcpDelivery(now);
            }
    }
}
