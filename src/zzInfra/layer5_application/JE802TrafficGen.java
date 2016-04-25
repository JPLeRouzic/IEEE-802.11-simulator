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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;

import IEEE11ac.layer2.acHopInfo;
import zzInfra.layer3_network.JE802RoutingConstants;
import zzInfra.layer4_transport.JE802TCPManager;
import zzInfra.layer4_transport.JE802TCPPacket;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import zzInfra.statistics.JERandomVar;
import zzInfra.statistics.JEStatEvalDelay;
import zzInfra.statistics.JEStatEvalThrp;
import zzInfra.emulator.JE802StatEval;
import zzInfra.layer3_network.JE802HopInfo;

/**
 * @author Stefan Mangold
 * 
 */
public class JE802TrafficGen extends JEEventHandler {

//        public TrafficGenMisc tgmisc  ;
    
        public TrafficGenCbr tgcbr ;
    
        public TrafficGenData tgdata ;
    
        public TrafficGenSatFix tgsatfix ;
    
        public TrafficGenHttp tghttp ;
    
        public TrafficGenFtp tgftp ;
    
        public TrafficGenSat tgsat ;
    
        public TrafficGenVideo tgvideo ;
        
	private int AC; // access category

	private int DA; // destination address

	private ArrayList<JE802HopInfo> hopAddresses;

	private int port;

	private int stationAddress;

	private TrafficType type;

	private int seqNo; // sequence number

	private long dataSizeByte;

	int max_packet_size_byte; // maximum size of packet [byte]

	double mean_load_Mbps; // load [Mb/s]

	private boolean EvalOffer;

	private boolean EvalThrp;

	private boolean EvalDelay;

	private int theHistogramNumOfBins;

	private double theHistogramMax_ms;

	JERandomVar var_data_size_byte;

	JERandomVar var_interarr_ms;

	private JE802TCPManager tcp;

	private JE802StatEval theStatEval;

	// throughput per hop, including repetition
	// private List<JEStatEvalThrp> hopThrpResults;

	// delays per hop
	// private List<JEStatEvalDelay> hopDelayResults;

	// end to end throughput results
	private JEStatEvalThrp endThrpResults;

	// end to end delay results
	private JEStatEvalDelay endDelayResults;

	private JEStatEvalThrp theOffer;

	private JETime startTime;

	private JETime stopTime;

	boolean stopped = false;

	private boolean isTcpStream;

	private String fileSuffix;

	// traffic type
	public enum TrafficType {
		data, cbr, saturation, disabled, saturation_fixed, ftp, http, video
	}

