package IEEE11ax.layer1;

import java.util.HashMap;
import org.w3c.dom.Node;
import zzInfra.layer1.JE802PhyMCS;

public class axMCS extends JE802PhyMCS {

    public axMCS(Node nodemcs) {
        super(nodemcs);
        String modulation, coding_rate;
        int GI;
        int rate = 54, bps = 216, id = 6, bep = 0;

        modulation = new String("64QAM");
        coding_rate = new String("5/6");
        GI = 800;

        modPhyMCS(theName, modulation, coding_rate, GI, rate, bps, id, bep);
    }

    public void modaxMCS(String name, String modulation, String coding_rate, int GI,
            int rate, int bps, int id, int bep) {
        modPhyMCS(name, modulation, coding_rate, GI, rate, bps, id, bep);
    }

    public void defaxMCS(String name) {
        String modulation, coding_rate;
        int GI;
        int rate = 54, bps = 216, id = 6, bep = 0;

        modulation = new String("64QAM");
        coding_rate = new String("5/6");
        GI = 800;

        modPhyMCS(name, modulation, coding_rate, GI, rate, bps, id, bep);
    }

    // modify PHY MCS while running
    public void modPhyMCS(
            String name, String modulation, String coding_rate, int GI,
            int rate, int bps, int id, int bep) {

        this.theName = name;
        this.modulation = modulation;
        this.coding_rate = coding_rate;
        this.GI = GI;
        this.theRate_Mbps = rate;
        this.theBitPerSymbol = bps;
        this.theId = id;
        this.bitErrorProbabilities = new HashMap<Integer, Double>(bep);
    }

    /* set a default PHY MCS 
     */
    public void defPhyMCS(String name) {
        int rate = 54, bps = 216, id = 6, bep = 0;

        this.theName = name;
        this.modulation = new String("64QAM");
        this.coding_rate = new String("5/6");
        this.GI = 800;
        this.theRate_Mbps = rate;
        this.theBitPerSymbol = bps;
        this.theId = id;
        this.bitErrorProbabilities = new HashMap<Integer, Double>(bep);
    }

}
