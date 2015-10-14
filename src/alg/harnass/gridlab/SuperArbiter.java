package alg.harnass.gridlab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import alg.sim.domain.Action;
import alg.sim.domain.Agent;
import alg.sim.domain.ArbiMDP;
import alg.sim.solver.data.ActionReward;

/**
 * Arbiter which devides power among multiple neighboorhoods
 * @author Christian
 *
 */
public class SuperArbiter {
	public static int fDesiredArbiterType;
	
	public void setDesiredArbiterType(int pDesiredArbiterType){
		fDesiredArbiterType = pDesiredArbiterType;
	}
	
	public static int[] arbitrage(int availablePower, double[] pGroupPenalty, int[] pGroupDesiredPower)
	{
		int[]	returnedPower = null;
		int		remainingPower = availablePower;
		
		int lTotalDesiredPower = 0;
		//Check if there is a power constraint
		for(int i = 0; i < pGroupDesiredPower.length; i++)
			lTotalDesiredPower += pGroupDesiredPower[i];
		
		if(lTotalDesiredPower <= availablePower) {
			return pGroupDesiredPower;
		}
		
		switch(fDesiredArbiterType){
			case(1):{ //give everyone its power until no power is left
				for(int i = 0; i < pGroupDesiredPower.length; i++){
					if(remainingPower > 0){
						if(remainingPower - pGroupDesiredPower[i] < 0){
							returnedPower[i] = remainingPower;
						}
						else{
							returnedPower[i] = pGroupDesiredPower[i];
						}
					}
					else{
						returnedPower[i] = 0;
					}
					
					remainingPower = remainingPower - pGroupDesiredPower[i];
				}
			}
			default:{
				for(int i = 0; i < pGroupDesiredPower.length; i++){
					returnedPower[i] = pGroupDesiredPower[i];
				}
			}
		}
		
		//Otherwise, perform arbitration
		return returnedPower;
	}
}
