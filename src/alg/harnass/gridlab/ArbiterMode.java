package alg.harnass.gridlab;

/**
 *
 *	Defines the available decision styles of the Arbiter.
 *
 * 
 *	@author Frits de Nijs
 *
 */
public enum ArbiterMode
{
	/**
	 *	Purely deterministic selection, select the largest subset of agents that benefit the most.
	 */
	Deterministic,

	/**
	 *	Purely random selection, selects the largest subset of agents that want to be on through random trials.
	 */
	Random,

	/**
	 *	Mix between deterministic and random, chooses the largest subset of agents with probability equal to their benefit.
	 */
	Logit;
}
