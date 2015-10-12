package alg.harnass.gridlab;

public enum PlanQuality
{
	/**
	 *	Added as counter-weight to VeryHigh, the VeryLow quality is not practical for anything. But it can act as a proof-of-competence of
	 *	planning over doing nothing
	 *
	 *	Transition-table = 30 outdoor x 22 mass x 10 air x 2 actions x (4 + 12 * ~8 destinations) bytes =~ 1.561 MB per agent
	 *	Q-table = ~2880 time-steps x 22 mass x 10 air x 2 actions x 8 bytes = ~9.668 MB per agent.
	 */
	VeryLow(  60,
			  30, PlanQuality.SETPOINT-65, PlanQuality.SETPOINT+25,			// 90 degrees divided 30 states, so steps of 3.0 degrees Fahrenheit appear.
			  20, PlanQuality.SETPOINT-20, PlanQuality.SETPOINT+20,			// 24 degrees divided 12 states, so steps of 2.0 degrees Fahrenheit appear.
			  12, 12, 														// 12 degrees divided 12 states, so steps of 1.0 degrees Fahrenheit appear.
			  30, PlanQuality.MINPRICE, PlanQuality.MAXPRICE,
			  30, PlanQuality.MINSOLAR, PlanQuality.MAXSOLAR),

	/**
	 *	The Low plan quality gives very `quick-and-dirty' plans, ideal for bug-hunting, draft plots, or very large-scale systems that must also
	 *	run without disk-swapping (such as a naive SmartCap implementation)
	 *
	 *	Transition-table = 30 outdoor x 26 mass x 10 air x 2 actions x (4 + 12 * ~8 destinations) bytes =~ 1.488 MB per agent
	 *	Q-table = ~2880 time-steps x 26 mass x 10 air x 2 actions x 8 bytes = ~11.43 MB per agent.
	 */
	Low(      60,
			  30, PlanQuality.SETPOINT-65, PlanQuality.SETPOINT+25,			// 90 degrees divided 30 states, so steps of 3.0 degrees Fahrenheit appear.
			  40, PlanQuality.SETPOINT-20, PlanQuality.SETPOINT+20,			// 24 degrees divided 24 states, so steps of 1.0 degrees Fahrenheit appear.
			  24, 12,														// 12 degrees divided 24 states, so steps of 0.5 degrees Fahrenheit appear.
			  30, PlanQuality.MINPRICE, PlanQuality.MAXPRICE,
			  30, PlanQuality.MINSOLAR, PlanQuality.MAXSOLAR),

	/**
	 *	The Normal plan quality should fit into main memory on any recent high-end machine for any reasonable (~200) number of agents.
	 *
	 *	Transition-table = 45 outdoor x 50 mass x 18 air x 2 actions x (4 + 12 * ~10 destinations) bytes =~ 9.579 MB per agent
	 *	Q-table = ~2880 time-steps x 50 mass x 18 air x 2 actions x 8 bytes = ~39.55 MB per agent.
	 */
	Normal(   60,
			  45, PlanQuality.SETPOINT-65, PlanQuality.SETPOINT+25,			// 90 degrees divided 45 states, so steps of 2.0 degrees Fahrenheit appear.
			  60, PlanQuality.SETPOINT-20, PlanQuality.SETPOINT+20,			// 24 degrees divided 48 states, so steps of 0.5 degrees Fahrenheit appear.
			  36, 12,														// 12 degrees divided 36 states, so steps of 0.33 degrees Fahrenheit appear.
			  45, PlanQuality.MINPRICE, PlanQuality.MAXPRICE,
			  45, PlanQuality.MINSOLAR, PlanQuality.MAXSOLAR),

	/**
	 *	The High plan quality aims to be the highest practical quality for camera ready quality results. High quality plans are reasonably
	 *	efficient to compute but still require hard-disk storage of Q-tables for practical number of agents.
	 *
	 *	Transition-table = 45 outdoor x 62 mass x 22 air x 2 actions x (4 + 12 * ~10 destinations) bytes =~ 14.5 MB per agent
	 *	Q-table = ~5760 time-steps x 62 mass x 22 air x 2 actions x 8 bytes = ~118.0 MB per agent.
	 */
	High(     30,
			  45, PlanQuality.SETPOINT-65, PlanQuality.SETPOINT+25,			// 90 degrees divided 45 states, so steps of 2.0 degrees Fahrenheit appear.
			  80, PlanQuality.SETPOINT-20, PlanQuality.SETPOINT+20,			// 24 degrees divided 60 states, so steps of 0.4 degrees Fahrenheit appear.
			  48, 12,														// 12 degrees divided 48 states, so steps of 0.25 degrees Fahrenheit appear.
			  45, PlanQuality.MINPRICE, PlanQuality.MAXPRICE,
			  45, PlanQuality.MINSOLAR, PlanQuality.MAXSOLAR),

