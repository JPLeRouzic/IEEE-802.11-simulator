/*
 * 
 * This is jemula.
 *
 *    Copyright (c) 2006-2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
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

package zzInfra.statistics;

import java.util.Random;

import zzInfra.kernel.JEmula;

/**
 * @author Stefan Mangold (debugged by S. Geiger & M. Imhof)
 * 
 */
public final class JERandomVar extends JEmula {

	private String theDistr;

	// Minimum value. 
	private double theMin;

	private double theMean; // Mean value of the distribution

	// Maximum value. 
        private double theMax;
        
        private double stdDevi ; // Standard deviation of the distribution

	/**
	 * @param aGenerator
	 * @param aDistribution
	 * @param aMinValue
	 * @param aMeanValue
	 */
	public JERandomVar(Random aGenerator, String aDistribution, double aMinValue, double aMeanValue, double stdDev) {

		this.theUniqueRandomGenerator = aGenerator;

		this.theDistr =  aDistribution;

		this.theMin = aMinValue;
		this.theMean = aMeanValue;
                this.stdDevi = stdDev ;
                
		if (this.theDistr.equals("NegExp")) 
                    {
                    this.theMax = this.theMean * 1000.0; // approximation
                    } 
                else
		if (this.theDistr.equals("Pareto")) 
                    {
                    this.theMax = this.theMean * 1000.0; // approximation
                    } 
                else
		if (this.theDistr.equals("LogNormal")) 
                    {
                    this.theMax = this.theMean * 1000.0; // approximation
                    } 
                else 
                if (this.theDistr.equals("Uniform") | this.theDistr.equals("UniformInt")) 
                    {
                    this.theMax = (this.theMin + this.theMean - this.theMin) * 2;
                    } 
                else
		if (this.theDistr.equals("cbr")) 
                    {
			this.theMin = 0;
			this.theMean = aMeanValue;
			this.theMax = 0;
                    }                    
                else {
			this.error("WARNING: unknown distribution '" + aDistribution + "' !!!");
		}
	}

	/**
	 * @param aCbrDistribution
	 * @param aMeanValue
	 */
	public JERandomVar(String aCbrDistribution, double aMeanValue) {

		this.theDistr = aCbrDistribution;

		if (this.theDistr.equals("cbr")) {
			this.theMin = 0;
			this.theMean = aMeanValue;
			this.theMax = 0;

		} else {
			this.error("WARNING: unknown distribution '" + aCbrDistribution + "', eventually wrong constructor !!!");
		}
	}

	/**
	 * @return next value in conformance to the distribution law
         * 	
	 * Generates a random variable obeying a specific distribution.
	 * return a double which obeys the distribution 
         * Math.random() generates a random number between 0 and 1, this number is 
         * transformed depending on the distribution law by the functions below.
	 */
	public double nextvalue() {
		if (this.theDistr.equals("NegExp")) 
                    {
			double val = this.negexp(/*theUniqueRandomGenerator.nextDouble()*/);
			return (val);
                    } 
                else 
                if (this.theDistr.equals("Pareto")) 
                    {
                    double alpha = 1.2 ;     // FIXME should be read from configuration file
                    double xM = 2 ;          // FIXME should be read from configuration file
                    
			double nextDouble = this.pareto(alpha, xM);
			return nextDouble;
                    } 
                else 
                if (this.theDistr.equals("LogNormal")) 
                    {
			double nextDouble = this.lognormal();
			return nextDouble;
                    } 
                else 
                if (this.theDistr.equals("Uniform")) 
                    {
			double nextDouble = this.uniform();
			return nextDouble;
                    } 
                else 
                if (this.theDistr.equals("UniformInt")) 
                    {
			int nextInt =(int) this.uniform();
			return nextInt;
                    } 
                else 
                if (this.theDistr.equals("cbr")) 
                    {
			return (this.theMean);
                    } 
                else {
			this.message("WARNING: unknown distribution !!!", 10);
			return 0.0;
		}
	}
        
	/**
	 * @param aRand
	 * @return Double
	 */
	private double uniform() {
//		return (aRand * (this.theMax - this.theMin)) + this.theMin;
                double rnd ;
                
                do
                    {
                    rnd = this.theMax * Math.random();
                    }
                while((rnd > this.theMax)||(rnd < this.theMin));
		return rnd;

	}

	/**
	 * @param aRand
	 * @return Double
	 */
	private double negexp() {
            //  java.lang.Math.min(int a, int b) returns the smaller of two int values. 
//		return Math.min(-Math.log(1 - aRand) * this.theMean, this.theMax);
                double rnd ;
                
                do
                    {
                    rnd = (this.theMax * Math.random());
                    rnd = - Math.log(rnd) ;
                    }
                while((rnd > this.theMax)||(rnd < this.theMin));
		return rnd;
	}

	/**
	 * @param aRand
	 * @return Double
	 */
	private double pareto(double alpha, double xM) 
            {
 //               return Math.min(-Math.log(1 - Math.random()) * this.theMean, this.theMax);
             
            double rnd, v ;
                do
                    {
                    v = Math.random();
                    rnd = (this.theMean * xM) / Math.pow(v, 1.0/alpha); 
                    }
                while((rnd > this.theMax)||(rnd < this.theMin));
            return rnd ;
            }                 

	/**
	 * @param aRand
	 * @return Double
	 */
	private double lognormal() 
                {
/*                    
                double rnd, R1 = 0, R2 = 0, rNormal = 0, rLogNormal = 0, ml = 0, sl = 0;
                // Generate uniform
                do {
                        // transform lognormal parameters to normal parameters
                        ml = 2
                        * java.lang.Math.log((double) this.theMean)
                        - java.lang.Math.log(java.lang.Math.pow((double) this.stdDevi,
                                        2)
                                        + java.lang.Math.pow((double) this.theMean, 2)) / 2;
                        sl = java.lang.Math.sqrt(-2
                                        * java.lang.Math.log((double) this.theMean)
                                        + java.lang.Math.log(java.lang.Math.pow((double) this.stdDevi,
                                                        2)
                                                        + java.lang.Math.pow((double) this.theMean, 2)));
                        // generate normal
                        R1 = Math.random();
                        R2 = Math.random();
                        rNormal = ml + sl * java.lang.Math.cos(2 * 3.14 * R1)
                        * java.lang.Math.sqrt(-java.lang.Math.log(R2));
                        // generate lognormal
                        rLogNormal = java.lang.Math.exp(rNormal);
                } while ((rLogNormal >= this.theMax)||(rLogNormal <= this.theMin)); // truncate
                rnd = (float)rLogNormal;
*/                
                Random rnd = new Random() ;    
                double gauss ;
                
                do
                    {
                    gauss = rnd.nextGaussian();
                    }
                while((gauss > this.theMax)||(gauss < this.theMin));
                    
                return gauss;

                }

        private double poisson(Random r, double lambda)
        {
        double L = Math.exp(-lambda); 
            int k = 0; 
            double p = 1.0; 
            do { 
                k++; 
                p = p * r.nextDouble(); 
            } while (p > L); 

            return k - 1;     
        }
}
