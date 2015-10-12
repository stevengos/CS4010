package alg.sim.domain;

/**
 *	The Agent Transition Stream is an interface designed for highly efficient access to the
 *	transition function of an Agent.
 *
 *	This interface is designed with the Value Iteration algorithm in mind. In VI, each time-step
 *	all State * Action transitions are evaluated, to discover the best Action to take in each State.
 *	Traversal is by Action then State, and this interface therefore returns transitions in this order.
 *	The Value Iteration algorithm is responsible for keeping track which (S, A) pair is the current one.
 *
 * 
 * @author Frits de Nijs
 *
 */
public interface IAgentTransitionStream
{
	/**
	 * Initialize the current position of the stream to read for the given Global State (which is
	 * assumed to be constant for the duration of a time-step).
	 * 
	 * @param pGlobal
	 */
	public void seek(GlobalState pGlobal);

	/**
	 * Advances the stream to the next State * Action pair.
	 */
	public void nextTransition();

	/**
	 * Returns the (variable) size of the current transition.
	 */
	public int sizeOfTransition();

	/**
	 * Advances the stream to the next position in the transition function for (S, A):
	 * 		(S,A) -> (s_1->[s_x,p], s_2->[s_y,p], ...)
	 * 					^
	 * 					|
	 */
	public void nextDestination();

	/**
	 * @return s_i of the current transition.
	 */
	public int getState();

	/**
	 * @return p of the current transition.
	 */
	public double getProbability();
}
