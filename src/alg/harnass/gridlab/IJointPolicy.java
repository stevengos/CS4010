package alg.harnass.gridlab;

import alg.sim.world.gridlab.IGridLabControllable;

/**
 *	A joint policy specifies which behavior agents should jointly perform in order to get some desired behavior. In
 *	the case of heat-pumps, it specifies which houses in the neighborhood may switch on under load limits.
 *
 * @author Frits de Nijs
 */
public interface IJointPolicy
{
	/**
	 * @return The frequency with which the policy is able to recommend actions. (Simulated seconds between consecutive applyPolicy calls).
	 */
	public int getStepsize();

	/**
	 * Applies the current policy to the provided pWorld, given that we believe the world to be in time-step pTime.
	 * 
	 * @param pTime Current time-step considered.
	 * @param pWorld World to which the policies are to be applied.
	 */
	public void applyPolicy(int pTime, IGridLabControllable pWorld);
}
