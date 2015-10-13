package alg.neighbourdhood;

import java.io.PrintStream;
import java.time.LocalDateTime;

import alg.harnass.gridlab.GLEvaluator;
import alg.harnass.gridlab.IJointPolicy;
import alg.sim.world.gridlab.MultiLevelSim;
import alg.sim.world.gridlab.house.GridLabHouseModel;
import alg.util.Celsius;

public class neighbourHoodHarnassEvaluator extends GLEvaluator {
	private 	int[]   fHouseGroups;
	private		int[]	fGroupMaxLoad;
	
	public void setHouseGroups(int[] pHouseGroups){
		this.fHouseGroups = pHouseGroups;
	}
	
	public neighbourHoodHarnassEvaluator(MultiLevelSim pSimulator, double pSetpoint, double pLoadLimit, LocalDateTime pStartTime, int pDurationS, int pStepsizeS)
	{
		super(pSimulator, pSetpoint, pLoadLimit, pStartTime, pDurationS, pStepsizeS);
	}
	
	public void computeNeighbourHoodArbiter(IJointPolicy pPlanToEvaluate, PrintStream pPenalty, PrintStream pTemperature, String pLabel){
		// Ensure clean start.
		this.fEvaluator.reset();
		
		//loop trough time
		for (int t = 0; t < this.fDurationS; t = t + this.fEvaluator.getStepsize())
		{
			// Ensure the policy maker experiences the correct world-state.
			this.fEvaluator.advanceWorld();
			
			//loop trough a neighbourhood
			int lHouseOffset = 0;
			double		lTempPenalty			= 0;
			double[]	lNeighbourhoodPenalty	= null;
			double		lTempUse				= 0;
			double[]	lNeighbourhoodUse		= null;
			double		lLoad					= 0;
			
			for (int i = 0; i < this.fHouseGroups.length; i++)
			{
				int    lHouses		= this.fHouseGroups[i];
				int	   lMaxHouseIdx	= lHouseOffset + lHouses;
				
				if (t % pPlanToEvaluate.getStepsize() == 0)
				{
					// We have hit the sample speed, update house actions from the policy.
					pPlanToEvaluate.applyPolicy(t / pPlanToEvaluate.getStepsize(), this.fEvaluator);
				}
				
				//compute neightbourhood penalty
				for (int j = lHouseOffset; j < lMaxHouseIdx; j++)
				{
					GridLabHouseModel lHouse = super.fEvaluator.getHouse(j);
					lTempPenalty	= lTempPenalty + this.computeHousePenalty(lHouse);
					lTempUse		= lTempUse + lHouse.getCurrentLoad();
				}
				
				lNeighbourhoodPenalty[i]	= lTempPenalty;
				lNeighbourhoodUse[i]		= lTempUse;
				lLoad						= lLoad + lTempUse;
				
				lHouseOffset = lHouseOffset + lHouses;
			}
			
			//do arbiter magic
			this.callArbiter(lNeighbourhoodPenalty, lNeighbourhoodUse);
			//apply policy again
			if (t % pPlanToEvaluate.getStepsize() == 0)
			{
				// We have hit the sample speed, update house actions from the policy.
				pPlanToEvaluate.applyPolicy(t / pPlanToEvaluate.getStepsize(), this.fEvaluator);
			}
			
			//do evaluation
			this.testLoadLimitExceeded(lLoad);
		}
	}
	
	protected int[] callArbiter(double[] lNeighbourhoodPenalty, double[] lNeighbourhoodUse){
		return null;
	}
	
	protected void testLoadLimitExceeded(double lLoad)
	{
		super.testLoadLimitExceeded(lLoad);

		int lHouseID = 0;
		for (int i = 0; i < this.fHouseGroups.length; i++)
		{
			int    lHouses     = this.fHouseGroups[i];
			double lLocalLimit = this.fGroupMaxLoad[i];
			double lLocalUse   = lHouses * super.fEvaluator.getBackgroundLoad() / super.fEvaluator.getSize();

			for (int j = 0; j < lHouses; j++)
			{
				GridLabHouseModel lHouse = super.fEvaluator.getHouse(lHouseID);
				lLocalUse = lLocalUse + lHouse.getCurrentLoad();
				lHouseID++;
			}

			if (lLocalUse > lLocalLimit) {
				System.err.println("Violated secondary load constraint by " + (lLocalUse-lLocalLimit));
			}
				
		}
	}
}
