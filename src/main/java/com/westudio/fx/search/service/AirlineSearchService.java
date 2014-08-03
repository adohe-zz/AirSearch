package com.westudio.fx.search.service;


public interface AirlineSearchService {

	public void createIndex() throws Exception;

	public String queryAirline(String fromCity, String toCity, String fromDate, String toDate, String type) throws Exception;

    public void rebuildIndex() throws Exception;

    public String getCityList();

    public String getAirlineList();
}
