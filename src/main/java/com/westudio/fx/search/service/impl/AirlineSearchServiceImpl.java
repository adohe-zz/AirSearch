package com.westudio.fx.search.service.impl;

import com.westudio.fx.search.json.JsonUtils;
import com.westudio.fx.search.lucene.Indexer;
import com.westudio.fx.search.lucene.SearchEngine;
import com.westudio.fx.search.model.FlightInfo;
import com.westudio.fx.search.service.AirlineSearchService;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("airlinesearch")
public class AirlineSearchServiceImpl implements AirlineSearchService {

    @Autowired
    private Indexer indexer;

    @Autowired
    private SearchEngine searchEngine;

	@Override
	public void createIndex() throws Exception {
        indexer.indexFlights();
	}

	@Override
	public String queryAirline(String fromCity, String toCity, String fromDate,
			String toDate, String type) throws Exception {

		TopDocs results = searchEngine.performSearch(fromCity, toCity, fromDate, toDate, type);

		Map<String, Object> resMap = new HashMap<String, Object>();

		List<FlightInfo> list = new ArrayList<FlightInfo>();
		ScoreDoc[] hits = results.scoreDocs;
        System.out.println(hits.length);
        for (int i = 0; i < hits.length; i++) {
            Document doc = searchEngine.getIndexSearcher().doc(hits[i].doc);
            FlightInfo flightInfo = new FlightInfo();
            flightInfo.setFromCity(fromCity);
            flightInfo.setToCity(toCity);
            flightInfo.setFromDate(doc.get("fromDate"));
            flightInfo.setToDate(doc.get("toDate"));
            flightInfo.setAirName(doc.get("airName"));
            flightInfo.setCabinClass(doc.get("cabinClass"));
            flightInfo.setFlightNumber(doc.get("flightNumber"));
            flightInfo.setFlightType(doc.get("flightType"));
            flightInfo.setFromAirPort(doc.get("fromAirport"));
            flightInfo.setToAirPort(doc.get("toAirport"));
            flightInfo.setPrice(doc.get("price"));
            flightInfo.setRoundTrip(doc.get("roundTrip"));
            flightInfo.setVendor(doc.get("source"));
            list.add(flightInfo);
        }

		resMap.put("results", list);
        System.out.println(JsonUtils.toJson(resMap));
		return JsonUtils.toJson(resMap);
	}

    @Override
    public void rebuildIndex() throws Exception {
        indexer.reIndexFlights();
    }

    @Override
    public String getCityList() {
        return searchEngine.getCityList();
    }

    @Override
    public String getAirlineList() {
        return searchEngine.getAirlineList();
    }
}
