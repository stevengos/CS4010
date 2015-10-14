package alg.harnass.gridlab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alg.sim.domain.Action;
import alg.sim.domain.GlobalState;
import alg.sim.domain.gridlab.action.GridLabActionManager;
import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab2.AdvancedGridLabDomain;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.domain.gridlab2.state.WeatherState;
import alg.sim.domain.gridlab2.transition.GridLabTransitionManager3;
import alg.sim.domain.tcl.SwitchingAction;
import alg.sim.solver.EfficientPolicyReader;
import alg.sim.solver.EfficientValueIteration;
import alg.sim.solver.data.ActionReward;
import alg.sim.world.gridlab.IGridLabControllable;
import alg.sim.world.gridlab.IGridLabWorld;
import alg.sim.world.gridlab.house.GridLabHouseAction;
import alg.sim.world.gridlab.house.GridLabHouseModel;

public class GLPlanningHarnass implements IJointPolicy
{
	protected IGridLabWorld fSimulator;

	protected AdvancedGridLabDomain fInstance;

	protected Map<AdvancedGridLabAgent, EfficientPolicyReader> fAgentPolicy;

	public GLPlanningHarnass(IGridLabWorld pSimulator, PlanQuality pQuality, int pSetpoint)
	{
		this.fSimulator = pSimulator;
		this.constructDomainFromSimulator(pSimulator, pQuality, pSetpoint);
	}

	public GLPlanningHarnass(IGridLabWorld pSimulator, PlanQuality pQuality, int pSetpoint, GridLabTransitionManager3 pTransitions)
	{
		this.fSimulator = pSimulator;
		this.constructDomainFromSimulator(pSimulator, pQuality, pSetpoint, pTransitions);
	}

	public AdvancedGridLabDomain getDomain()
	{
		return this.fInstance;
	}

	public IGridLabWorld getSimulator()
	{
		return this.fSimulator;
	}

	@Override
	public int getStepsize()
	{
		return this.fSimulator.getStepsize();
	}

	public EfficientPolicyReader getPolicy(int pAgentID)
	{
		return this.fAgentPolicy.get(this.fInstance.getAgentList().get(pAgentID));
	}

	protected void constructDomainFromSimulator(IGridLabWorld pSimulator, PlanQuality pQuality, double pSetpoint)
	{
		this.constructDomainFromSimulator(pSimulator.getSize(),
										  pQuality.getNumMassStates(),    pQuality.getMinimumMassTempF(),    pQuality.getMaximumMassTempF(),
										  pQuality.getNumAirStates(),     pQuality.getAirBandF(),
										  pQuality.getNumOutdoorStates(), pQuality.getMinimumOutdoorTempF(), pQuality.getMaximumOutdoorTempF(),
										  pSetpoint, null);
	}

	protected void constructDomainFromSimulator(IGridLabWorld pSimulator, PlanQuality pQuality, double pSetpoint, GridLabTransitionManager3 pManager)
	{
		this.constructDomainFromSimulator(pSimulator.getSize(),
										  pQuality.getNumMassStates(),    pQuality.getMinimumMassTempF(),    pQuality.getMaximumMassTempF(),
										  pQuality.getNumAirStates(),     pQuality.getAirBandF(),
										  pQuality.getNumOutdoorStates(), pQuality.getMinimumOutdoorTempF(), pQuality.getMaximumOutdoorTempF(),
										  pSetpoint, pManager);
	}
	
