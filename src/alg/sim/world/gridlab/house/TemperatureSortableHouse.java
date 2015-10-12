package alg.sim.world.gridlab.house;

public class TemperatureSortableHouse implements Comparable<TemperatureSortableHouse>
{
	private final GridLabHouseModel fModel;

	public TemperatureSortableHouse(GridLabHouseModel pModel)
	{
		this.fModel = pModel;
	}

	public GridLabHouseModel getHouseModel()
	{
		return this.fModel;
	}

	@Override
	public int compareTo(TemperatureSortableHouse pOther)
	{
		GridLabHouseModel lMine  = this.getHouseModel();
		GridLabHouseModel lTheir = pOther.getHouseModel();

		if (lMine.getAirTemp() < lTheir.getAirTemp())
		{
			return -1;
		}
		else if  (lMine.getAirTemp() > lTheir.getAirTemp())
		{
			return  1;
		}

		return 0;
	}
}
