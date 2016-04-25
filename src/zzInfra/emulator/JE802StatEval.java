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

package zzInfra.emulator;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer1.JE802PhyMCS;
import zzInfra.layer3_network.JE802HopInfo;;
import IEEE11ac.layer2.acMPDU;
import zzInfra.layer4_transport.JE802TCPPacket;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import zzInfra.layer2.JE802Mpdu;

import zzInfra.statistics.JEStatEval80215Delay;
import zzInfra.statistics.JEStatEvalDelay;
import zzInfra.statistics.JEStatEvalThrp;

public class JE802StatEval extends JEEventHandler {

	private boolean flag_EvalTotalDelay80215Disc; // Bluetooth Discovery
	private JEStatEval80215Delay theTotalDelay80215DiscResults;

	private boolean flag_EvalThrpPerAC;
	private boolean flag_EvalOfferPerAC;
	private boolean flag_EvalDelayPerAC;
	private boolean flag_EvalTotalThrp;
	private boolean flag_EvalTotalOffer;
	private boolean flag_EvalTotalDelay;
	private Vector<JEStatEvalThrp> theThrpResultsPerACList;
	private Vector<JEStatEvalDelay> theDelayResultsPerACList;
	private Vector<JEStatEvalThrp> theOfferResultsPerACList;
	private JEStatEvalThrp theTotalThrpResults;
	private JEStatEvalThrp theTotalOfferResults;
	private JEStatEvalDelay theTotalDelayResults;
	private String path2Results;
	private JETime theEvaluationInterval;
	private JETime theEvaluationStarttime;
	private int theHistogramNumOfBins;
	private double theHistogramMax_ms;

	private JE802PowerEval powerEval;

	private JE802RouteEval routeEval;

	private JE802PhyMCSEval phyModeEval;

	private JE802PacketCountEval countEval;

	private JE802TCPRetransmissionEval tcpRetransmissionEval;

