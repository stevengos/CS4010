package alg.sim.domain;

public interface WorldObserver
{
	public void observedWorld(World lWorld);

	public void observeState(int pTime, Agent pAgent, Object pNewAgentState, double pError);
}
