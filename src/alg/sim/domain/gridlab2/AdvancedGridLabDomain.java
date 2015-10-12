package alg.sim.domain.gridlab2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alg.harnass.gridlab.PlanQuality;
import alg.sim.domain.Action;
import alg.sim.domain.Agent;
import alg.sim.domain.ArbiMDP;
import alg.sim.domain.GlobalState;
import alg.sim.domain.IAgentTransitionStream;
import alg.sim.domain.RelevantState;
import alg.sim.domain.State;
import alg.sim.domain.Transition;
import alg.sim.domain.World;
import alg.sim.domain.gridlab.action.GridLabActionManager;
import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab.state.IETPStateManager;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.domain.gridlab2.state.AdvancedETPStateManager;
import alg.sim.domain.gridlab2.state.WeatherState;
import alg.sim.domain.gridlab2.state.WeatherStateManager;
import alg.sim.domain.gridlab2.success.ActionSuccessLearner;
import alg.sim.domain.gridlab2.success.IActionSuccessLearner;
import alg.sim.domain.gridlab2.transition.GridLabTransitionManager3;
import alg.sim.domain.tcl.SwitchingAction;
import alg.sim.solver.data.ActionReward;
import alg.sim.world.gridlab.house.GridLabHouseModel;

public class AdvancedGridLabDomain implements ArbiMDP
{
	/**
	 *	The horizon h is the number of time-steps in the system. 0 <= t < h
	 */
	private final int fHorizon;

	private final double fBackgroundHeatBTU;

	private final GridLabActionManager		fActionSet;

	private final IETPStateManager			fStateList;

	private final WeatherStateManager		fWeatherStates;

	private final GridLabTransitionManager3	fTransitions;

	private final List<AdvancedGridLabAgent> fAgentList;

	private final Set<AdvancedGridLabAgent> fAgentSet;

	private final Map<Integer, Integer> fMapTimeToLimit;

	private IActionSuccessLearner fSuccessEstimator;

	public AdvancedGridLabDomain(int pHorizon, double pBackgroundBTU,
								 int pMassStates, double pMinMassTemp, double pMaxMassTemp, int pAirStates, double pAirRange,
								 int pWeatherStates, double pWeatherMin, double pWeatherMax)
	{
		this.fHorizon           = pHorizon;
		this.fBackgroundHeatBTU = pBackgroundBTU;
		this.fActionSet         = new GridLabActionManager();
		this.fStateList         = new AdvancedETPStateManager(pMassStates, pMinMassTemp, pMaxMassTemp, pAirStates, pAirRange);
		this.fWeatherStates     = new WeatherStateManager(pWeatherStates, pWeatherMin, pWeatherMax);
		this.fTransitions       = new GridLabTransitionManager3(this.fWeatherStates, this.fStateList, this.fActionSet.getActions(), "./scratch/transition/");
		this.fAgentList         = new ArrayList<AdvancedGridLabAgent>();
		this.fAgentSet			= new HashSet<AdvancedGridLabAgent>();
		this.fMapTimeToLimit    = new HashMap<Integer, Integer>();
	}

	public AdvancedGridLabDomain(int pHorizon, double pBackgroundBTU,
								 int pMassStates, double pMinMassTemp, double pMaxMassTemp, int pAirStates, double pAirRange,
								 int pWeatherStates, double pWeatherMin, double pWeatherMax,
								 GridLabTransitionManager3 pManager)
	{
		this.fHorizon           = pHorizon;
		this.fBackgroundHeatBTU = pBackgroundBTU;
		this.fActionSet         = new GridLabActionManager();
		this.fStateList         = new AdvancedETPStateManager(pMassStates, pMinMassTemp, pMaxMassTemp, pAirStates, pAirRange);
		this.fWeatherStates     = new WeatherStateManager(pWeatherStates, pWeatherMin, pWeatherMax);
		this.fTransitions       = pManager;
		this.fAgentList         = new ArrayList<AdvancedGridLabAgent>();
		this.fAgentSet			= new HashSet<AdvancedGridLabAgent>();
		this.fMapTimeToLimit    = new HashMap<Integer, Integer>();
	}