	public JE802StatEval(JEEventScheduler aScheduler, Random aGenerator, Node aTopLevelNode) {
		super(aScheduler, aGenerator);

		Element statEvalElem = (Element) aTopLevelNode;
		if (statEvalElem.getNodeName().equals("JE802StatEval")) {
			this.theState = state.idle;
			this.theThrpResultsPerACList = new Vector<JEStatEvalThrp>();
			this.theDelayResultsPerACList = new Vector<JEStatEvalDelay>();
			this.theOfferResultsPerACList = new Vector<JEStatEvalThrp>();

			this.path2Results = statEvalElem.getAttribute("Path2Results");
			this.flag_EvalThrpPerAC = new Boolean(statEvalElem.getAttribute("EvalThrpPerAC"));
			this.flag_EvalDelayPerAC = new Boolean(statEvalElem.getAttribute("EvalDelayPerAC"));
			this.flag_EvalOfferPerAC = new Boolean(statEvalElem.getAttribute("EvalOfferPerAC"));
			this.flag_EvalTotalDelay = new Boolean(statEvalElem.getAttribute("EvalTotalDelay"));
			this.flag_EvalTotalDelay80215Disc = new Boolean(statEvalElem.getAttribute("EvalTotalDelay80215Discovery"));
			this.flag_EvalTotalThrp = new Boolean(statEvalElem.getAttribute("EvalTotalThrp"));
			this.flag_EvalTotalOffer = new Boolean(statEvalElem.getAttribute("EvalTotalOffer"));
			this.theEvaluationInterval = new JETime(Double.parseDouble(statEvalElem.getAttribute("EvaluationInterval_ms")));
			this.theEvaluationStarttime = new JETime(Double.parseDouble(statEvalElem.getAttribute("EvaluationStarttime_ms")));
			this.powerEval = new JE802PowerEval(path2Results, this.theEvaluationStarttime,
					theUniqueEventScheduler.getEmulationEnd());
			this.routeEval = new JE802RouteEval(theUniqueEventScheduler.getEmulationEnd());
			this.phyModeEval = new JE802PhyMCSEval(this.path2Results, this.theEvaluationStarttime, this.theEvaluationInterval);
			this.countEval = new JE802PacketCountEval();
			this.tcpRetransmissionEval = new JE802TCPRetransmissionEval(path2Results, theUniqueEventScheduler);
			if (this.theEvaluationStarttime.isLaterThan(theUniqueEventScheduler.getEmulationEnd())) {
				this.warning("Evaluation start time later than simulation end. There will be no evaluation result!");
			}
			this.theHistogramNumOfBins = new Integer(statEvalElem.getAttribute("HistogramNumOfBins"));
			this.theHistogramMax_ms = new Double(statEvalElem.getAttribute("HistogramMax_ms"));

			String description = "% time[ms] | #packets | overall #packets | avrg.packetsize[byte] | overall sum packetsize[byte] | thpt overall[Mb/s] | tpht last interval[Mb/s]";
			if (this.flag_EvalTotalThrp) {
				this.theTotalThrpResults = new JEStatEvalThrp(this.path2Results, "total_thrp", description);
			}

			if (this.flag_EvalTotalOffer) {
				this.theTotalOfferResults = new JEStatEvalThrp(this.path2Results, "total_offer", description);
			}

			if (this.flag_EvalTotalDelay) {
				this.theTotalDelayResults = new JEStatEvalDelay(
						this.path2Results,
						"total_delay",
						"% time[ms] | #packets | overall # | avrg[ms] | max[ms] | overall avrg[ms] | overall max[ms] || the delay histogram: BinWidth[ms] bin1 bin2 bin2 ...",
						this.theHistogramNumOfBins, this.theHistogramMax_ms);
			}

			if (this.flag_EvalTotalDelay80215Disc) {
				this.theTotalDelay80215DiscResults = new JEStatEval80215Delay(this.path2Results, "total_delay_80215_discovery",
						"% scanner address | guest address | discovery start time[ms] | discovery end time[ms]");
			}

		} else {
			this.error("XML definition " + statEvalElem.getNodeName() + " found, but JE802Station expected!");
		}
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		String anEventName = anEvent.getName();

		if (this.theState == state.idle) {

			if (anEventName.equals("stop_req")) {
				// ignore;

			} else if (anEventName.equals("start_req")) {
				if (now.getTimeMs() >= this.theEvaluationStarttime.getTimeMs()) {
					this.send("eval_start_req", this);
				} else {
					this.send(new JEEvent("eval_start_req", this, this.theEvaluationStarttime));
				}
				this.theState = state.active;
			}
		} else if (this.theState == state.active) {
			if (anEventName.equals("start_req")) {
				// ignore
			} else if (anEventName.equals("stop_req")) {
				this.theState = state.idle;

			} else if (anEventName.equals("eval_start_req")) {
				if (this.flag_EvalTotalThrp)
					this.theTotalThrpResults.reset();
				if (this.flag_EvalTotalOffer)
					this.theTotalOfferResults.reset();
				if (this.flag_EvalTotalDelay)
					this.theTotalDelayResults.reset();
				if (this.flag_EvalTotalDelay80215Disc) {
					this.theTotalDelay80215DiscResults.reset();
				}
				if (this.flag_EvalThrpPerAC) {
					// loop through list
					for (int cnt = 0; cnt < this.theThrpResultsPerACList.size(); cnt++)
						if (this.theThrpResultsPerACList.elementAt(cnt) != null) {
							this.theThrpResultsPerACList.elementAt(cnt).reset();
						}
				}
				if (this.flag_EvalDelayPerAC) {
					// loop through list
					for (int cnt = 0; cnt < this.theDelayResultsPerACList.size(); cnt++)
						if (this.theDelayResultsPerACList.elementAt(cnt) != null) {
							this.theDelayResultsPerACList.elementAt(cnt).reset();
						}
				}
				if (this.flag_EvalOfferPerAC) {
					// loop through list
					for (int cnt = 0; cnt < this.theOfferResultsPerACList.size(); cnt++)
						if (this.theOfferResultsPerACList.elementAt(cnt) != null) {
							this.theOfferResultsPerACList.elementAt(cnt).reset();
						}
				}
				this.send(new JEEvent("eval_req", this, now));

			} else if (anEventName.equals("eval_req")) {
				this.send(new JEEvent("eval_req", this, now.plus(this.theEvaluationInterval)));
				if (this.flag_EvalThrpPerAC) {
					// loop through list
					for (int cnt = 0; cnt < this.theThrpResultsPerACList.size(); cnt++)
						if (this.theThrpResultsPerACList.elementAt(cnt) != null) {
							this.theThrpResultsPerACList.elementAt(cnt).evaluation(now.getTimeMs());
						}
				}
				if (this.flag_EvalDelayPerAC) {
					// loop through list
					for (int cnt = 0; cnt < this.theDelayResultsPerACList.size(); cnt++)
						if (this.theDelayResultsPerACList.elementAt(cnt) != null) {
							this.theDelayResultsPerACList.elementAt(cnt).evaluation(now.getTimeMs());
						}
				}
				if (this.flag_EvalOfferPerAC) {
					// loop through list
					for (int cnt = 0; cnt < this.theOfferResultsPerACList.size(); cnt++)
						if (this.theOfferResultsPerACList.elementAt(cnt) != null) {
							this.theOfferResultsPerACList.elementAt(cnt).evaluation(now.getTimeMs());
						}
				}
				if (this.flag_EvalTotalThrp) {
					this.theTotalThrpResults.evaluation(now.getTimeMs());
				}
				if (this.flag_EvalTotalOffer) {
					this.theTotalOfferResults.evaluation(now.getTimeMs());
				}
				if (this.flag_EvalTotalDelay) {
					this.theTotalDelayResults.evaluation(now.getTimeMs());
				}
				if (this.flag_EvalTotalDelay80215Disc) {
					// this.theTotalDelay80215DiscResults.evaluation(now.getTimeMs());
				}
				phyModeEval.evaluate();
				routeEval.evaluate();
				tcpRetransmissionEval.evaluate();

			} else if (anEventName.equals("packet_exiting_system_ind")) {
				this.parameterlist = anEvent.getParameterList();
				JE802TCPPacket tcpPacket = (JE802TCPPacket) this.parameterlist.elementAt(0);
				int anAC = (Integer) this.parameterlist.get(1);
				int SA = (Integer) this.parameterlist.get(2);
				double aDelay = theUniqueEventScheduler.now().getTimeMs() - tcpPacket.getCreationTime().getTimeMs();
				long seqNo = tcpPacket.getSeqNo();
				int length = tcpPacket.getLength();

				if (anEvent.getScheduledTime().isLaterThan(theEvaluationStarttime)) {
					if (this.flag_EvalTotalThrp) {
						this.theTotalThrpResults.sample(now.getTimeMs(), SA, seqNo, length);
					}
					if (this.flag_EvalTotalDelay) {
						this.theTotalDelayResults.sample(now.getTimeMs(), SA, seqNo, aDelay);
					}
					if (this.flag_EvalTotalDelay) {
						this.theTotalDelayResults.sample(now.getTimeMs(), SA, seqNo, aDelay);
					}
					if (this.flag_EvalThrpPerAC) {
						if (this.theThrpResultsPerACList.size() < anAC + 1) {
							this.theThrpResultsPerACList.setSize(anAC + 1);
						}
						if (this.theThrpResultsPerACList.elementAt(anAC) == null) {
							this.theThrpResultsPerACList
									.setElementAt(
											new JEStatEvalThrp(this.path2Results, "thrp_AC" + anAC,
													"% time[ms] | #packets | overall #packets | avrg.packetsize[byte] | overall sum packetsize[byte] | thpt overall[Mb/s] | tpht last interval[Mb/s]"),
											anAC);
						}
						this.theThrpResultsPerACList.elementAt(anAC).sample(now.getTimeMs(), SA, seqNo, length);
					}
					if (this.flag_EvalDelayPerAC) {
						if (this.theDelayResultsPerACList.size() < anAC + 1) {
							this.theDelayResultsPerACList.setSize(anAC + 1);
						}
						if (this.theDelayResultsPerACList.elementAt(anAC) == null) {
							this.theDelayResultsPerACList
									.setElementAt(
											new JEStatEvalDelay(
													this.path2Results,
													"delay_AC" + anAC,
													"% time[ms] | #packets | overall # | avrg[ms] | max[ms] | overall avrg[ms] | overall max[ms] || the delay histogram: BinWidth[ms] bin1 bin2 bin2 ...",
													this.theHistogramNumOfBins, this.theHistogramMax_ms), anAC);
						}
						this.theDelayResultsPerACList.elementAt(anAC).sample(now.getTimeMs(), SA, seqNo, aDelay);
					}
				}

			} else if (anEventName.equals("packet_inject_into_system_ind")) {
				this.parameterlist = anEvent.getParameterList();
				int sourceID = (Integer) this.parameterlist.get(3);
				int seqNo = (Integer) this.parameterlist.get(1);
				if (this.flag_EvalTotalOffer) {
					this.theTotalOfferResults.sample(theUniqueEventScheduler.now().getTimeMs(), sourceID, seqNo, new Double(
							(Integer) this.parameterlist.elementAt(0)).doubleValue()); // framebody
																						// size
				}
				if (this.flag_EvalOfferPerAC) {
					Integer anAC = ((Integer) this.parameterlist.elementAt(4));
					if (this.theOfferResultsPerACList.size() < anAC.intValue() + 1) {
						this.theOfferResultsPerACList.setSize(anAC.intValue() + 1);
					}
					if (this.theOfferResultsPerACList.elementAt(anAC.intValue()) == null) {
						this.theOfferResultsPerACList
								.setElementAt(
										new JEStatEvalThrp(this.path2Results, "offer_AC" + anAC,
												"% time[ms] | #packets | overall #packets  | avrg.packetsize[byte] | overall sum packetsize[byte] | thpt overall[Mb/s] | tpht last interval[Mb/s]"),
										anAC);
					}
					this.theOfferResultsPerACList.elementAt(anAC.intValue()).sample(theUniqueEventScheduler.now().getTimeMs(),
							sourceID, seqNo, new Double((Integer) this.parameterlist.elementAt(0)).doubleValue());
				}
			} else if (anEventName.equals("hop_evaluation")) {

			} else if (anEventName.equals("80215_device_discovery_ind")) {
				this.parameterlist = anEvent.getParameterList();
				int scannerAddr = (Integer) this.parameterlist.elementAt(0);
				int guestAddr = (Integer) this.parameterlist.elementAt(1);
				JETime startTime = (JETime) this.parameterlist.elementAt(2);
				JETime endTime = (JETime) this.parameterlist.elementAt(3);
				int cycle = (Integer) this.parameterlist.elementAt(4);
				double doubleStartTime = startTime.getTimeMs();
				double doubleEndTime = endTime.getTimeMs();
				if (anEvent.getScheduledTime().isLaterThan(theEvaluationStarttime) && doubleEndTime >= 0 && doubleStartTime >= 0) {
					if (this.flag_EvalTotalDelay80215Disc) {
						this.theTotalDelay80215DiscResults.evaluation(scannerAddr, guestAddr, doubleStartTime, doubleEndTime,
								cycle);
					}
				}
			} else {
				this.error("undefined event '" + anEventName + "' in state " + this.theState);
			}
		} else {
			this.error("undefined event handler state '" + this.theState + "'.");
		}
	}

