package alg.sim.domain;

import java.io.Serializable;

import alg.util.cluster.data.IClusterable;

public abstract class Agent implements IClusterable, Serializable
{
	private static final long serialVersionUID = 4245486317802078119L;

	private final int fID;

	public Agent(int pID)
	{
		this.fID = pID;
	}

	public final int getID()
	{
		return this.fID;
	}

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	@Override
	public String toString()
	{
		return "Agent(" + this.getID() + ")";
	}
}

