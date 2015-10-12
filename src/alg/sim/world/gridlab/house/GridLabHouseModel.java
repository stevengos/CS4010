package alg.sim.world.gridlab.house;

import java.io.Serializable;

import alg.gridlabd.builder.models.glm.AdvancedHouseGLM;
import alg.gridlabd.builder.models.glm.ConstantsGLM;

public class GridLabHouseModel implements Serializable
{
	private static final long serialVersionUID = -393464086083036632L;
	public static long UPGRADE_LOAD = 1000 * 3600 / 60; // 1 kWh = 3600 * 1 / stepsize_used
	public static double DEFAULT_BALANCE_FACTOR = 1.0;

	public final double Ca;
	public final double Cm;
	public       double Ua;
	public final double Hm;
	public       double dt;

	public double c1;
	public double c2;

	public double r1;
	public double r2;
	public double A3;
	public double A4;

	private double fCOP;
	private double fHeat;
	private double fAux;
	private double fFan;

	private final double fInitAir;
	private final double fInitMass;

	private GridLabHouseAction	fCurrentAction;
	private double				fCurrentAirTemp;
	private double				fCurrentMassTemp;
	private double              fCurrentLoad;
	private boolean				fJustUpgraded;
	
	private double fBalanceFactor;

	public GridLabHouseModel(int pStepSeconds, double Ca, double Cm, double Ua, double Hm,
							 double pCOP, double pHeatDesign, double pAuxDesign, double pFanDesign,
							 double pInitAir, double pInitMass)
	{
		this.fCOP  = pCOP;
		this.fHeat = pHeatDesign;
		this.fAux  = pAuxDesign;
		this.fFan  = pFanDesign;

		this.Ca = Ca;
		this.Cm = Cm;
		this.Ua = Ua;
		this.Hm = Hm;

		this.dt = pStepSeconds/3600.0;

		this.computeOtherValues();
		
		this.fInitAir  = pInitAir;
		this.fInitMass = pInitMass;
		this.fBalanceFactor = DEFAULT_BALANCE_FACTOR;

		this.reset();
	}

	public GridLabHouseModel(int pStepSeconds, 
							 AdvancedHouseGLM pHouse,
							 double pInitAir, double pInitMass)
	{
		this.Ca    = ConstantsGLM.computeAirThermalMass();
		this.Cm    = ConstantsGLM.computeMassThermalMass();
		this.Ua    = ConstantsGLM.computeUA(pHouse);
		this.Hm    = ConstantsGLM.computeHeatTransfer();
		this.fHeat = ConstantsGLM.computeDesignHeating(this.Ua);
		this.fAux  = ConstantsGLM.computeDesignAux(this.Ua);
		this.fFan  = ConstantsGLM.computeFanPower(this.fHeat, this.fAux) / 1000;	// W to kW.
		this.fCOP  = pHouse.getHeatingCOP();

		this.dt = pStepSeconds/3600.0;  // Fraction of an hour.

		computeOtherValues();

		this.fInitAir  = pInitAir;
		this.fInitMass = pInitMass;
		this.fBalanceFactor = DEFAULT_BALANCE_FACTOR;

		this.reset();
	}
	
	private void computeOtherValues() {
		double a  = Cm*Ca/Hm;
		double b  = Cm*(Ua+Hm)/Hm+Ca;
		double c  = Ua;
		c1 = -(Ua + Hm)/Ca;
		c2 = Hm/Ca;
		double rr = Math.sqrt(b*b-4*a*c)/(2*a);
		double r  = -b/(2*a);
		r1 = r+rr;
		r2 = r-rr;
		A3 = Ca/Hm * r1 + (Ua+Hm)/Hm;
		A4 = Ca/Hm * r2 + (Ua+Hm)/Hm;
	}
	
	public void setHouse(AdvancedHouseGLM lHouse) {
		this.setHouse(lHouse, true);
	}
	
