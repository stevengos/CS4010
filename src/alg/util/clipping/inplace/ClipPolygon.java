package alg.util.clipping.inplace;

import alg.util.PolygonArea;

public class ClipPolygon
{
	private final double[][] fVertices;
	private       int	     fSize;

	public ClipPolygon(int pMaxNumVertices)
	{
		this.fVertices = new double[pMaxNumVertices][2];
		this.fSize     = 0;
	}

	public int getSize()
	{
		return this.fSize;
	}

	public void clear()
	{
		this.fSize = 0;
	}

	public void addVertex(double x, double y)
	{
		if (this.fSize >= this.fVertices.length)
			throw new IndexOutOfBoundsException(String.format("Polygon can only contain %d vertices.", this.fVertices.length));

		this.setX(this.fSize, x);
		this.setY(this.fSize, y);
		this.fSize++;
	}

	public double getX(int i)
	{
		if (i > this.fSize)
			throw new IndexOutOfBoundsException(String.format("Getting vertex %d while Polygon only contains %d vertices.", i, this.fSize));

		return this.fVertices[i][0];
	}

	public double getY(int i)
	{
		if (i > this.fSize)
			throw new IndexOutOfBoundsException(String.format("Getting vertex %d while Polygon only contains %d vertices.", i, this.fSize));

		return this.fVertices[i][1];
	}

	public double[] getVertex(int i)
	{
		if (i > this.fSize)
			throw new IndexOutOfBoundsException(String.format("Getting vertex %d while Polygon only contains %d vertices.", i, this.fSize));

		return this.fVertices[i];
	}

	public double getArea()
	{
		return PolygonArea.areaOf(this.fVertices, this.fSize);
	}

	private void setX(int i, double x)
	{
		this.fVertices[i][0] = x;
	}

	private void setY(int i, double y)
	{
		this.fVertices[i][1] = y;
	}

	private double[][] getVertices()
	{
		return this.fVertices;
	}

	/*
	protected void addVertexCopy(ClipPolygon pSource, int i)
	{
		System.arraycopy(pSource.getVertices()[i], 0, this.fVertices[this.fSize], 0, 2);
		this.fSize++;
	}
	/**/

	protected void copyOfPolygon(ClipPolygon pSource)
	{
		double[][] lVertices = pSource.getVertices();
		this.fSize = pSource.getSize();
		for (int i = 0; i < this.fSize; i++)
		{
			this.fVertices[i][0] = lVertices[i][0];
			this.fVertices[i][1] = lVertices[i][1];
		}
	}
}
