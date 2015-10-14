package alg.harnass.gridlab;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alg.sim.domain.Action;
import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.domain.gridlab2.state.WeatherState;
import alg.sim.solver.EfficientPolicyReader;
import alg.sim.solver.data.ActionReward;
import alg.sim.world.gridlab.IGridLabControllable;
import alg.sim.world.gridlab.IGridLabWorld;
import alg.sim.world.gridlab.house.GridLabHouseModel;
/**
 * 
 * @author Christian
 *
 */
public class GLMultiLevelPlanningHarnass extends GLPlanningHarnass {
	
	private int[]    fHouseGroups;
	private int		 fSetpoint;
	
	public GLMultiLevelPlanningHarnass(int[] pHouseGroups, IGridLabWorld pSimulator, PlanQuality pQuality, int pSetpoint) {
		super(pSimulator, pQuality, pSetpoint);
		fHouseGroups = pHouseGroups;
		this.fSetpoint    = pSetpoint;
	}
	
	/**
	 * Gets the action preference off the agents with startIndex till endIndex
	 * 
	 * @param startIndex
	 * @param endIndex
	 * @param pTime
	 * @param pGlobal
	 * @param pLocal
	 * @return
	 */
	protected Map<AdvancedGridLabAgent, List<ActionReward>> getActionPreference(int startIndex, int endIndex, int pTime, WeatherState pGlobal, Map<AdvancedGridLabAgent, ETPState> pLocal)
	{
		Map<AdvancedGridLabAgent, List<ActionReward>> lPreferences = new HashMap<AdvancedGridLabAgent, List<ActionReward>>();
		Set<? extends Action>						  lActions     = this.fInstance.getActions();

		for (int i = startIndex; i < endIndex && i < this.fInstance.getAgentList().size(); i++)
		{
			AdvancedGridLabAgent  lAgent  = this.fInstance.getAgentList().get(i);
			EfficientPolicyReader lPolicy = this.fAgentPolicy.get(lAgent);
			ETPState              lState  = pLocal.get(lAgent);

			List<ActionReward> lRewards = lPolicy.getActionReward(lState.getID(), lActions);
			Collections.sort(lRewards);

			/*
			 *	In case that we are in a boundary state, then any deviating action would be really, really bad for the agent. So
			 *	the arbiter has to be informed that neglecting this agent is unacceptable.
			 */
			if (lState.isBoundaryState())
			{
				if (Double.isInfinite(lState.getAirMin()) || Double.isInfinite(lState.getAirMax()))
					System.err.println("WARNING: Air Reached Boundary!");

				if (Double.isInfinite(lState.getMassMin()) || Double.isInfinite(lState.getMassMax()))
					System.err.println("WARNING: Mass Reached Boundary!");

				// The second-preferred action (OFF if too cold) is 10-times worse than it was during planning.
				lRewards.get(1).shapeReward(10);
			}

			lPreferences.put(lAgent, lRewards);
		}

		return lPreferences;
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
	private double computeGroupPenalty(int startIndex, int endIndex)
	{
		double lSumPenalty = 0;

		// Add the discomforts experienced for all houses linearly.
		for (int i = startIndex; i < endIndex; i++)
		{
			GridLabHouseModel lModel = this.fSimulator.getHouse(i);

			lSumPenalty = lSumPenalty + this.computeHousePenalty(lModel);
		}

		return lSumPenalty;
	}
	
	/**
	 * Computes the desierd power of a range of houses 
	 */
	private int computeGroupDesiredPower(int pStartIndex, int pEndIndex) {
		return 1;
	}
	
	/**
	 * Applies the current policy to the provided pWorld, given that we believe the world to be in time-step pTime. If
	 * there occurs an overconsumption of energy from applying the policy, arbitrage ensures the conflict is resolved.
	 * By setting pLearn, we allow the agents to discover which time-steps they are (un)likely to receive energy.
	 * 
	 * @param pTime Current time-step considered.
	 * @param pWorld World to which the policies are applied.
	 * @param pLearn Toggle to enable `learning' of success probabilities.
	 */
	protected void applyPolicy(int pTime, IGridLabControllable pWorld, boolean pLearn, boolean pLogit)
	{
		// Ensure the invariant, at time 0 the policies should be aligned.
		if (pTime == 0) this.resetPolicies();

		// Read the current state.
		WeatherState						lGlobalState = this.convertOutdoorToGlobalState(pWorld);
		Map<AdvancedGridLabAgent, ETPState> lLocalStates = this.convertIndoorToLocalState(pWorld);

		// Find the agents' preferences.
		Map<AdvancedGridLabAgent, List<ActionReward>> lPreferences = this.getActionPreference(pTime, lGlobalState, lLocalStates);
		
		// Initially, all agents get their first choice (i.e., option zero).
		List<Integer> lChosenAction = Collections.nCopies(pWorld.getSize(), 0);

		//
		//TODO: OUR ARBITER
		fInstance.getAgentList().get(1).getModel().getAirTemp();
		int lTotalPower = fInstance.getOnLimits().get(pTime);
		int lStartIndex = 0;
		double[] lGroupPenalty = new double[fHouseGroups.length];
		int[] lGroupDesiredPower = new int[fHouseGroups.length];
		for (int i = 0; i < lGroupPenalty.length; i++) {
			int lEndIndex = lStartIndex + fHouseGroups[i] - 1;
			lGroupPenalty[i] = computeGroupPenalty(lStartIndex, lEndIndex);
			lGroupDesiredPower[i] = computeGroupDesiredPower(lStartIndex, lEndIndex);
			lStartIndex += fHouseGroups[i];
		}
		int[] lGroupLimit = SuperArbiter.arbitrage(lTotalPower, lGroupPenalty, lGroupDesiredPower);

		//Divide power among neighbourhoods
		List<Integer> lArbitrateAction = null;
		lStartIndex = 0;
		for (int i = 0; i < this.fHouseGroups.length; i++)
		{
			int lHousesInGroup = fHouseGroups[i];	//Number of houses in group i
			
			int lEndIndex = lStartIndex + lHousesInGroup - 1;
			
			Map<AdvancedGridLabAgent, List<ActionReward>> lNeighbourhoodPreferences = this.getActionPreference(lStartIndex, lEndIndex, pTime, lGlobalState, lLocalStates);
			
			lStartIndex += lHousesInGroup;
			
			ArbiterMode lMode = (pLogit ? ArbiterMode.Logit : ArbiterMode.Deterministic);
			List<Integer> lArbitrateGroupAction = MultiLevelArbiter.arbitrage(this.fInstance, pTime, lGroupLimit[i], lNeighbourhoodPreferences, lChosenAction, lMode);
			if(lArbitrateAction == null)
				lArbitrateAction = lArbitrateGroupAction;
			else
				lArbitrateAction.addAll(lArbitrateGroupAction);
		}
		
		// Apply the assigned actions.
		super.applySelectedActions(pWorld, lPreferences, lArbitrateAction);

					
		//Learning is disabled
		// Learn from the assignments.
		//if (pLearn) this.learnConstraint(pTime, lGlobalState, lLocalStates, lPreferences, lChosenAction, lArbitrateAction);
		
		// Advance the policy pointer.
		if ((pTime+1) < this.fInstance.getHorizon()) this.advancePolicies();
	}

}
