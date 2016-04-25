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

package IEEE11ax.layer0;

import zzInfra.layer0.WirelessChannel;
import zzInfra.layer0.WirelessMedium;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;

import zzInfra.layer1.JE802Phy;
import zzInfra.layer1.JE802Ppdu;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import zzInfra.gui.JE802Gui;

/**
 * @author Fabian
 * 
 *         the wireless medium. Coordinates the existing channels, PhyTransc
 *         associations, and location updates
 */
public class axMediumUnitDisk extends JEEventHandler implements WirelessMedium {

	private final Map<Integer, JE802ChannelEntry> channels;

	private double theReuseDistance;

	private final JE802Gui theUniqueGui;

	/**
     * @param aGui
	 * @param aScheduler
	 * @param aGenerator
	 * @param aTopLevelNode
	 *            the constructor
	 * @throws XPathExpressionException
	 */

	public axMediumUnitDisk(final JE802Gui aGui, final JEEventScheduler aScheduler, final Random aGenerator,
			final Node aTopLevelNode) throws XPathExpressionException {

		super(aScheduler, aGenerator);
		this.theUniqueGui = aGui;
		this.channels = new HashMap<Integer, axMediumUnitDisk.JE802ChannelEntry>();
		Element wirelessElem = (Element) aTopLevelNode;
		if (wirelessElem.getNodeName().equals("JEWirelessChannels")) {

			this.theReuseDistance = new Double(wirelessElem.getAttribute("theReuseDistance_m"));

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList channelNodeList = (NodeList) xpath.evaluate("aChannel", wirelessElem, XPathConstants.NODESET);
			if (channelNodeList != null && channelNodeList.getLength() > 0) {
				for (int i = 0; i < channelNodeList.getLength(); i++) {
					Node channelNode = channelNodeList.item(i);
					WirelessChannel aNewChannel = new WirelessChannel(channelNode);
					JE802ChannelEntry entry = new JE802ChannelEntry(aNewChannel);
					this.channels.put(aNewChannel.getChannelNumber(), entry);
				}
			} else {
				error("XML definition JEWirelessChannels has no child nodes!");
			}
			this.theState = state.active;
		}
	}

	@Override
	public void event_handler(final JEEvent anEvent) {

		String anEventName = anEvent.getName();
		if (anEventName.equals("MEDIUM_TxStart_req")) {
			message("MEDIUM_TxStart_req event", 10);
			transmitStart(anEvent);
		} else if (anEventName.equals("tx_end")) {
			message("tx_end event", 10);
			transmitEnd(anEvent);
		} else if (anEventName.equals("register_req")) {
			message("register_req event", 10);
			registerPhy(anEvent);
		} else if (anEventName.equals("location_update_req")) {
			message("location_update_req event", 10);
			updateConnectivity(anEvent);
		} else if (anEventName.equals("channel_switch_req")) {
			message("channel_switch_req event", 10);
			switchChannel(anEvent);
		} else {
			error("undefined event '" + anEventName + "' in state " + this.theState.toString());
		}
	}

	private void transmitStart(final JEEvent anEvent) {
		JETime now = anEvent.getScheduledTime();
		// anEvent.display_status();
		// anEvent.getParameterList().toString();

		JE802Ppdu aPpdu = (JE802Ppdu) anEvent.getParameterList().elementAt(1);
		// aPpdu.display_status();
		JE802Phy aTxPhy = (JE802Phy) anEvent.getParameterList().elementAt(0);
		// aTxPhy.display_status();
		JE802ChannelEntry chan = this.channels.get(aTxPhy.getCurrentChannelNumberTX());
		// create new transmission
		JE802MediumTxRecord transmission = chan.addTransmission(aTxPhy);
		aTxPhy.setOnceTx(true);
		// for all receivers, send Rx_start and set jammed indicator in Ppdu
		Map<JE802Phy, Boolean> rxPhys = transmission.getRxMap();
		for (JE802Phy rxPhy : rxPhys.keySet()) {
			if (rxPhy != aTxPhy) {
				JE802Ppdu aRxPpdu = aPpdu.clone();
				if (rxPhys.get(rxPhy) && rxPhy.isOnceTx()) {
					aRxPpdu.jam();
					aPpdu.jam();
					// this.warning("jamming during tx start");
					// if(this.theUniqueGui != null)
					// theUniqueGui.addLine(theUniqueEventScheduler.now(),
					// aRxPpdu.getMpdu().getSA(), 1, "green",1);
				}
				this.parameterlist = new Vector<Object>();
				this.parameterlist.add(aRxPpdu);
				this.send(new JEEvent("MEDIUM_RxStart_ind", rxPhy.getHandlerId(), now, this.parameterlist));
			}
		}
		// send end event to our selves
		this.parameterlist = new Vector<Object>();
		this.parameterlist.add(aPpdu);
		this.parameterlist.add(aTxPhy);
		this.send(new JEEvent("tx_end", this, now.plus(aPpdu.getMpdu().getTxTime()), this.parameterlist));
	}

