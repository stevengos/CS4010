package alg.util.clipping;

public class ClipVertex
{
	private final double[] fPoint;

	public ClipVertex(double x, double y)
	{
		this.fPoint = new double[] { x, y };
	}

	public ClipVertex(double[] pPoint)
	{
		this.fPoint = pPoint;
	}

	public double getX()
	{
		return this.fPoint[0];
	}

	public double getY()
	{
		return this.fPoint[1];
	}

	@Override
	public String toString()
	{
		return "(" + this.getX() + ", " + this.getY() + ")";
	}

	@Override
	public boolean equals(Object pOther)
	{
		if (pOther instanceof ClipVertex)
		{
			ClipVertex that = (ClipVertex) pOther;

			return ((this.getX() == that.getX()) && (this.getY() == that.getY()));
		}

		return false;
	}
}
