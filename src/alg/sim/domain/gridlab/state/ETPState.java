package alg.sim.domain.gridlab.state;

import alg.sim.domain.State;

public class ETPState extends State
{
	private static final long serialVersionUID = 3926578760688143421L;

	private final int fID;

	private final int    fAirIndex;
	private final double fAirMin;
	private final double fAirMax;

	private final int    fMassIndex;
	private final double fMassMin;
	private final double fMassMax;

	public ETPState(int pID, int pAirIndex, int pMassIndex, 
					double pAirMin, double pAirMax, double pMassMin, double pMassMax)
	{
		super(String.format("(%2d:[%6.2f;%6.2f), %2d:[%6.2f;%6.2f))", pAirIndex, pAirMin, pAirMax, pMassIndex, pMassMin, pMassMax));

		this.fID		= pID;

		this.fAirIndex  = pAirIndex;
		this.fAirMin	= pAirMin;
		this.fAirMax	= pAirMax;

		this.fMassIndex = pMassIndex;
		this.fMassMin	= pMassMin;
		this.fMassMax	= pMassMax;
	}

	@Override
	public int getID()
	{
		return this.fID;
	}

	public int getAirIndex()
	{
		return this.fAirIndex;
	}

	public double getAirMin()
	{
		return this.fAirMin;
	}

	public double getAirMax()
	{
		return this.fAirMax;
	}

	public int getMassIndex()
	{
		return this.fMassIndex;
	}

	public double getMassMin()
	{
		return this.fMassMin;
	}

	public double getMassMax()
	{
		return this.fMassMax;
	}

	public boolean isBoundaryState()
	{
		return (Double.isInfinite(this.fAirMin)  ||
				Double.isInfinite(this.fAirMax)  ||
				Double.isInfinite(this.fMassMin) ||
				Double.isInfinite(this.fMassMax));
	}

	public boolean isAirInRange(double pTest)
	{
		return ((this.fAirMin <= pTest) && (pTest < this.fAirMax));
	}
	
	public boolean isMassInRange(double pTest)
	{
		return ((this.fMassMin <= pTest) && (pTest < this.fMassMax));
	}

	public boolean isInRange(double pAir, double pMass)
	{
		return (this.isAirInRange(pAir) && this.isMassInRange(pMass));
	}

	public double getReward(double pSetPoint)
	{
		double lMin = this.getAirMin();
		double lMax = this.getAirMax();

		if (Double.isInfinite(lMin)) lMin = lMax-2;
		if (Double.isInfinite(lMax)) lMax = lMin+2;

		double lMinDiff = Math.max(0, Math.abs(lMin - pSetPoint) - 1);
		double lMaxDiff = Math.max(0, Math.abs(lMax - pSetPoint) - 1);
		//double lMinDiff = Math.min(15, Math.max(0, Math.abs(this.getAirMin() - pSetPoint) - 0.5*1.8));
		//double lMaxDiff = Math.min(15, Math.max(0, Math.abs(this.getAirMax() - pSetPoint) - 0.5*1.8));
		double lReward  = Math.min(-lMinDiff * lMinDiff, -lMaxDiff * lMaxDiff);

		return lReward;
	}

	public double getLinearHeatReward(double pSetPoint)
	{
		double lMinDiff = this.getAirMin() - pSetPoint;
		double lMaxDiff = this.getAirMax() - pSetPoint;

		if (Math.abs(lMinDiff) > Math.abs(lMaxDiff))
		{
			if (lMinDiff < 0)
			{
				return this.getReward(pSetPoint);
			}
			else
			{
				return Math.min(15, Math.max(0, lMaxDiff - 0.5*1.8));
			}
		}
		else
		{
			if (lMaxDiff < 0)
			{
				return this.getReward(pSetPoint);
			}
			else
			{
				return Math.min(15, Math.max(0, lMaxDiff - 0.5*1.8));
			}
		}
	}

	public double getOnlyNegativeReward(double pSetPoint)
	{
		double lMinDiff = Math.min(15, Math.max(0, -(Math.min(0, this.getAirMin() - pSetPoint) + 0.5*1.8)));
		double lMaxDiff = Math.min(15, Math.max(0, -(Math.min(0, this.getAirMax() - pSetPoint) + 0.5*1.8)));
		double lReward  = Math.min(-lMinDiff * lMinDiff, -lMaxDiff * lMaxDiff);

		return lReward;
	}

	@Override
	public boolean equals(Object pOther)
	{
		if (pOther instanceof ETPState)
		{
			ETPState that = (ETPState) pOther;

			return (this.getID() == that.getID());
		}

		return false;
	}

	/*
	public static void main(String[] args)
	{
		ETPState lState = new ETPState(5, 2, 3, 19, 20, 19, 20);

		System.out.println(lState.getReward(21));
		System.out.println(lState.getReward(18));
		System.out.println(lState.getOnlyNegativeReward(21));
		System.out.println(lState.getOnlyNegativeReward(18));
	}
	/**/
}