	protected void constructDomainInstance(int pMassStates,   double pMassMin,   double pMassMax,
			  int pAirStates,    double pAirRange,
			  int pGlobalStates, double pGlobalMin, double pGlobalMax,
			  double pSetpoint,
			  GridLabTransitionManager3 pManager) {

		int    lHorizon    = this.fSimulator.getHorizon();
		double lBackground = this.fSimulator.computeAverageBackground();
		// Construct instance.
		if (pManager == null)
		{
			this.fInstance = new AdvancedGridLabDomain(lHorizon, lBackground,
													   pMassStates, pMassMin, pMassMax,
													   pAirStates, pAirRange,
													   pGlobalStates, pGlobalMin, pGlobalMax);
		}
		else
		{
			this.fInstance = new AdvancedGridLabDomain(lHorizon, lBackground,
													   pMassStates, pMassMin, pMassMax,
													   pAirStates, pAirRange,
													   pGlobalStates, pGlobalMin, pGlobalMax,
													   pManager);
		}
	}

	protected void constructDomainFromSimulator(int pAgents,
			  int pMassStates,   double pMassMin,   double pMassMax,
			  int pAirStates,    double pAirRange,
			  int pGlobalStates, double pGlobalMin, double pGlobalMax,
			  double pSetpoint,
			  GridLabTransitionManager3 pManager)
	{
		this.constructDomainInstance(pMassStates, pMassMin, pMassMax, pAirStates, pAirRange, pGlobalStates, pGlobalMin, pGlobalMax, pSetpoint, pManager);

		// Add agents.
		for (int i = 0; i < pAgents; i++)
		{
			GridLabHouseModel lModel = this.fSimulator.getHouse(i);
			
			this.fInstance.addAgent(lModel, pSetpoint);
		}

		// Prepare the success estimator.
		this.fInstance.initializeSuccessEstimator();

		// Prepare transition function.
		if (pManager == null)
		{
			System.out.print("[");
			this.fInstance.prepareAllTransitionFunctions();
			System.out.println("]");
		}

		// Impose the estimated load limit.
		List<Integer> lLimitOn = this.fSimulator.computeMaximumOnEstimate();
		for (int i = 0; i < lLimitOn.size(); i++)
		{
			if (lLimitOn.get(i) < this.fSimulator.getSize())
			{
				this.fInstance.setLimit(i, lLimitOn.get(i));
			}
		}
	}

	protected List<GlobalState> extractWeatherProgression()
	{
		List<GlobalState> lOutProgression = new ArrayList<GlobalState>();
		List<Double>	  lInProgression  = this.fSimulator.getTemperatureProgressionCopy();

		for (int i = 0; i < lInProgression.size(); i++)
		{
			Double		 lOutTempF     = lInProgression.get(i);
			WeatherState lWeatherState = this.fInstance.mapWeatherState(lOutTempF);

			lOutProgression.add(lWeatherState);
		}

		return lOutProgression;
	}

	public EfficientPolicyReader planPolicy(AdvancedGridLabAgent pAgent)
	{
		List<GlobalState> lGlobalProgression = this.extractWeatherProgression();

		System.out.print(String.format("%3d [", pAgent.getID()));
		EfficientPolicyReader lPolicy = EfficientValueIteration.computeAgentPolicy(this.fInstance, pAgent, lGlobalProgression);
		System.out.println("]");

		return lPolicy;
	}

	public void planAll()
	{
		this.fAgentPolicy = new HashMap<AdvancedGridLabAgent, EfficientPolicyReader>();

		for (AdvancedGridLabAgent lAgent : this.fInstance.getAgentList())
		{
			this.fAgentPolicy.put(lAgent, this.planPolicy(lAgent));
		}
	}

	public void planBestresponse(int pIterations, int pSimulations)
	{
		this.planBestresponse(pIterations, pSimulations, true, true);
	}

	public void planBestresponse(int pIterations, int pSimulations, boolean pForget, boolean pLogit)
	{
		for (int i = 0; i < pIterations; i++)
		{
			this.planAll();
			this.simulateMany(pSimulations, pForget, pLogit);
		}
	}

	public void resetPolicies()
	{
		for (EfficientPolicyReader lReader : this.fAgentPolicy.values())
		{
			lReader.resetReader();
		}
	}

