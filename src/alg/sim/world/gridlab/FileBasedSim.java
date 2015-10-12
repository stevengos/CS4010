package alg.sim.world.gridlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import alg.gridlabd.builder.models.glm.AdvancedHouseGLM;
import alg.gridlabd.builder.models.glm.HouseIntegrityLevel;
import alg.gridlabd.builder.models.glm.HouseThermostat;
import alg.gridlabd.builder.models.glm.MeterGLM;
import alg.sim.world.gridlab.house.GridLabHouseModel;

public class FileBasedSim implements IGridLabWorld, IGridLabControllable
{
	//	State randomizer.
	private Random fRandom;

	//	GridLabSim Design Variables
	private         List<GridLabHouseModel> fNeighborhood;
	protected final List<Double>            fTemperatureProgression;
	protected final List<Double>            fTotalLoadProgression;
	protected final List<Double>            fHeatingLoadProgression;
	private         double	   	            fLoadLimitkW;

	//	Scaling of the time series
	protected final int fSecondsBetweenData;
	protected       int fSecondsBetweenSteps;

	//	Initial State Variables
	protected int    fInitialStep;
	protected int    fFinalStep;

	//	Current State Variables
	protected int  fCurrentStep;
	private double fCurrentOutTemp;
	private double fCurrentTotalLoad;
	private double fCurrentHeatLoad;

