package alg.sim.solver;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;

public class EfficientPolicyWriter
{
	private final RandomAccessFile	fFile;
	private final FileChannel		fChannel;
	private final DoubleBuffer		fBuffer;

	private final double[][] fExpectedValue;
	private final double[]   fActionValue;
	private final int        fNumActions;
	private int fCurrentBuffer;

	public EfficientPolicyWriter(String pFile, int pHorizon, int pNumStates, int pNumActions)
	{
		this.fExpectedValue = new double[2][pNumStates];
		this.fActionValue   = new double[pNumStates * pNumActions];
		this.fNumActions    = pNumActions;
		this.fCurrentBuffer = 0;

		try
		{
			this.fFile    = new RandomAccessFile(pFile, "rw");
			this.fChannel = this.fFile.getChannel();
			this.fBuffer  = this.fChannel.map(FileChannel.MapMode.READ_WRITE, 0, 8*pHorizon*pNumStates*pNumActions).asDoubleBuffer();
		}
		catch (IOException lException)
		{
			throw new RuntimeException("Failed to open the policy file '" + pFile + "'.", lException);
		}
	}

	public double getNextExpectedReward(int pNextState)
	{
		return this.fExpectedValue[1 - this.fCurrentBuffer][pNextState];
	}

	public void setExpectedReward(int pState, double pReward)
	{
		this.fExpectedValue[this.fCurrentBuffer][pState] = pReward;
	}

	public void setActionReward(int pState, int pAction, double pReward)
	{
		this.fActionValue[pState * this.fNumActions + pAction] = pReward;
	}

	public void flipBuffer()
	{
		this.fCurrentBuffer = 1 - this.fCurrentBuffer;

		this.fBuffer.put(this.fActionValue);
	}

	public void close()
	{
		try
		{
			this.fChannel.close();
			this.fFile.close();
		}
		catch (IOException lIgnored) { }
	}
}
