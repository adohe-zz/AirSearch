package com.westudio.fx.search.lucene;

import com.westudio.fx.search.exception.ApplicationException;
import com.westudio.fx.search.json.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: zbhe
 * Date: 14-4-23
 * Time: 下午2:27
 * To change this template use File | Settings | File Templates.
 */
@Component
public class SearchEngine {

    @Value("#{configProperties['lucene.index.dir']}")
    private String indexDir;

    @Value("#{configProperties['lucene.query.topnsize']}")
    private int topnSize;

    private IndexSearcher indexSearcher;

    private IndexReader indexReader;

    /**
     * Init the index searcher
     */
    private void initSearch() {
        if (indexSearcher == null) {
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(FSDirectory
                        .open(new File(indexDir)));
                indexSearcher = new IndexSearcher(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Init the index reader
     */
    private void initReader() {
        if (indexReader == null) {
            try {
                indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SearchEngine() {

    }

    public TopDocs performCitySearch(String hit) {
        initSearch();

        //String regexp = "*" +
        return null;
    }

    /**
     * Perform the specific search
     * @param fromCity The from city
     * @param toCity The end city
     * @param fromDate The from date
     * @param toDate The end date
     * @return Match documents
     */
    public TopDocs performSearch(String fromCity, String toCity, String fromDate, String toDate, String type) {

        initSearch();

        BooleanQuery bolQuery = new BooleanQuery();

        Query query = new TermQuery(new Term("fromCity", fromCity));
        bolQuery.add(query, BooleanClause.Occur.MUST);
        query = new TermQuery(new Term("toCity", toCity));
        bolQuery.add(query, BooleanClause.Occur.MUST);
        query = new TermQuery(new Term("roundTrip", type));
        bolQuery.add(query, BooleanClause.Occur.MUST);
        query = new PrefixQuery(new Term("fromDate", fromDate));
        bolQuery.add(query, BooleanClause.Occur.MUST);
        if (toDate != null && StringUtils.isNotEmpty(toDate)) {
            query = new PrefixQuery(new Term("toDate", toDate));
            bolQuery.add(query, BooleanClause.Occur.MUST);
        }

        if (indexSearcher != null) {
            try {
                TopDocs docs = indexSearcher.search(bolQuery, null, topnSize, new Sort(
                    new SortField("byPrice", SortField.Type.DOUBLE, false)));
                return docs;
            } catch (IOException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get the city list
     *
     */
    public String getCityList() {

        initReader();

        HashSet<String> set = new HashSet<>();

        if (indexReader != null) {
            try {
                Fields fields = MultiFields.getFields(indexReader);
                Terms terms = fields.terms("toCity");
                TermsEnum iterator = terms.iterator(null);
                BytesRef bytesRef = null;
                while ((bytesRef = iterator.next()) != null) {
                    String term = new String(bytesRef.bytes, bytesRef.offset, bytesRef.length);
                    set.add(term);
                }
                return JsonUtils.toJson(set);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ApplicationException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    /**
     * Get airline name list
     *
     */
    public String getAirlineList() {

        initReader();

        HashSet<String> set = new HashSet<>();

        if (indexReader != null) {
            try {
                Fields fields = MultiFields.getFields(indexReader);
                Terms terms = fields.terms("airName");
                TermsEnum iterator = terms.iterator(null);
                BytesRef bytesRef = null;
                while ((bytesRef = iterator.next()) != null) {
                    String term = new String(bytesRef.bytes, bytesRef.offset, bytesRef.length);
                    set.add(term);
                }
                return JsonUtils.toJson(set);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ApplicationException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }
}