	protected void advancePolicies()
	{
		for (EfficientPolicyReader lReader : this.fAgentPolicy.values())
		{
			lReader.nextTimeStep();
		}
	}

	protected WeatherState convertOutdoorToGlobalState(IGridLabControllable pWorld)
	{
		double       lOutF    = pWorld.getOutdoorTemp();

		WeatherState lWeather = this.fInstance.mapWeatherState(lOutF);

		return lWeather;
	}

	protected Map<AdvancedGridLabAgent, ETPState> convertIndoorToLocalState(IGridLabControllable pWorld)
	{
		Map<AdvancedGridLabAgent, ETPState> lLocalStates = new HashMap<AdvancedGridLabAgent, ETPState>();

		for (int i = 0; i < pWorld.getSize(); i++)
		{
			GridLabHouseModel    lModel = pWorld.getHouse(i);
			AdvancedGridLabAgent lAgent = this.fInstance.getAgentList().get(i);

			double lAirF    = lModel.getAirTemp();
			double lMassF   = lModel.getMassTemp();
			ETPState lLocal = this.fInstance.mapState(lAirF, lMassF);

			lLocalStates.put(lAgent, lLocal);
		}

		return lLocalStates;
	}

	protected Map<AdvancedGridLabAgent, List<ActionReward>> getActionPreference(int pTime, WeatherState pGlobal, Map<AdvancedGridLabAgent, ETPState> pLocal)
	{
		Map<AdvancedGridLabAgent, List<ActionReward>> lPreferences = new HashMap<AdvancedGridLabAgent, List<ActionReward>>();
		Set<? extends Action>						  lActions     = this.fInstance.getActions();

		for (int i = 0; i < this.fInstance.getAgentList().size(); i++)
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
	 *	This function lets the system learn when the arbitrator is likely to step in, thereby limiting
	 *	an agent in its action selection procedure.
	 *
	 *	@param pTime The current time-step in the simulation.
	 *	@param pGlobalState The global state at this time-step.
	 *	@param pLocalStates The local state of each agent at this time-step, according to the simulator.
	 *	@param lPreferences For each agent, what its preferred action is, and how much utility the agent expects from it.
	 *	@param pChosenAction For each agent, which action in the list of action preferences it wants (always the first one...)
	 *	@param pAssignedAction For each agent, which action in the list of action preferences it gets.
	 */
	protected void learnConstraint( int pTime,
									WeatherState pGlobalState,
									Map<AdvancedGridLabAgent, ETPState> pLocalStates,
									Map<AdvancedGridLabAgent, List<ActionReward>> lPreferences,
									List<Integer> pChosenAction,
									List<Integer> pAssignedAction
								  )
	{
		 this.fInstance.experienceProbability(pTime, lPreferences, pChosenAction, pAssignedAction);
	}

	/**
	 * Applies the current policy to the provided pWorld, given that we believe the world to be in time-step pTime.
	 * 
	 * @param pTime Current time-step considered.
	 * @param pWorld World to which the policies are applied.
	 */
	@Override
	public void applyPolicy(int pTime, IGridLabControllable pWorld)
	{
		// When the policy must be applied to some external world, we should not learn.
		this.applyPolicy(pTime, pWorld, false, true);
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

		// Then, apply arbitrage to limit the number of agents using their best choice.
		ArbiterMode lMode = (pLogit ? ArbiterMode.Logit : ArbiterMode.Deterministic);
		List<Integer> lArbitrateAction = Arbiter.arbitrage(this.fInstance, pTime, lPreferences, lChosenAction, lMode);

		// Apply the assigned actions.
		this.applySelectedActions(pWorld, lPreferences, lArbitrateAction);

		// Learn from the assignments.
		if (pLearn) this.learnConstraint(pTime, lGlobalState, lLocalStates, lPreferences, lChosenAction, lArbitrateAction);

		// Advance the policy pointer.
		if ((pTime+1) < this.fInstance.getHorizon()) this.advancePolicies();
	}

	protected void applySelectedActions(IGridLabControllable pWorld, Map<AdvancedGridLabAgent, List<ActionReward>> pPreferences, List<Integer> pAssignment)
	{
		for (int i = 0; i < pWorld.getSize(); i++)
		{
			GridLabHouseModel    lModel  = pWorld.getHouse(i);
			AdvancedGridLabAgent lAgent  = this.fInstance.getAgentList().get(i);
			ActionReward         lAction = pPreferences.get(lAgent).get(pAssignment.get(i));

			if (((SwitchingAction) lAction.getAction()).isOn())
			{
				lModel.setAction(GridLabHouseAction.HEAT);
			}
			else
			{
				lModel.setAction(GridLabHouseAction.OFF);
			}
		}
	}

	private void printStatistics(int pTime)
	{
		double lOutdoor   = this.fSimulator.getOutdoorTemp();
		double lAvgAir    = 0;
		double lAvgMass   = 0;
		int    lHeating   = 0;
		int    lHeatLimit = this.fInstance.getActionLimit(new SwitchingAction(true), pTime);

		for (int i = 0; i < this.fSimulator.getSize(); i++)
		{
			GridLabHouseModel lHouse = this.fSimulator.getHouse(i);

			lAvgAir  = lAvgAir  + lHouse.getAirTemp();
			lAvgMass = lAvgMass + lHouse.getMassTemp();

			if (lHouse.getAction().equals(GridLabHouseAction.HEAT))
				lHeating++;
		}

		lAvgAir  = lAvgAir  / this.fSimulator.getSize();
		lAvgMass = lAvgMass / this.fSimulator.getSize();

		GridLabHouseModel    lHouse = this.fSimulator.getHouse(0);
		AdvancedGridLabAgent lAgent = this.fInstance.getAgentList().get(0);
		if (pTime % 10 == 0)
			System.out.print(pTime
					+ "\t" + lOutdoor
					+ "\t" + lAvgAir
					+ "\t" + lAvgMass
					+ "\t" + lHeating
					+ "\t" + lHeatLimit
					+ "\t" + lHouse.getAirTemp()
					+ "\t" + lHouse.getMassTemp()
					+ "\t" + lHouse.getAction()
					+ "\t" + this.fInstance.getSuccessProbability(pTime, lAgent, ((GridLabActionManager)this.fInstance.getActions()).getOnAction())
					);
	}

	private void printlnLoad(int pTime, double pLoadkW)
	{
		if (pTime % 10 == 0)
			System.out.println("\t" + pLoadkW);
	}

	public void simulate()
	{
		this.simulate(true, true);
	}

	public void simulate(boolean pPrint, boolean pLogit)
	{
		if (this.fInstance.getAgentList().size() > 0)
		{
			this.fSimulator.randomizedReset(this.fInstance.getAgentList().get(0).getSetpoint());
		}
		else
		{
			throw new IllegalStateException("Simulating a neighborhood without houses.");
		}

		for (int t = 0; t < this.fSimulator.getHorizon(); t++)
		{
			// Ensure the policies are testing the correct environment.
			this.fSimulator.advanceWorld();

			// Apply policies to the simulator.
			this.applyPolicy(t, this.fSimulator, true, pLogit);

			if (pPrint)	this.printStatistics(t);

			// Measure the system load.
			double lLoad = this.fSimulator.advanceNeighborhood();

			if (pPrint)	this.printlnLoad(t, lLoad);
		}
	}

	public void simulateMany(int pIterations)
	{
		this.simulateMany(pIterations, true, false);
	}

	public void simulateMany(int pIterations, boolean pForget, boolean pLogit)
	{
		if (pForget) this.fInstance.forgetExperiences();
		for (int i = 0; i < pIterations; i++)
		{
			this.simulate(false, pLogit);
			//System.out.print("-");
			//System.out.flush();
		}
	}
}