	private void transmitEnd(final JEEvent anEvent) {
		JE802Ppdu aPpdu = (JE802Ppdu) anEvent.getParameterList().elementAt(0);
		JE802Phy aTxPhyTransc = (JE802Phy) anEvent.getParameterList().elementAt(1);
		JE802ChannelEntry chan = this.channels.get(aTxPhyTransc.getCurrentChannelNumberTX());

		JE802MediumTxRecord transmission = chan.removeTransmission(aTxPhyTransc);

		Map<JE802Phy, Boolean> rxPhys = transmission.getRxMap();
		for (JE802Phy rxPhy : rxPhys.keySet()) {
			if (rxPhy != aTxPhyTransc) {
				this.parameterlist = new Vector<Object>();
				JE802Ppdu rxPpdu = aPpdu.clone();
				if ((rxPhys.get(rxPhy) && rxPhy.isOnceTx()) || aPpdu.isJammed()) {
					rxPpdu.jam();
					// this.warning("jamming during tx end");
					// if(this.theUniqueGui != null)
					// theUniqueGui.addLine(theUniqueEventScheduler.now(),
					// rxPpdu.getMpdu().getSA(), 1, "magenta",1);

				}
				this.parameterlist.add(rxPpdu);
				this.send(new JEEvent("MEDIUM_RxEnd_ind", rxPhy, anEvent.getScheduledTime(), this.parameterlist));
			}
		}
		aTxPhyTransc.setOnceTx(false);
	}

	private void switchChannel(final JEEvent anEvent) {
		JE802Phy phy = (JE802Phy) anEvent.getParameterList().get(0);
		Integer from = (Integer) anEvent.getParameterList().get(1);
		Integer to = phy.getCurrentChannelNumberTX();
		JE802ChannelEntry fromChannel = this.channels.get(from);
		fromChannel.removePhy(phy);
		JE802ChannelEntry toChannel = this.channels.get(to);
		toChannel.registerPhy(phy);
	}

	private void registerPhy(final JEEvent anEvent) {
		JE802Phy newPhy = (JE802Phy) anEvent.getParameterList().get(0);
		JE802ChannelEntry chan = this.channels.get(newPhy.getCurrentChannelNumberTX());
		// message(newPhy.getDot11CurrentChannelNumberTX());
		// message(chan);
		chan.registerPhy(newPhy);
	}

	private void updateConnectivity(final JEEvent anEvent) {
		JE802Phy aTxPhy = (JE802Phy) anEvent.getParameterList().elementAt(0);
		JE802ChannelEntry chan = this.channels.get(aTxPhy.getCurrentChannelNumberTX());
		chan.updateLocation(aTxPhy);
	}

	@Override
	public double getReuseDistance() {
		return this.theReuseDistance;
	}

	@Override
	public List<WirelessChannel> getAvailableChannels() {
		List<WirelessChannel> availableChannels = new ArrayList<WirelessChannel>();
		for (JE802ChannelEntry channel : this.channels.values()) {
			if (channel != null) {
				availableChannels.add(channel.getChannel());
			}
		}
		return availableChannels;
	}

	/**
	 * @param aPhy1
	 * @param aPhy2
	 * @return true if two PhyTranscs (two phys) are close enough to each other
	 *         to share data - determined by reuse distance
	 */
	private boolean within_reuse_distance(final JE802Phy aPhy1, final JE802Phy aPhy2) {
		JETime now = theUniqueEventScheduler.now();
		double x1 = aPhy1.getMobility().getXLocation(now);
		double y1 = aPhy1.getMobility().getYLocation(now);
		double z1 = aPhy1.getMobility().getZLocation(now);
		double x2 = aPhy2.getMobility().getXLocation(now);
		double y2 = aPhy2.getMobility().getYLocation(now);
		double z2 = aPhy2.getMobility().getZLocation(now);
		double aDistance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
		return this.theReuseDistance > aDistance;
	}

	@Override
	public double getRxPowerLevel_mW(final JE802Phy phy) {
		JE802ChannelEntry chan = this.channels.get(phy.getCurrentChannelNumberTX());
		if (chan == null) {
			this.error("Channel " + phy.getCurrentChannelNumberTX() + " not defined in XML");
			return 0.0;
		} else {
			// if there is an ongoing transmission, the power level is high,
			// certainly above threshold
			if (chan.hasTransmissionInRange(phy)) {
				return Double.MAX_VALUE;
			} else {
				return -Double.MAX_VALUE;
			}
		}
	}

	// for testing only, do not use otherwise
	protected Map<Integer, JE802ChannelEntry> getChannelMap() {
		return this.channels;
	}

	@Override
	public double getBusyPowerLevel_mW() {
		return 100;
	}

	@Override
	public double getSnirAtRx(int da, JE802Phy je802Phy) {
		return Double.MAX_VALUE;
	}

	protected class JE802ChannelEntry {

		private final WirelessChannel channel;

		private final Map<JE802Phy, Map<JE802Phy, Boolean>> connectionMap;

		// for each transmission, keep track of each receiver since a packet can
		// be jammed for only a part of the receivers
		private final List<JE802MediumTxRecord> transmissions;

