package alg.gridlabd.builder.models.glm;

/**
 * 
 * Snippet from house.cpp in GridLAB-D:
 * 

	case TI_ABOVE_NORMAL:
		if(Rroof <= 0.0) Rroof = 30;
		if(Rwall <= 0.0) Rwall = 19;
		if(Rfloor <= 0.0) Rfloor = 11;
		if(Rdoors <= 0.0) Rdoors = 3;
		if(Rwindows <= 0.0) Rwindows = 1/0.6;
		if(airchange_per_hour < 0.0) airchange_per_hour = 1.0;
		break;
	case TI_GOOD:
		if(Rroof <= 0.0) Rroof = 30;
		if(Rwall <= 0.0) Rwall = 19;
		if(Rfloor <= 0.0) Rfloor = 22;
		if(Rdoors <= 0.0) Rdoors = 5;
		if(Rwindows <= 0.0) Rwindows = 1/0.47;
		if(airchange_per_hour < 0.0) airchange_per_hour = 0.5;
		break;
	case TI_VERY_GOOD:
		if(Rroof <= 0.0) Rroof = 48;
		if(Rwall <= 0.0) Rwall = 22;
		if(Rfloor <= 0.0) Rfloor = 30;
		if(Rdoors <= 0.0) Rdoors = 11;
		if(Rwindows <= 0.0) Rwindows = 1/0.31;
		if(airchange_per_hour < 0.0) airchange_per_hour = 0.5;
		break;

 * 
 * This enumerator simply makes the same enumeration available in my code, concerning only the R-values.
 * 
 * 
 * @author Frits de Nijs
 *
 */
public enum HouseIntegrityLevel
{
	ABOVE_NORMAL(30, 19, 11,  3, 1/0.6,  1  ),
	GOOD(        30, 19, 22,  5, 1/0.47, 0.5),
	VERY_GOOD(   48, 22, 30, 11, 1/0.31, 0.5);

	private final double fRroof;
	private final double fRwall;
	private final double fRfloor;
	private final double fRdoors;
	private final double fRwindows;
	private final double fAirchange;

	private HouseIntegrityLevel(double pRroof, double pRwall, double pRfloor, double pRdoors, double pRwindows, double pAirchange)
	{
		this.fRroof     = pRroof;
		this.fRwall     = pRwall;
		this.fRfloor    = pRfloor;
		this.fRdoors    = pRdoors;
		this.fRwindows  = pRwindows;
		this.fAirchange = pAirchange;
	}

	public double getRroof()     { return this.fRroof;     }
	public double getRwall()     { return this.fRwall;     }
	public double getRfloor()    { return this.fRfloor;    }
	public double getRdoors()    { return this.fRdoors;    }
	public double getRwindows()  { return this.fRwindows;  }
	public double getAirchange() { return this.fAirchange; }
}
