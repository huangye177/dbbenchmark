package org.itc.controller;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ScenarioController
{

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
    public String startSimulation()
    {
        return "redirect:/pages/scenarios/scenariosetting_basic.json";
    }
}
