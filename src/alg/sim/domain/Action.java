package alg.sim.domain;

import java.io.Serializable;

public abstract class Action implements Serializable
{
	private static final long serialVersionUID = 5373790161103016248L;

	private static int NEXT_ID = 0;

	private final int fID = NEXT_ID++;

	private final String fLabel;

	public Action(String pLabel)
	{
		this.fLabel = pLabel;
	}

	public int getID()
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
		return "A(" + this.getID() + " - " + this.fLabel + ")";
	}
}
