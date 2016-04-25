/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IEEE11af.layer2;

import IEEE11af.layer2.*;
import zzInfra.ARC.JE802Station;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.layer2.JE802MacAlgorithm;

/**
 *
 * @author jplr
 */
class afMacAlgorithm extends JE802MacAlgorithm {

    public afMacAlgorithm(JE802Station sta, JEEventScheduler aScheduler) {
        super(sta, aScheduler);
    }

    @Override
    public void compute() {
        
    }

    @Override
    public void plot() {
        
    }
    
}
