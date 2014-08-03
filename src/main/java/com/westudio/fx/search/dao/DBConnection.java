package com.westudio.fx.search.dao;

import java.sql.*;

public class DBConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:55944/crawlerresultmdb";
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String USER_NAME = "uapp_crawler_r";
    private static final String USER_PASSWORD = "WXzHvxCjYClbP1wLjUMx";

    /**
     * Get Database Connection
     * @return {@code} connection
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        return DriverManager.getConnection(URL, USER_NAME, USER_PASSWORD);
    }
}
