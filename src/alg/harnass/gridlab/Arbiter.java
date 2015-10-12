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
 * 
 *		The Resource Arbiter
 *
 *	The Arbiter is responsible for guarding the resource limit. Whenever agents select actions that collectively
 *	exceed the resource constraint, the arbiter decides which agents must change to their less preferred action.
 *	In the current version, that choice is made greedily, by consecutively changing the action of the agent that 
 *	loses the least utility by the change.
 *
 *
 * @author Frits de Nijs
 */
public class Arbiter
{
	/**
	 *	The Alpha parameter of logit sampling determines the degree of `randomness' in the selection. Higher
	 *	values result in less random behavior.
	 */
	public static double LOGIT_ALPHA = 10;

	/**
	 *	The random number generator of the logit sampling behavior. Setting this to a seeded random generator
	 *	ensures repeatability.
	 */
	public static Random RAND_GEN = new Random();

	private static void deterministicArbitrage(Map<? extends Agent, List<ActionReward>> pPreferences, int[] pActionLimits, int[] pActionCounts, List<Integer> pArbitratedChoice)
	{
		Agent	lChanger   = null;
		int		lNewAction = -1;
		double	lMinLoss   = Integer.MAX_VALUE;

		/*
		 * 		Find, for each agent, its best unconstrained alternative.
		 */
		for (Agent lAgent : pPreferences.keySet())
		{
			List<ActionReward> lPreferences     = pPreferences.get(lAgent);
			int				   lInitialChoice   = pArbitratedChoice.get(lAgent.getID());
			ActionReward	   lChoice			= lPreferences.get(lInitialChoice);

			// If this agent chooses a constrained action.
			if (pActionCounts[lChoice.getAction().getID()] > pActionLimits[lChoice.getAction().getID()])
			{
				int				   lBestAlternative = lInitialChoice;
				ActionReward	   lAlternative		= lPreferences.get(lBestAlternative);
				double			   lLoss			= lChoice.getReward() - lAlternative.getReward();

				/*
				 *		While, an alternative exists, and, the current alternative is constrained.
				 */
				while ((lBestAlternative+1) < lPreferences.size() && pActionCounts[lAlternative.getAction().getID()] > pActionLimits[lAlternative.getAction().getID()])
				{
					lBestAlternative++;
					lAlternative = lPreferences.get(lBestAlternative);
					lLoss		 = lChoice.getReward() - lAlternative.getReward();
				}

				/*
				 *		If, an alternative was found, see if this alternative loses the least so far.
				 */
				if (lBestAlternative != lInitialChoice && lLoss < lMinLoss)
				{
					lMinLoss   = lLoss;
					lChanger   = lAgent;
					lNewAction = lBestAlternative;
				}
			}
		}

		/*
		 *		Change the best changer's action.
		 */
		pArbitratedChoice.set(lChanger.getID(), lNewAction);
	}