	/**
	 *	The Very High plan quality fixes some non-intuitiveness about the Canonical quality. For example, it centers the mass range around the
	 *	set-point, and decreases the size of the air range to be less over-sized (see empirical testing in experiment.micro.StateCoverage). It
	 *	uses some of the freed-up state-space to increase the sampling of weather states. Still only really useful for proof-of-concept runs
	 *	and benchmarking.
	 *
	 *	Transition-table = 90 outdoor x 82 mass x 34 air x 2 actions x (4 + 12 * ~10 destinations) bytes =~ 59.35 MB per agent
	 *	Q-table = ~5760 time-steps x 82 mass x 34 air x 2 actions x 8 bytes = ~245.0 MB per agent.
	 */
	VeryHigh( 30,
			  90, PlanQuality.SETPOINT-65, PlanQuality.SETPOINT+25,			// 90 degrees divided 90 states, so steps of 1.0 degrees Fahrenheit appear.
			  80, PlanQuality.SETPOINT-12, PlanQuality.SETPOINT+12,			// 24 degrees divided 80 states, so steps of 0.3 degrees Fahrenheit appear.
			  96, 12,														// 12 degrees divided 96 states, so steps of 0.125 degrees Fahrenheit appear.
			  90, PlanQuality.MINPRICE, PlanQuality.MAXPRICE,
			  90, PlanQuality.MINSOLAR, PlanQuality.MAXSOLAR),

	@Deprecated
	/**
	 *	The Canonical plan quality is (approximately) equal to the quality of the plans used in the AAAI paper on
	 *	first-order TCL models. Very, very slow, good (only) for proof-of-concept runs and benchmarking.
	 *
	 *	Transition-table = 45 outdoor x 82 mass x 52 air x 2 actions x (4 + 12 * ~10 destinations) bytes =~ 45.38 MB per agent
	 *	Q-table = ~5760 time-steps x 82 mass x 52 air x 2 actions x 8 bytes = ~374.8 MB per agent.
	 */
	Canonical(30,
			  45, PlanQuality.SETPOINT-65, PlanQuality.SETPOINT+25,			// 90 degrees divided 45 states, so steps of 2.0 degrees Fahrenheit appear.
			  80, PlanQuality.SETPOINT-10, PlanQuality.SETPOINT+6,			// 16 degrees divided 80 states, so steps of 0.2 degrees Fahrenheit appear.
			  50, 5,														//  5 degrees divided 50 states, so steps of 0.1 degrees Fahrenheit appear.
			  45, PlanQuality.MINPRICE, PlanQuality.MAXPRICE,
			  45, PlanQuality.MINSOLAR, PlanQuality.MAXSOLAR);

	/**
	 * It is assumed that houses desire a setpoint of approximately 21.1 degrees Celsius.
	 */
	private static final int SETPOINT = 70;
	
	private static final int MINPRICE = -271;
	private static final int MAXPRICE = 601;

	private static final int MINSOLAR = 0;
	private static final int MAXSOLAR = 1;

	private final int fStepSizeS;

	private final int fNumOutStates;
	private final int fMinOutF;
	private final int fMaxOutF;

	private final int fNumMassStates;
	private final int fMinMassF;
	private final int fMaxMassF;

	private final int fNumAirStates;
	private final int fAirBandF;
	
	private final int fNumPriceStates;
	private final int fMinPrice;
	private final int fMaxPrice;

	private final int fNumSolarStates;
	private final int fMinSolar;
	private final int fMaxSolar;

	private PlanQuality(int pStepSize,
						int pNumOut,  int pOutMinF,  int pOutMaxF,
						int pNumMass, int pMassMinF, int pMassMaxF,
						int pNumAir,  int pAirBandF,
						int pNumPrice, int pPriceMin, int pPriceMax,
						int pNumSolar, int pSolarMin, int pSolarMax)
	{
		this.fStepSizeS     = pStepSize;

		this.fNumOutStates  = pNumMass;
		this.fMinOutF       = pOutMinF;
		this.fMaxOutF       = pOutMaxF;

		this.fNumMassStates = pNumMass;
		this.fMinMassF      = pMassMinF;
		this.fMaxMassF      = pMassMaxF;

		this.fNumAirStates  = pNumAir;
		this.fAirBandF      = pAirBandF;
		
		this.fNumPriceStates = pNumPrice;
		this.fMinPrice = pPriceMin;
		this.fMaxPrice = pPriceMax;

		this.fNumSolarStates = pNumSolar;
		this.fMinSolar = pSolarMin;
		this.fMaxSolar = pSolarMax;
	}

