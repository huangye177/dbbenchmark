package org.itc.scenario;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONSettingReaderWriter
{
    private static final Logger LOGGER = Logger.getLogger("JSONSettingReaderWriter");

    public JSONSettingReaderWriter()
    {

    }

    /**
     * Load JSON based scenario build input
     * 
     * @param inputContent
     * @return
     */
    public static Scenarios launchScenarioSetting(String inputContent)
    {
        Scenarios scenarios = null;
        File settingFile = null;

        ObjectMapper mapper = new ObjectMapper();

        try
        {
            if (inputContent == null || inputContent.trim().isEmpty())
            {
                // read scenario json from ./scenariosetting.json
                settingFile = new File("./scenariosetting.json");
                scenarios = mapper.readValue(settingFile, Scenarios.class);

            }
            else
            {

                // read scenario json from web input
                scenarios = mapper.readValue(inputContent, Scenarios.class);

            }

            LOGGER.info("Loading scenario settings...");
            for (ScenarioUnit su : scenarios.getScenarioUnits())
            {
                LOGGER.info(su.toString());
            }
            LOGGER.info("Scenario settings loaded.");

        }
        catch (JsonGenerationException e)
        {
            e.printStackTrace();
            System.exit(-1);

        }
        catch (JsonMappingException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return scenarios;
    }
}