        /*
        DATA: default model, behavior as described above. Packet interarrival time is Poisson distributed
            The DATA traffic type creates packets with arbitrary lengths, between 0 and the maximum provided (1500 Byte for example, 
            but any number is possible in the simulator – in real life it has to be less than 2305 Byte). The length is uniformly distributed.
        
        CBR: constant bit rate: packet size is always the maximum, interarrival time is constant
        SATURATION: same as data, but always pumps packets so that the channel is always full, and the listen before talk CSMA protocol is always active.
        SATURATION_FIXED: Combination of CBR and SATURATION
        DISABLED: No packets are generated        
        */
	public JE802TrafficGen(JEEventScheduler theScheduler, Random aGenerator, Node aTopLevelNode, int aStationAddress,
			JE802StatEval theStatEval, JE802TCPManager aTCP) throws XPathExpressionException {
		super(theScheduler, aGenerator);
		Element trafficElem = (Element) aTopLevelNode;
		if (trafficElem.getNodeName().equals("JE802TrafficGen")) {
			this.message("XML definition " + aTopLevelNode.getNodeName() + " found.", 1);
			this.stationAddress = aStationAddress;
			this.theStatEval = theStatEval;
			this.tcp = aTCP;
			this.initFromXml(trafficElem);
                                
                        /*
                        DATA: default model. Packet interarrival time is Poisson distributed
                        * The DATA traffic type creates packets with arbitrary lengths, between 0 and 
                        *   the maximum provided (1500 Byte for example, but any number is possible in the simulator 
                        *   – in real life it has to be less than 2305 Byte). 
                        * The length is uniformly distributed.
                        */
			if (this.type == TrafficType.data) 
                            {
                            tgdata = new TrafficGenData(this, aGenerator) ;
                            } 

                        /*
                        CBR: constant bit rate: packet size is always the maximum, interarrival time is constant
                        */                        
                        else if (this.type == TrafficType.cbr) 
                            { // cbr = Constant Bit Rate
                            tgcbr = new TrafficGenCbr(this, aGenerator) ;
                            } 
                        
                        /*
                         * SATURATION: same as data, but instead of following the throughput definition, 
                        * this always pumps packets so that the channel is always full, and the listen 
                        * before talk CSMA protocol is always active.
                         */                        
                        else if (this.type == TrafficType.saturation) 
                            {
                            tgsat = new TrafficGenSat(this, aGenerator) ;
                            } 
                        
                        else if (this.type == TrafficType.saturation_fixed) 
                            {
                            tgsatfix = new TrafficGenSatFix(this, aGenerator) ;
                            }
                        
                        else if (this.type == TrafficType.http) 
                            {
                            tghttp = new TrafficGenHttp(this, aGenerator) ;
                            } 
                        
                        else if (this.type == TrafficType.ftp) 
                            {
                            tgftp = new TrafficGenFtp(this, aGenerator) ;
                            } 
                        
                        else if (this.type == TrafficType.video) 
                            {
                            tgvideo = new TrafficGenVideo(this, aGenerator) ;
                            }              
                        
                        /*
                         * DISABLED: No packets are generated        
                        */        
                        else if (this.type == TrafficType.disabled) 
                            {
                            // no activity, this generator remains in sleep mode, define
                            // default 'cbr' anyway
                            this.var_data_size_byte = new JERandomVar("cbr", new Double(this.max_packet_size_byte));
                            this.var_interarr_ms = new JERandomVar("cbr", new Double((this.max_packet_size_byte / 2.0 * 8.0)
						/ this.mean_load_Mbps * 1e3 / 1e6));

                            } 
                        else 
                            {
                            this.error("unknown type of traffic: " + this.type);
                            }
                        
			if (this.type != TrafficType.disabled) {
				this.theStatEval.setTcpTrafficType(stationAddress, port, fileSuffix);
				String aPath2Results = theStatEval.getPath2Results();
				
				String thrpFileheader = "% time[ms] | #packets | overall #packets | avrg.packetsize[byte] | overall sum packetsize[byte] | thpt overall[Mb/s] | tpht last interval[Mb/s]";

				if (this.EvalThrp) {
					String filename = "thrp_SA" + this.stationAddress + "_DA" + this.DA + "_AC" + this.AC + "_ID_"
							+ this.getHandlerId() + "_" + this.type + "_" + fileSuffix;
					this.endThrpResults = new JEStatEvalThrp(aPath2Results, filename + "_End", thrpFileheader);
					/*
					 * this.hopThrpResults = new ArrayList<JEStatEvalThrp>();
					 * for(int i = 0; i<hopAddresses.size(); i++){
					 * JEStatEvalThrp thrpEval = new
					 * JEStatEvalThrp(aPath2Results, filename+i,
					 * thrpFileheader); hopThrpResults.add(thrpEval); }
					 */
				}

				if (this.EvalOffer) {
					String filename = "offer_SA" + this.stationAddress + "_DA" + this.DA + "_AC" + this.AC + "_ID_"
							+ this.getHandlerId() + "_" + this.type + "_" + fileSuffix;
					this.theOffer = new JEStatEvalThrp(aPath2Results, filename, thrpFileheader);
				}

				if (this.EvalDelay) {
					String filename = "delay_SA" + this.stationAddress + "_DA" + this.DA + "_AC" + this.AC + "_ID_"
							+ this.getHandlerId() + "_" + this.type + "_" + fileSuffix;
					String delayHeader = "% time[ms] | #packets | overall packet # | avrg[ms] | max[ms] | overall avrg[ms] | overall max[ms] || the delay histogram: BinWidth[ms] bin1 bin2 bin2 ...";
					this.endDelayResults = new JEStatEvalDelay(aPath2Results, filename + "_End", delayHeader,
							this.theHistogramNumOfBins, this.theHistogramMax_ms);
					/*
					 * this.hopDelayResults = new ArrayList<JEStatEvalDelay>();
					 * 
					 * for (int i = 0; i<hopAddresses.size(); i++) {
					 * JEStatEvalDelay delayEval = new
					 * JEStatEvalDelay(aPath2Results, filename + i,delayHeader,
					 * this.theHistogramNumOfBins, this.theHistogramMax_ms);
					 * hopDelayResults.add(delayEval); }
					 */
				}
				this.send(new JEEvent("eval_start_req", this, this.theStatEval.getEvaluationStarttime()));
			}
		} else {
			this.message("WARNING: messed up xml, dude.", 10);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jemula.kernel.JEEventHandler#event_handler(jemula.kernel.JEEvent)
	 */
    @Override
    public void event_handler(JEEvent anEvent) {
        try {
            JETime now = anEvent.getScheduledTime();
            String anEventName = anEvent.getName();
            this.theState = state.active;
            // an event arrived
            this.message("TG at Station " + this.stationAddress + " " + this.toString() + " received event '" + anEventName + "'", 10);
            this.theLastRxEvent = anEvent;
            switch (anEventName) {
                case "start_req":
                    try {
                        if (type == TrafficType.data) { // random data model
                            this.tgdata.event_start_req(this, now);
                        } else if (type == TrafficType.cbr) { // deterministic data model
                            this.tgcbr.event_start_req(this, now);
                        } else if (type == TrafficType.saturation) { // random data
                            this.tgsat.event_start_req(this, now);
                        } else if (type == TrafficType.saturation_fixed) { // random
                            this.tgsatfix.event_start_req(this, now);
                        } else if (type == TrafficType.disabled) {
                            // do nothing
                        } else if (type == TrafficType.ftp) { // random data model
                            this.tgftp.event_start_req(this, now);
                        } else if (type == TrafficType.http) { // random data model
                            this.tghttp.event_start_req(this, now);

                        } else if (type == TrafficType.video) {
                            // random data model
                            this.tgvideo.event_start_req(this, now);

                        } else {
                            error("unknown type of traffic (2): " + type);
                        }
                    } catch (XPathExpressionException ex) {
                        Logger.getLogger(JE802TrafficGen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case "stop_req":
                    if (now.isLaterThan(new JETime(0))) {
                        stopped = true;
                    }
                    break;
                case "eval_start_req":
                    if (this.EvalThrp) {
                        this.endThrpResults.reset();
                        /*
                         * for (JEStatEvalThrp thrp : hopThrpResults) { thrp.reset(); }
                         */
                    }
                    if (this.EvalDelay) {
                        this.endDelayResults.reset();
                        /*
                         * for(JEStatEvalDelay delay: hopDelayResults) { delay.reset();
                         * }
                         */
                    }
                    if (this.EvalOffer) {
                        theOffer.reset();
                    }
                    this.send(new JEEvent("eval_req", this, now));
                    break;

                case "TCPBufferFull":
                    stopped = true;
                    break;

                case "TCPBufferReady":
                    if (this.type == TrafficType.saturation || this.type == TrafficType.saturation_fixed) {
                        if (!tcp.isBufferFull(port)) {
                            this.send(new JEEvent("newpacket_ind", this.getHandlerId(), now));
                        }
                    }
                    stopped = false;
                    break;
                case "eval_req":
                    this.send(new JEEvent("eval_req", this, now.plus(this.theStatEval.getEvaluationInterval())));
                    double timeMs = now.getTimeMs();
                    if (this.EvalThrp) {
                        this.endThrpResults.evaluation(timeMs);
                        /*
                         * for(JEStatEvalThrp thrp : hopThrpResults){
                         * thrp.evaluation(timeMs); }
                         */
                    }
                    if (this.EvalOffer) {
                        theOffer.evaluation(timeMs);
                    }
                    if (this.EvalDelay) {
                        this.endDelayResults.evaluation(timeMs);
                        /*
                         * for(JEStatEvalDelay delay : hopDelayResults) {
                         * delay.evaluation(timeMs); }
                         */
                    }
                    break;
                case "empty_queue_ind":
                    int channel = (Integer) anEvent.getParameterList().get(1);
                        // just make a new packet if the empty queue event was on our
                    // channel
                    boolean onSameChannel;
                    if (JE802RoutingConstants.channelSwitchingEnabled) {
                            // since we don't know on which channel we finally send, we just
                        // generate a new packet
                        onSameChannel = true;
                    } else {
                        onSameChannel = (channel == hopAddresses.get(0).getChannel());
                    }
                    if (onSameChannel && (this.type == TrafficType.saturation || this.type == TrafficType.saturation_fixed)) {
                        if (!isTcpStream) {
                            this.send(new JEEvent("newpacket_ind", this.getHandlerId(), now));
                        }
                    }
                    break;
                case "TCPPacket_discarded_ind":
                    break;
                case "TCPPacket_delivered_ind":
                    break;
                case "hop_evaluation":
                    break;
                case "packet_exiting_system_ind":
                    JE802TCPPacket packet = (JE802TCPPacket) anEvent.getParameterList().get(0);
                    boolean toSample = true;
                    if (EvalThrp) {
                        toSample = endThrpResults.sampleNoDuplicate(now.getTimeMs(), packet.getSourceHandlerId(), packet.getSeqNo(),
                                packet.getLength());
                    }
                    if (EvalDelay && toSample) {
                        double delay = now.getTimeMs() - packet.getCreationTime().getTimeMs();
                        endDelayResults.sampleNoDuplicate(now.getTimeMs(), packet.getSourceHandlerId(), packet.getSeqNo(), delay);
                    }
                    if (toSample) {
                        anEvent.getParameterList().add(this.AC);
                        anEvent.getParameterList().add(this.stationAddress);
                        this.send(anEvent, theStatEval); // forward all packets for
                        // global evaluation
                    }
                    break;
                case "newpacket_ind":
                    if (tcp.isBufferFull(port)) {
                        stopped = true;
                    }
                    switch (this.type) {
                        case data:
                            this.tgdata.event_newpacket_ind(this, now);
                            break;
                        case saturation:
                            this.tgsat.event_newpacket_ind(this, now);
                            break;
                        case saturation_fixed:
                            this.tgsatfix.event_newpacket_ind(this, now);
                            break;
                        case cbr:
                            this.tgcbr.event_newpacket_ind(this, now);
                            break;
                        case ftp:
                            this.tgftp.event_newpacket_ind(this, now);
                            break;
                        case http:
                            this.tghttp.event_newpacket_ind(this, now);
                            break;
                        case video:
                            this.tgvideo.event_newpacket_ind(this, now);
                            break;
                        default:
                            this.error("unknown type of traffic (3): " + this.type);
                            break;
                    }
                    if (this.EvalOffer && !stopped) {
                        this.theOffer.sample(theUniqueEventScheduler.now().getTimeMs(), this.stationAddress,
                                (Integer) this.parameterlist.get(1), new Double(((Integer) this.parameterlist.get(0)).doubleValue()));
                    }
                    if (!stopped) {
                        this.send(new JEEvent("packet_inject_into_system_ind", this.theStatEval.getHandlerId(), theUniqueEventScheduler
                                .now(), this.parameterlist));
                    }
                    break;
                default:
                    this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
                    break;
            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(JE802TrafficGen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	void tcpDelivery(JETime deliveryTime) {
		if (dataSizeByte > 0) {
			this.parameterlist.clear();
			int packetSize = (int) this.var_data_size_byte.nextvalue();
			dataSizeByte -= packetSize;
			this.parameterlist.add(packetSize);
			this.parameterlist.add(this.seqNo);
			this.parameterlist.add(hopAddresses.get(0));
			this.parameterlist.add(this.getHandlerId());
			this.parameterlist.add(this.AC);
			this.parameterlist.add(hopAddresses.clone());
			this.parameterlist.add(port);
			this.parameterlist.add(isTcpStream);
			this.send(new JEEvent("TCPDeliv_req", this.tcp.getHandlerId(), deliveryTime, this.parameterlist));
			seqNo++;
		}
	}

	public int getAC() {
		return AC;
	}

        @Override
	public void end_of_emulation() {
		if (this.type != TrafficType.disabled) {
			tcp.end_of_emulation();
			if (this.EvalThrp) {
				this.endThrpResults.end_of_emulation();
				/*
				 * for(JEStatEvalThrp thrp:hopThrpResults) {
				 * thrp.end_of_emulation(); }
				 */
			}
			if (this.EvalDelay) {
				this.endDelayResults.end_of_emulation();
				/*
				 * for(JEStatEvalDelay delay : hopDelayResults) {
				 * delay.end_of_emulation(); }
				 */
			}
			if (this.EvalOffer) {
				theOffer.end_of_emulation();
			}
			// this.message("last rx: " + this.theLastRxEvent.getName() +
			// " -- last tx: " + this.theLastTxEvent.getName(), 10);
			this.message("last packet seqno: " + this.seqNo + " | type: " + this.type, 10);
		}
	}

	public List<JEStatEvalThrp> getThrpResults() {
		if (endThrpResults == null) {
			return null;
		}
		List<JEStatEvalThrp> thrp = new ArrayList<JEStatEvalThrp>();
		thrp.add(endThrpResults);
		return thrp;
	}

	public List<JEStatEvalDelay> getDelayResults() {
		return null;
	}

	public JEStatEvalThrp getOffer() {
		return theOffer;
	}

	public boolean isEvaluatingOffer() {
		return EvalOffer;
	}

	public boolean isEvaluatingThrp() {
		return EvalThrp;
	}

	public boolean isEvaluatingDelay() {
		return EvalDelay;
	}

	public int getDA() {
		return DA;
	}

	public JETime getStartTime() {
		return this.startTime;
	}

	public JETime getStopTime() {
		return this.stopTime;
	}

	public int getEvaluationStartTimeStep() {
		double interval = theStatEval.getEvaluationInterval().getTimeMs();
		return (int) Math.floor(startTime.getTimeMs() / interval);
	}

	public int getEvaluationStopTimeStep() {
		double interval = theStatEval.getEvaluationInterval().getTimeMs();
		if (stopTime.getTimeMs() != 0.0) {
			return (int) Math.floor(stopTime.getTimeMs() / interval);
		}
		return theStatEval.getSampleCount();
	}

	public ArrayList<JE802HopInfo> getHopAddresses() {
		return hopAddresses;
	}

	public JE802StatEval getStatEval() {
		return theStatEval;
	}

	@Override
	public String toString() {
		return "DA:" + DA + " AC:" + AC;
	}

	private void initFromXml(Element trafficElem) throws XPathExpressionException {
		this.startTime = new JETime(new Double(trafficElem.getAttribute("starttime_ms")));
		this.stopTime = new JETime(new Double(trafficElem.getAttribute("stoptime_ms")));

		this.AC = new Integer(trafficElem.getAttribute("AC")); // Access
																// Category

		String typeStr = trafficElem.getAttribute("type"); // traffic type
                if(typeStr.equals("data"))
                    {
                    this.type = TrafficType.data;
                    }
                else
                if(typeStr.equals("saturation"))
                    {
                    this.type = TrafficType.saturation;
                    }
                else
                if(typeStr.equals("const bit rate"))
                    {
                    this.type = TrafficType.cbr;
                    }
                else
                if(typeStr.equals("disabled"))
                    {
                    this.type = TrafficType.disabled;
                    }
                else
                if(typeStr.equals("saturation_fixed"))
                    {
                    this.type = TrafficType.saturation_fixed;
                    }
                else
                if(typeStr.equals("ftp (3gpp)"))
                    {
                    this.type = TrafficType.ftp;
                    }
                else
                if(typeStr.equals("http (3gpp)"))
                    {
                    this.type = TrafficType.http;
                    }
                else
                if(typeStr.equals("video (3gpp)"))
                    {
                    this.type = TrafficType.video;
                    }
                else
                    System.err.println("Undefined traffic type: " + typeStr);
                
		this.seqNo = 0;
		this.max_packet_size_byte = new Integer(trafficElem.getAttribute("max_packet_size_byte")); // packet
																									// size
		this.mean_load_Mbps = new Double(trafficElem.getAttribute("mean_load_Mbps")); // offered
																						// traffic

		if ((this.mean_load_Mbps < 1e-12) || (this.max_packet_size_byte < 1e-12)) {
			this.type = TrafficType.disabled;
		}
		String isTcp = trafficElem.getAttribute("isTcpTraffic");
		if (isTcp.equals("")) {
			this.isTcpStream = false;
		} else {
			this.isTcpStream = new Boolean(isTcp);
		}

		fileSuffix = trafficElem.getAttribute("fileSuffix");

		String portStr = trafficElem.getAttribute("port");
		// when there is no port attribute or when port is zero, we just
		// generate a random port
		if (portStr != null && !portStr.equals("") && !portStr.equals("0")) {
			this.port = new Integer(portStr);
		} else {
			this.port = theUniqueRandomGenerator.nextInt(1000000000);
			this.warning("no port specified in trafficgen, using random port " + this.port);
		}
		this.EvalOffer = new Boolean(trafficElem.getAttribute("EvalThrpOffer"));
		this.EvalThrp = new Boolean(trafficElem.getAttribute("EvalThrp"));
		this.EvalDelay = new Boolean(trafficElem.getAttribute("EvalDelay"));
		this.theHistogramNumOfBins = new Integer(trafficElem.getAttribute("HistogramNumOfBins"));

        this.theHistogramMax_ms = new Double(trafficElem.getAttribute("HistogramMax_ms"));
//        this.RateDist = new Double(trafficElem.getAttribute("RateDistribution"));
//        this.AveRate = new Double(trafficElem.getAttribute("AverageRate"));

		XPath xpath = XPathFactory.newInstance().newXPath();
		hopAddresses = new ArrayList<JE802HopInfo>();
		String fileSizeStr = trafficElem.getAttribute("fileSizeByte");
		if (fileSizeStr.equals("")) {
			this.dataSizeByte = Long.MAX_VALUE;
		} else {
			this.dataSizeByte = new Long(fileSizeStr);
		}
		NodeList hops = (NodeList) xpath.evaluate("MeshRoute/@*", trafficElem, XPathConstants.NODESET);

                Node route = (Node) xpath.evaluate("@MeshRoute", trafficElem, XPathConstants.NODE);
                if (route != null) 
                    {
                        String routeString = route.getNodeValue();
                        // remove first and last parenthesis
                        routeString = routeString.substring(1, routeString.length() - 1);
                        String[] hopList = routeString.split("\\);\\(");
                        for (int i = 0; i < hopList.length; i++) 
                            {
                                String[] parts = hopList[i].split(",");
                                int address = new Integer(parts[0]);
                                int channel = new Integer(parts[1]);
                                hopAddresses.add(new acHopInfo(address, channel));
                            }
                    }
/*                else      FIXME
                if(thisStation.isAP == false)
                    {
                        // we need to send all traffic through the AP in a BSS
                        String[] parts = AP's address';
                        int address = new Integer(parts[0]);
                        int channel = new Integer(parts[1]);
                        hopAddresses.add(new acHopInfo(address, channel));
                    }
*/
		// Add destination address as last element of hop addresses
		String daString = trafficElem.getAttribute("DA");
		acHopInfo daInfo;
		int channel;
		// address has the format DA="(DA,channel)"
		if (daString.startsWith("(")) {
			daString = daString.substring(1, daString.length() - 1);
			String[] parts = daString.split(",");
			this.DA = new Integer(parts[0]);
			channel = new Integer(parts[1]);
			// address has old format DA="da"
		} else {
			channel = 6;  // FIXME
			this.DA = new Integer(daString);
		}
		daInfo = new acHopInfo(this.DA, channel);
		this.hopAddresses.add(daInfo);
	}
}
