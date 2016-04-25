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
package zzInfra.layer1;

import IEEE11ac.layer1.acMCS;
import IEEE11af.layer1.afMCS;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import zzInfra.kernel.JEmula;

/**
 * @author Stefan Mangold
 *
 */
public class JE802PhyMCS extends JEmula {

    public String theName;

    public int theRate_Mbps;

    public int theBitPerSymbol;

    public int theId;

    public Map<Integer, Double> bitErrorProbabilities;
    public String modulation;
    public String coding_rate;
    public int GI;

    /**
     *
     * @param aPhyMCSNode
     */
    public JE802PhyMCS(final Node aPhyMCSNode) {
        Element phyModeNode = (Element) aPhyMCSNode;

//        if (phyModeNode.getNodeName().equals("aPhyMCS")) {
            this.message("XML definition " + phyModeNode.getNodeName() + " found.", 1);
            this.theName = phyModeNode.getAttribute("Name");
            this.theRate_Mbps = new Integer(phyModeNode.getAttribute("Mbps"));
            this.theBitPerSymbol = new Integer(phyModeNode.getAttribute("bit_per_symbol"));
            this.theId = new Integer(phyModeNode.getAttribute("id"));
            this.bitErrorProbabilities = new HashMap<Integer, Double>();
            String bitErrorStr = phyModeNode.getAttribute("bitErrorProbabilitiesPerDb");
            if (!bitErrorStr.isEmpty()) {
                String[] bitErrorsDb = bitErrorStr.split(",");
                int size = bitErrorsDb.length;
                for (int i = 0; i < size; i++) {
                    Double probability = new Double(bitErrorsDb[i]);
                    this.bitErrorProbabilities.put(i, probability);
                }
            }
/*        } else {
            error("aPhyMCS not found in " + phyModeNode.getNodeName() + " !!!");
        }
*/    }

    @Override
    public void display_status() {
        System.out.println("=========== JEmula object (" + this.getClass() + ") ==========");
        System.out.println("  - name:           " + this.theName);
        System.out.println("  - rate [Mb/s]:    " + this.theRate_Mbps);
        System.out.println("  - bit per symbol: " + this.theBitPerSymbol);
        System.out.println("  - id:             " + this.theId);
        super.display_status();
        System.out.println("=======================================================");
    }

    @Override
    public String toString() {
        return this.theRate_Mbps + "Mb/s";
    }

    public double getPacketErrorProb(int length, double SNIR) {
        if (SNIR < 0) {
            return 1.0;
        }
        Double bitErrorProb = bitErrorProbabilities.get(new Integer((int) SNIR));
        double packetErrorProb;
        if (bitErrorProb == null || bitErrorProb == 0) {
            packetErrorProb = 0.0;
        } else if (bitErrorProb >= 1) {
            packetErrorProb = 1.0;
        } else {
            packetErrorProb = 1 - Math.pow(1 - bitErrorProb, 8 * length);
        }
        return packetErrorProb;
    }

    public String getName() {
        return this.theName;
    }

    public int getBitsPerSymbol() {
        return this.theBitPerSymbol;
    }

    public int getModeId() {
        return this.theId;
    }

    public int getRateMbps() {
        return this.theRate_Mbps;
    }
}
