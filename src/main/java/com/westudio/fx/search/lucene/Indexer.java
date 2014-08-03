package com.westudio.fx.search.lucene;

import com.westudio.fx.search.dao.DBConnection;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: zbhe
 * Date: 14-4-22
 * Time: 下午3:18
 * To change this template use File | Settings | File Templates.
 */
@Component
public class Indexer {

    @Value("#{configProperties['lucene.index.dir']}")
    private String indexDir;

    @Value("#{configProperties['lucene.query.sql.zuji']}")
    private String zujiSql;

    @Value("#{configProperties['lucene.query.sql.expedia']}")
    private String expediaSql;

    @Value("#{configProperties['lucene.query.sql.cn.ctrip']}")
    private String ctripCnSql;

    @Value("#{configProperties['lucene.query.sql.en.ctrip']}")
    private String ctripEnSql;

    @Value("#{configProperties['lucene.query.sql.city.code']}")
    private String cityCodeSql;

    private IndexWriter indexWriter = null;

    private IndexReader indexReader = null;

    private Logger logger = LoggerFactory.getLogger(Indexer.class);

    /**
     * Create a Index Writer
     * @param create whether should erase previous index
     * @return A new Index Writer instance
     * @throws IOException
     */
    private IndexWriter getIndexWriter(boolean create) throws IOException {
        if(indexWriter == null) {
            // Create a new index writer instance
            Directory directory = FSDirectory.open(new File(indexDir));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            // Set open mode to create then remove any previous index every update
            if(create) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            }
            // Optional: for better index performance
            iwc.setRAMBufferSizeMB(256.0);
            indexWriter = new IndexWriter(directory, iwc);
        }

        return indexWriter;
    }

    private IndexReader getIndexReader() throws IOException {
        if (indexReader == null) {
            Directory directory = FSDirectory.open(new File(indexDir));
            indexReader = DirectoryReader.open(directory);
        }
        return null;
    }

    /**
     * Build index for flights
     */
    public void indexFlights() {

        try {
            getIndexWriter(false);
        } catch (IOException e) {
            return;
        }

        addDocuments();
    }

    /**
     * Rebuild index for flights
     */
    public void reIndexFlights() {
        try {
            getIndexWriter(true);
        } catch (IOException e) {
            return;
        }

        addDocuments();
    }

    /**
     * Add documents to index writer
     */
    private void addDocuments() {
        Connection connection;
        try {
            connection = DBConnection.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            return;
        }

        // Build ctrip_cn index
        try(PreparedStatement ps = connection.prepareStatement(ctripCnSql)) {
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    indexWriter.addDocument(createDocument(rs, "ctrip_cn"));
                }
            }
        } catch (SQLException | IOException e) {
            logger.error("index ctrip documents error");
        }

        // Build ctrip_en index
        try(PreparedStatement ps = connection.prepareStatement(ctripEnSql)) {
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    indexWriter.addDocument(createDocument(rs, "ctrip_en"));
                }
            }
        } catch (SQLException | IOException e) {
            logger.error("index ctrip_en documents error");
        }

        // Build expedia index
        try(PreparedStatement ps = connection.prepareStatement(expediaSql)) {
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    indexWriter.addDocument(createDocument(rs, "expedia"));
                }
            }
        } catch (SQLException | IOException e) {
            logger.error("index expedia documents error");
        }

        // Build zuji index
        try(PreparedStatement ps = connection.prepareStatement(zujiSql)) {
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    indexWriter.addDocument(createDocument(rs, "zuji"));
                }
            }
        } catch (SQLException | IOException e) {
            logger.error("index zuji documents error");
        }

        // Build city code index
        try(PreparedStatement ps = connection.prepareStatement(cityCodeSql)) {
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    indexWriter.addDocument(createCityCodeDocument(rs));
                }
            }
        } catch (SQLException | IOException e) {

        }
        // Close index writer
        try {
            closeIndexWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close the database connection
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create city code document
     *
     */
    private Document createCityCodeDocument(ResultSet rs) throws SQLException {
        Document document = new Document();
        String cityName = rs.getString("cityName");
        if (cityName != null) {
            document.add(new StringField("cityName", cityName, Field.Store.YES));
        }
        String cityCode = rs.getString("cityCode");
        if (cityCode != null) {
            document.add(new StringField("cityCode", cityCode, Field.Store.YES));
        }

        return document;
    }

    /**
     * Create single document
     * @param rs
     * @param vendor
     * @return
     * @throws SQLException
     */
    private Document createDocument(ResultSet rs, String vendor) throws SQLException {
        Document document = new Document();
        document.add(new StringField("source", vendor, Field.Store.YES));

        String fromCity = rs.getString("from_city");
        if(fromCity != null) {
            document.add(new StringField("fromCity", fromCity, Field.Store.YES));
        }
        String toCity = rs.getString("to_city");
        if(toCity != null) {
            document.add(new StringField("toCity", toCity, Field.Store.YES));
        }
        String fromAirport = rs.getString("from_airport");
        if(fromAirport != null) {
            document.add(new StringField("fromAirport", fromAirport, Field.Store.YES));
        }
        String toAirport = rs.getString("to_airport");
        if(toAirport != null) {
            document.add(new StringField("toAirport", toAirport, Field.Store.YES));
        }
        String fromDate = rs.getString("from_date");
        if(fromDate != null) {
            document.add(new StringField("fromDate", fromDate, Field.Store.YES));
        }
        String toDate = rs.getString("to_date");
        if(toDate != null) {
            document.add(new StringField("toDate", toDate, Field.Store.YES));
        }
        String roundTrip = rs.getString("is_round_trip");
        if(roundTrip != null) {
            document.add(new StringField("roundTrip", roundTrip, Field.Store.YES));
        }
        String airName = rs.getString("air_name");
        if(airName != null) {
            document.add(new StringField("airName", airName, Field.Store.YES));
        }
        String flightType = rs.getString("flight_type");
        if(flightType != null) {
            document.add(new StringField("flightType", flightType, Field.Store.YES));
        }
        String flightNumber = rs.getString("flight_number");
        if(flightNumber != null) {
            document.add(new StringField("flightNumber", flightNumber, Field.Store.YES));
        }
        String price = rs.getString("price");
        if(price != null) {
            document.add(new StringField("price", price, Field.Store.YES));
            Double p = Double.valueOf(price);
            document.add(new Field("byPrice", String.valueOf(p), Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        String cabinClass = rs.getString("cabin_class");
        if(cabinClass != null) {
            document.add(new StringField("cabinClass", cabinClass, Field.Store.YES));
        }
        return document;
    }

    /**
     * Close the index writer
     * @throws IOException
     */
    private void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
    }

    public String getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(String indexDir) {
        this.indexDir = indexDir;
    }

    public String getZujiSql() {
        return zujiSql;
    }

    public void setZujiSql(String zujiSql) {
        this.zujiSql = zujiSql;
    }

    public String getExpediaSql() {
        return expediaSql;
    }

    public void setExpediaSql(String expediaSql) {
        this.expediaSql = expediaSql;
    }

    public String getCtripCnSql() {
        return ctripCnSql;
    }

    public void setCtripCnSql(String ctripCnSql) {
        this.ctripCnSql = ctripCnSql;
    }

    public String getCtripEnSql() {
        return ctripEnSql;
    }

    public void setCtripEnSql(String ctripEnSql) {
        this.ctripEnSql = ctripEnSql;
    }
}
