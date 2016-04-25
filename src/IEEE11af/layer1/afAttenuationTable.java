/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package IEEE11af.layer1;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class afAttenuationTable {
    
    	// lookup table in JE802MediumInterferenceModel for attenuation factors
        // key, (key, value)
        // station, (station, attenuation)
	static Map<Integer, Map<Integer, Double>> attenuationTable;

        public afAttenuationTable()
            {
            attenuationTable = new HashMap<Integer, Map<Integer, Double>>();
            }

    public static Map<Integer, Double> get(int macAddress) {
        return attenuationTable.get(macAddress) ;
    }

    public void put(int macAddress, Map<Integer, Double> attenuations) {
        attenuationTable.put(macAddress, attenuations) ;
    }

}
