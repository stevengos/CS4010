package alg.sim.domain.gridlab2.transition;

import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab.state.IETPStateManager;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.domain.gridlab2.state.WeatherState;
import alg.sim.domain.gridlab2.state.WeatherStateManager;
import alg.sim.domain.tcl.SwitchingAction;
import alg.sim.world.gridlab.house.GridLabHouseAction;
import alg.util.clipping.inplace.ClipPolygon;
import alg.util.clipping.inplace.InplaceSutherlandHodgman;

public class AgentTransitionWriter
{
	private static final int WEATHER_SAMPLES = 25;

	private final AdvancedGridLabAgent  fAgent;
	private final WeatherStateManager   fGlobal;
	private final IETPStateManager      fLocal;
	private final List<SwitchingAction> fAction;
	private final double				fQi;

	private final String   fFile;
	private final int[]    fPosition;
	private final int[]    fState;
	private final double[] fProbability;
	private int            fBlocks;

	private final int[] fTransitionPosition;
	private final int[] fTransitioned;
	private       int   fNumTransitioned;
	private       int   fCurrentPosition;

	private final InplaceSutherlandHodgman fClipper;
	private final ClipPolygon			   fTransitionPolygon;
	private final ClipPolygon			   fClippedPolygon;

	private long fMeasurer;

	public AgentTransitionWriter(AdvancedGridLabAgent pAgent, String pFileName,
								 WeatherStateManager pGlobal, IETPStateManager pLocal, List<SwitchingAction> pAction, double pQi)
	{
		this.fAgent  = pAgent;
		this.fGlobal = pGlobal;
		this.fLocal  = pLocal;
		this.fAction = pAction;
		this.fQi	 = pQi;

		this.fBlocks = 10;
		int  pSize   = pGlobal.getNumStates() * pLocal.size() * pAction.size();

		this.fFile        = pFileName;
		this.fPosition    = new int[   pSize + 1];
		this.fState       = new int[   pSize * this.fBlocks];
		this.fProbability = new double[pSize * this.fBlocks];

		this.fTransitionPosition = new int[pLocal.size()];
		this.fTransitioned       = new int[2 * this.fBlocks];
		this.fNumTransitioned    = 0;
		this.fCurrentPosition    = 0;

		this.fClipper           = new InplaceSutherlandHodgman(4);
		this.fTransitionPolygon = new ClipPolygon(4);
		this.fClippedPolygon    = new ClipPolygon(8);

		for (int i = 0; i < this.fTransitionPosition.length; i++)
		{
			this.fTransitionPosition[i] = -1;
		}
	}

	public void computeTransitionFunction()
	{
		int lIndex    = 0;

		long computetotal = 0;
		for (int g = 0; g < this.fGlobal.getNumStates(); g++)
		{
			WeatherState lGlobal = this.fGlobal.getWeatherStateByID(g);

			for (int l = 0; l < this.fLocal.size(); l++)
			{
				ETPState lLocal = this.fLocal.get(l);

				for (int a = 0; a < this.fAction.size(); a++)
				{
					SwitchingAction lAction = this.fAction.get(a);

					/*
					if(lIndex != lAction.getID()                       + 
								 lLocal.getID()  * this.fAction.size() +
								 lGlobal.getID() * this.fAction.size() * this.fLocal.size()) System.out.println("Ahhh!");
					/**/

					long start = System.nanoTime();
					this.computeTransition(lGlobal, lLocal, lAction);
					long end = System.nanoTime();

					computetotal = computetotal + end-start;
					/*
					for (ETPState lState : lTransition.keySet())
					{
						this.fState[lPosition]       = lState.getID();
						this.fProbability[lPosition] = lTransition.get(lState);
						lPosition++;
					}
					/**/

					this.fPosition[++lIndex] = this.fCurrentPosition;
				}
			}
		}

		long start = System.nanoTime();
		this.writeTransitionFunction();
		long end   = System.nanoTime();

		//System.out.println("Write time " + (end-start)/1000000d + ", compute time " + computetotal / 1000000d + " of which " + this.fMeasurer / 1000000d);
	}

