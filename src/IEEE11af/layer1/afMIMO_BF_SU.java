/*
 * If there are only two antennas at the moment...

Val absolute ((d1 % lambda) - (d2 % lambda )) < (0.25 * lambda) indicate a wavelike 
additive interference 
(d1 - d2) % lambda is comprised between 0 and lambda

if ((d1 % lambda) - (d2 % lambda )) = 0 => best additive interference
if ((d1 % lambda) - (d2 % lambda )) = 0.25 => moderate additive interference
if ((d1 % lambda) - (d2 % lambda )) = 0.5 => best destructive interference
if ((d1 % lambda) - (d2 % lambda )) = 0.75 => moderate additive interference

Dist:   0   0.1  0.2  0.3  0.4  0.5  0.6  0.7  0.8  0.9  1
Gain:   2           1           0             1          2              

two antennas and a remote object, d1 = distance of an antenna to the object

ant_dist is the distance between the two antennas

l = cosine (ang) * d1

You can estimate the distance that lacks to d2, when wave interference is destructive.
a lambda distance corresponds to a phase angle of 360 °
We therefore express this distance that lacks in degrees
 */
package IEEE11af.layer1;

import IEEE11af.station.afStation;
import static java.lang.Math.abs;
import java.util.Map;
import zzInfra.kernel.JETime;
import IEEE11af.layer0.afMediumInterferenceModel;
import zzInfra.layer1.JE802PhyMCS;

/**
 * @author Jean-Pierre Le Rouzic
 */
public class afMIMO_BF_SU {
public void chg_gain_bf_su (afMediumInterferenceModel mim, JE802PhyMCS aPhyMCS, afStation localsta, afStation distantsta, int nbanttenas) 
    {
    /*
     * We assume the location of this STA is exactly at the outer right antenna.
     * and that there are 0,03125 m between antennas and only two antennas.
     * We suppose the two antennas are aligned north/south.
    */
    int newrate = 0, newbitspersymbol = 0, newerrorprob = 0 ;
    double lambda = 0.125 ; // wave length at 2.4 GHz
    double ant_dist = 0.03125, interfr, new_attn ;
    // ant_dist is the distance between the two antennas

    
    JETime now = mim.theUniqueEventScheduler().now() ;
        double locSTAxLocation = localsta.getXLocation(now);
        double locSTAyLocation  = localsta.getYLocation(now);
        double locSTAzLocation  = localsta.getZLocation(now);
        double distSTAxLocation = distantsta.getXLocation(now);
        double distSTAyLocation  = distantsta.getYLocation(now);
        double distSTAzLocation  = distantsta.getZLocation(now);

        double d1 = localsta.DistBtwPoints( 
            locSTAxLocation, distSTAxLocation,
            locSTAyLocation, distSTAyLocation,
            locSTAzLocation, distSTAzLocation        
            ) ;
    
        double d2 = localsta.DistBtwPoints( 
            locSTAxLocation + ant_dist , distSTAxLocation, // the second antenna is to the north of the first
            locSTAyLocation , distSTAyLocation,
            locSTAzLocation , distSTAzLocation  
                
            ) ;
    /*
    Val absolute ((d1 % lambda) - (d2 % lambda )) < (0.25 * lambda) indicate a wavelike 
    additive interference (d1 - d2) %lambda    
    */
        
  //      below is a skeleton only
                
    interfr = abs((abs(d1 -d2) % lambda) - (0.25 * lambda)) ;
       
    // get MAC of local station
    Integer localkey = localsta.getMac().getMacAddress() ;
    
    // read (station, attenuation)
    Map<Integer, Double> attenuations = afAttenuationTable.get(localkey);

    // get MAC of distant station
    Integer distkey = distantsta.getMac().getMacAddress() ;
    
    // get the old value for attenuation
    new_attn = attenuations.get(distkey) ;
    
    // compute the new value for attenuation
    // Dist:   0   0.1  0.2  0.3  0.4  0.5  0.6  0.7  0.8  0.9  1
    // Gain:   2           1           0             1          2              
    
    if(interfr < 0.25)
        {
        new_attn = new_attn * 1.5 ;
        }
    else
    if(interfr < 0.75)
        {
        new_attn = new_attn * 0.5 ;
        }
    else
        {
        new_attn = new_attn * 1.5 ;
        }
    
    // store new attenuation value
    attenuations.put(distantsta.getMac().getMacAddress(), new_attn);    
            
    /*
    when chg_gain_bf_su is invoked, it checks if there is need to change the channel width
    */
//    aPhyMCS.setRateMbps(newrate);
//    aPhyMCS.setBitsPerSymbol(newbitspersymbol);
//    aPhyMCS.setbitErrorProbabilities(newerrorprob) ;
    }
}
