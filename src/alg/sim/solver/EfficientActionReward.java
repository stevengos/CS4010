package alg.sim.solver;

import java.util.PriorityQueue;

import alg.sim.domain.Action;
import alg.sim.domain.tcl.SwitchingAction;

public class EfficientActionReward implements Comparable<EfficientActionReward>
{
	private final Action fAction;
	private double fReward;

	public EfficientActionReward(Action pAction)
	{
		this.fAction = pAction;
	}

	public Action getAction()
	{
		return this.fAction;
	}

	public void setReward(double pReward)
	{
		this.fReward = pReward;
	}

	public double getReward()
	{
		return this.fReward;
	}

	@Override
	public int compareTo(EfficientActionReward o)
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
		PriorityQueue<EfficientActionReward> lQueue = new PriorityQueue<EfficientActionReward>();

		EfficientActionReward lRewardOn  = new EfficientActionReward(new SwitchingAction(true));
		EfficientActionReward lRewardOff = new EfficientActionReward(new SwitchingAction(false));

		lRewardOn.setReward(3.1);
		lRewardOff.setReward(3.0);

		lQueue.add(lRewardOn);
		lQueue.add(lRewardOff);

		System.out.println(lQueue.peek().getAction());
	}
}
