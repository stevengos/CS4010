package alg.sim.domain.gridlab2.state;

import java.util.ArrayList;
import java.util.List;

import alg.harnass.gridlab.PlanQuality;

public class WeatherStateManager
{
	private final List<WeatherState> fWeatherStates;

	private final double fOutMin;
	private final double fOutMax;
	private final double fOutStep;

	public WeatherStateManager(PlanQuality pQuality)
	{
		this(pQuality.getNumOutdoorStates(), pQuality.getMinimumOutdoorTempF(), pQuality.getMaximumOutdoorTempF());
	}

	public WeatherStateManager(int pNumStates, double pOutMin, double pOutMax)
	{
		this.fWeatherStates = new ArrayList<WeatherState>();

		this.fOutMin	 = pOutMin;
		this.fOutMax	 = pOutMax;
		this.fOutStep	 = (pOutMax - pOutMin) / pNumStates;

		for (int i = 0; i < pNumStates; i++)
		{
			double lStateMin = this.fOutMin +  i    * this.fOutStep;
			double lStateMax = this.fOutMin + (i+1) * this.fOutStep;

			WeatherState lState = new WeatherState(i, lStateMin, lStateMax);

			this.fWeatherStates.add(lState);
		}
	}

	public int getNumStates()
	{
		return this.fWeatherStates.size();
	}

	public double getMinOutTemp()
	{
		return this.fOutMin;
	}

	public double getMaxOutTemp()
	{
		return this.fOutMax;
	}

	public WeatherState getWeatherStateByID(int pID)
	{
		return this.fWeatherStates.get(pID);
	}

	public WeatherState getWeatherState(double pTemperature)
	{
		if (pTemperature <  this.fOutMin)
			throw new IndexOutOfBoundsException(String.format("Asked for out-of-bounds temperature state (%6.2f < %6.2f)", pTemperature, this.fOutMin));
		if (pTemperature >= this.fOutMax)
			throw new IndexOutOfBoundsException(String.format("Asked for out-of-bounds temperature state (%6.2f >= %6.2f)", pTemperature, this.fOutMax));

		int lOutTempNorm  = (int)((pTemperature  - this.fOutMin)  / this.fOutStep);

		return this.fWeatherStates.get(lOutTempNorm);
	}
}
