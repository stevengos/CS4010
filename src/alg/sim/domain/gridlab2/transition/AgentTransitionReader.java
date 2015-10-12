package alg.sim.domain.gridlab2.transition;

import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import alg.sim.domain.GlobalState;
import alg.sim.domain.IAgentTransitionStream;
import alg.sim.domain.Transition;
import alg.sim.domain.TransitionProbability;
import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab.state.IETPStateManager;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.domain.gridlab2.state.WeatherState;
import alg.sim.domain.gridlab2.state.WeatherStateManager;
import alg.sim.domain.tcl.SwitchingAction;

public class AgentTransitionReader implements IAgentTransitionStream
{
	private final IETPStateManager      fLocal;
	private final List<SwitchingAction> fAction;

	private final int[]    fPosition;
	private final int[]    fState;
	private final double[] fProbability;

	private int fTransition;
	private int fTransitionIndex;

	public AgentTransitionReader(AdvancedGridLabAgent pAgent, String pFileName,
								 WeatherStateManager pGlobal, IETPStateManager pLocal, List<SwitchingAction> pAction)
	{
		int lNumStates = pGlobal.getNumStates() * pLocal.size() * pAction.size();
		this.fLocal    = pLocal;
		this.fAction   = pAction;
		this.fPosition = new int[lNumStates + 1];

		RandomAccessFile lFile    = null;
		FileChannel      lChannel = null;
		try
		{
			lFile    = new RandomAccessFile(pFileName, "r");
			lChannel = lFile.getChannel();

			IntBuffer lBufPos = lChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4*this.fPosition.length).asIntBuffer();
			lBufPos.get(this.fPosition);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		int lNumTransitions = this.fPosition[this.fPosition.length-1];
		this.fState         = new int[lNumTransitions];
		this.fProbability   = new double[lNumTransitions];

		try
		{
			int lStateOffset = 4*this.fPosition.length;
			int lProbOffset  = 4*this.fState.length + lStateOffset; 

			IntBuffer    lBufState = lChannel.map(FileChannel.MapMode.READ_ONLY, lStateOffset, 4*this.fState.length).asIntBuffer();
			DoubleBuffer lBufProb  = lChannel.map(FileChannel.MapMode.READ_ONLY, lProbOffset,  8*this.fProbability.length).asDoubleBuffer();

			lBufState.get(this.fState);
			lBufProb.get(this.fProbability);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				lChannel.close();
				lFile.close();
			}
			catch (Exception e) { }
		}
	}

	public Transition getTransitionFunction(WeatherState pGlobal, ETPState pLocal, SwitchingAction pAction)
	{
		int lIndex = pAction.getID()                       + 
					 pLocal.getID()  * this.fAction.size() +
					 pGlobal.getID() * this.fAction.size() * this.fLocal.size();

		TransitionProbability[] lTransitions = new TransitionProbability[this.fPosition[lIndex+1] - this.fPosition[lIndex]];
		for (int i = this.fPosition[lIndex]; i < this.fPosition[lIndex+1]; i++)
		{
			lTransitions[i - this.fPosition[lIndex]] = new TransitionProbability(this.fState[i], this.fProbability[i]);
		}

		return new Transition(lTransitions);
	}

	@Override
	public void seek(GlobalState pGlobal)
	{
		this.fTransition      = pGlobal.getTransitionPosition() * this.fAction.size() * this.fLocal.size();
		this.fTransitionIndex = this.fPosition[this.fTransition];
	}

	@Override
	public void nextTransition()
	{
		this.fTransition++;
	}

	@Override
	public int sizeOfTransition()
	{
		return this.fPosition[this.fTransition+1] - this.fPosition[this.fTransition];
	}

	@Override
	public void nextDestination()
	{
		this.fTransitionIndex++;
	}

	@Override
	public int getState()
	{
		return this.fState[this.fTransitionIndex];
	}

	@Override
	public double getProbability()
	{
		return this.fProbability[this.fTransitionIndex];
	}
}