	private static void logitArbitrage(Map<? extends Agent, List<ActionReward>> pPreferences, int[] pActionLimits, int[] pActionCounts, List<Integer> pArbitratedChoice)
	{
		/*
		 * 		Find, for each agent, its best unconstrained alternative.
		 */
		double              lMaxLoss   = 0;
		Map<Agent, Integer> lAgentAlt  = new HashMap<Agent, Integer>();
		Map<Agent, Double>  lAgentLoss = new HashMap<Agent, Double>();
		for (Agent lAgent : pPreferences.keySet())
		{
			List<ActionReward> lPreferences     = pPreferences.get(lAgent);
			int				   lInitialChoice   = pArbitratedChoice.get(lAgent.getID());
			ActionReward	   lChoice			= lPreferences.get(lInitialChoice);

			// If this agent chooses a constrained action.
			if (pActionCounts[lChoice.getAction().getID()] > pActionLimits[lChoice.getAction().getID()])
			{
				int				   lBestAlternative = lInitialChoice;
				ActionReward	   lAlternative		= lPreferences.get(lBestAlternative);
				double			   lLoss			= lChoice.getReward() - lAlternative.getReward();

				/*
				 *		While, an alternative exists, and, the current alternative is constrained.
				 */
				while ((lBestAlternative+1) < lPreferences.size() && pActionCounts[lAlternative.getAction().getID()] > pActionLimits[lAlternative.getAction().getID()])
				{
					lBestAlternative++;
					lAlternative = lPreferences.get(lBestAlternative);
					lLoss		 = lChoice.getReward() - lAlternative.getReward();
				}

				/*
				 *		If, an alternative was found, make this alternative possible.
				 */
				if (lBestAlternative != lInitialChoice)
				{
					lAgentLoss.put(lAgent, lLoss);
					lAgentAlt.put(lAgent, lBestAlternative);
					if (lLoss > lMaxLoss) lMaxLoss = lLoss;
				}
			}
		}

		/*
		 *		Initialize probability of selecting an agent to uniform.
		 */
		Map<Agent, Double> lAgentProb = new HashMap<Agent, Double>();
		for (Agent lAgent : lAgentLoss.keySet())
			lAgentProb.put(lAgent, (1.0 / lAgentLoss.size()));

		/*
		 *		If agents are affected by their choice, modulate probability of being selected by utility.
		 */
		if (lMaxLoss > 0)
		{
			double lSumExpected = 0;
			Map<Agent, Double> lAgentScore = new HashMap<Agent, Double>();
			for (Agent lAgent : lAgentLoss.keySet())
			{
				double lNormValue    = lAgentLoss.get(lAgent) / lMaxLoss;
				double lExpectedUtil = Math.exp(Arbiter.LOGIT_ALPHA * (1 - lNormValue));
				lAgentScore.put(lAgent, lExpectedUtil);
				lSumExpected = lSumExpected + lExpectedUtil;
			}

			for (Agent lAgent : lAgentScore.keySet())
			{
				lAgentProb.put(lAgent, lAgentScore.get(lAgent) / lSumExpected);
			}
		}

		/*
		 *		Choose a changer by the probability.
		 */
		boolean lChanged = false;
		double  lChosen  = RAND_GEN.nextDouble();
		double  lSumProb = 0;
		for (Agent lAgent : lAgentProb.keySet())
		{
			lSumProb = lSumProb + lAgentProb.get(lAgent);

			if (lChosen < lSumProb)
			{
				pArbitratedChoice.set(lAgent.getID(), lAgentAlt.get(lAgent));
				lChanged = true;
				break;
			}
		}

		if (!lChanged)
			throw new IllegalStateException("Failed to arbitrate in " + lAgentProb);
	}

	private static void randomArbiter(Map<? extends Agent, List<ActionReward>> pPreferences, int[] pActionLimits, int[] pActionCounts, List<Integer> pArbitratedChoice)
	{
		Map<Agent, Integer> lOveruserAlt = new HashMap<Agent, Integer>();

		for (Agent lAgent : pPreferences.keySet())
		{
			List<ActionReward> lPreference = pPreferences.get(lAgent);
			Integer            lChoice     = pArbitratedChoice.get(lAgent.getID());
			ActionReward       lAction     = lPreference.get(lChoice);
			int                lActionID   = lAction.getAction().getID();
			boolean            lOverused   = pActionCounts[lActionID] > pActionLimits[lActionID];

			if (lOverused)
			{
				for (int i = 0; i < lPreference.size(); i++)
				{
					ActionReward       lAltAction   = lPreference.get(i);
					int                lAltActionID = lAltAction.getAction().getID();
					boolean            lAltOverused = pActionCounts[lAltActionID] > pActionLimits[lAltActionID];

					if (!lAltOverused)
					{
						lOveruserAlt.put(lAgent, i);
						break;
					}
				}
			}
		}

		int lChanger = (int)Math.floor(Math.random() * lOveruserAlt.size());
		int lIndex   = 0;
		boolean lSwitch = false;

		for (Agent lAgent : lOveruserAlt.keySet())
		{
			if (lIndex >= lChanger)
			{
				int lNewChoice = lOveruserAlt.get(lAgent);
				pArbitratedChoice.set(lAgent.getID(), lNewChoice);
				lSwitch = true;
				break;
			}
			lIndex++;
		}

		if (!lSwitch)
			throw new IllegalStateException("Failed to arbitrate in " + pPreferences);
	}

	public static List<Integer> arbitrage(ArbiMDP pMDP, 
										  int     pTime, 
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

			lActionLimits[lActionID] = pMDP.getActionLimit(lAction, pTime);
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
