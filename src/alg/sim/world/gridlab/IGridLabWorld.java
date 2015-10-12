package alg.sim.world.gridlab;

import java.util.List;

public interface IGridLabWorld extends IGridLabControllable
{
	/**
	 *	@return The size of a single advanceTime() step of the world, in seconds
	 */
	public int getStepsize();

	/**
	 *	Reset the state of the World back to its initial position, for example to start a second run.
	 */
	public void reset();

	/**
	 *	Reset the state of the World back to a randomized initial position, important for 
	 *	learning the dynamics of Arbitrage, which is a part of Best-Response planning.
	 */
	public void randomizedReset(double pSetpoint);

	/**
	 *	Advances the state of the world by the step-size
	 */
	public void advanceWorld();

	/**
	 *	Advances the entire neighborhood by the step-size
	 *
	 *	@return Real load in kW drawn by the entire neighborhood, including any active heat-pumps.
	 */
	public double advanceNeighborhood();

	/**
	 *	@return The number of time-steps to be simulated.
	 */
	public int getHorizon();

	/**
	 *	@return The average background heat contribution of ZIP loads over the horizon.
	 */
	public double computeAverageBackground();

	/**
	 *	@return Estimate of the maximum number of houses able to switch on at each time-step.
	 */
	public List<Integer> computeMaximumOnEstimate();

	/**
	 *	@return List of temperatures for all steps up to the horizon.
	 */
	public List<Double> getTemperatureProgressionCopy();
	
	/**
	 *  @return List of energy prizes for all steps up to the horizon.
	 */
	public List<Double> getPriceProgressionCopy();

	/**
	 *  @return List of Solar energy obtained for all steps up to the horizon.
	 */
	public List<Double> getSolarProgressionCopy();
	
	/**
	 *	@return List of total loads for all steps up to the horizon.
	 */
	public List<Double> getTotalLoadProgressionCopy();

	/**
	 *	@return List of heating loads for all steps up to the horizon.
	 */
	public List<Double> getHeatingLoadProgressionCopy();

}
