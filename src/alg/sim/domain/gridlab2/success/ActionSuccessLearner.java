package alg.sim.domain.gridlab2.success;

import java.util.List;
import java.util.Map;

import alg.sim.domain.Action;
import alg.sim.domain.Agent;
import alg.sim.domain.State;
import alg.sim.domain.gridlab2.AdvancedGridLabDomain;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.solver.data.ActionReward;

public class ActionSuccessLearner implements IActionSuccessLearner
{
	private final AdvancedGridLabDomain fDomain;

	private final int[][] fGlobalRequests;
	private final int[][] fGlobalReceives;

	private final int[][][] fAgentRequests;
	private final int[][][] fAgentReceives;

	private boolean fLearned;

	public ActionSuccessLearner(AdvancedGridLabDomain pDomain, int pHorizon, int pNumAgents, int pNumActions)
	{
		this.fDomain = pDomain;

		this.fGlobalRequests = new int[pHorizon][pNumActions];
		this.fGlobalReceives = new int[pHorizon][pNumActions];

		this.fAgentRequests = new int[pNumAgents][pHorizon][pNumActions];
		this.fAgentReceives = new int[pNumAgents][pHorizon][pNumActions];

		this.fLearned = false;
	}

	@Override
	public void experienceProbability(int pTime, Map<AdvancedGridLabAgent, List<ActionReward>> pPreferences, List<Integer> pSelected, List<Integer> pAwarded)
	{
		this.fLearned = true;

		for (AdvancedGridLabAgent lAgent : pPreferences.keySet())
		{
			List<ActionReward> lPreference = pPreferences.get(lAgent);
			int                lAgentID    = lAgent.getID();
			int				   lPreferred  = pSelected.get(lAgentID);
			int                lReceived   = pAwarded.get(lAgentID);

			// All the actions between the selected (always 0) and the awarded were requested.
			for (int i = lPreferred; i <= lReceived; i++)
			{
				int lActionID = lPreference.get(i).getAction().getID();

				this.fGlobalRequests[pTime][lActionID]++;
				this.fAgentRequests[lAgentID][pTime][lActionID]++;
			}

			// But the agent only receives the awarded action.
			int lActionID = lPreference.get(lReceived).getAction().getID();
			this.fGlobalReceives[pTime][lActionID]++;
			this.fAgentReceives[lAgentID][pTime][lActionID]++;
		}
	}

	@Override
	public void forgetExperiences()
	{
		for (int i = 0; i < this.fAgentReceives.length; i++)
			for (int j = 0; j < this.fAgentReceives[i].length; j++)
				for (int k = 0; k < this.fAgentReceives[i][j].length; k++)
				{
					this.fGlobalRequests[j][k]   = 0;
					this.fGlobalReceives[j][k]   = 0;
					this.fAgentRequests[i][j][k] = 0;
					this.fAgentReceives[i][j][k] = 0;
				}

		this.fLearned = false;
	}

	private double pointEstimateActionSuccess(int pTime, Agent pAgent, Action pAction)
	{
		//int lGlobalRq = this.fGlobalRequests[pTime][pAction.getID()];
		//int lGlobalRc = this.fGlobalReceives[pTime][pAction.getID()];
		int lAgentRq  = this.fAgentRequests[pAgent.getID()][pTime][pAction.getID()];
		int lAgentRc  = this.fAgentReceives[pAgent.getID()][pTime][pAction.getID()];

		double lSuccess = 1;

		if (lAgentRq > 0)
		{
			// Agent success matches its observed success.
			lSuccess = (double) lAgentRc / lAgentRq;
		}
		//else if (lGlobalRq > 0)
		//{
		//	// Agent success matches the success of others.
		//	lSuccess = (double) lGlobalRc / lGlobalRq;
		//}
		else
		{
			if (!this.fLearned)
			{
				// Pessimistic assumption.
				lSuccess = Math.min(1, (double) this.fDomain.getActionLimit(pAction, pTime) / this.fDomain.getAgents().size());
			}
			else
			{
				// Optimistic assumption.
				lSuccess = 1;
			}
		}

		return lSuccess;
	}

	private double rangeEstimateActionSuccess(int pTime, Agent pAgent, Action pAction, int pSpread)
	{
		double lSum   = 0;
		int    lCount = 0;
		for (int t = pTime - pSpread; t <= pTime + pSpread; t++)
		{
			if (t > 0 && t < this.fDomain.getHorizon())
			{
				lSum = lSum + this.pointEstimateActionSuccess(t, pAgent, pAction);
				lCount++;
			}
		}

		return lSum / lCount;
	}

	@Override
	public double estimateActionSuccess(int pTime, Agent pAgent, State pState, Action pAction)
	{
		return this.rangeEstimateActionSuccess(pTime, pAgent, pAction, 5);
	}
}
