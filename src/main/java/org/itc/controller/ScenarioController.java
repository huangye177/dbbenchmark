package org.itc.controller;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.itc.Trunk;
import org.itc.scenario.ScenarioResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ScenarioController
{
    private Trunk scenarioTrunk = null;

    private @Autowired
    ServletContext servletContext;

    @RequestMapping("/")
    public String loadDefaultPage(Model m)
    {
        m.addAttribute("name", "DBBenchmark");
        return "home";
    }

    @RequestMapping("home")
    public String loadHomePage(Model m)
    {
        m.addAttribute("name", "DBBenchmark");
        return "home";
    }

    @RequestMapping(value = "dbbenchmark", method = RequestMethod.GET)
    public String scenarioBuilder()
    {
        return "redirect:/pages/dbbenchmark_scenariobuilder.html";
    }

    @RequestMapping(value = "dbbenchmark/defaultscenario", method = RequestMethod.GET)
    public String getDefaultScenario()
    {
        return "redirect:/pages/scenarios/scenariosetting_basic.json";
    }

    @RequestMapping(value = "dbbenchmark/startsimulation", method = RequestMethod.POST)
    public String startSimulation(HttpServletRequest request, HttpServletResponse response)
    {
        String jsonInput = request.getParameter("scenariowebinput");
        this.scenarioTrunk = null;
        this.scenarioTrunk = new Trunk(jsonInput);

        return "redirect:/pages/scenarioresults.html";
    }

    @RequestMapping(value = "dbbenchmark/runsimulation", method = RequestMethod.GET)
    public @ResponseBody
    ScenarioResult runSimulation(HttpServletRequest request, HttpServletResponse response)
    {
        this.scenarioTrunk.getTraceObserver().clearsysLog();
        this.scenarioTrunk.getStaticObserver().clearsysLog();

        this.scenarioTrunk.runScenarios();

        return this.scenarioTrunk.getScenarioResult();
    }

    @RequestMapping(value = "dbbenchmark/getsimulation", method = RequestMethod.GET)
    public synchronized @ResponseBody
    ScenarioResult getSimulationResult(HttpServletRequest request, HttpServletResponse response)
    {
        if (this.scenarioTrunk == null)
        {
            return null;
        }
        else
        {
            try
            {
                while (!this.scenarioTrunk.getScenarioResult().isStarted())
                {
                    System.out.println("getsimulation: Simulation not started yet");

                    Thread.sleep(100);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println(this.scenarioTrunk.getScenarioResult().getScenarioResultName());
            return this.scenarioTrunk.getScenarioResult();
        }
    }

    @RequestMapping(value = "dbbenchmark/countsimulation", method = RequestMethod.GET)
    public synchronized @ResponseBody
    List<String> counterSimulationResult(HttpServletRequest request, HttpServletResponse response)
    {
        if (this.scenarioTrunk != null)
        {
            try
            {
                while (!this.scenarioTrunk.getScenarioResult().isStarted())
                {
                    System.out.println("getsimulation: Simulation not started yet");

                    Thread.sleep(100);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            this.scenarioTrunk.analyzeAllScenarios();
            return this.scenarioTrunk.getAllScenarios();
        }
        else
        {
            return null;
        }

    }

}
