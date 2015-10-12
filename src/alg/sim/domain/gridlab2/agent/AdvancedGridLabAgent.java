package alg.sim.domain.gridlab2.agent;

import alg.sim.domain.Agent;
import alg.sim.world.gridlab.house.GridLabHouseModel;
import alg.util.Fahrenheit;

public class AdvancedGridLabAgent extends Agent
{
	private static final long serialVersionUID = -8666392993978347606L;

	private final GridLabHouseModel fModel;

	private final double fSetpoint;

	public AdvancedGridLabAgent(int pID, GridLabHouseModel pModel)
	{
		this(pID, pModel, Fahrenheit.convert(20));
	}

	public AdvancedGridLabAgent(int pID, GridLabHouseModel pModel, double pSetpoint)
	{
		super(pID);

		this.fModel    = pModel;
		this.fSetpoint = pSetpoint;
	}

	public GridLabHouseModel getModel()
	{
		return this.fModel;
	}

	public double getSetpoint()
	{
		return this.fSetpoint;
	}

	@Override
	public int getClusteringDimensions() { return 0; }

	@Override
	public double getClusteringValue(int pDimension) { return 0; }
}
