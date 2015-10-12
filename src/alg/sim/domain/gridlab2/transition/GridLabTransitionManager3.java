package alg.sim.domain.gridlab2.transition;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import alg.sim.domain.IAgentTransitionStream;
import alg.sim.domain.Transition;
import alg.sim.domain.gridlab.state.ETPState;
import alg.sim.domain.gridlab.state.IETPStateManager;
import alg.sim.domain.gridlab2.agent.AdvancedGridLabAgent;
import alg.sim.domain.gridlab2.state.WeatherState;
import alg.sim.domain.gridlab2.state.WeatherStateManager;
import alg.sim.domain.tcl.SwitchingAction;

public class GridLabTransitionManager3
{
	private final static int MAX_THREADS = 4;

	private final WeatherStateManager        fGlobal;
	private final IETPStateManager           fLocal;
	private final List<SwitchingAction>      fAction;

	private final String fFileDir;

	private AgentTransitionReader[] fTransitionFunctions;

	public GridLabTransitionManager3(WeatherStateManager pGlobal, IETPStateManager pLocal, List<SwitchingAction> pAction, String pFileDir)
	{
		this.fGlobal = pGlobal;
		this.fLocal  = pLocal;
		this.fAction = pAction;

		this.fFileDir = pFileDir;
		File lDir = new File(this.fFileDir);
		if (!lDir.exists() && !lDir.mkdirs())
			throw new RuntimeException("Could not create directory " + this.fFileDir);
	}

	private String getAgentFile(AdvancedGridLabAgent pAgent)
	{
		return String.format("%sa%03d.trans", this.fFileDir, pAgent.getID());
	}

	public void computeAllTransitionFunctions(List<AdvancedGridLabAgent> pAgents, double pQi)
	{
		int lIndex = 0;
		this.fTransitionFunctions = new AgentTransitionReader[pAgents.size()];
		while (lIndex < pAgents.size())
		{
			int lTodo = (pAgents.size() - lIndex);
			int lJobs = (MAX_THREADS < lTodo) ? MAX_THREADS : lTodo;

			CountDownLatch lLatch = new CountDownLatch(lJobs);

			for (int i = 0; i < lJobs; i++)
			{
				AdvancedGridLabAgent lAgent = pAgents.get(lIndex++);
				String               lFile  = this.getAgentFile(lAgent);

				AgentTransitionWriter lWriter = new AgentTransitionWriter(lAgent, lFile, this.fGlobal, this.fLocal, this.fAction, pQi);
				(new Thread(new AgentTransitionWriteJob(lWriter, lLatch))).start();
			}

			try
			{
				lLatch.await();
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}

			for (int i = 0; i < lJobs; i++)
				System.out.print("-");
			System.out.flush();
		}
	}

	public IAgentTransitionStream getAgentStream(AdvancedGridLabAgent pAgent)
	{
		return new AgentTransitionReader(pAgent, this.getAgentFile(pAgent), this.fGlobal, this.fLocal, this.fAction);
	}

	public void loadAgent(AdvancedGridLabAgent pAgent)
	{
		this.fTransitionFunctions[pAgent.getID()] = new AgentTransitionReader(pAgent, this.getAgentFile(pAgent), this.fGlobal, this.fLocal, this.fAction);
	}

	public Transition getTransitionFunction(AdvancedGridLabAgent pAgent, WeatherState pWeather, ETPState pState, SwitchingAction pAction)
	{
		return this.fTransitionFunctions[pAgent.getID()].getTransitionFunction(pWeather, pState, pAction);
	}

	public void unloadAgent(AdvancedGridLabAgent pAgent)
	{
		this.fTransitionFunctions[pAgent.getID()] = null;
	}
}
