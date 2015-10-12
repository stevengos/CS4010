package alg.sim.solver;

import java.io.File;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import alg.sim.domain.Action;
import alg.sim.domain.Agent;
import alg.sim.domain.ArbiMDP;
import alg.sim.domain.GlobalState;
import alg.sim.domain.IAgentTransitionStream;
import alg.sim.domain.State;

public class EfficientValueIteration
{
	public static EfficientPolicyReader computeAgentPolicy(	ArbiMDP				pMDP,
															Agent				pAgent,
															List<GlobalState>	pGlobalState	)
	{
		return computeAgentPolicy(pMDP, pAgent, pGlobalState, 2013);
	}

	public static EfficientPolicyReader computeAgentPolicy(	ArbiMDP				pMDP,
															Agent				pAgent,
															List<GlobalState>	pGlobalState,
															int 				pYear)
	{
		final int					 lHorizon     = pMDP.getHorizon();
		final int					 lNumStates   = pMDP.getNumStates();
		final List<? extends Action> lActions     = pMDP.getActionList();

		final EfficientActionReward[] lActionRewards = new EfficientActionReward[lActions.size()];
		for (Action lAction : lActions)
			lActionRewards[lAction.getID()] = new EfficientActionReward(lAction);

		File lDir = new File("./scratch/policy/");
		if (!lDir.exists() && !lDir.mkdirs())
			throw new RuntimeException("Could not create directory " + lDir);

		final String				lPolicyFile = String.format("./scratch/policy/Agent%3d-%3d.policy", pAgent.getID(), pYear);
		final EfficientPolicyWriter lWriter     = new EfficientPolicyWriter(lPolicyFile, lHorizon, lNumStates, lActions.size());

		final IAgentTransitionStream lTransitionStream = pMDP.getTransitionStream(pAgent);

		final double lReportStepsize = lHorizon / 100.0;
		int          lLastReport     = 100;

		Queue<EfficientActionReward> lQueue = new PriorityQueue<EfficientActionReward>();

		for (int t = lHorizon - 1; t >= 0; t--)
		{
			final GlobalState		lGlobalState	= pGlobalState.get(t);

			lTransitionStream.seek(lGlobalState);

			for (int s = 0; s < lNumStates; s++)
			{
				final State			lAgentState    = pMDP.getState(s);

				// Compute the expected reward assigned to every potential action in this state, and sort actions by their reward.
				lQueue.clear();
				for (int a = 0; a < lActions.size(); a++)
				{
					Action lAction = lActions.get(a);

					double lFutureReward = pMDP.getRewardFunction(pAgent, lAgentState, lGlobalState, lAction);

					int lTransitions = lTransitionStream.sizeOfTransition();
					//double lProbSum = 0;
					for (int i = 0; i < lTransitions; i++)
					{
						final double lProbability    = lTransitionStream.getProbability();
						final double lExpectedReward = lWriter.getNextExpectedReward(lTransitionStream.getState());

						lFutureReward = lFutureReward + lProbability * lExpectedReward;

						/*
						if (t == 5758 && ((ETPState) lAgentState).getAirIndex() == 2)
						{
							lProbSum += lProbability;
							AdvancedGridLabDomain lDomain = (AdvancedGridLabDomain) pMDP;
							System.out.print("\n" + ((ETPState) lAgentState).getMassIndex() + " " + lAction.getID() + " " + lDomain.getState(lTransitionStream.getState()) + " " + lProbability + " " + lProbSum);
						}
						*/

						lTransitionStream.nextDestination();
					}

					/*
					if (t == 5758 && ((ETPState) lAgentState).getAirIndex() == 2)
						System.out.println("\n" + ((ETPState) lAgentState).getMassIndex() + " " + lAction.getID() + " " + lFutureReward);
					*/

					lActionRewards[lAction.getID()].setReward(lFutureReward);
					lQueue.add(lActionRewards[lAction.getID()]);
					lWriter.setActionReward(lAgentState.getID(), lAction.getID(), lFutureReward);

					lTransitionStream.nextTransition();
				}

				//double lStateValue			 = lQueue.poll().getReward();

				///*
				double lStateValue			 = 0;
				double lRemainingProbability = 1;
				Action lNextBestAction       = null;
				do
				{
					EfficientActionReward lActionReward = lQueue.poll();
								 lNextBestAction = lActionReward.getAction();
					double		 lReward		 = lActionReward.getReward();
					double		 lSuccessProb	 = pMDP.getSuccessProbability(t, pAgent, lAgentState, lNextBestAction);

					lStateValue				= lStateValue + (lRemainingProbability * lSuccessProb) * lReward;
					lRemainingProbability	= lRemainingProbability * (1 - lSuccessProb);
				}
				while (pMDP.canFail(lNextBestAction) && !lQueue.isEmpty());
				/**/

				lWriter.setExpectedReward(lAgentState.getID(), lStateValue);
			}

			lWriter.flipBuffer();

			if (t / lReportStepsize < lLastReport)
			{
				lLastReport = (int)(t / lReportStepsize);

				if (lLastReport % 10 == 0)
				{
					System.out.print("|");
				}
				else
				{
					System.out.print(".");
				}

				System.out.flush();
			}
		}

		lWriter.close();

		return new EfficientPolicyReader(lPolicyFile, lHorizon, lNumStates, lActions.size());
	}
}
