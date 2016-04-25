/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package IEEE11af.layer0;

import IEEE11af.layer1.afAttenuationTable;
import IEEE11af.layer1.afPhy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zzInfra.kernel.JETime;
import zzInfra.layer1.JE802Mobility;
import zzInfra.layer1.JE802Phy;
import zzInfra.util.ConvertUnits;
import zzInfra.util.Vector3d;

/**
 * @author Jean-Pierre Le Rouzic
 */

public class afPathLoss {
    private final double maxAllowedPathloss = ConvertUnits.dBToFactor(-110);
    // maximal interference at receiverStation
    Map<Integer, Double> rxMaxInterferencemW;
    // current interference at receiverStation
    Map<Integer, Double> rxInterferencemW;

    void updateAttenuations(afPhy newPhy, afMediumInterferenceModel mim) {
		afMediumInterferenceModel.JE802ChannelEntry entry = mim.channels.get(newPhy.getCurrentChannelNumberTX());
		Map<Integer, Double> attenuations = afAttenuationTable.get(newPhy.getMac().getMacAddress());
		if (attenuations == null) {
			attenuations = new HashMap<Integer, Double>();
		}
		for (afPhy otherPhy : mim.allPhys) {
			double pathLossFactor = mim.computePathlossFactor(newPhy, otherPhy, entry.getP1m());
			if (pathLossFactor < maxAllowedPathloss) {
				pathLossFactor = maxAllowedPathloss;
			}
			// update attenuation from us to other station
			attenuations.put(otherPhy.getMac().getMacAddress(), pathLossFactor);
			// update our attenuation at the other station
			Map<Integer, Double> othersAttenuations = afAttenuationTable.get(otherPhy.getMac().getMacAddress());
			if (othersAttenuations != null) {
				othersAttenuations.put(newPhy.getMac().getMacAddress(), pathLossFactor);
			} else {
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				map.put(otherPhy.getMac().getMacAddress(), pathLossFactor);
			}
			mim.attnTable.put(newPhy.getMac().getMacAddress(), attenuations);
		}
    }

    double getPathloss(final Integer sta1, final Integer sta2, afMediumInterferenceModel mim) {
		Map<Integer, Double> mapPhy1 = afAttenuationTable.get(sta1);
		Double pathloss = mapPhy1.get(sta2);
		if (pathloss != null) {
			return pathloss;
		}
		// outside of transmission range
		return 0.0;
    }

    public double getLinkRxPowerLevel_mW(afPhy locphy, afPhy dstphy, afMediumInterferenceModel mim) {
        afMediumInterferenceModel.JE802ChannelEntry channel = mim.channels.get(locphy.getCurrentChannelNumberTX());
        if (channel == null) {
            mim.error("Channel " + locphy.getCurrentChannelNumberTX() + " not defined in XML");
            return 0.0;
        } else {
            double attenuation = getPathloss(locphy.getMac().getMacAddress(), dstphy.getMac().getMacAddress(), mim);
            double reveived_pwr = dstphy.getCurrentTransmitPower_mW() * attenuation;
            return reveived_pwr;
        }
    }

    // compute the path loss from phy1 to phy2
    private double computePathlossFactor(final JE802Phy srcPhy, final JE802Phy dstPhy, final double p1m, afMediumInterferenceModel mim) {
        JETime now = mim.theUniqueEventScheduler().now();
        JE802Mobility srcMob = srcPhy.getMobility();
        JE802Mobility dstMob = dstPhy.getMobility();
        Vector3d src = new Vector3d(srcMob.getXLocation(now), srcMob.getYLocation(now), srcMob.getZLocation(now));
        Vector3d dst = new Vector3d(dstMob.getXLocation(now), dstMob.getYLocation(now), dstMob.getZLocation(now));
        Vector3d pathDirection = dst.sub(src).normalize();
        double distance = src.getDistanceTo(dst);
        // compute attenuation
        double attenuation;
        if (distance > 1.0) {
            attenuation = p1m - 20 * Math.log10(distance);
        } else {
            attenuation = p1m;
        }
        double srcDirectionalGain = srcPhy.getAntenna().getGainIndBForDirection(pathDirection, src.getLat(), src.getLon(), srcMob.getTraceHeading(now));
        double dstDirectionalGain = dstPhy.getAntenna().getGainIndBForDirection(pathDirection.reflect(), dst.getLat(), dst.getLon(), dstMob.getTraceHeading(now));
        return ConvertUnits.dBToFactor(attenuation + srcDirectionalGain + dstDirectionalGain);
    }
    