	@Override
	public void end_of_emulation() {
		if (this.flag_EvalThrpPerAC) {
			// loop through list
			for (int cnt = 0; cnt < this.theThrpResultsPerACList.size(); cnt++)
				if (this.theThrpResultsPerACList.elementAt(cnt) != null) {
					this.theThrpResultsPerACList.elementAt(cnt).end_of_emulation();
				}
		}
		if (this.flag_EvalDelayPerAC) {
			// loop through list
			for (int cnt = 0; cnt < this.theDelayResultsPerACList.size(); cnt++)
				if (this.theDelayResultsPerACList.elementAt(cnt) != null) {
					this.theDelayResultsPerACList.elementAt(cnt).end_of_emulation();
				}
		}
		if (this.flag_EvalOfferPerAC) {
			// loop through list
			for (int cnt = 0; cnt < this.theOfferResultsPerACList.size(); cnt++)
				if (this.theOfferResultsPerACList.elementAt(cnt) != null) {
					this.theOfferResultsPerACList.elementAt(cnt).end_of_emulation();
				}
		}
		if (this.flag_EvalTotalThrp) {
			this.theTotalThrpResults.end_of_emulation();
		}
		if (this.flag_EvalTotalDelay) {
			this.theTotalDelayResults.end_of_emulation();
		}
		if (this.flag_EvalTotalDelay80215Disc) {
			this.theTotalDelay80215DiscResults.end_of_emulation();
		}
		if (this.flag_EvalTotalOffer) {
			this.theTotalOfferResults.end_of_emulation();
		}
		powerEval.evaluatePowerConsumption();
		phyModeEval.endOfEmulation();
		countEval.endOfEmulation();
		tcpRetransmissionEval.endOfEmulation();
	}

