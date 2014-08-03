package com.westudio.fx.search.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.westudio.fx.search.service.AirlineSearchService;

@Controller
public class FlightController {

	@Autowired
	private AirlineSearchService airlineSearchService;

	@RequestMapping(value = "build.do")
	@ResponseBody
	public String buildIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		airlineSearchService.createIndex();
		return "ok";
	}

	@RequestMapping(value = "query.do", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String queryAirline(@RequestParam String fromCity,
			@RequestParam String toCity, @RequestParam String fromDate, @RequestParam String toDate, @RequestParam String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return airlineSearchService.queryAirline(fromCity, toCity, fromDate, toDate, type);
	}

    @RequestMapping(value = "list.do")
    @ResponseBody
    public String getCityList() {
        return airlineSearchService.getCityList();
    }

    @RequestMapping(value = "airlines.do")
    @ResponseBody
    public String getAirlineList() {
        return null;
    }
}
