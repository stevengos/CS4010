package alg.sim.world.gridlab;

import alg.sim.world.gridlab.house.GridLabHouseModel;

public interface IGridLabControllable
{
	/**
	 *	@return The size of the simulated neighborhood.
	 */
	public int getSize();

	/**
	 * @throws IndexOutOfBoundsException if pID >= getSize().
	 * 
	 * @param pID House index in the neighborhood.
	 * 
	 * @return The house model at this index.
	 */
	public GridLabHouseModel getHouse(int pID);

	/**
	 *	@return The current outdoor temperature (in degrees Fahrenheit) in the World.
	 */
	public double getOutdoorTemp();

	/**
	 *	@return The current background load (in kW).
	 */
	public double getBackgroundLoad();
}
