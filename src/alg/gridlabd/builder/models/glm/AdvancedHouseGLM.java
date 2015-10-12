package alg.gridlabd.builder.models.glm;

import java.util.Random;

import alg.sim.world.gridlab.house.GridLabHouseAction;

public class AdvancedHouseGLM implements NamedGLM
{
	private final int fID;

	/*
	 *		GridLAB model properties
	 *
	 *	These properties are the minimum properties needed to report
	 *	to GridLAB in order to construct a house that has an identical
	 *	response in GridLAB as in our `GridLAB Simulator'.
	 *
	 */
	private final MeterGLM fParent;
	private final HouseThermostat fMode;
	private final HouseIntegrityLevel fQuality;

	private final double fRroof;
	private final double fRwall;
	private final double fRfloor;
	private final double fRdoors;
	private final double fRwindows;

	private final double fHeatingCOP;

	private final double fHeatingSetpoint;
	private final double fCoolingSetpoint;

	/**
	 *	Non-randomized version of the house, quality matches exactly the specified quality.
	 *
	 * @param pID
	 * @param pQuality
	 * @param pSetpoint
	 */
	public AdvancedHouseGLM(int pID, HouseIntegrityLevel pQuality, double pSetpoint)
	{
		this(pID, pQuality, HouseThermostat.NONE, pSetpoint);
	}

	public AdvancedHouseGLM(int pID, HouseIntegrityLevel pQuality, HouseThermostat pMode, double pSetpoint)
	{
		this.fID      = pID;
		this.fParent  = new MeterGLM(pID, "houses");
		this.fMode    = pMode;
		this.fQuality = pQuality;

		this.fHeatingSetpoint = pSetpoint;
		this.fCoolingSetpoint = pSetpoint;

		this.fRroof    = pQuality.getRroof();
		this.fRwall    = pQuality.getRwall();
		this.fRfloor   = pQuality.getRfloor();
		this.fRdoors   = pQuality.getRdoors();
		this.fRwindows = pQuality.getRwindows();

		this.fHeatingCOP = 3.5;
	}

	public AdvancedHouseGLM(int pID, MeterGLM pParent, HouseThermostat pMode, HouseIntegrityLevel pQuality, double pSetpoint, Random pRandGen)
	{
		this.fID      = pID;
		this.fParent  = pParent;
		this.fMode    = pMode;
		this.fQuality = pQuality;

		this.fHeatingSetpoint = pSetpoint;
		this.fCoolingSetpoint = pSetpoint;

		this.fRroof    = pQuality.getRroof()    * (0.95 + 0.1*pRandGen.nextDouble());
		this.fRwall    = pQuality.getRwall()    * (0.95 + 0.1*pRandGen.nextDouble());
		this.fRfloor   = pQuality.getRfloor()   * (0.95 + 0.1*pRandGen.nextDouble());
		this.fRdoors   = pQuality.getRdoors()   * (0.95 + 0.1*pRandGen.nextDouble());
		this.fRwindows = pQuality.getRwindows() * (0.95 + 0.1*pRandGen.nextDouble());

		this.fHeatingCOP = 3.5;
	}

	public int getID()
	{
		return this.fID;
	}

	public String getName()
	{
		return String.format("house_%03d", this.getID());
	}

	public String getParent()
	{
		return this.fParent.getName();
	}

	public String getParentGroup()
	{
		return this.fParent.getGroup();
	}

	public MeterGLM getMeter()    { return this.fParent; }
	public double getAirchange()  { return this.fQuality.getAirchange(); }
	public double getRroof()      { return this.fRroof;      }
	public double getRwall()      { return this.fRwall;      }
	public double getRfloor()     { return this.fRfloor;     }
	public double getRdoors()     { return this.fRdoors;     }
	public double getRwindows()   { return this.fRwindows;   }
	public double getHeatingCOP() { return this.fHeatingCOP; }

	public String toModel(double pAirF, double pMassF)
	{
		return this.toModel(pAirF, pMassF, null);
	}

	public String toModel(double pAirF, double pMassF, GridLabHouseAction pAction)
	{
		String lHouse = new String();

		lHouse += "object house {\n";
		lHouse += "  name "						+ this.getName()		+ ";\n";
		lHouse += "  parent "   				+ this.getParent()		+ ";\n";
		lHouse += "  groupid "  				+ this.getParentGroup()	+ ";\n";
		lHouse += "  thermal_integrity_level "	+ this.fQuality			+ ";\n";
		lHouse += "  thermostat_control "       + this.fMode			+ ";\n";
		lHouse += "  Rroof "    				+ this.getRroof()		+ ";\n";
		lHouse += "  Rwall "    				+ this.getRwall()		+ ";\n";
		lHouse += "  Rfloor "   				+ this.getRfloor()		+ ";\n";
		lHouse += "  Rdoors "   				+ this.getRdoors()		+ ";\n";
		lHouse += "  Rwindows " 				+ this.getRwindows()	+ ";\n";
		lHouse += "  heating_COP "				+ this.getHeatingCOP()  + ";\n";
		lHouse += "  heating_setpoint "			+ this.fHeatingSetpoint + ";\n";
		lHouse += "  cooling_setpoint "			+ this.fCoolingSetpoint + ";\n";
		lHouse += "  air_temperature "			+ pAirF					+ ";\n";
		lHouse += "  mass_temperature "			+ pMassF				+ ";\n";
		if (pAction != null)
		lHouse += "  system_mode "				+ pAction				+ ";\n";
		lHouse += "}\n";

		return lHouse;
	}

	public static void main(String[] args)
	{
		AdvancedHouseGLM lHouse = new AdvancedHouseGLM(1, new MeterGLM(1, "houses"), HouseThermostat.NONE, HouseIntegrityLevel.VERY_GOOD, 70, new Random());
		System.out.println(lHouse.toModel(70.1, 70.3, GridLabHouseAction.HEAT));
	}
}