	public AdvancedGridLabDomain(int pHorizon, double pBackgroundBTU, PlanQuality pQuality)
	{
		this.fHorizon           = pHorizon;
		this.fBackgroundHeatBTU = pBackgroundBTU;
		this.fActionSet         = new GridLabActionManager();
		this.fStateList         = new AdvancedETPStateManager(pQuality);
		this.fWeatherStates     = new WeatherStateManager(pQuality);
		this.fTransitions       = new GridLabTransitionManager3(this.fWeatherStates, this.fStateList, this.fActionSet.getActions(), "./scratch/transition/");
		this.fAgentList         = new ArrayList<AdvancedGridLabAgent>();
		this.fAgentSet			= new HashSet<AdvancedGridLabAgent>();
		this.fMapTimeToLimit    = new HashMap<Integer, Integer>();
	}

	public void initializeLimit(int pStart, int pEnd, int pLimit)
	{
		for (int i = pStart; i < pEnd; i++)
		{
			this.setLimit(i, pLimit); //this.fMapTimeToLimit.put(i, pLimit);
		}
	}

	public void setLimit(int pTime, int pLimitOn)
	{
		this.fMapTimeToLimit.put(pTime, pLimitOn);
	}

	public AdvancedGridLabAgent addAgent(GridLabHouseModel pModel, double pSetpoint)
	{
		AdvancedGridLabAgent lAgent = new AdvancedGridLabAgent(this.fAgentList.size(), pModel, pSetpoint);

		this.fAgentList.add(lAgent);
		this.fAgentSet.add(lAgent);

		return lAgent;
	}

	public Map<Integer, Integer> getOnLimits()
	{
		return this.fMapTimeToLimit;
	}

	public Map<Integer, Integer> getOffLimits()
	{
		return new HashMap<Integer, Integer>();
	}

	public void initializeSuccessEstimator()
	{
		this.fSuccessEstimator = new ActionSuccessLearner(this, this.fHorizon, this.fAgentList.size(), this.fActionSet.size());
	}

	public void experienceProbability(int pTime, Map<AdvancedGridLabAgent, List<ActionReward>> pPreferences, List<Integer> pSelected, List<Integer> pAwarded)
	{
		this.fSuccessEstimator.experienceProbability(pTime, pPreferences, pSelected, pAwarded);
	}

	public void forgetExperiences()
	{
		this.fSuccessEstimator.forgetExperiences();
	}

	@Override
	public int getHorizon()
	{
		return this.fHorizon;
	}

	public IETPStateManager getStateManager()
	{
		return this.fStateList;
	}

	@Override
	public int getNumStates()
	{
		return this.fStateList.size();
	}
	
	public int getNumAgents() {
		return this.fAgentList.size();
	}

	@Override
	public State getState(int pID)
	{
		return this.fStateList.get(pID);
	}

	public ETPState mapState(double pAirF, double pMassF)
	{
		return this.fStateList.getState(pAirF, pMassF);
	}	

	public WeatherState mapWeatherState(double pOutTempF)
	{
		return this.fWeatherStates.getWeatherState(pOutTempF);
	}

	@Override
	public Set<? extends Action> getActions()
	{
		return this.fActionSet;
	}

	@Override
	public List<? extends Action> getActionList()
	{
		return this.fActionSet.getActions();
	}

	@Override
	public Set<? extends Action> getActions(int pTime)
	{
		return this.getActions();
	}

	@Override
	public Set<? extends Agent> getAgents()
	{
		return this.fAgentSet;
	}

	public List<AdvancedGridLabAgent> getAgentList()
	{
		return this.fAgentList;
	}

	@Override @Deprecated
	public void prepareTransitionFunction(Agent pAgent)
	{
		throw new IllegalArgumentException("Deprecated transition function preparation (one agent).");
		//this.fTransitions.computeTransitionFunction((AdvancedGridLabAgent) pAgent, this.fBackgroundHeatBTU);
	}