	public void setHouse(AdvancedHouseGLM lHouse, boolean incurCost) {
		this.Ua = ConstantsGLM.computeUA(lHouse);

		this.fHeat = ConstantsGLM.computeDesignHeating(this.Ua);
		this.fAux  = ConstantsGLM.computeDesignAux(this.Ua);
		this.fFan  = ConstantsGLM.computeFanPower(this.fHeat, this.fAux) / 1000;	// W to kW.

		this.computeOtherValues();
		this.fJustUpgraded = incurCost;
	}
	
	public GridLabHouseModel setBalanceFactor(double lFactor) {
		this.fBalanceFactor = lFactor;
		return this;
	}

	public double getDesignHeatBTUh()
	{
		return this.fHeat;
	}

	public double getDesignFanBTUh()
	{
		return this.fFan * HeatingLoad.BTUPHPKW;
	}

	public double getAirTemp()
	{
		return this.fCurrentAirTemp;
	}

	public double getMassTemp()
	{
		return this.fCurrentMassTemp;
	}

	public void setTemperature(double pAir, double pMass)
	{
		this.fCurrentAirTemp  = pAir;
		this.fCurrentMassTemp = pMass;
	}

	public double getQh(double Tout)
	{
		double Qh    = 0;

		switch (this.fCurrentAction)
		{
			case OFF:
			{
				Qh    = 0;
				break;
			}
			case HEAT:
			{
				Qh    = HeatingLoad.computeHeatingBTU(Tout, this.fHeat, this.fFan);
				break;
			}
			case AUX:
			{
				Qh    = HeatingLoad.computeAuxiliaryBTU(this.fAux, this.fFan);
				break;
			}
		}
		return Qh;
	}

	public double estimateLoad(double Tout)
	{
		return HeatingLoad.computeHeatingKW(Tout, this.fCOP, this.fHeat, this.fFan);
	}

	public double getDTa(double Tout)
	{
		double Qh    = 0;

		switch (this.fCurrentAction)
		{
			case OFF:
			{
				Qh    = 0;
				break;
			}
			case HEAT:
			{
				Qh    = HeatingLoad.computeHeatingBTU(Tout, this.fHeat, this.fFan);
				break;
			}
			case AUX:
			{
				Qh    = HeatingLoad.computeAuxiliaryBTU(this.fAux, this.fFan);
				break;
			}
		}

		return c2*this.fCurrentMassTemp + c1*this.fCurrentAirTemp - (c1+c2)*Tout + Qh/Ca;
	}

	public double getTeq(double Tout, double Qi)
	{
		double Qh    = 0;

		switch (this.fCurrentAction)
		{
			case OFF:
			{
				Qh    = 0;
				break;
			}
			case HEAT:
			{
				Qh    = HeatingLoad.computeHeatingBTU(Tout, this.fHeat, this.fFan);
				break;
			}
			case AUX:
			{
				Qh    = HeatingLoad.computeAuxiliaryBTU(this.fAux, this.fFan);
				break;
			}
		}

		return (Qh+Qi)/Ua + Tout;
	}

	public double getCurrentLoad() {
		return fCurrentLoad; 
	}

	public GridLabHouseAction getAction()
	{
		return this.fCurrentAction;
	}

	public void setAction(GridLabHouseAction pNewAction)
	{
		this.fCurrentAction = pNewAction;
	}

