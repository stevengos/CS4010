package alg.harnass.gridlab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import alg.sim.domain.Action;
import alg.sim.domain.Agent;
import alg.sim.domain.ArbiMDP;
import alg.sim.solver.data.ActionReward;
/**
 * Extensie van Arbiter.java. Deze is omgebouwd zodat deze werkt met het GLMutliLevelPlanningHarnass
 * @author Christian
 *
 */
public class MultiLevelArbiter extends Arbiter {
	public static List<Integer> arbitrage(ArbiMDP pMDP, 
			int     pTime, int pLimit,
			Map<? extends Agent, List<ActionReward>> pPreferences,
					List<Integer> pChosenAction,
					ArbiterMode pMode)
					{
		List<Integer> lArbitratedChoice = new ArrayList<Integer>(pChosenAction);
		List<Action>  lActions          = new ArrayList<Action>(pMDP.getActions());

		int[] lActionLimits = new int[lActions.size()];
		for (int i = 0; i < lActions.size(); i++)
		{
			Action lAction   = lActions.get(i);
			int    lActionID = lAction.getID();

			lActionLimits[lActionID] = pLimit;
		}

		boolean lOverUse = false;
		do
		{
			/*
			 *		Measure current action usage.
			 */
			int[] lActionCounts = new int[lActions.size()];
			for (Agent lAgent : pPreferences.keySet())
			{
				int          lChosenAction = lArbitratedChoice.get(lAgent.getID());
				ActionReward lActReward    = pPreferences.get(lAgent).get(lChosenAction);
				Action       lAction       = lActReward.getAction();

				lActionCounts[lAction.getID()]++;
			}

			/*
			 *		Determine if overuse occurs.
			 */
			lOverUse = false;
			for (int i = 0; i < lActions.size(); i++)
			{
				if (lActionCounts[i] > lActionLimits[i])
				{
					lOverUse = true;
				}
			}

			/*
			 *		When overuse occurs, select the agent with the best unconstrained alternative, and change his action.
			 */
			if (lOverUse)
			{
				switch (pMode)
				{
				case Deterministic:
				{
					Arbiter.deterministicArbitrage(pPreferences, lActionLimits, lActionCounts, lArbitratedChoice);
					break;
				}
				case Random:
				{
					Arbiter.randomArbiter(pPreferences, lActionLimits, lActionCounts, lArbitratedChoice);
					break;
				}
				case Logit:
				{
					Arbiter.logitArbitrage(pPreferences, lActionLimits, lActionCounts, lArbitratedChoice);
					break;
				}
				}
			}
		}
		while (lOverUse);

		return lArbitratedChoice;
					}
}
