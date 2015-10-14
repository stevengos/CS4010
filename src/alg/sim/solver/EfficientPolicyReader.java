package alg.sim.solver;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import alg.sim.domain.Action;
import alg.sim.solver.data.ActionReward;

public class EfficientPolicyReader
{
	private final RandomAccessFile	fFile;
	private final FileChannel		fChannel;

	private final double[] fExpectedValue;
	private final int fStepsize;
	private final int fNumActions;
	private final int fInitialPosition;
	private int fCurrentPosition;

	public EfficientPolicyReader(String pFile, int pHorizon, int pNumStates, int pNumActions)
	{
		this.fExpectedValue   = new double[pNumStates*pNumActions];
		this.fInitialPosition = 8*pNumStates*pHorizon*pNumActions;
		this.fStepsize        = 8*pNumStates*pNumActions;
		this.fNumActions      = pNumActions;

		try
		{
			this.fFile    = new RandomAccessFile(pFile, "rw");
			this.fChannel = this.fFile.getChannel();
		}
		catch (IOException lException)
		{
			throw new RuntimeException("Failed to open the policy file '" + pFile + "'.", lException);
		}

		this.resetReader();
	}

	public void resetReader()
	{
		this.fCurrentPosition = this.fInitialPosition;

		this.nextTimeStep();
	}

	public double getExpectedReward(int pState, int pAction)
	{
		return this.fExpectedValue[pState*this.fNumActions + pAction];
	}

	public List<ActionReward> getActionReward(int pState, Set<? extends Action> pActions)
	{
		int lIndex = pState * this.fNumActions;

		List<ActionReward> lActionRewards = new ArrayList<ActionReward>();
		for (Action lAction : pActions)
		{
			lActionRewards.add(new ActionReward(lAction, this.fExpectedValue[lIndex + lAction.getID()]));
		}

		return lActionRewards;
	}

	public void nextTimeStep()
	{
		this.fCurrentPosition = this.fCurrentPosition - this.fStepsize;

		try
		{
		    DoubleBuffer lBuffer = this.fChannel.map(MapMode.READ_ONLY, this.fCurrentPosition, this.fStepsize).asDoubleBuffer();
		    lBuffer.get(this.fExpectedValue);
		}
		catch (IOException lException)
		{
			throw new RuntimeException("Failed to read the policy file.", lException);
		}
	}
}
