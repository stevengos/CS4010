package alg.sim.domain;

import java.util.Map;
import java.util.Set;

public interface MDP
{
	public void prepareTransitionFunction(Agent a);

	public void prepareTransitionFunctions(Set<Agent> as);

	public int getHorizon();

	public int getNumStates();

	public State getState(int pID);

	public Set<? extends Action> getActions();

	public Set<? extends Action> getActions(int pTime);

	public Set<? extends Agent> getAgents();

	public Map<? extends State, Double> getTransitionFunction(Agent p_i, State s_i, Action a_i);

	public Double getRewardFunction(Agent p_i, State s_i, Action a_i);

	public Integer getActionLimit(Action a_i, int t);

	//public Agent toMetaAgent(Set<Agent> as);

	@Deprecated
	public World instantiateWorld();
}