	/**
	 *
	 *	The transition function is written to file in three continuous blocks. First the position array is written,
	 *	followed by the array of destination states, and the array of probabilities.
	 *
	 * @param pFile
	 */
	private void writeTransitionFunction()
	{
		RandomAccessFile lFile    = null;
		FileChannel      lChannel = null;
		try
		{
			lFile    = new RandomAccessFile(this.fFile, "rw");
			lChannel = lFile.getChannel();

			int lNumStates      = this.fPosition.length;
			int lNumTransitions = this.fPosition[lNumStates-1];

			int lStateIndex = 4 * lNumStates;
			int lProbIndex  = 4 * lNumStates + 4 * lNumTransitions;

			IntBuffer    lBufPos   = lChannel.map(FileChannel.MapMode.READ_WRITE, 0,           4*lNumStates).asIntBuffer();
			IntBuffer    lBufState = lChannel.map(FileChannel.MapMode.READ_WRITE, lStateIndex, 4*lNumTransitions).asIntBuffer();
			DoubleBuffer lBufProb  = lChannel.map(FileChannel.MapMode.READ_WRITE, lProbIndex,  8*lNumTransitions).asDoubleBuffer();

			lBufPos.put(  this.fPosition);
			lBufState.put(this.fState,       0, lNumTransitions);	// Only write the filled values.
			lBufProb.put( this.fProbability, 0, lNumTransitions);	// Only write the filled values.
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

	private void computeTransition(WeatherState pWeather, ETPState pLocal, SwitchingAction pAction)
	{
		double lAirMin  = pLocal.getAirMin();
		double lAirMax  = pLocal.getAirMax();
		double lMassMin = pLocal.getMassMin();
		double lMassMax = pLocal.getMassMax();

		if (Double.isInfinite(lAirMin))  lAirMin  = lAirMax  - this.fLocal.getAirStep();
		if (Double.isInfinite(lAirMax))	 lAirMax  = lAirMin  + this.fLocal.getAirStep();
		if (Double.isInfinite(lMassMin)) lMassMin = lMassMax - this.fLocal.getMassStep();
		if (Double.isInfinite(lMassMax)) lMassMax = lMassMin + this.fLocal.getMassStep();

		GridLabHouseAction	  lAction     = pAction.isOn() ? GridLabHouseAction.HEAT : GridLabHouseAction.OFF;
		//Map<ETPState, Double> lTransition = new HashMap<ETPState, Double>();

		long start;
		long end;

		for (int i = 0; i < WEATHER_SAMPLES; i++)
		{
			double lOutTemp = pWeather.getOutTempMin() + i * pWeather.getOutTempStep() / WEATHER_SAMPLES;

			this.fTransitionPolygon.clear();
			this.fTransitionPolygon.addVertex(lMassMin, lAirMin); // South-West
			this.fTransitionPolygon.addVertex(lMassMax, lAirMin); // South-East
			this.fTransitionPolygon.addVertex(lMassMax, lAirMax); // North-East
			this.fTransitionPolygon.addVertex(lMassMin, lAirMax); // North-West

			for (int j = 0; j < this.fTransitionPolygon.getSize(); j++)
				this.fAgent.getModel().advanceTime(lOutTemp, this.fTransitionPolygon.getVertex(j), this.fQi, lAction);

			ETPState lSWstate = this.fLocal.getState(this.fTransitionPolygon.getY(0), this.fTransitionPolygon.getX(0));
			ETPState lNEstate = this.fLocal.getState(this.fTransitionPolygon.getY(2), this.fTransitionPolygon.getX(2));
	
			List<ETPState> lAffectedGrid      = this.fLocal.getOverlappingStates(lSWstate, lNEstate);
			double		   lTransitionArea    = this.fTransitionPolygon.getArea();

			for (ETPState lAffectedState : lAffectedGrid)
			{
				start = System.nanoTime();
				this.fClipper.clipPolygon(this.fTransitionPolygon,
										  this.fClippedPolygon,
										  lAffectedState.getAirMax(),
										  lAffectedState.getMassMin(),
										  lAffectedState.getAirMin(),
										  lAffectedState.getMassMax());
				end = System.nanoTime();
	
				double lClippedArea = this.fClippedPolygon.getArea();

				this.fMeasurer += end-start;
	
				if (lClippedArea > 0)
				{
					int    lStateID  = lAffectedState.getID();
					double lBaseArea = lClippedArea / lTransitionArea;

					if (this.fTransitionPosition[lStateID] < 0)
					{
						this.fTransitionPosition[lStateID]              = this.fCurrentPosition;
						this.fState[this.fTransitionPosition[lStateID]] = lStateID;
						this.fCurrentPosition++;

						this.fTransitioned[this.fNumTransitioned] = lStateID;
						this.fNumTransitioned++;
					}

					this.fProbability[this.fTransitionPosition[lStateID]] += lBaseArea;
				}
			}
		}

		for (int i = 0; i < this.fNumTransitioned; i++)
		{
			this.fProbability[this.fTransitionPosition[this.fTransitioned[i]]] /= WEATHER_SAMPLES;
			this.fTransitionPosition[this.fTransitioned[i]] = -1;
		}

		this.fNumTransitioned = 0;

		//return lTransition;
	}
}
