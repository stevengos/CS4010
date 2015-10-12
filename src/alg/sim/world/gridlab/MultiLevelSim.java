package alg.sim.world.gridlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import alg.gridlabd.builder.models.glm.HouseIntegrityLevel;

public class MultiLevelSim extends FileBasedSim
{
	private final List<Double> fWindProgression;

	public MultiLevelSim(String pFilename, int pHouses, HouseIntegrityLevel pLevel, double pSetpoint, long pSeed, double pPeakWindkW)
	{
		super(pFilename, pHouses, pLevel, pSetpoint, pSeed, 0);

		this.fWindProgression = new ArrayList<Double>();

		try
		{
			/**
			 *		@TODO Make this into parameters...
			 */
			LocalDateTime lStartDate = LocalDateTime.of(2009,10, 24, 5, 0);
			LocalDateTime lEndDate   = LocalDateTime.of(2009,10, 25, 5, 0);

			this.initializeWindProgression(lStartDate, lEndDate, pPeakWindkW);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void initializeWindProgression(LocalDateTime lStartDate, LocalDateTime lEndDate, double pPeakPowerkW) throws IOException
	{
		this.fWindProgression.clear();

		BufferedReader lIn = new BufferedReader(new FileReader(new File("./data/data_training.csv")));

		DateTimeFormatter lInFormatter  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// Read the header.
		String lLine = lIn.readLine();

		// Read the data.
		LocalDateTime lPrevDate  = null;
		double        lPrevPower = 0;
		while ((lLine = lIn.readLine()) != null)
		{
			String[] lSplit = lLine.split(",");

			LocalDateTime lDate  = LocalDateTime.parse(lSplit[0].trim(), lInFormatter); 
			double        lSpeed = Double.parseDouble(lSplit[1].trim());
			double        lPower = pPeakPowerkW * Math.pow(1+Math.pow(Math.E, (6-2*lSpeed/3)),-1);

			if (lDate.isAfter(lStartDate) && (lDate.isEqual(lEndDate) || lDate.isBefore(lEndDate)))
			{
				while (lPrevDate.isBefore(lDate))
				{
					this.fWindProgression.add(lPrevPower);
					lPrevDate = lPrevDate.plusSeconds(30);
				}
			}

			lPrevDate  = lDate;
			lPrevPower = lPower;
		}

		lIn.close();
	}

	@Override
	public void setSimProperties(int pInitialSecond, int pDuration, int pStepSize, double pLimitkW)
	{
		this.fSecondsBetweenSteps = pStepSize;
		this.fInitialStep         = pInitialSecond / pStepSize;
		this.fCurrentStep         = this.fInitialStep;

		int lIndex = (this.fCurrentStep * this.fSecondsBetweenSteps / this.fSecondsBetweenData);
		for (int i = 0; i < this.fWindProgression.size(); i++)
		{
			this.fTotalLoadProgression.set(lIndex, this.fTotalLoadProgression.get(lIndex) - this.fWindProgression.get(i));
			lIndex++;
		}

		super.setSimProperties(pInitialSecond, pDuration, pStepSize, pLimitkW);
	}
}