        // Distance at which two BSS can use the same channel safely, 
        // otherwise if they use the same channel, they are in OBSS
        // The Rx power should be higher than -82dBm        
//	@Override
	public double getReuseDistance(afPhy locphy, afPhy dstphy, afMediumInterferenceModel mim) {
		double distance ;
                double rxpwr, rxdb ;
                
                rxpwr = getLinkRxPowerLevel_mW(locphy, dstphy, mim);
                rxdb = ConvertUnits.mWtodBm(rxpwr) ;
                if(rxdb < -82)
                    {
                    distance = 999999999 ;
                    }
                else
                    {
                    /*
                     * we compute the distance who will give -82dBm
                     */

		JETime now = mim.theUniqueEventScheduler().now();
		double x1 = locphy.getMobility().getXLocation(now);
		double y1 = locphy.getMobility().getYLocation(now);
		double z1 = locphy.getMobility().getZLocation(now);
		double x2 = dstphy.getMobility().getXLocation(now);
		double y2 = dstphy.getMobility().getYLocation(now);
		double z2 = dstphy.getMobility().getZLocation(now);
		distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
        
                    int mult = 0 ;
                    // rxdb is > -82
                    mult = (int) (1 + rxpwr - ConvertUnits.dBmtomW(-82)) ;
                    mult = (int) (Math.pow(mult, 2)) ;
                    distance = distance * mult ;        
                    }
		return distance;
	}

//	@Override
	public double getBusyPowerLevel_mW(afMediumInterferenceModel mim) {
		return mim.busyThreshold_mW;
	}

//	@Override
	public double getGlobalRxPowerLevel_mW(afPhy phy, afMediumInterferenceModel mim) {
		double neighborChannelInterference = 0.0;
		afMediumInterferenceModel.JE802ChannelEntry channel = mim.channels.get(phy.getCurrentChannelNumberTX());
		if (channel == null) {
			mim.error("Channel " + phy.getCurrentChannelNumberTX() + " not defined in XML");
			return 0.0;
		} else 
                    {
                    for (Integer interferingChannel : channel.getInterferingChannels()) 
                        {
                        afMediumInterferenceModel.JE802ChannelEntry entry = mim.channels.get(interferingChannel);
                        List<afMediumInterferenceModel.JE802MediumTxRecord> neighborTransmissions = entry.getTransmittingPhys();
                        for (afMediumInterferenceModel.JE802MediumTxRecord rec : neighborTransmissions) 
                            {
                            afPhy txPhy = (afPhy) rec.getTxPhy();
                            double attenuation = getPathloss(phy.getMac().getMacAddress(), txPhy.getMac().getMacAddress(), mim);
                            double interference = txPhy.getCurrentTransmitPower_mW() * attenuation;
                            neighborChannelInterference += interference;
                            }
                        }
                    List<afMediumInterferenceModel.JE802MediumTxRecord> ownTransmssions = channel.getTransmittingPhys();
                    double ownChannelInterference = 0.0;
                    for (afMediumInterferenceModel.JE802MediumTxRecord rec : ownTransmssions) 
                        {
                        ownChannelInterference += getCurrentInterference(phy.getMac().getMacAddress());
                        double attenuation = getPathloss((rec.getTxPhy()).getMac().getMacAddress(), phy.getMac().getMacAddress(), mim);
                        double rxPower = rec.getTxPhy().getCurrentTransmitPower_mW() * attenuation;
                        ownChannelInterference += rxPower;
                        }
                    return neighborChannelInterference + ownChannelInterference + mim.noiseLevel_mW;
                    }
	}

//	@Override
	// only reception power according to distance
	public double getSinrAtRx(Integer rxAddr, afPhy txPhy, afMediumInterferenceModel mim) {
		double ownChannelInterference = mim.noiseLevel_mW;
		double attenuation = getPathloss(txPhy.getMac().getMacAddress(), rxAddr, mim);
		double rxPower = txPhy.getCurrentTransmitPower_mW() * attenuation;
		double sinr = rxPower / ownChannelInterference;
		return sinr;
	}