	@Override
	public void display_status() {
		System.out.println("=========== JEmula object (" + this.getClass() + ") ==========");
		System.out.println("  - evalThrpPerAC:           " + this.flag_EvalThrpPerAC);
		System.out.println("  - evalDelayPerAC:          " + this.flag_EvalDelayPerAC);
		System.out.println("  - evalOfferPerAC:          " + this.flag_EvalOfferPerAC);
		System.out.println("  - evalTotalThrp:           " + this.flag_EvalTotalThrp);
		System.out.println("  - evalTotalDelay:          " + this.flag_EvalTotalDelay);
		System.out.println("  - evalTotalDelay80215Disc: " + this.flag_EvalTotalDelay);
		System.out.println("  - evalTotalOffer:          " + this.flag_EvalTotalOffer);
		System.out.println("  - thePath2Results:         " + this.path2Results);
		System.out.println("  - evaluation interval:     " + this.theEvaluationInterval);
		System.out.println("  - evaluation starttime:    " + this.theEvaluationStarttime);
	}

	public String getPath2Results() {
		return path2Results;
	}

	public int getSampleCount() {
		return new Double((this.getEvaluationEnd().getTimeMs() - theEvaluationStarttime.getTimeMs())
				/ theEvaluationInterval.getTimeMs()).intValue();
	}