	public FileBasedSim(String pFilename, int pHouses, HouseIntegrityLevel pLevel, double pSetpoint, long pSeed, double pSolarkWpeak)
	{
		// For now, assume the sample size is fixed.
		final int lSampleStep = 30;

		this.fNeighborhood			 = new ArrayList<GridLabHouseModel>();
		this.fTemperatureProgression = new ArrayList<Double>();
		this.fTotalLoadProgression	 = new ArrayList<Double>();
		this.fHeatingLoadProgression = new ArrayList<Double>();

		this.fSecondsBetweenData  = lSampleStep;
		this.fSecondsBetweenSteps = lSampleStep;

		this.fInitialStep      = 0;
		this.fFinalStep        = 0;
		this.fCurrentStep      = 0;
		this.fCurrentOutTemp   = 0;
		this.fCurrentTotalLoad = 0;
		this.fCurrentHeatLoad  = 0;

		this.fRandom = new Random();

		this.initializeNeighborhood(pHouses, pLevel, pSetpoint, pSeed, lSampleStep);

		try
		{
			this.initializeProgressions(pFilename, pHouses, pSolarkWpeak);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void initializeNeighborhood(int pHouses, HouseIntegrityLevel pLevel, double pSetpoint, long pSeed, int pSimStep)
	{
		Random lRandGen = new Random(pSeed);
		for (int i = 0; i < pHouses; i++)
		{
			AdvancedHouseGLM  lHouse = new AdvancedHouseGLM(i, new MeterGLM(i, "h"), HouseThermostat.NONE, pLevel, pSetpoint, lRandGen);
			GridLabHouseModel lModel = new GridLabHouseModel(pSimStep, lHouse, pSetpoint, pSetpoint);

			this.fNeighborhood.add(lModel);
		}
	}

	protected void initializeProgressions(String pFilename, int pHouses, double pSolarkWpeak) throws IOException
	{
		BufferedReader lIn = new BufferedReader(new FileReader(new File(pFilename)));

		// Read the header.
		String lLine = lIn.readLine();

		// Read the data.
		while ((lLine = lIn.readLine()) != null)
		{
			String[] lSplit = lLine.split(",");

			double lZIPload   = Double.parseDouble(lSplit[1]); // kW
			double lBackHeat  = Double.parseDouble(lSplit[2]); // BTU
			double lTotalLoad = Double.parseDouble(lSplit[3]); // W
			double lOutTemp   = Double.parseDouble(lSplit[4]); // F

			// Compute the kW of power produced by 1 kW worth of solar panels.
			double lSolarGain = lTotalLoad/1000 - lZIPload;

			// Scale the load curves for the number of houses and installed solar potential.
			lZIPload   = lZIPload   * pHouses;
			lSolarGain = lSolarGain * pHouses * pSolarkWpeak;

			this.fTemperatureProgression.add(lOutTemp);
			this.fTotalLoadProgression.add(lSolarGain + lZIPload);
			this.fHeatingLoadProgression.add(lBackHeat);
		}

		lIn.close();
	}

	protected void updateCurrentValues()
	{
		int lIndex = (this.fCurrentStep * this.fSecondsBetweenSteps / this.fSecondsBetweenData);

		// If out-of-bounds, assume constant behavior from then on.
		if (lIndex < this.fTemperatureProgression.size())
		{
			this.fCurrentOutTemp   = this.fTemperatureProgression.get(lIndex);
			this.fCurrentTotalLoad = this.fTotalLoadProgression.get(lIndex);
			this.fCurrentHeatLoad  = this.fHeatingLoadProgression.get(lIndex);
		}
	}

	public void setSimProperties(int pInitialSecond, int pDuration, int pStepSize, double pLimitkW)
	{
		this.fSecondsBetweenSteps = pStepSize;
		this.fInitialStep         = pInitialSecond / pStepSize;
		this.fFinalStep           = (pInitialSecond + pDuration) / pStepSize;
		this.fLoadLimitkW         = pLimitkW;

		for (GridLabHouseModel lHouse : this.fNeighborhood)
			lHouse.setDelta(pStepSize);

		this.reset();
	}
	
	public void setSimPropertiesForContinuation(int pInitialSecond, int pDuration) {
		this.fInitialStep         = pInitialSecond / this.fSecondsBetweenSteps;
		this.fFinalStep           = (pInitialSecond + pDuration) / this.fSecondsBetweenSteps;

		List<GridLabHouseModel> newNeighborhood = new ArrayList<>(this.fNeighborhood.size());
		
		for (GridLabHouseModel lHouse : this.fNeighborhood) {
			GridLabHouseModel lNewHouse = lHouse.continuationCopyOf(this.fSecondsBetweenSteps);
			newNeighborhood.add(lNewHouse);
		}
		
		this.fNeighborhood = newNeighborhood;

		this.reset();
	}

	@Override
	public int getStepsize()
	{
		return this.fSecondsBetweenSteps;
	}

	@Override
	public int getSize()
	{
		return this.fNeighborhood.size();
	}

	@Override
	public GridLabHouseModel getHouse(int pID)
	{
		return this.fNeighborhood.get(pID);
	}

	@Override
	public double getOutdoorTemp()
	{
		return this.fCurrentOutTemp;
	}

	@Override
	public double getBackgroundLoad()
	{
		return this.fCurrentTotalLoad;
	}

	public double getHeatingLoad()
	{
		return this.fCurrentHeatLoad;
	}

	@Override
	public List<Integer> computeMaximumOnEstimate()
	{
		List<Integer> lMaximumOnProgression = new ArrayList<Integer>();

		for (int t = this.fInitialStep; t < this.fFinalStep; t++)
		{
			int    lIndex     = (t * this.fSecondsBetweenSteps / this.fSecondsBetweenData);
			double lLoad      = this.fTotalLoadProgression.get(lIndex);
			double lOut       = this.fTemperatureProgression.get(lIndex);
			double lAvailable = this.fLoadLimitkW - lLoad;
			double lAvgLoad   = this.fNeighborhood.get(0).estimateLoad(lOut);

			lMaximumOnProgression.add((int) Math.max(0, (lAvailable / lAvgLoad)));
		}

		return lMaximumOnProgression;
	}

	@Override
	public double computeAverageBackground()
	{
		double lAvgLoad = 0;

		for (int t = this.fInitialStep; t < this.fFinalStep; t++)
		{
			int lIndex = (t * this.fSecondsBetweenSteps / this.fSecondsBetweenData);

			lAvgLoad = lAvgLoad + this.fHeatingLoadProgression.get(lIndex);
		}

		return lAvgLoad / this.getHorizon();
	}

	@Override
	public List<Double> getTemperatureProgressionCopy()
	{
		List<Double> lTemperatureProgression = new ArrayList<Double>();

		for (int t = this.fInitialStep; t < this.fFinalStep; t++)
		{
			int lIndex = (t * this.fSecondsBetweenSteps / this.fSecondsBetweenData);

			lTemperatureProgression.add(this.fTemperatureProgression.get(lIndex));
		}

		return lTemperatureProgression;
	}
	
	@Override
	public List<Double> getPriceProgressionCopy() {
		// NOT IMPLEMENTED. Should use child class.
		System.err.println("getPriceProgressionCopy is not implemented for FileBasedSim. You require FileBasedWithPriceSim.");
		return new ArrayList<>();
	}

	@Override
	public List<Double> getSolarProgressionCopy() {
		// NOT IMPLEMENTED. Should use child class.
		System.err.println("getSolarProgressionCopy is not implemented for FileBasedSim. You require FileBasedWithPriceSim.");
		return new ArrayList<>();
	}

	@Override
	public List<Double> getTotalLoadProgressionCopy()
	{
		List<Double> lLoadProgression = new ArrayList<Double>();

		for (int t = this.fInitialStep; t < this.fFinalStep; t++)
		{
			int lIndex = (t * this.fSecondsBetweenSteps / this.fSecondsBetweenData);

			lLoadProgression.add(this.fTotalLoadProgression.get(lIndex));
		}

		return lLoadProgression;
	}

	@Override
	public List<Double> getHeatingLoadProgressionCopy()
	{
		List<Double> lHeatProgression = new ArrayList<Double>();

		for (int t = this.fInitialStep; t < this.fFinalStep; t++)
		{
			int lIndex = (t * this.fSecondsBetweenSteps / this.fSecondsBetweenData);

			lHeatProgression.add(this.fHeatingLoadProgression.get(lIndex));
		}

		return lHeatProgression;
	}

	@Override
	public void reset()
	{
		this.fCurrentStep = this.fInitialStep;

		this.updateCurrentValues();

		for (GridLabHouseModel lModel : this.fNeighborhood)
		{
			lModel.reset();
		}
	}

	/**
	 *	@param pSeed The fresh seed for the state randomizer.
	 */
	public void setRandomizer(long pSeed)
	{
		this.fRandom = new Random(pSeed);
	}

	/**
	 *	Reset the state of the World back to a randomized initial position, important for 
	 *	learning the dynamics of Arbitrage, which is a part of Best-Response planning.
	 */
	public void randomizedReset(double pSetpoint)
	{
		this.reset();

		for (GridLabHouseModel lModel : this.fNeighborhood)
		{
			double lRandAirTemp  = pSetpoint + (2 * this.fRandom.nextDouble() - 1);
			double lRandMassTemp = pSetpoint + (2 * this.fRandom.nextDouble() - 1);

			lModel.setTemperature(lRandAirTemp, lRandMassTemp);
		}
	}

	@Override
	public void advanceWorld()
	{
		// Update the current values from the arrays.
		this.updateCurrentValues();

		// Advance time.
		this.fCurrentStep++;
	}

	@Override
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

	@Override
	public int getHorizon()
	{
		return (this.fFinalStep - this.fInitialStep);
	}

}
