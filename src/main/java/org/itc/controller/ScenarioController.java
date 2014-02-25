package org.itc.controller;

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
    	this.scenarioTrunk = new Trunk(jsonInput);
        
        return "redirect:/pages/scenarioresults.html";
    }
    
    @RequestMapping(value = "dbbenchmark/runsimulation", method = RequestMethod.GET)
    public synchronized @ResponseBody ScenarioResult runSimulation(HttpServletRequest request, HttpServletResponse response)
    {
        this.scenarioTrunk.getTraceObserver().clearsysLog();
        this.scenarioTrunk.getStaticObserver().clearsysLog();

        ScenarioResult scenarioResult = scenarioTrunk.runScenarios();
        
        return scenarioResult;
    }
    
}