	@Override @Deprecated
	public void prepareTransitionFunctions(Set<Agent> as)
	{
		throw new IllegalArgumentException("Deprecated transition function preparation (many agents).");
		/*
		int i = 0;
		for (Agent lAgent : as)
		{
			this.prepareTransitionFunction(lAgent);
			i++;
			System.out.print("-");
			if ((i%5) == 0)
			{
				System.out.print("|");
				i = 0;
			}
			System.out.flush();
		}
		/**/
	}

	@Override
	public void prepareAllTransitionFunctions()
	{
		this.fTransitions.computeAllTransitionFunctions(this.fAgentList, this.fBackgroundHeatBTU);
	}

	@Override @Deprecated
	public Map<? extends State, Double> getTransitionFunction(Agent p_i, State s_i, Action a_i)
	{
		throw new IllegalArgumentException("Deprecated transition function (missing world weather-state).");
	}

	@Override
	public Double getRewardFunction(Agent p_i, State s_i, Action a_i)
	{
		AdvancedGridLabAgent lAgent  = (AdvancedGridLabAgent) p_i;
		ETPState             lState  = (ETPState)             s_i;
		//SwitchingAction      lAction = (SwitchingAction)      a_i;

		//return lState.getOnlyNegativeReward(lAgent.getSetpoint());
		//return lState.getLinearHeatReward(lAgent.getSetpoint());
		return lState.getReward(lAgent.getSetpoint());
	}

	public Double getRewardFunction(Agent p_i, State s_i, GlobalState g_i, Action a_i) {
		return getRewardFunction(p_i, s_i, a_i);
	}

	@Override
	public Integer getActionLimit(Action a_i, int t)
	{
		SwitchingAction lAction = (SwitchingAction) a_i;

		int lLimit = this.fAgentList.size();
		if (lAction.isOn() && this.fMapTimeToLimit.get(t) != null)
		{
			lLimit = this.fMapTimeToLimit.get(t);
		}

		return lLimit;
	}

	@Override @Deprecated
	public World instantiateWorld()
	{
		return null;
	}

	@Override
	public Transition getTransitionFunction(Agent p_i, RelevantState s_i, Action a_i)
	{
		AdvancedGridLabAgent lAgent   = (AdvancedGridLabAgent) p_i;
		WeatherState	     lWeather = (WeatherState)         s_i.getGlobalState();
		ETPState             lState   = (ETPState)             s_i.getLocalState();
		SwitchingAction      lAction  = (SwitchingAction)      a_i;

		return this.fTransitions.getTransitionFunction(lAgent, lWeather, lState, lAction);
	}

	@Override
	public boolean canFail(Action a_i)
	{
		if (a_i instanceof SwitchingAction)
		{
			SwitchingAction lAction = (SwitchingAction) a_i;

			return lAction.isOn();
		}

		return true;
	}

	@Override
	public double getSuccessProbability(int t, Agent p_i, Action a_i)
	{
		throw new IllegalStateException("Planner called the deprecated state estimator function.");
	}

	@Override
	public double getSuccessProbability(int t, Agent p_i, State s_i, Action a_i)
	{
		if (this.fSuccessEstimator == null)
			this.initializeSuccessEstimator();

		return this.fSuccessEstimator.estimateActionSuccess(t, p_i, s_i, a_i);
	}

	@Override
	public void loadAgent(Agent pAgent)
	{
		this.fTransitions.loadAgent((AdvancedGridLabAgent) pAgent);
	}

	@Override
	public void unloadAgent(Agent pAgent)
	{
		this.fTransitions.unloadAgent((AdvancedGridLabAgent) pAgent);
	}

	@Override
	public IAgentTransitionStream getTransitionStream(Agent pAgent)
	{
		return this.fTransitions.getAgentStream((AdvancedGridLabAgent) pAgent);
	}

	public GridLabTransitionManager3 getTransitionFunctions()
	{
		return this.fTransitions;
	}

	public void setSuccessLearner(IActionSuccessLearner pEstimator)
	{
		this.fSuccessEstimator = pEstimator;
	}
}
