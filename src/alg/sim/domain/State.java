package alg.sim.domain;

import java.io.Serializable;

public abstract class State implements Serializable
{
	private static final long serialVersionUID = 2571920331097241305L;

	private final String fLabel;

	public State(String pLabel)
	{
		this.fLabel = pLabel;
	}

	public abstract int getID();

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	@Override
	public String toString()
	{
		return String.format("State(%3d - %s)", this.getID(), this.fLabel);
	}
}