	/**
	 *	@return Step-size of the plan in seconds. This value is the number of seconds between decisions. Increasing
	 *	this value linearly decreases total solution complexity, at the cost of increased overshoot.
	 */
	public int getStepSize()
	{
		return this.fStepSizeS;
	}

	/**
	 *	@return Number of states used to discretize the range of possible outdoor temperatures. The size of the
	 *	transition matrix increases linearly with the number of outdoor states. This factor has no effect on the
	 *	solution complexity (assuming that we have perfect knowledge about the outdoor temperature progression!).
	 *	However, higher number of outdoor states increases the fit of the transition function, and thus plan accuracy.
	 */
	public int getNumOutdoorStates()
	{
		return this.fNumOutStates;
	}

	/**
	 *	@return Lowest possible outdoor temperature. Outdoor temperatures lower than this value are mapped to the
	 *	range starting from this temperature.
	 */
	public int getMinimumOutdoorTempF()
	{
		return this.fMinOutF;
	}

	/**
	 *	@return Highest possible outdoor temperature. Outdoor temperatures higher than this value are mapped to the
	 *	range ending at this temperature.
	 */
	public int getMaximumOutdoorTempF()
	{
		return this.fMaxOutF;
	}

	/**
	 *	@return Number of states used to discretize the range of likely mass temperatures. The state space increases
	 *	linearly with the number of mass states, which in turn affects the complexity of finding the transition matrix
	 *	and the solutions.
	 */
	public int getNumMassStates()
	{
		return this.fNumMassStates;
	}

	/**
	 *	@return Lowest likely mass temperature. Mass temperatures lower than this value are lumped together in the
	 *	boundary mass state ranging from minus infinity up to this value.
	 */
	public int getMinimumMassTempF()
	{
		return this.fMinMassF;
	}

	/**
	 *	@return Highest likely mass temperature. Mass temperatures higher than this value are lumped together in the
	 *	boundary mass state ranging from this value up to infinity.
	 */
	public int getMaximumMassTempF()
	{
		return this.fMaxMassF;
	}
/**
	 *	@return Number of states used to discretize the bounding range of likely air temperatures. The state space increases 
	 *	linearly with the number of air states, which in turn affects the complexity of finding the transition matrix
	 *	and the solutions.
	 */
	public int getNumAirStates()
	{
		return this.fNumAirStates;
	}

	/**
	 *	@return Number of degrees in the band of likely air temperatures surrounding each likely mass temperature. Air
	 *	temperatures outside the mass temperature plus/minus half the air band are lumped in infinite boundary states.
	 */
	public int getAirBandF()
	{
		return this.fAirBandF;
	}
	

	/**
	 *	@return Number of states used to discretize the range of possible energy prices. The size of the
	 *	transition matrix increases linearly with the number of price states. 
	 *	Higher number of price states increases the fit of the transition function, and thus plan accuracy.
	 */
	public int getNumPriceStates()
	{
		return this.fNumPriceStates;
	}

	/**
	 *	@return Lowest possible energy price. Prices lower than this value are mapped to the
	 *	range starting from this price.
	 */
	public int getMinimumPrice()
	{
		return this.fMinPrice;
	}

	/**
	 *	@return Highest possible energy price. Prices higher than this value are mapped to the
	 *	range ending at this price.
	 */
	public int getMaximumPrice()
	{
		return this.fMaxPrice;
	}

	/**
	 *	@return Number of states used to discretize the range of possible solar states. The size of the
	 *	transition matrix increases linearly with the number of solar states. 
	 *	Higher number of solar states increases the fit of the transition function, and thus plan accuracy.
	 */
	public int getNumSolarStates()
	{
		return this.fNumSolarStates;
	}

	/**
	 *	@return Lowest possible solar energy. Solars lower than this value are mapped to the
	 *	range starting from this amount of energy.
	 */
	public int getMinimumSolar()
	{
		return this.fMinSolar;
	}

	/**
	 *	@return Highest possible solary energy. Solars higher than this value are mapped to the
	 *	range ending at this amount of energy.
	 */
	public int getMaximumSolar()
	{
		return this.fMaxSolar;
	}

}
