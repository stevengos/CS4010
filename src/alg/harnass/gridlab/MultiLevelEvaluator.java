package alg.harnass.gridlab;

import java.time.LocalDateTime;

import alg.sim.world.gridlab.MultiLevelSim;
import alg.sim.world.gridlab.house.GridLabHouseModel;

public class MultiLevelEvaluator extends GLEvaluator
{
	private int[]    fHouseGroups;
	private double[] fGroupMaxLoad;

	public MultiLevelEvaluator(MultiLevelSim pSimulator, double pSetpoint, double pLoadLimit, LocalDateTime pStartTime, int pDurationS, int pStepsizeS)
	{
		super(pSimulator, pSetpoint, pLoadLimit, pStartTime, pDurationS, pStepsizeS);
	}

	public void setLocalLimits(int[] pHouseGroups, double[] pGroupMaxLoad)
	{
		this.fHouseGroups  = pHouseGroups;
		this.fGroupMaxLoad = pGroupMaxLoad;
	}
	
	protected void testLoadLimitExceeded(double lLoad)
	{
		super.testLoadLimitExceeded(lLoad);

		int lHouseID = 0;
		for (int i = 0; i < this.fHouseGroups.length; i++)
		{
			int    lHouses     = this.fHouseGroups[i];	//Number of houses in neighbourhood i
			double lLocalLimit = this.fGroupMaxLoad[i];	//Limit of neighboorhood i
			double lLocalUse   = lHouses * super.fEvaluator.getBackgroundLoad() / super.fEvaluator.getSize();

			
			//Test whether we made valid choices
			for (int j = 0; j < lHouses; j++)
			{
				GridLabHouseModel lHouse = super.fEvaluator.getHouse(lHouseID);
				lLocalUse = lLocalUse + lHouse.getCurrentLoad();
				lHouseID++;
			}
			
			if (lLocalUse > lLocalLimit)
				System.err.println("Violated secondary load constraint by " + (lLocalUse-lLocalLimit));
				
		}
	}

}
