package alg.gridlabd.builder.models.glm;

public class MeterGLM
{
	private final int fID;

	private final String fGroup;

	public MeterGLM(int pID, String pGroup)
	{
		this.fID    = pID;
		this.fGroup = pGroup;
	}

	public int getID()
	{
		return this.fID;
	}

	public String getGroup()
	{
		return this.fGroup;
	}

	public String getName()
	{
		return String.format("house_%03d_meter", this.getID());
	}

	public String toModel()
	{
		String lModel = new String();

		lModel += "object triplex_meter {\n";
		lModel += "  name "    + this.getName()  + ";\n";
		lModel += "  groupid " + this.getGroup() + ";\n";
		lModel += "  phases BS;\n";
		lModel += "  nominal_voltage 120;\n";
		lModel += "};\n";

		return lModel;
	}
}
