package alg.gridlabd.builder.models.glm;

/**
 *
 *			Constants for GLM house-model
 *
 *	List of constants, and functions to compute house-property constants. Based completely on house.cpp from GridLAB-D.
 *
 *
 * @author Frits de Nijs
 *
 */
public class ConstantsGLM
{
	/*
	 *		Numeric constants.
	 */
	private static final double number_of_stories						= 1.0;							// #
	private static final double aspect_ratio							= 1.5;							// width / depth
	private static final double floor_area								= 2500.0;						// feet^2
	private static final double ceiling_height							= 8.0;							// feet
	private static final double window_wall_ratio						= 0.15;							// % / 100
	private static final double number_of_doors							= 4.0;							// #
	private static final double total_thermal_mass_per_floor_area		= 2.0;							// ?   -> lb / feet^2 ?
	private static final double interior_surface_heat_transfer_coeff	= 1.46;							// ?
	private static final double interior_exterior_wall_ratio			= 1.5;							// Based partions for six rooms per floor
	private static final double air_density								= 0.0735;						// density of air [lb/cf]
	private static final double air_heat_capacity						= 0.2402;						// heat capacity of air @ 80F [BTU/lb/F]
	private static final double glazing_shgc							= 0.67;
	private static final double window_exterior_transmission_coefficient = 0.6;
	private static final double over_sizing_factor						= 0;
	private static final double latent_load_fraction					= 0.3;
	private static final double cooling_design_temperature				= 95;
	private static final double design_cooling_setpoint					= 75;
	private static final double design_peak_solar						= 195.0;
	private static final double design_heating_setpoint					= 70;
	private static final double heating_design_temperature				= 0;
	private static final double cooling_supply_air_temp					= 50.0;
	private static final double heating_supply_air_temp					= 150.0;
	private static final double duct_pressure_drop						= 0.5;

	/*
	 *		Derived constants.
	 */
	private static final double volume				   = ceiling_height * floor_area;					// volume of air [cf]
	private static final double air_mass			   = air_density    * volume;						// mass of air [lb]
	private static final double door_area              = number_of_doors * 3.0 * 78.0 / 12.0; 			// 3' wide by 78" tall
	private static final double gross_wall_area        = 2.0 * number_of_stories * (aspect_ratio + 1.0) * ceiling_height * Math.sqrt(floor_area/aspect_ratio/number_of_stories);
	private static final double window_area            = gross_wall_area * window_wall_ratio;
	private static final double net_exterior_wall_area = gross_wall_area - window_area - door_area;
	private static final double exterior_ceiling_area  = floor_area / number_of_stories;
	private static final double exterior_floor_area    = floor_area / number_of_stories;

	private static final double air_thermal_mass       = 3 * air_heat_capacity * air_mass;
	private static final double mass_thermal_mass      = total_thermal_mass_per_floor_area * floor_area - 2 * air_heat_capacity * air_mass;
	private static final double heat_transfer_coeff    = interior_surface_heat_transfer_coeff * ((gross_wall_area - window_area - door_area)
															 + gross_wall_area * interior_exterior_wall_ratio
															 + number_of_stories * exterior_ceiling_area);

	private static final double design_internal_gains  = 167.09 * Math.pow(floor_area, 0.442);
	private static final double solar_heatgain_factor  = window_area * glazing_shgc * window_exterior_transmission_coefficient;

	public static double computeAirThermalMass()
	{
		return air_thermal_mass;
	}

	public static double computeMassThermalMass()
	{
		return mass_thermal_mass;
	}

	public static double computeUA(AdvancedHouseGLM pHouse)
	{
		double airchange_UA = pHouse.getAirchange() * volume * air_density * air_heat_capacity;

		double envelope_UA = exterior_ceiling_area  / pHouse.getRroof()    + 
							 exterior_floor_area    / pHouse.getRfloor()   + 
							 net_exterior_wall_area / pHouse.getRwall()    + 
							 window_area            / pHouse.getRwindows() + 
							 door_area              / pHouse.getRdoors();

		return envelope_UA + airchange_UA;
	}

	public static double computeHeatTransfer()
	{
		return heat_transfer_coeff;
	}

	public static double computeDesignHeating(double pUA)
	{
		double round_value = 0.0;
		double design_heating_capacity = (1.0 + over_sizing_factor) * (1.0 + latent_load_fraction) * ((pUA) * (cooling_design_temperature - design_cooling_setpoint) + design_internal_gains + (design_peak_solar * solar_heatgain_factor));
		round_value = (design_heating_capacity) / 6000.0;
		design_heating_capacity = Math.ceil(round_value) * 6000.0; // design_heating_capacity is rounded up to the next 6000 btu/hr

		return design_heating_capacity;
	}

	public static double computeDesignAux(double pUA)
	{
		double aux_heat_capacity = (1.0 + over_sizing_factor) * (pUA) * (design_heating_setpoint - heating_design_temperature);
		double round_value = (aux_heat_capacity) / 10000.0;
		aux_heat_capacity = Math.ceil(round_value) * 10000.0; // aux_heat_capacity is rounded up to the next 10,000 btu/hr

		return aux_heat_capacity;
	}

	public static double computeFanPower(double pDesignHeat, double pDesignAux)
	{
		double design_heating_cfm = (pDesignHeat > pDesignAux ? pDesignHeat : pDesignAux) / (air_density * air_heat_capacity * (heating_supply_air_temp - design_heating_setpoint)) / 60.0;
		double design_cooling_cfm = pDesignHeat / (1.0 + latent_load_fraction) / (air_density * air_heat_capacity * (design_cooling_setpoint - cooling_supply_air_temp)) / 60.0;
		double fan_design_airflow = (design_heating_cfm > design_cooling_cfm ? design_heating_cfm : design_cooling_cfm);
		double roundval = Math.ceil((0.117 * duct_pressure_drop * fan_design_airflow / 0.42 / 745.7)*8);
		double fan_design_power = roundval / 8.0 * 745.7 / 0.88; // fan rounds to the nearest 1/8 HP

		return fan_design_power;
	}
}