	public double advanceTime(double Tout, double Qi)
	{
		double Qh    = 0;
		double lLoad = 0;

		switch (this.fCurrentAction)
		{
			case OFF:
			{
				Qh    = 0;
				lLoad = 0;
				break;
			}
			case HEAT:
			{
				Qh    = HeatingLoad.computeHeatingBTU(Tout, this.fHeat, this.fFan);
				lLoad = HeatingLoad.computeHeatingKW(Tout, this.fCOP, this.fHeat, this.fFan);
				break;
			}
			case AUX:
			{
				Qh    = HeatingLoad.computeAuxiliaryBTU(this.fAux, this.fFan);
				lLoad = HeatingLoad.computeAuxiliaryKW(this.fAux, this.fFan);
				break;
			}
		}

		double Teq = (Qh+Qi)/Ua + Tout;

		double dTa = c2*this.fCurrentMassTemp + c1*this.fCurrentAirTemp - (c1+c2)*Tout + (Qh+0.5*Qi)/Ca;
		double k1  = (r2*this.fCurrentAirTemp - r2*Teq - dTa)/(r2-r1);
		double k2  = this.fCurrentAirTemp - Teq - k1;

		double e1 = k1*Math.exp(r1*dt);
		double e2 = k2*Math.exp(r2*dt);

		this.fCurrentAirTemp = e1 + e2 + Teq;
		this.fCurrentMassTemp = A3*e1 + A4*e2 + (0.5*Qi)/Hm + Teq;
		this.fCurrentLoad = lLoad + (fJustUpgraded ? UPGRADE_LOAD : 0);
		this.fJustUpgraded = false;
		
		return lLoad;
	}

	/**
	 *	Allows an external party to modify the seconds between advanceTime steps.
	 *
	 *	@param pStepSeconds Amount of time advanced by advanceTime().
	 */
	public void setDelta(int pStepSeconds)
	{
		this.dt = pStepSeconds/3600.0;
	}

	public void advanceTime(double Tout, double[] TMassAir, double Qi, GridLabHouseAction pAction)
	{
		double Qh    = 0;

		switch (pAction)
		{
			case OFF:  Qh = 0; break;
			case HEAT: Qh = HeatingLoad.computeHeatingBTU(Tout, this.fHeat, this.fFan); break;
			case AUX:  Qh = HeatingLoad.computeAuxiliaryBTU(this.fAux, this.fFan); break;
		}

		double Teq = (Qh+Qi)/Ua + Tout;

		double dTa = c2*TMassAir[0] + c1*TMassAir[1] - (c1+c2)*Tout + (Qh+0.5*Qi)/Ca;
		double k1  = (r2*TMassAir[1] - r2*Teq - dTa)/(r2-r1);
		double k2  = TMassAir[1] - Teq - k1;

		double e1 = k1*Math.exp(r1*dt);
		double e2 = k2*Math.exp(r2*dt);

		TMassAir[1] = e1 + e2 + Teq;
		TMassAir[0] = A3*e1 + A4*e2 + (0.5*Qi)/Hm + Teq;
	}

	public void reset()
	{
		this.fCurrentAction   = GridLabHouseAction.OFF;
		this.fCurrentAirTemp  = this.fInitAir;
		this.fCurrentMassTemp = this.fInitMass;
	}

	/**
	 *	Copies the GridLabHouseModel, allowing the caller to set a different step-size for the advanceTime delta.
	 *
	 *	@param pStepsizeS The new step-size in seconds.
	 *	@return GridLabHouseModel with identical thermal properties but a delta derived from pStepSize.
	 */
	public GridLabHouseModel copyOf(int pStepsizeS)
	{
		return new GridLabHouseModel(pStepsizeS, this.Ca, this.Cm, this.Ua, this.Hm, 
									 this.fCOP, this.fHeat, this.fAux, this.fFan,
									 this.fInitAir, this.fInitMass).setBalanceFactor(this.getBalanceFactor());
	}
	
	/**
	 *	Copies the GridLabHouseModel, allowing the caller to set a different step-size for the advanceTime delta,
	 *  whilst retaining the current air and mass temperatures.
	 *
	 *	@param pStepsizeS The new step-size in seconds.
	 *	@return GridLabHouseModel with identical thermal properties but a delta derived from pStepSize.
	 */
	public GridLabHouseModel continuationCopyOf(int pStepsizeS)
	{
		return new GridLabHouseModel(pStepsizeS, this.Ca, this.Cm, this.Ua, this.Hm, 
									 this.fCOP, this.fHeat, this.fAux, this.fFan,
									 this.fCurrentAirTemp, this.fCurrentMassTemp).setBalanceFactor(this.getBalanceFactor());
	}

	public double getBalanceFactor() {
		return this.fBalanceFactor;
	}
}
