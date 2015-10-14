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
	public static int[] arbitrage(int availablePower, double[] pGroupPenalty, int[] pGroupDesiredPower)
	{
		int lTotalDesiredPower = 0;
		//Check if there is a power constraint
		for(int i = 0; i < pGroupDesiredPower.length; i++)
			lTotalDesiredPower += pGroupDesiredPower[i];
		
		if(lTotalDesiredPower <= availablePower) {
			return pGroupDesiredPower;
		}
		
		
		
		//Otherwise, perform arbitration
		return new int[] {1, 1, 1};
	}
}
