package alg.sim.domain.gridlab2.success;

import java.util.List;
import java.util.Map;

import alg.sim.domain.Action;
import alg.sim.domain.Agent;
import alg.sim.domain.State;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.solver.data.ActionReward;

public interface IActionSuccessLearner
{
	public void experienceProbability(int pTime, Map<AdvancedGridLabAgent, List<ActionReward>> pPreferences, List<Integer> pSelected, List<Integer> pAwarded);

	public void forgetExperiences();

	public double estimateActionSuccess(int pTime, Agent pAgent, State pState, Action pAction);
}