    //
    // calculates the reveived_pwr power level in mW at the position of atPhy
    public double calculateInterference(final afPhy atPhy, final JE802Phy txPhy, final afMediumInterferenceModel.JE802ChannelEntry chan, afMediumInterferenceModel jE802MediumInterferenceModel) {
        double interferenceSummW = 0.0;
        List<afMediumInterferenceModel.JE802MediumTxRecord> physOnChannel = chan.getTransmittingPhys();
        int size = physOnChannel.size();
        for (int i = 0; i < size; i++) {
            afMediumInterferenceModel.JE802MediumTxRecord otherTx = physOnChannel.get(i);
            afPhy currentPhy = (afPhy) otherTx.getTxPhy();
            if (currentPhy != txPhy) {
                double attenuationFactor = jE802MediumInterferenceModel.palo.getPathloss(atPhy.getMac().getMacAddress(), currentPhy.getMac().getMacAddress(), jE802MediumInterferenceModel);
                double powerLevelAtcurrentPhy = currentPhy.getCurrentTransmitPower_mW() * attenuationFactor;
                interferenceSummW += powerLevelAtcurrentPhy;
            }
        }
        return interferenceSummW;
    }

    // update the rx interferences induced by txPhy
    public void addInterference(final afPhy aTxPhy, final double crossChannelInterference, afMediumInterferenceModel mim, JE802Phy txPhy) {
        if (aTxPhy != txPhy) {
            for (Integer rxAddr : rxInterferencemW.keySet()) {
                double attenuation = getPathloss(aTxPhy.getMac().getMacAddress(), rxAddr, mim);
                double interferenceAtRx = aTxPhy.getCurrentTransmitPower_mW() * attenuation * crossChannelInterference;
                Double existingInterference = rxInterferencemW.get(rxAddr);
                double newInterference = existingInterference + interferenceAtRx;
                rxInterferencemW.put(rxAddr, newInterference);
                Double maxInterference = rxMaxInterferencemW.get(rxAddr);
                if (maxInterference < newInterference) {
                    rxMaxInterferencemW.put(rxAddr, newInterference);
                }
            }
        }
    }

    public void decreaseInterference(final afPhy aTxPhy, final double crossChannelInterference, afMediumInterferenceModel mim, JE802Phy txPhy) {
        if (aTxPhy != txPhy) {
            for (Integer rxAddr : rxInterferencemW.keySet()) {
                double attenuation = getPathloss(aTxPhy.getMac().getMacAddress(), rxAddr, mim);
                double interferenceAtRx = aTxPhy.getCurrentTransmitPower_mW() * attenuation * crossChannelInterference;
                Double existingInterference = rxInterferencemW.get(rxAddr);
                rxInterferencemW.put(rxAddr, existingInterference - interferenceAtRx);
            }
        }
    }

    public void addRxInterference(final afPhy aRxPhy, final double interferencemW) {
        Integer addr = aRxPhy.getMac().getMacAddress();
        Double interference = rxInterferencemW.get(addr);
        Double newInterference = interference + interferencemW;
        rxInterferencemW.put(addr, newInterference);
        Double maxInterference = rxMaxInterferencemW.get(addr);
        if (maxInterference < newInterference) {
            rxMaxInterferencemW.put(addr, newInterference);
        }
    }

    public double getCurrentInterference(final Integer addr) {
        Double interference = rxInterferencemW.get(addr);
        if (interference != null) {
            return interference;
        } else {
            return 0.0;
        }
    }

    public double getMaxInterference(final Integer addr) {
        Double interference = rxMaxInterferencemW.get(addr);
        if (interference != null) {
            return interference;
        } else {
            return 0.0;
        }
    }
        
}
