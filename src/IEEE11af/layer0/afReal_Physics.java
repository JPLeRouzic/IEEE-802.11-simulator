/*
 * It takes in account distance, but also the fact that the AP is limiting the overall throughput.
 */
package IEEE11af.layer0;

import IEEE11af.station.afStation;

/* import hew_sim.simulator.logic.afStation;
import hew_sim.simulator.logic.Simulator; */

/**
 * @author jean-Pierre Le Rouzic
 */
public class afReal_Physics {

    private long distance_impact(afStation sta, long max_nb_bits)
    {
        long nb_transmitted_bits = 0 ;
        double d1, d2 ;
        double dist_ratio;

        d1 = sta.DistFromAP() ;
        d2 = sta.DistFromAP() ;
        dist_ratio = (1 + ((d1) * (d2) / sta.propag));
        nb_transmitted_bits = (long) (max_nb_bits / dist_ratio);

        return nb_transmitted_bits ;
    }
    
}
