package alg.sim.domain.gridlab.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import alg.sim.domain.tcl.SwitchingAction;

public class GridLabActionManager extends HashSet<SwitchingAction>
{
	private static final long serialVersionUID = -4904999636487129642L;

	private final SwitchingAction fOff;

	private final SwitchingAction fOn;

	private final List<SwitchingAction> fActions;

	public GridLabActionManager()
	{
		this.fOff = new SwitchingAction(false);
		this.fOn  = new SwitchingAction(true);

		this.add(this.fOff);
		this.add(this.fOn);

		this.fActions = Arrays.asList(new SwitchingAction[] { this.fOff, this.fOn });
	}

	public List<SwitchingAction> getActions()
	{
		return this.fActions;
	}

	public SwitchingAction getOnAction()
	{
		return this.fOn;
	}
}
