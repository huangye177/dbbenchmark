package org.itc.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class InteroperatorGeomoniBasic extends IDataInteroperator {

	private Random generator = new Random();
	private SimpleDateFormat format = new SimpleDateFormat("yyyyD");
	
	final private int amount_of_day_of_year_2014 = 365;
	final protected int amount_of_dataseries = 1000;
    final private int amount_of_project = 1000;
    final private int value_of_measurement = 1000;
    
	@Override
	public String interoperate(String originalSQL, DBType dbtype,
			OperationType operType) {
		
		return null;
	}

	
	protected int getProjectId()
    {
        return this.generator.nextInt(this.amount_of_project);
    }

    protected int getDataSeriesId()
    {
        return this.generator.nextInt(this.amount_of_dataseries);
    }

    protected Date getMeasDate()
    {
        String julianDate = "2014" + this.generator.nextInt(this.amount_of_day_of_year_2014);

        try
        {
            Date parseDate = this.format.parse(julianDate);
            return parseDate;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    protected double getMeasValue()
    {
        return this.generator.nextDouble() * value_of_measurement;
    }
}
