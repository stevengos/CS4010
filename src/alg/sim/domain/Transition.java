package alg.sim.domain;

public class Transition
{
	private final TransitionProbability[] fProbabilities;

	public Transition(TransitionProbability[] pProbabilities)
	{
		this.fProbabilities = pProbabilities;
	}

	public TransitionProbability[] getProbabilities()
	{
		return this.fProbabilities;
	}

	@Override
	public String toString()
	{
		String lTransition = "Transition:";

		for (int i = 0; i < this.fProbabilities.length; i++)
		{
			lTransition += "\n  [" + this.fProbabilities[i].toString() + "]";
		}

		return lTransition;
	}
}
