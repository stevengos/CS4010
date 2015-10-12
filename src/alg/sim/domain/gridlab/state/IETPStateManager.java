package alg.sim.domain.gridlab.state;

import java.util.List;

public interface IETPStateManager extends List<ETPState>
{
	public double getAirStep();

	public double getMassStep();

	public List<ETPState> getOverlappingStates(ETPState pSWstate, ETPState pNEstate);

	public ETPState getState(double pAirF, double pMassF);
}
