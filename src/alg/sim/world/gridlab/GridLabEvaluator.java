package alg.sim.world.gridlab;

import java.util.ArrayList;
import java.util.List;

import alg.sim.world.gridlab.house.GridLabHouseModel;

/**
 *		GridLAB Evaluator
 *
 *	Due to legacy mistakes, the GridLabSim is locked to a sample frequency that is identical to the planning frequency. This
 *	means that a plan made at 60 second per step would make less `mistakes' than the same plan made at 30 second per step.
 *	Clearly this is undesirable for accuracy. Thus, this class `converts' the flawed Simulator into an evaluator that always
 *	advances time by 1 second at a time, while respecting the sample frequency of the weather and load data.
 *
 * 
 * @author Frits de Nijs
 *
 */
public class GridLabEvaluator implements IGridLabControllable
{
	//	GridLabSim Design Variables
	private final List<GridLabHouseModel> fNeighborhood;
	private final List<Double>            fTemperatureProgression;
	private final List<Double>            fTotalLoadProgression;
	private final List<Double>            fHeatingLoadProgression;

	/**	Seconds between samples in the Progression Lists. */
	private final int					  fSampleStepsizeS;

	//	Current State Variables
	private int    fCurrentSecond;
	private double fCurrentOutTemp;
	private double fCurrentTotalLoad;
	private double fCurrentHeatLoad;

	public GridLabEvaluator(IGridLabWorld pSimulator, int pSampleStepsizeS)
	{
		// Copy the Simulator progressions.
		this.fNeighborhood           = new ArrayList<GridLabHouseModel>();
		this.fTemperatureProgression = pSimulator.getTemperatureProgressionCopy();
		this.fTotalLoadProgression   = pSimulator.getTotalLoadProgressionCopy();
		this.fHeatingLoadProgression = pSimulator.getHeatingLoadProgressionCopy();
		this.fSampleStepsizeS        = pSampleStepsizeS;

		// Copy the Simulator neighborhood.
		this.copyNeighborhood(pSimulator);

		// Ensure correct initial state.
		this.reset();
	}

	/**
	 *	Copies the neighborhood simulated by the pSimulator, changing the sample rate
	 *	to one second per step in the process.
	 *
	 *	@param pSimulator The GridLabSim to copy the neighborhood from.
	 */
	private void copyNeighborhood(IGridLabWorld pSimulator)
	{
		for (int i = 0; i < pSimulator.getSize(); i++)
		{
			// Copy the original house with single-second time-steps.
			GridLabHouseModel lOriginal = pSimulator.getHouse(i);
			GridLabHouseModel lCopy     = lOriginal.copyOf(this.getStepsize());

			this.fNeighborhood.add(lCopy);
		}
	}

	/**
	 *	@return The size of a single advanceTime() step of the world, fixed to one second.
	 */
	public int getStepsize()
	{
		return 1;
	}

	/**
	 *	@return The size of the simulated neighborhood.
	 */
	public int getSize()
	{
		return this.fNeighborhood.size();
	}

	/**
	 * @throws IndexOutOfBoundsException if pID >= getSize().
	 * 
	 * @param pID House index in the neighborhood.
	 * 
	 * @return The house model at this index.
	 */
	public GridLabHouseModel getHouse(int pID)
	{
		return this.fNeighborhood.get(pID);
	}

	/**
	 *	@return The current outdoor temperature (in degrees Fahrenheit) in the World.
	 */
	public double getOutdoorTemp()
	{
		return this.fCurrentOutTemp;
	}

	/**
	 *	@return The current background load (in kW).
	 */
	public double getBackgroundLoad()
	{
		return this.fCurrentTotalLoad;
	}

	/**
	 *	Return the simulator state to its initial state (to allow multiple runs). Without randomness
	 *	in the evolution, there is not much point to this though...
	 *
	 *	TODO: Decide if we want to add randomness to the starting condition.
	 */
	public void reset()
	{
		// Reset each house to its initial temperature.
		for (GridLabHouseModel lModel : this.fNeighborhood)
		{
			lModel.reset();
		}

		// Reset the state variables.
		this.fCurrentSecond    = 0;
		this.fCurrentOutTemp   = 0;
		this.fCurrentTotalLoad = 0;
		this.fCurrentHeatLoad  = 0;
	}

	/**
	 *	Advances the state of the world by one simulated second.
	 */
	public void advanceWorld()
	{
		// If the current second lines up with the sample size of the progressions,
		if (this.fCurrentSecond % this.fSampleStepsizeS == 0)
		{
			// Update the current environment state.
			final int lCurrentTime = this.fCurrentSecond / this.fSampleStepsizeS;
			this.fCurrentOutTemp   = this.fTemperatureProgression.get(lCurrentTime);
			this.fCurrentTotalLoad = this.fTotalLoadProgression.get(  lCurrentTime);
			this.fCurrentHeatLoad  = this.fHeatingLoadProgression.get(lCurrentTime);
		}

		// Advanced simulator by one second.
		this.fCurrentSecond++;
	}

	/**
	 *	Advances the entire neighborhood by one simulated second.
	 *
	 *	@return Real load in kW drawn by the entire neighborhood, including any active heat-pumps.
	 */
	public double advanceNeighborhood()
	{
		// Update each house according to its current temperature, current selected action and the current environment.
		double lCurrentLoad = this.fCurrentTotalLoad;
		for (GridLabHouseModel lHouse : this.fNeighborhood)
		{
			lCurrentLoad += lHouse.advanceTime(this.fCurrentOutTemp, this.fCurrentHeatLoad);
		}

		// Return the total load of the neighborhood including heat-pumps.
		return lCurrentLoad;
	}
}
