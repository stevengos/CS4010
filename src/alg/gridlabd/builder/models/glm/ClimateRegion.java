package alg.gridlabd.builder.models.glm;

public enum ClimateRegion
{
	NETHERLANDS("Netherlands", "in/knmiData.csv", +1, 0),
	RENO("Reno NV", "in/climate/NV-Reno.tmy2", -8, 1341),
	SEATTLE("Seattle WA", "in/climate/WA-Seattle.tmy2", -8, 122),
	NYC("New_york_city NY", "in/climate/NY-New_york_city.tmy2", -5, 57),
	BAKERSFIELD("Bakersfield CA", "in/climate/CA-Bakersfield.tmy2", -8, 150);

	private final String fName;
	private final String fTMYfilePath;
	private final int    fTimeZoneOffsetHours;
	private final int    fElevationMeters;

	private ClimateRegion(String pName, String pFilePath, int pOffset, int pElevation)
	{
		this.fName				  = pName;
		this.fTMYfilePath		  = pFilePath;
		this.fTimeZoneOffsetHours = pOffset;
		this.fElevationMeters	  = pElevation;
	}

	public String getName()
	{
		return this.fName;
	}

	public String getFilePath()
	{
		return this.fTMYfilePath;
	}

	public int getTimeZoneOffset()
	{
		return this.fTimeZoneOffsetHours;
	}

	public int getElevation()
	{
		return this.fElevationMeters;
	}
}
