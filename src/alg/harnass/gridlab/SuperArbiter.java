package alg.harnass.gridlab;


/**
 * Arbiter which devides power among multiple neighboorhoods
 * @author Christian
 *
 */
public class SuperArbiter {
	public static int fDesiredArbiterType = 1;
	
	public void setDesiredArbiterType(int pDesiredArbiterType){
		fDesiredArbiterType = pDesiredArbiterType;
	}
	
	public static int[] arbitrage(int availablePower, double[] pGroupPenalty, int[] pGroupDesiredPower)
	{
		int[]	returnedPower = new int[pGroupDesiredPower.length];
		int		remainingPower = availablePower;
		
		int lTotalDesiredPower = 0;
		//Check if there is a power constraint
		for(int i = 0; i < pGroupDesiredPower.length; i++)
			lTotalDesiredPower += pGroupDesiredPower[i];
		
		if(lTotalDesiredPower <= availablePower) {
			return pGroupDesiredPower;
		}
		
		switch(fDesiredArbiterType){
			case(1):{ //give everyone its power until no power is left
				for(int i = 0; i < pGroupDesiredPower.length; i++){
					if(remainingPower > 0){
						if(remainingPower - pGroupDesiredPower[i] < 0){
							returnedPower[i] = remainingPower;
						}
						else{
							returnedPower[i] = pGroupDesiredPower[i];
						}
					}
					else{
						returnedPower[i] = 0;
					}
					
					remainingPower = remainingPower - pGroupDesiredPower[i];
				}
				break;
			}
			default:{
				for(int i = 0; i < pGroupDesiredPower.length; i++){
					returnedPower[i] = pGroupDesiredPower[i];
				}
				break;
			}
		}
		
		//Otherwise, perform arbitration
		return returnedPower;
	}
}
