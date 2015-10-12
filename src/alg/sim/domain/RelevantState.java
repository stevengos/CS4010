package alg.sim.domain;

/**
 *		Relevant State of an Agent in an Arbitrage MDP
 *
 *	The Relevant State of an Agent is the decomposed joint state. The decomposition tells the Agent
 *	its own local State (which transitions through a selected Action), and the relevant aspect of
 *	the Global State (which the Agent is unable to influence, but which may influence the Agents
 *	transition function).
 *
 * @author Frits de Nijs
 *
 */
public class RelevantState extends State
{
	private static final long serialVersionUID = 887338012502686673L;

	private final int			fTime;
	private final GlobalState	fGlobal;
	private final State			fLocal;

	public RelevantState(int pTime, GlobalState pGlobal, State pLocal)
	{
		super("");
		

		this.fTime   = pTime;
		this.fGlobal = pGlobal;
		this.fLocal  = pLocal;
	}

	@Override
	public int getID()
	{
		return 0;
	}

	public int getTime()
	{
		return this.fTime;
	}

	public GlobalState getGlobalState()
	{
		return this.fGlobal;
	}

	public State getLocalState()
	{
		return this.fLocal;
	}

	@Override
	public String toString()
	{
		return String.format("[Relevant State at %d of %s]", this.fTime, this.fLocal.toString());
	}
}
