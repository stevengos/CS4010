package alg.harnass.gridlab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import alg.sim.world.gridlab.IGridLabControllable;
import alg.sim.world.gridlab.house.GridLabHouseAction;
import alg.sim.world.gridlab.house.GridLabHouseModel;
import alg.sim.world.gridlab.house.TemperatureSortableHouse;

public class GLThermostatPolicy implements IJointPolicy
{
	/**
	 *	The setpoint desired by all houses.
	 */
	private final double fSetpoint;

	/**
	 *	The maximum load that may be consumed by baseloads and heat-pumps.
	 */
	private final double fLoadLimitkW;
	
	private final Random fRng;

	public GLThermostatPolicy(double pSetpoint, double pLoadLimit)
	{
		this.fSetpoint    = pSetpoint;
		this.fLoadLimitkW = pLoadLimit;
		this.fRng         = new Random();
	}

	public GLThermostatPolicy(double pSetpoint, double pLoadLimit, long lSeed)
	{
		this.fSetpoint    = pSetpoint;
		this.fLoadLimitkW = pLoadLimit;
		this.fRng         = new Random(lSeed);
	}
	
	

	/**
	 * @return The frequency with which the policy is able to recommend actions. (Simulated seconds between consecutive applyPolicy calls).
	 */
	public int getStepsize()
	{
		// TCLs sample the current temperature every second.
		return 1;
	}

	/**
	 * Applies the current policy to the provided pWorld, given that we believe the world to be in time-step pTime.
	 * 
	 * @param pTime Current time-step considered.
	 * @param pWorld World to which the policies are to be applied.
	 */
	public void applyPolicy(int pTime, IGridLabControllable pWorld)
	{
		// The thermostat-controlled devices that would like to be on.
		List<TemperatureSortableHouse> lWantOn = new ArrayList<TemperatureSortableHouse>();

		for (int i = 0; i < pWorld.getSize(); i++)
		{
			GridLabHouseModel lHouse = pWorld.getHouse(i);

			/*
			 *	Thermostat behavior, band of 2 degrees Fahrenheit surrounding the setpoint. No AUX, no cooling.
			 */
			if  (lHouse.getAirTemp() > this.fSetpoint+1)
			{
				lHouse.setAction(GridLabHouseAction.OFF);
			}

			// Devices that are too cold, or that were on in the previous time-step, want to be on in this step.
			if ((lHouse.getAirTemp() < this.fSetpoint-1) || (lHouse.getAction() == GridLabHouseAction.HEAT) || (pTime == 0 && this.fRng.nextDouble() > 0.4))
			{
				lWantOn.add(new TemperatureSortableHouse(lHouse));
			}
		}

		// We sort the devices that want to be on by their temperature, so that in case of a limit, the hottest units are turned off first.
		Collections.sort(lWantOn);

		// Estimate the cost of switching on heat-pumps, and enable as many as will fit within the load limit.
		double lCurrentLoad = pWorld.getBackgroundLoad();
		for (int i = 0; i < lWantOn.size(); i++)
		{
			GridLabHouseModel lHouse = lWantOn.get(i).getHouseModel();

			if (lCurrentLoad + lHouse.estimateLoad(pWorld.getOutdoorTemp()) < this.fLoadLimitkW)
			{
				lHouse.setAction(GridLabHouseAction.HEAT);

				lCurrentLoad += lHouse.estimateLoad(pWorld.getOutdoorTemp());
			}
			else
			{
				lHouse.setAction(GridLabHouseAction.OFF);
			}
		}
	}
}
