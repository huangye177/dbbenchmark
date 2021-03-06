package org.itc.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.mongodb.BasicDBObject;

/**
 * Interoperator for type "GeomoniBasic"
 */
public class InteroperatorGeomoniBasic extends IDataInteroperator
{

    private Random generator = new Random();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyD");
    private SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    final private int amount_of_day_of_year_2014 = 365;
    final protected int amount_of_dataseries = 1000;
    final private int amount_of_project = 1000;
    final private int value_of_measurement = 1000;

    @Override
    public Object interoperate(Object originalContent, DBType dbtype,
            OperationType operType)
    {
        if (DBType.MYSQL.equals(dbtype))
        {
            if (OperationType.INSERT.equals(operType))
            {
                return this.interoperateMySQLInsert((String) originalContent);
            }
            else if (OperationType.SELECT.equals(operType))
            {
                return this.interoperateMySQLSelect((String) originalContent);
            }
            else
            {
                return originalContent;
            }

        }
        else if (DBType.MONGODB.equals(dbtype))
        {
            if (OperationType.INSERT.equals(operType))
            {
                return this.interoperateMongoDBInsert(originalContent);
            }
            else if (OperationType.SELECT.equals(operType))
            {
                return this.interoperateMongoDBSelect(originalContent);
            }
            else
            {
                return originalContent;
            }
        }
        else
        {
            return originalContent;
        }
    }

    private Object interoperateMySQLSelect(String originalContent)
    {
        String var = originalContent.replaceFirst("\\?", String.valueOf(this.getDataSeriesId()));

        return var;
    }

    private String interoperateMySQLInsert(String statement)
    {
        String var = "";

        var = statement
                .replaceFirst("\\?", String.valueOf(this.getDataSeriesId()))
                .replaceFirst("\\?", "\'" + this.datetimeFormat.format(this.getMeasDate()) + "\'")
                .replaceFirst("\\?", "\'" + this.datetimeFormat.format(this.getMeasDate()) + "\'")
                .replaceFirst("\\?", String.valueOf(this.getProjectId()))
                .replaceFirst("\\?", String.valueOf(this.getMeasValue()));

        return var;
    }

    @SuppressWarnings("unchecked")
    private Object interoperateMongoDBInsert(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;
        List<String> inputArray = (ArrayList<String>) contentMap.get("document");

        BasicDBObject insertObject = new BasicDBObject();

        insertObject.append(inputArray.get(0), this.getDataSeriesId());
        insertObject.append(inputArray.get(1), this.datetimeFormat.format(this.getMeasDate()));
        insertObject.append(inputArray.get(2), this.datetimeFormat.format(this.getMeasDate()));
        insertObject.append(inputArray.get(3), this.getProjectId());
        insertObject.append(inputArray.get(4), this.getMeasValue());
        insertObject.append(inputArray.get(5), false);
        insertObject.append(inputArray.get(6), 1.0);

        // create the to-return result
        Map<String, Object> returnContentMap = new HashMap<String, Object>(contentMap);
        returnContentMap.put("document", insertObject);

        return returnContentMap;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object interoperateMongoDBSelect(Object content)
    {
        Map<String, Object> contentMap = (HashMap<String, Object>) content;
        // List<String> inputArray = (ArrayList<String>)
        // contentMap.get("document");

        List<ArrayList> documents = (ArrayList<ArrayList>) contentMap.get("document");

        // find query condition attributes & to-return attributes
        List<String> conditions = documents.get(0);
        List<String> returns = documents.get(1);

        BasicDBObject conditionObject = new BasicDBObject();
        conditionObject.append(conditions.get(0), this.getDataSeriesId());

        BasicDBObject returnsObject = new BasicDBObject();
        returnsObject.append("_id", 0);
        for (String doc : returns)
        {
            returnsObject.append(doc, 1);
        }

        // create the to-return result
        Map<String, Object> returnContentMap = new HashMap<String, Object>(contentMap);
        returnContentMap.put("conditions", conditionObject);
        returnContentMap.put("returns", returnsObject);

        return returnContentMap;
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
            Date parseDate = this.dateFormat.parse(julianDate);
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
