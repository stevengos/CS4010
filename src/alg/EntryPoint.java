package alg;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import alg.gridlabd.builder.models.glm.ClimateRegion;
import alg.gridlabd.builder.models.glm.HouseIntegrityLevel;
import alg.harnass.gridlab.GLEvaluator;
import alg.harnass.gridlab.GLMultiLevelPlanningHarnass;
import alg.harnass.gridlab.GLPlanningHarnass;
import alg.harnass.gridlab.GLThermostatPolicy;
import alg.harnass.gridlab.IJointPolicy;
import alg.harnass.gridlab.MultiLevelEvaluator;
import alg.harnass.gridlab.PlanQuality;
import alg.sim.world.gridlab.FileBasedSim;
import alg.sim.world.gridlab.MultiLevelSim;

public class EntryPoint
{
	public static void runExperiment(LocalDate pDate, String pDirectory) throws IOException
	{
		int               lAgents   = 20;
		int               lSetpoint = 70;
		PlanQuality       lQuality  = PlanQuality.Low;
		DateTimeFormatter lFormat   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		ClimateRegion     pRegion   = ClimateRegion.NETHERLANDS;
		HouseIntegrityLevel pInsulation = HouseIntegrityLevel.GOOD;

		String lRunDirectory = pDirectory + "runs/";
		File lRoot = new File(lRunDirectory);
		if (!lRoot.exists()) lRoot.mkdirs();

		// Compute how many kW of solar panels are required.
		double lSolarRequired = 8.5;

		// Prepare the simulator.
		FileBasedSim lSimulator = new FileBasedSim("./instances/gridlab/"+pRegion+".txt", lAgents, pInsulation, lSetpoint, 3985l, lSolarRequired);

		// Set the limit to start from day 0.
		int     lStartSecond = pDate.getDayOfYear() * (24*60*60) + 12*60*60;			// Start midday.
		int     lDuration    = 24*60*60;
		int     lStepsize    = lQuality.getStepSize();
		double  lLimitkW     = 3.25*lAgents;

		PrintStream lPenalty   = new PrintStream(lRunDirectory+"p_"+pInsulation+"_"+pDate.getDayOfYear()+".txt");
		PrintStream lTemprture = new PrintStream(lRunDirectory+"t_"+pInsulation+"_"+pDate.getDayOfYear()+".txt");

		lSimulator.setSimProperties(lStartSecond, lDuration, lStepsize, lLimitkW);
		GLEvaluator lEvaluator = new GLEvaluator(lSimulator, lSetpoint, lLimitkW, LocalDateTime.of(pDate, LocalTime.of(12, 0)), lDuration, lStepsize);

		// Prepare the transition tables of the planner.
		GLPlanningHarnass lHarnass = null;
		lHarnass     = new GLPlanningHarnass(lSimulator, lQuality, lSetpoint);

		// The thermostats' policy.
		lSimulator.reset();
		IJointPolicy lPolicy = new GLThermostatPolicy(lSetpoint, lLimitkW);
		double lThermEval = lEvaluator.evaluate(lPolicy, lPenalty, lTemprture, "reactive");

		// The planned policy.
		lSimulator.reset();
		lHarnass.planAll();
		double lPlannedEval = lEvaluator.evaluate(lHarnass, lPenalty, lTemprture, "planned");

		// The best-response policy.
		lSimulator.reset();
		lHarnass.planBestresponse(10, 25);
		double lBestRespEval = lEvaluator.evaluate(lHarnass, lPenalty, lTemprture, "bestresponse");

		lPenalty.close();
		lTemprture.close();

		System.out.println(pRegion + "," + pDate.format(lFormat) + "," + (int)Math.ceil(lLimitkW) + ",reactive," + lThermEval);
		System.out.println(pRegion + "," + pDate.format(lFormat) + "," + (int)Math.ceil(lLimitkW) + ",planned," + lPlannedEval);
		System.out.println(pRegion + "," + pDate.format(lFormat) + "," + (int)Math.ceil(lLimitkW) + ",bestresp," + lBestRespEval);
	}

