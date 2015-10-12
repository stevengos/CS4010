package alg.util.clipping.inplace;


public class InplaceSutherlandHodgman
{
	private final ClipPolygon fScratch;

	public InplaceSutherlandHodgman(int pMaxEdges)
	{
		/*
		 *		Since we clip only by rectangles, every edge can cross at most two sides of the
		 *	clipping rectangle, which thus creates at most two new vertices in the destination or
		 *	scratch Polygon:
		 *
		 *		       *
		 *		      /
		 *		   *-X----
		 *		   |/
		 *		   X
		 *		  /|
		 *		 / |
		 *		*  |
		 */
		this.fScratch = new ClipPolygon(pMaxEdges*2);
	}

	public void clipPolygon(ClipPolygon pSource, ClipPolygon pDestination,
							double pMax, double pLeft, double pMin, double pRight)
	{
		pDestination.copyOfPolygon(pSource);

		/*
		 *		This is an unroll of the more general loop-construct, because each edge
		 *	has different `isInside()' conditions. Testing this was the dominant cost
		 *	of the algorithm, and unrolling it like this gave 50% speed-up.
		 *
		 *	Since clipping itself is the dominant cost in computing the transition function,
		 *	this choice is justified as giving a 50% speed-up to an expensive operation.
		 *
		 */
		InplaceSutherlandHodgman.clipMaximum(pDestination,  this.fScratch, pMax);
		InplaceSutherlandHodgman.clipLeft(   this.fScratch, pDestination,  pLeft);
		InplaceSutherlandHodgman.clipMinimum(pDestination,  this.fScratch, pMin);
		InplaceSutherlandHodgman.clipRight(  this.fScratch, pDestination,  pRight);		// By the end, pDestination contains the output!
	}

	private static void clipMaximum(ClipPolygon pInput, ClipPolygon pOutput, double pMax)
	{
		int    size;
		double x, y;

		size = pInput.getSize();
		pOutput.clear();

		if (size > 0) {
			int start = size-1;
			for (int i = 0; i < size; i++) {
				int end = i;

				double xstart = pInput.getX(start);
				double ystart = pInput.getY(start);
				double xend   = pInput.getX(end);
				double yend   = pInput.getY(end);

				if (yend < pMax) {
					if (ystart >= pMax) {
						x = (pMax - ystart) * (xend - xstart) / (yend - ystart) + xstart;
						y =  pMax;
						pOutput.addVertex(x, y);
					}

					pOutput.addVertex(xend, yend);
				}
				else if (ystart < pMax) {
					x = (pMax - ystart) * (xend - xstart) / (yend - ystart) + xstart;
					y =  pMax;
					pOutput.addVertex(x, y);
				}

				start = end;
			}
		}
	}

	private static void clipMinimum(ClipPolygon pInput, ClipPolygon pOutput, double pMin)
	{
		int    size;
		double x, y;

		size = pInput.getSize();
		pOutput.clear();

		if (size > 0) {
			int start = size-1;
			for (int i = 0; i < size; i++) {
				int end = i;

				double xstart = pInput.getX(start);
				double ystart = pInput.getY(start);
				double xend   = pInput.getX(end);
				double yend   = pInput.getY(end);

				if (yend >= pMin) {
					if (ystart < pMin) {
						x = (pMin - ystart) * (xend - xstart) / (yend - ystart) + xstart;
						y = pMin;
						pOutput.addVertex(x, y);
					}

					pOutput.addVertex(xend, yend);
				}
				else if (ystart >= pMin) {
					x = (pMin - ystart) * (xend - xstart) / (yend - ystart) + xstart;
					y = pMin;
					pOutput.addVertex(x, y);
				}

				start = end;
			}
		}
	}

	private static void clipRight(ClipPolygon pInput, ClipPolygon pOutput, double pRight)
	{
		int    size;
		double x, y;

		size = pInput.getSize();
		pOutput.clear();

		if (size > 0) {
			int start = size-1;
			for (int i = 0; i < size; i++) {
				int end = i;

				double xstart = pInput.getX(start);
				double ystart = pInput.getY(start);
				double xend   = pInput.getX(end);
				double yend   = pInput.getY(end);

				if (xend < pRight) {
					if (xstart >= pRight) {
						x = pRight;
						y = (pRight - xstart) * (yend - ystart) / (xend - xstart) + ystart;
						pOutput.addVertex(x, y);
					}

					pOutput.addVertex(xend, yend);
				}
				else if (xstart < pRight) {
					x = pRight;
					y = (pRight - xstart) * (yend - ystart) / (xend - xstart) + ystart;
					pOutput.addVertex(x, y);
				}

				start = end;
			}
		}
	}

	private static void clipLeft(ClipPolygon pInput, ClipPolygon pOutput, double pLeft)
	{
		int    size;
		double x, y;

		size = pInput.getSize();
		pOutput.clear();

		if (size > 0) {
			int start = size-1;
			for (int i = 0; i < size; i++) {
				int end = i;

				double xstart = pInput.getX(start);
				double ystart = pInput.getY(start);
				double xend   = pInput.getX(end);
				double yend   = pInput.getY(end);

				if (xend >= pLeft) {
					if (xstart < pLeft) {
						x = pLeft;
						y = (pLeft - xstart) * (yend - ystart) / (xend - xstart) + ystart;
						pOutput.addVertex(x, y);
					}

					pOutput.addVertex(xend, yend);
				}
				else if (xstart >= pLeft) {
					x = pLeft;
					y = (pLeft - xstart) * (yend - ystart) / (xend - xstart) + ystart;
					pOutput.addVertex(x, y);
				}

				start = end;
			}
		}
	}
}
