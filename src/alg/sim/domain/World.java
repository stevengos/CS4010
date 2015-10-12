package alg.sim.domain;

import java.util.Map;
import java.util.Set;

public interface World
{
	public int getCurrentTime();

	public Map<? extends Agent, ? extends State> getCurrentState();

	public State getAverageState(Set<Agent> pAgents);

	public void advanceTime(Map<Agent, Action> pActions);

	public void attachObserver(WorldObserver pObserver);
}