	public JETime getEvaluationInterval() {
		return theEvaluationInterval;
	}

	public JETime getEvaluationStarttime() {
		return theEvaluationStarttime;
	}

	public JETime getEvaluationEnd() {
		return this.theUniqueEventScheduler.getEmulationEnd();
	}

	public JE802RouteEval getRouteEval() {
		return routeEval;
	}

	public JE802PhyMCSEval getPhyMCSEval() {
		return phyModeEval;
	}

	public void setPhyMCSs(List<JE802PhyMCS> availablePhymodes) {
		phyModeEval.setPhyMCSs(availablePhymodes);
	}

	public JE802TCPRetransmissionEval getTcpRetransmissionEval() {
		return tcpRetransmissionEval;
	}

	public void recordPhyMCS(int fromAddr, int toAddr, JETime when, JE802PhyMCS phyMode) {
		phyModeEval.recordTransmission(fromAddr, toAddr, when, phyMode);
	}

	public void recordPowerTx(int station, int phyId, JETime when, JETime duration) {
		powerEval.recordPowerTx(station, phyId, when, duration);
	}

	public void recordPowerRx(int station, int phyId, JETime when, JETime duration) {
		powerEval.recordPowerRx(station, phyId, when, duration);
	}

	public void linkBroken(JE802HopInfo linkSource, JE802HopInfo linkDestination, JETime when) {
		routeEval.linkBroken(linkSource.getAddress(), linkDestination.getAddress(), when);
	}

	public void addPacketForCounts(JE802Mpdu aMpdu) {
		countEval.addTransmittedPacket(aMpdu);
	}

	public void addRoute(JE802HopInfo source, JE802HopInfo da, JETime when, List<JE802HopInfo> hops) {
		routeEval.addRoute(source.getAddress(), da.getAddress(), when, hops);
	}

	public void tcpPacketSent(int station, int port) {
		tcpRetransmissionEval.sentPacket(station, port);
	}

	public void tcpPacketAcked(int station, int port) {
		tcpRetransmissionEval.ackedPacket(station, port);
	}

	public void setTcpTrafficType(int station, int port, String type) {
		tcpRetransmissionEval.setTrafficType(station, port, type);
	}
}