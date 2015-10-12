package alg.util;

import java.util.List;

import alg.util.clipping.ClipVertex;

public class PolygonArea
{
	/**
	 *		Computes the area of an irregular polygon. Input is an array of x,y coordinates.
	 *
	 *			*  .
	 *			|	  .
	 *			*---|---*
	 *
	 *		If:						Input:								Output:
	 *			Point 1 is (0,0)	 new double[][] { { 0, 0 },			((1+0)/2) * (0-0) = 0
	 *			Point 2 is (2,0)					  { 2, 0 },			((0+0)/2) * (0-2) = 0
	 *			Point 3 is (0,1)					  { 0, 1 } }		((0+1)/2) * (2-0) = 1
	 *																					  ----- +
	 *																						1
	 *
	 * @param lPolygon
	 * @return
	 */
	public static double areaOf(double[][] lPolygon)
	{
		double   lArea = 0;
		if (lPolygon.length > 0)
		{
			double[] lFrom = lPolygon[lPolygon.length - 1];
	
			for (int i = 0; i < lPolygon.length; i++)
			{
				double[] lTo = lPolygon[i];
	
				lArea = lArea + (lFrom[1] + lTo[1]) * (lFrom[0] - lTo[0]);
				lFrom = lTo;
			}
		}

		return lArea / 2;
	}

	public static double areaOf(double[][] lPolygon, int lSize)
	{
		double   lArea = 0;
		if (lSize > 0)
		{
			double[] lFrom = lPolygon[lSize - 1];
	
			for (int i = 0; i < lSize; i++)
			{
				double[] lTo = lPolygon[i];
	
				lArea = lArea + (lFrom[1] + lTo[1]) * (lFrom[0] - lTo[0]);
				lFrom = lTo;
			}
		}

		return lArea / 2;
	}

	public static double areaOf(List<ClipVertex> pPolygon)
	{
		ClipVertex lFrom = null;
		ClipVertex lTo   = null;
		double     lArea = 0;

		if (pPolygon.size() > 0)
		{
			lFrom = pPolygon.get(pPolygon.size() - 1);
	
			for (int i = 0; i < pPolygon.size(); i++)
			{
				lTo = pPolygon.get(i);
	
				lArea = lArea + (lFrom.getY() + lTo.getY()) * (lFrom.getX() - lTo.getX());
				lFrom = lTo;
			}
		}

		return lArea / 2;
	}

	public static void main(String[] args)
	{
		double lArea = PolygonArea.areaOf(new double[][] {	{ 0, 0},
															{ 2, 0},
															{ 0, 1}	 });

		System.out.println(lArea);
	}
}