	public static void runMultiLevelExperiment(LocalDate pDate, String pDirectory) throws IOException
	{
		int               lAgents   = 20;	//Number of houses
		int               lSetpoint = 70;	//??
		PlanQuality       lQuality  = PlanQuality.Low;	//Precision of calculation
		DateTimeFormatter lFormat   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		ClimateRegion     pRegion   = ClimateRegion.NETHERLANDS;		//Model parameter
		HouseIntegrityLevel pInsulation = HouseIntegrityLevel.GOOD;		//House parameter

		String lRunDirectory = pDirectory + "runs/";
		File lRoot = new File(lRunDirectory);
		if (!lRoot.exists()) lRoot.mkdirs();

		// Compute how many kW of wind farm is required.
		double lWindRequired = 32.5;

		// Prepare the simulator.
		MultiLevelSim lSimulator = new MultiLevelSim("./instances/gridlab/"+pRegion+".txt", lAgents, pInsulation, lSetpoint, 3985l, lWindRequired);

		// Set the limit to start from day 0.
		int     	lStartSecond = pDate.getDayOfYear() * (24*60*60) + 12*60*60;			// Start midday.
		int     	lDuration    = 24*60*60;
		int     	lStepsize    = lQuality.getStepSize();
		double  	lLimitkW     = 2.75*lAgents;
		int[]		lHouseGroups = new int[] {10, 10};
		double[]	lGroupMaxLoad = new double[] {35, 35};

		PrintStream lPenalty   = new PrintStream(lRunDirectory+"p_"+pInsulation+"_"+pDate.getDayOfYear()+".txt");
		PrintStream lTemprture = new PrintStream(lRunDirectory+"t_"+pInsulation+"_"+pDate.getDayOfYear()+".txt");

		lSimulator.setSimProperties(lStartSecond, lDuration, lStepsize, lLimitkW);
		MultiLevelEvaluator lEvaluator = new MultiLevelEvaluator(lSimulator, lSetpoint, lLimitkW, LocalDateTime.of(pDate, LocalTime.of(12, 0)), lDuration, lStepsize);
		lEvaluator.setLocalLimits(lHouseGroups, lGroupMaxLoad);

		// Prepare the transition tables of the planner.
		GLPlanningHarnass lHarnass = null;
		lHarnass     = new GLMultiLevelPlanningHarnass(lHouseGroups, lSimulator, lQuality, lSetpoint);

		// The thermostats' policy.
		lSimulator.reset();
		IJointPolicy lPolicy = new GLThermostatPolicy(lSetpoint, lLimitkW);
		double lThermEval = lEvaluator.evaluate(lPolicy, lPenalty, lTemprture, "reactive");

		// The planned policy.
		lSimulator.reset();
		lHarnass.planAll();
		double lPlannedEval = lEvaluator.evaluate(lHarnass, lPenalty, lTemprture, "planned");

		// The best-response policy.
		lSimulator.reset();
		lHarnass.planBestresponse(10, 25);	//For 10 iterations and 25 simulations?
		double lBestRespEval = lEvaluator.evaluate(lHarnass, lPenalty, lTemprture, "bestresponse");

		lPenalty.close();
		lTemprture.close();

		System.out.println(pRegion + "," + pDate.format(lFormat) + "," + (int)Math.ceil(lLimitkW) + ",reactive," + lThermEval);
		System.out.println(pRegion + "," + pDate.format(lFormat) + "," + (int)Math.ceil(lLimitkW) + ",planned," + lPlannedEval);
		System.out.println(pRegion + "," + pDate.format(lFormat) + "," + (int)Math.ceil(lLimitkW) + ",bestresp," + lBestRespEval);
	}

