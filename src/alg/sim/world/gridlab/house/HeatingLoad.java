package alg.sim.world.gridlab.house;


public class HeatingLoad
{
	public static final double BTUPHPW  = (3.4120);			// BTUPH/W
	public static final double BTUPHPKW = (1e3 * 3.4120);		// BTUPH/kW
	public static final double KWPBTUPH = (1e-3/BTUPHPW);	// kW/BTUPH

	private static double computeHeatingCOP(double heating_COP, double pTout)
	{
		double temp_temperature;
		double heating_cop_adj;

		if(pTout > 80){
			temp_temperature = 80;
			heating_cop_adj = heating_COP / (2.03914613 - 0.03906753*temp_temperature + 0.00045617*temp_temperature*temp_temperature - 0.00000203*temp_temperature*temp_temperature*temp_temperature);
		} else {
			heating_cop_adj = heating_COP / (2.03914613 - 0.03906753*(pTout) + 0.00045617*(pTout)*(pTout) - 0.00000203*(pTout)*(pTout)*(pTout));
		}

		return heating_cop_adj;
	}

	private static double computeHeatingCAP(double design_heating_capacity, double pTout)
	{
		double heating_capacity_adj;
		heating_capacity_adj = design_heating_capacity*(0.34148808 + 0.00894102*(pTout) + 0.00010787*(pTout)*(pTout));
		return heating_capacity_adj;
	}

	public static double computeHeatingLoad(double pOutdoorTemperatureF)
	{
		/**
		 *		TODO: Clearly, these should not be magic numbers, but derived somewhere...
		 */
		final double lCOP        = 3.5;			// Buildings are designed on a COP of 3.5 (see VariableHouseGLM)
		final double lHeatDesign = 8 * 6000;    // Designed loads are rounded to 6000 BTUh, and 8 was the measured value.
		final double lFanDesign  = 0.31777;		// Designed fan input energy (kW).

		double lEff = HeatingLoad.computeHeatingCOP(lCOP, pOutdoorTemperatureF);
		double lCap = HeatingLoad.computeHeatingCAP(lHeatDesign, pOutdoorTemperatureF);

		double lHeatKW   = ((lCap / lEff) * KWPBTUPH + lFanDesign);

		return lHeatKW;
	}

	public static double computeHeatingBTU(double pOutdoorTemperatureF, double pHeatDesign, double pFanDesign)
	{
		double lFanGain = pFanDesign * BTUPHPKW;
		double lCap     = HeatingLoad.computeHeatingCAP(pHeatDesign, pOutdoorTemperatureF);

		return (lCap + lFanGain);
	}

	public static double computeAuxiliaryBTU(double pAuxDesign, double pFanDesign)
	{
		double lFanGain = pFanDesign * BTUPHPKW;

		return (pAuxDesign + lFanGain);
	}

	public static double computeHeatingKW(double pOutdoorTemperatureF, double pCOP, double pHeatDesign, double pFanDesign)
	{
		double lCOP = HeatingLoad.computeHeatingCOP(pCOP, pOutdoorTemperatureF);
		double lCap = HeatingLoad.computeHeatingCAP(pHeatDesign, pOutdoorTemperatureF);

		return ((lCap / lCOP) * KWPBTUPH + pFanDesign);
	}

	public static double computeAuxiliaryKW(double pAuxDesign, double pFanDesign)
	{
		return (pAuxDesign    * KWPBTUPH + pFanDesign);
	}
}
