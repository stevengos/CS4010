package alg.harnass.gridlab;

import java.io.PrintStream;
import java.time.LocalDateTime;

import alg.sim.world.gridlab.GridLabEvaluator;
import alg.sim.world.gridlab.IGridLabWorld;
import alg.sim.world.gridlab.house.GridLabHouseModel;
import alg.util.Celsius;

public class GLEvaluator
{
	protected final GridLabEvaluator fEvaluator;
	private final double        fSetpoint;
	private final double		fLoadLimitkW;

	//private final LocalDateTime fStartTime;
	private final int           fDurationS;
	//private final int           fSampleStepS;

	public GLEvaluator(IGridLabWorld pSimulator, double pSetpoint, double pLoadLimit, LocalDateTime pStartTime, int pDurationS, int pStepsizeS)
	{
		this.fEvaluator   = new GridLabEvaluator(pSimulator, pStepsizeS);
		this.fSetpoint    = pSetpoint;
		this.fLoadLimitkW = pLoadLimit;
		//this.fStartTime   = pStartTime;
		this.fDurationS   = pDurationS;
		//this.fSampleStepS = pStepsizeS;
	}

	/**
	 *	Computes the penalty associated with the discomfort of the houses' occupants. Penalty is
	 *	defined as the square of the difference between desired and actual, with a small grace-range which
	 *	is identical to the range used by a default thermostat.
	 *
	 *	@param pModel The house model to compute the penalty for.
	 *	@return House discomfort penalty for the current time-step.
	 */
	private double computeHousePenalty(GridLabHouseModel pModel)
	{
		double lPenalty = 0;

		double lActualTemp  = pModel.getAirTemp();
		double lDesiredTemp = this.fSetpoint;
		double lDifference  = Math.abs(lActualTemp - lDesiredTemp) - 1;

		// In case we are outside our set-point, square the difference as penalty for discomfort.
		if (lDifference > 0) lPenalty = lDifference * lDifference;

		return lPenalty;
	}

	/**
	 *	Computes the penalty associated with the discomfort of the neighborhoods' occupants. Linear
	 *	sum of the individual houses' discomforts.
	 *
	 *	@return Global discomfort penalty for the current time-step.
	 */
	private double computeNeighborhoodPenalty()
	{
		double lSumPenalty = 0;

		// Add the discomforts experienced for all houses linearly.
		for (int i = 0; i < this.fEvaluator.getSize(); i++)
		{
			GridLabHouseModel lModel = this.fEvaluator.getHouse(i);

			lSumPenalty = lSumPenalty + this.computeHousePenalty(lModel);
		}

		return lSumPenalty;
	}

	protected void testLoadLimitExceeded(double lLoad)
	{
		if (lLoad > this.fLoadLimitkW)
			System.err.println("Violated hard load constraint by " + (lLoad-this.fLoadLimitkW));
	}

	public double evaluate(IJointPolicy pPlanToEvaluate, PrintStream pPenalty, PrintStream pTemperature, String pLabel)
	{
		// Ensure clean start.
		this.fEvaluator.reset();

		// Simulate all seconds in the horizon.
		double lCumulativePenalty = 0;
		for (int t = 0; t < this.fDurationS; t = t + this.fEvaluator.getStepsize())
		{
			// Ensure the policy maker experiences the correct world-state.
			this.fEvaluator.advanceWorld();

			/**
			 * TODO: Risky, this assumes this.fEvaluator.getStepsize() is divisible by pPlanToEvaluate.getStepsize()
			 * 
			 * Potential solution: Keep track of when the next update should have happened, and fire the policy if t bigger or equal.
			 */
			if (t % pPlanToEvaluate.getStepsize() == 0)
			{
				// We have hit the sample speed, update house actions from the policy.
				pPlanToEvaluate.applyPolicy(t / pPlanToEvaluate.getStepsize(), this.fEvaluator);
			}

			// Ensure that we do not emit too many data points, throttle by minutes.
			if (t % 60 == 0)
			{
				double lAvgAir  = 0;
				double lAvgMass = 0;
				for (int i = 0; i < this.fEvaluator.getSize(); i++)
				{
					if (pPenalty != null) pPenalty.println(t + "," + pLabel + ",h" + i + "," + this.computeHousePenalty(this.fEvaluator.getHouse(i)));
					lAvgAir  = lAvgAir  + this.fEvaluator.getHouse(i).getAirTemp();
					lAvgMass = lAvgMass + this.fEvaluator.getHouse(i).getMassTemp();
				}
				lAvgAir  = lAvgAir  / this.fEvaluator.getSize();
				lAvgMass = lAvgMass / this.fEvaluator.getSize();

				if (pTemperature != null)
				{
					pTemperature.println(t + "," + pLabel + ",air,air,"   + Celsius.convert(lAvgAir));
					pTemperature.println(t + "," + pLabel + ",indiv,h0,"  + Celsius.convert(this.fEvaluator.getHouse(0).getAirTemp()));
					pTemperature.println(t + "," + pLabel + ",mass,mass," + Celsius.convert(lAvgMass));
					pTemperature.println(t + "," + pLabel + ",air,set,"   + Celsius.convert(this.fSetpoint));
					pTemperature.println(t + "," + pLabel + ",mass,set,"  + Celsius.convert(this.fSetpoint));
					pTemperature.println(t + "," + pLabel + ",out,out,"   + Celsius.convert(this.fEvaluator.getOutdoorTemp()));
				}
			}

			// Test for penalty.
			lCumulativePenalty = lCumulativePenalty + this.computeNeighborhoodPenalty();

			// Advance the simulator.
			double lLoad = this.fEvaluator.advanceNeighborhood();

			// Ensure we are within the capacity constraint.
			this.testLoadLimitExceeded(lLoad);
		}

		//Return the cumulative penalty.
		return lCumulativePenalty;
	}
}
