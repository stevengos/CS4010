package alg.sim.domain.tcl;

import alg.sim.domain.Action;

public class SwitchingAction extends Action
{
	private static final long serialVersionUID = -3916500860380038619L;

	private final boolean fAction;

	public SwitchingAction(boolean pAction)
	{
		super(pAction ? "ON" : "OFF");

		this.fAction = pAction;
	}

	public boolean isOn()
	{
		return this.fAction;
	}

	@Override
	public int getID()
	{
		return (this.isOn() ? 1 : 0);
	}

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	@Override
	public boolean equals(Object pOther)
	{
		if (pOther instanceof SwitchingAction)
		{
			SwitchingAction lThat = (SwitchingAction) pOther;

			return (this.isOn() == lThat.isOn());
		}

		return false;
	}
}
