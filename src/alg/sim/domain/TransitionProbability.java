package alg.sim.domain;

public class TransitionProbability
{
	private final int    fState;
	private final double fProbability;

	public TransitionProbability(int pState, double pProbability)
	{
		this.fState       = pState;
		this.fProbability = pProbability;
	}

	public int getState()
	{
		return this.fState;
	}

	public double getProbability()
	{
		return this.fProbability;
	}

	@Override
	public String toString()
	{
		return String.format("%7d | %7.5f", this.fState, this.fProbability);
	}
}
