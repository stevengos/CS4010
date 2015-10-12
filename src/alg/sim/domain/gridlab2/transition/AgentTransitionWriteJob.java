package alg.sim.domain.gridlab2.transition;

import java.util.concurrent.CountDownLatch;

public class AgentTransitionWriteJob implements Runnable
{
	private final CountDownLatch fLatch;

	private final AgentTransitionWriter fWriter;

	public AgentTransitionWriteJob(AgentTransitionWriter pWriter, CountDownLatch pLatch)
	{
		this.fWriter = pWriter;
		this.fLatch  = pLatch;
	}

	@Override
	public void run()
	{
		this.fWriter.computeTransitionFunction();

		this.fLatch.countDown();
	}
}