	/**
	 *	This is an example of how I am currently running the planner for an instance in my research.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			//runExperiment(LocalDate.of(2013, 1, 16), "./output/");
			runMultiLevelExperiment(LocalDate.of(2013, 1, 16), "./output/");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}

/**
 *
 *		Here is an R script to view the produced data (you'll need to modify the absolute paths in the script to run it on your machine).
 * 

require(reshape)
require(ggplot2)
require(grid)
require(scales)
require(extrafont)
require(lubridate)
Sys.setenv(R_GSCMD = "C:/Program Files/gs/gs9.15/bin/gswin64c.exe")
Sys.setenv(LANGUAGE="en")
Sys.setlocale("LC_TIME", "English")

housetemp = read.table("C:/workspace/CS4010_F/output/runs/t_GOOD_16.txt", header=FALSE, sep=",")
housetemp$V1 <- housetemp$V1 / (60*60)
housetemp$V2 <- factor(housetemp$V2, levels=c("reactive","planned","bestresponse"), labels=c("Reactive","Planned","Best-Resp."))
housetemp$V3 <- factor(housetemp$V3, levels=c("air","mass","out"), labels=c("Indoor","Mass","Outdoor"))
housetemp$V2[which(housetemp$V4=="out" | housetemp$V4=="set")] = NA
housetemp$V4[which(housetemp$V4=="mass")] = "air"
housetemp$V4[which(housetemp$V4=="out")] = "air"
p <- ggplot(housetemp, aes(x=V1, y=V5, color=V2, linetype=V4)) + facet_grid(V3 ~ ., scales="free_y") +
	geom_line(size=0.2) +
	scale_x_continuous(name="Time (h)", limits=c(0,24), breaks=seq(0,24,4)) +  
	scale_y_continuous(name="Temperature (C)", breaks=seq(-15,30,2.5)) +  
	guides(color = guide_legend(title="Policy", keywidth = 0.25, keyheight = 0.5), linetype = F) +
	theme_bw() +
		theme(panel.border = element_rect(fill = NA, size=0.25,colour = "grey50")) +
		theme(text = element_text(family="Tahoma", size=6)) +
		theme(panel.grid.minor = element_blank()) +
		theme(panel.grid.major = element_line(colour = "#BBBBBB", size = 0.075)) +
		theme(axis.ticks = element_line(size = 0.25)) +
		theme(axis.title.x=element_text(vjust=unit(0.125, "cm"))) +
		theme(axis.title.y=element_text(vjust=unit(0.225, "cm"))) +
		theme(axis.line = element_blank()) +
		theme(plot.title=element_text(vjust=unit(1.0, "cm"))) +
		theme(strip.background = element_blank()) +
		theme(panel.margin = unit(0.025, "cm")) +
		theme(plot.margin = unit(c( 0 , 0 , 0 , 0 ), "cm")) +
		theme(legend.key = element_blank()) +
		theme(legend.margin = unit(-0.8, "cm"))
p

house = read.table("C:/workspace/CS4010_F/output/runs/p_GOOD_16.txt", header=FALSE, sep=",")
q <- ggplot(house, aes(x=V1, y=V4, color=V3)) + facet_grid(V2 ~ .) + geom_line(size=0.25) +
	scale_x_continuous(name="Time") +  
	scale_y_continuous(name="Instant Penalty") + 
	guides(color = guide_legend(keywidth = 0.5, keyheight = 0.5, ncol=2, title="House")) +
	theme_bw() +
		theme(text = element_text(size=9)) +
		theme(axis.title.x=element_text(vjust=unit(0.125, "cm"))) +
		theme(axis.title.y=element_text(vjust=unit(0.225, "cm"))) +
		theme(plot.title=element_text(vjust=unit(1.0, "cm"))) +
		theme(strip.background = element_blank()) +
		theme(panel.margin = unit(0.075, "cm")) +
		theme(plot.margin = unit(c( 0.2 , 0 , 0 , 0 ), "cm")) +
		theme(legend.key = element_blank()) +
		theme(legend.margin = unit(-0.4, "cm"))
q
*/