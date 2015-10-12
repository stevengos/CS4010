package alg.sim.solver.data;

import java.util.PriorityQueue;

import alg.sim.domain.Action;
import alg.sim.domain.tcl.SwitchingAction;

public class ActionReward implements Comparable<ActionReward>
{
	private final Action fAction;
	private       double fReward;

	public ActionReward(Action pAction, double pReward)
	{
		this.fAction = pAction;
		this.fReward = pReward;
	}

	public Action getAction()
	{
		return this.fAction;
	}

	public double getReward()
	{
		return this.fReward;
	}

	@Override
	public int compareTo(ActionReward o)
	{
		if (this.getReward() > o.getReward())
			return -1;

		else if (this.getReward() < o.getReward())
			return 1;

		else
			return 0;
	}

	@Override
	public String toString()
	{
		String lString = String.format("[Action: %s, Reward: %7.2f]", this.fAction.toString(), this.fReward);

		return lString;
	}

	public static void main(String[] args)
	{
		PriorityQueue<ActionReward> lQueue = new PriorityQueue<ActionReward>();

		lQueue.clear();
		lQueue.add(new ActionReward(new SwitchingAction(true),  3.10));
		lQueue.add(new ActionReward(new SwitchingAction(false), 3.0));
		System.out.println(lQueue.peek().getAction());

		lQueue.clear();
		lQueue.add(new ActionReward(new SwitchingAction(false), 3.0));
		lQueue.add(new ActionReward(new SwitchingAction(true),  3.0));
		System.out.println(lQueue.peek().getAction());

		lQueue.clear();
		lQueue.add(new ActionReward(new SwitchingAction(true),  3.0));
		lQueue.add(new ActionReward(new SwitchingAction(false), 3.0));
		System.out.println(lQueue.peek().getAction());
	}

	public void shapeReward(int pShape)
	{
		this.fReward = this.fReward * pShape;
	}
}
