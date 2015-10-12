package alg.sim.domain.gridlab2.state;

import alg.sim.domain.GlobalState;
import alg.sim.domain.State;

public class WeatherState extends State implements GlobalState
{
	private static final long serialVersionUID = -1927787612440652366L;

	private final int fID;

	private final int    fOutTempIndex;
	private final double fOutTempMin;
	private final double fOutTempMax;

	public WeatherState(int pID, double pOutMin, double pOutMax)
	{
		super(String.format("(WeatherState %2d:[%6.2f;%6.2f))", pID, pOutMin, pOutMax));

		this.fID			= pID;
		this.fOutTempIndex	= pID;
		this.fOutTempMin	= pOutMin;
		this.fOutTempMax	= pOutMax;
	}

	@Override
	public int getID()
	{
		return this.fID;
	}

	public int getOutTempIndex()
	{
		return this.fOutTempIndex;
	}

	public double getOutTempMin()
	{
		return this.fOutTempMin;
	}

	public double getOutTempMax()
	{
		return this.fOutTempMax;
	}

	public double getOutTempStep()
	{
		return (this.fOutTempMax - this.fOutTempMin);
	}

	public boolean isInRange(double pTest)
	{
		return ((this.fOutTempMin <= pTest) && (pTest < this.fOutTempMax));
	}

	@Override
	public int getTransitionPosition() {
		return getID();
	}
}