		protected JE802ChannelEntry(final WirelessChannel channel) {
			this.channel = channel;
			this.connectionMap = new HashMap<JE802Phy, Map<JE802Phy, Boolean>>();
			this.transmissions = new ArrayList<JE802MediumTxRecord>();
		}

		protected void removePhy(final JE802Phy phy) {
			Map<JE802Phy, Boolean> connections = this.connectionMap.get(phy);
			for (JE802Phy connectedPhy : connections.keySet()) {
				Map<JE802Phy, Boolean> othersConnections = this.connectionMap.get(connectedPhy);
				othersConnections.remove(phy);
			}
			this.connectionMap.remove(phy);
		}

		protected void updateLocation(final JE802Phy aPhy) {
			Map<JE802Phy, Boolean> aPhysConnections = this.connectionMap.get(aPhy);
			for (JE802Phy otherPhy : this.connectionMap.keySet()) {
				if (otherPhy != aPhy) {
					if (within_reuse_distance(otherPhy, aPhy)) {
						aPhysConnections.put(otherPhy, true);
						Map<JE802Phy, Boolean> otherMap = this.connectionMap.get(otherPhy);
						otherMap.put(aPhy, true);
					} else {
						aPhysConnections.remove(otherPhy);
						Map<JE802Phy, Boolean> otherMap = this.connectionMap.get(otherPhy);
						otherMap.remove(aPhy);
					}
				}
			}
			this.connectionMap.put(aPhy, aPhysConnections);
		}

		protected JE802MediumTxRecord removeTransmission(final JE802Phy aTxPhyTransc) {
			int size = transmissions.size();
			int index = 0;
			for (int i = 0; i < size; i++) {
				JE802Phy aPhy = transmissions.get(i).getTxPhy();
				if (aPhy.equals(aTxPhyTransc)) {
					index = i;
				}
			}
			return transmissions.remove(index);
		}

		protected JE802MediumTxRecord addTransmission(final JE802Phy aTxPhy) {
			JE802MediumTxRecord transmission = new JE802MediumTxRecord(aTxPhy);

			Set<JE802Phy> rxPhys = getConnectedPhys(aTxPhy);
			for (JE802Phy rxPhy : rxPhys) {
				transmission.addRxPhy(rxPhy, hasTransmissionInRange(rxPhy));
			}
			for (JE802MediumTxRecord otherTransmission : transmissions) {
				Map<JE802Phy, Boolean> rxTrans = otherTransmission.getRxMap();
				for (JE802Phy rxPhy : rxTrans.keySet()) {
					if (within_reuse_distance(rxPhy, aTxPhy)) {
						rxTrans.put(rxPhy, true);
					}
				}
			}
			transmissions.add(transmission);
			return transmission;
		}

		protected boolean hasTransmissionInRange(final JE802Phy phy) {
			for (JE802MediumTxRecord record : transmissions) {
				if (within_reuse_distance(record.getTxPhy(), phy)) {
					return true;
				}
			}
			return false;
		}

		protected void registerPhy(final JE802Phy newPhy) {
			// message("register802Phy");
			Map<JE802Phy, Boolean> newPhyConnections = new HashMap<JE802Phy, Boolean>();
			for (JE802Phy otherPhy : this.connectionMap.keySet()) {
				if (within_reuse_distance(newPhy, otherPhy)) {
					newPhyConnections.put(otherPhy, true);
					Map<JE802Phy, Boolean> otherMap = this.connectionMap.get(otherPhy);
					// message("Before " + this.connectionMap.toString());
					otherMap.put(newPhy, true);
				}
			}
			// add the phy to the currently ongoing transmissions, mark as
			// jammed
			for (JE802MediumTxRecord rec : transmissions) {
				JE802Phy txPhy = rec.getTxPhy();
				if (within_reuse_distance(txPhy, newPhy)) {
					rec.addRxPhy(newPhy, true);
				}
			}
			this.connectionMap.put(newPhy, newPhyConnections);
		}

		protected Set<JE802Phy> getConnectedPhys(final JE802Phy aPhy) {
			Map<JE802Phy, Boolean> connectedPhys = this.connectionMap.get(aPhy);
			// message("size " + this.connectionMap.size());
			// message("channel " + this.channel);
			// message("get " + this.connectionMap.get(aPhy));
			// message("toString " + this.connectionMap.toString());
			// message("KeySet " + connectedPhys.keySet());
			return connectedPhys.keySet();
		}

		protected WirelessChannel getChannel() {
			return this.channel;
		}

		@Override
		public String toString() {
			return "JE802ChannelEntry: " + getChannel();
		}
	}

	private class JE802MediumTxRecord {

		private final JE802Phy txPhy;

		private final Map<JE802Phy, Boolean> rxMap;

		public JE802MediumTxRecord(JE802Phy txPhy) {
			this.txPhy = txPhy;
			this.rxMap = new HashMap<JE802Phy, Boolean>();
		}

		public JE802Phy getTxPhy() {
			return txPhy;
		}

		protected void addRxPhy(JE802Phy rxPhy, Boolean jammed) {
			rxMap.put(rxPhy, jammed);
		}

		protected Map<JE802Phy, Boolean> getRxMap() {
			return this.rxMap;
		}
	}
}