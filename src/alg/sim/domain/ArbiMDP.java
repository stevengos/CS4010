package alg.sim.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *		Arbitrage Markov Decision Process
 *
 *	An Arbitrage MDP defines the functions necessary to plan for an MDP that will be executed using
 *	the arbitrage process. Such an MDP defines the usual states, actions, transitions and rewards.
 *	Additionally, it defines a success function f(t,a) -> [0-1] and a failure risk cf(a) -> {0,1}.
 *	Actions that consume resources can fail (cf(a) = 1), if at run-time the resources are better 
 *	distributed among the other agents. The probability that it does succeed is given by f(t,a).
 *
 *	Since Arbitrage MDPs are planned in a decoupled fashion, agents must be informed of the shared
 *	world state (time, outdoor temperature) and their transition-relevant local state (air, mass
 *	temperature).
 *
 * @author Frits de Nijs
 */
public interface ArbiMDP extends MDP
{
	/**
	 *	The regular transition function using only the local state may not provide enough information
	 *	to obtain the appropriate transition function. Therefore, using this method is discouraged.
	 */
	@Override @Deprecated
	public Map<? extends State, Double> getTransitionFunction(Agent p_i, State s_i, Action a_i);

	@Override @Deprecated
	public void prepareTransitionFunction(Agent a);

	@Override @Deprecated
	public void prepareTransitionFunctions(Set<Agent> as);

	/**
	 *	This function computes the transition table for all agents. It is an expensive operation
	 *	that does not need to be performed for every execution, since it stores the intermediate
	 *	results to disk.
	 *
	 */
	public void prepareAllTransitionFunctions();

	/**
	 *	The relevant-state transition function gets the transition function that is appropriate for the
	 *	current GlobalState - local State pair.
	 *
	 * @param p_i
	 * @param s_i
	 * @param a_i
	 * @return
	 */
	public Transition getTransitionFunction(Agent p_i, RelevantState s_i, Action a_i);

	/**
	 *	Returns a list of actions, guaranteed to be in ID order.
	 *
	 * @return
	 */
	public List<? extends Action> getActionList();

	/**
	 *	Returns an efficient Stream implementation of the transition function, optimized for Value Iteration.
	 *
	 * @param p_i
	 * @return
	 */
	public IAgentTransitionStream getTransitionStream(Agent p_i);

	/**
	 *	Actions that `can fail' are at risk of being blocked by the execution time Arbitrage process.
	 *	Arbitrage assigns resources based on merit, and agents that can not be satisfied receive their
	 *	next-best feasible action.
	 *
	 * @param a_i
	 * @return
	 */
	public boolean canFail(Action a_i);

	@Deprecated
	public double getSuccessProbability(int t, Agent p_i, Action a_i);

	/**
	 *	The estimated success probability of an action is a best-effort estimate of how likely an action
	 *	is to be replaced by Arbitrage.
	 *
	 * @param a_i
	 * @return
	 */
	public double getSuccessProbability(int t, Agent p_i, State s_i, Action a_i);

	/**
	 * These two functions ensure cache consistency of the transition function by loading the transitions
	 * into memory and freeing memory once done with them.
	 * 
	 * TODO: This is the dumb way of implementing MDP.getAgentTransitions(pAgent), and letting GC kill the returned object once done...
	 * 
	 * @param pAgent
	 */
	public void loadAgent(Agent pAgent);
	public void unloadAgent(Agent pAgent);
	
	@Deprecated
	public Double getRewardFunction(Agent p_i, State s_i, Action a_i);

	public Double getRewardFunction(Agent p_i, State s_i, GlobalState g_i, Action a_i);
}
