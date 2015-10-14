package alg.sim.domain.gridlab2.state;

import java.util.ArrayList;
import java.util.List;

import alg.harnass.gridlab.PlanQuality;
import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab.state.IETPStateManager;

public class AdvancedETPStateManager extends ArrayList<ETPState> implements IETPStateManager
{
	private static final long serialVersionUID = -6494769725159712961L;

	private final int    fMassStates;
	private final double fMassMin;

	private final int    fAirStates;
	private final double fAirOffset;

	private final double fMassStep;
	private final double fAirStep;

	public AdvancedETPStateManager(PlanQuality pQuality)
	{
		this(pQuality.getNumMassStates(), pQuality.getMinimumMassTempF(), pQuality.getMaximumMassTempF(),
			 pQuality.getNumAirStates(),  pQuality.getAirBandF());
	}

	public AdvancedETPStateManager(int pNumMassStates, double pMassMin, double pMassMax,
								   int pNumAirStates,  double pAirRange)
	{
		this.fMassStates = pNumMassStates;
		this.fMassMin    = pMassMin;

		this.fAirStates  = pNumAirStates;
		this.fAirOffset  = pAirRange / 2;

		this.fMassStep   = (pMassMax - pMassMin) / pNumMassStates;
		this.fAirStep    =  pAirRange            / pNumAirStates;

		for (int i = 0; i < this.fMassStates+2; i++)
		{
			double lMassMin = this.fMassMin + (i-1) * this.fMassStep;
			double lMassMax = this.fMassMin + (i  ) * this.fMassStep;
			double lMassMid = lMassMin + 0.5 * this.fMassStep;

			if (i == 0)						lMassMin = Double.NEGATIVE_INFINITY;
			if (i == this.fMassStates+1)	lMassMax = Double.POSITIVE_INFINITY;

			for (int j = 0; j < this.fAirStates+2; j++)
			{
				double lAirMin = (j-1) * this.fAirStep - this.fAirOffset;
				double lAirMax = (j  ) * this.fAirStep - this.fAirOffset;

				if (j == 0)					lAirMin = Double.NEGATIVE_INFINITY;
				if (j == this.fAirStates+1)	lAirMax = Double.POSITIVE_INFINITY;

				ETPState lState = new ETPState(this.size(), j, i, lMassMid + lAirMin, lMassMid + lAirMax, lMassMin, lMassMax);
				this.add(lState);
			}
		}
	}

	@Override
	public double getAirStep()
	{
		return this.fAirStep;
	}

	@Override
	public double getMassStep()
	{
		return this.fMassStep;
	}

	private int getAirNorm(double pTemp, int pMassState)
	{
		// Compute the Center of the Mass Index.
		double lMassMid = this.fMassMin + pMassState * this.fMassStep - 0.5 * this.fMassStep;

		// Compute the Air Index.
		int lAirNorm = (int)((this.fAirOffset + pTemp - lMassMid) / this.fAirStep + 1);

		// Bound the Air Index.
		if (lAirNorm  < 0)                    lAirNorm  = 0;
		if (lAirNorm  > this.fAirStates + 1)  lAirNorm  = this.fAirStates  + 1;

		return lAirNorm;
	}

	@Override
	public ETPState getState(double pAirTemp, double pMassTemp)
	{
		// Compute the Mass Index.
		int lMassNorm = (int)((pMassTemp - this.fMassMin) / this.fMassStep + 1);

		// Bound the Mass Index.
		if (lMassNorm < 0)                    lMassNorm = 0;
		if (lMassNorm > this.fMassStates + 1) lMassNorm = this.fMassStates + 1;

		// Compute the Air Index.
		int lAirNorm = this.getAirNorm(pAirTemp, lMassNorm);

		// Compute the State Index.
		int lLookupID = (this.fAirStates+2) * lMassNorm + lAirNorm;

		return this.get(lLookupID);
	}

	/**
	 *
	 *		Returns a sorted list of potentially overlapped states. A state is overlapped if
	 *	its temperature range lies within pMinState.min and pMaxState.max for both mass and air.
	 *
	 */
	@Override
	public List<ETPState> getOverlappingStates(ETPState pMinState, ETPState pMaxState)
	{
		//return this;

		/*
		List<ETPState> lOverlapped = new ArrayList<ETPState>();

		for (ETPState lState : this)
			if (lState.getMassMax() >= pMinState.getMassMin() &&
					lState.getMassMin() <  pMaxState.getMassMax() &&
					lState.getAirMax()  >= pMinState.getAirMin() &&
					lState.getAirMin()  <  pMaxState.getAirMax())
					lOverlapped.add(lState);

		return lOverlapped;
		*/

		///*
		List<ETPState> lOverlapped = new ArrayList<ETPState>();

		int lMassMin = pMinState.getMassIndex();
		int lMassMax = pMaxState.getMassIndex();

		for (int lMass = lMassMin; lMass <= lMassMax; lMass++)
		{
			for (int lAir = 0; lAir < this.fAirStates+2; lAir++)
			{
				int      lLookupID = (this.fAirStates+2) * lMass + lAir;
				ETPState lState    = this.get(lLookupID);

				if (lState.getMassMax() >= pMinState.getMassMin() &&
					lState.getMassMin() <  pMaxState.getMassMax() &&
					lState.getAirMax()  >= pMinState.getAirMin() &&
					lState.getAirMin()  <  pMaxState.getAirMax())
					lOverlapped.add(lState);
			}
		}

		return lOverlapped;
		/**/

		/*
		List<ETPState> lOverlapped = new ArrayList<ETPState>();

		int lMassMin = pMinState.getMassIndex();
		int lMassMax = pMaxState.getMassIndex();

		for (int lMass = lMassMin; lMass <= lMassMax; lMass++)
		{
			int lAirMin = this.getAirNorm(pMinState.getAirMin(), lMass);
			int lAirMax = this.getAirNorm(pMaxState.getAirMax(), lMass);

			for (int lAir = lAirMin; lAir < lAirMax; lAir++)
			{
				int lLookupID = (this.fAirStates+2) * lMass + lAir;
				lOverlapped.add(this.get(lLookupID));
			}
		}

		return lOverlapped;
		/**/
	}

	public static void main(String[] args)
	{
		AdvancedETPStateManager lManager = new AdvancedETPStateManager(3, 16, 19, 3, 3);

		System.out.println(lManager.size());

		for (int i = 0; i < 10; i++)
		{
			System.out.println(lManager.get(i));
		}

		System.out.println(lManager.getState(14.5, 15.5));
	}
}
