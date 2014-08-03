package com.westudio.fx.search.search;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zbhe
 * Date: 14-4-11
 * Time: 下午2:41
 * To change this template use File | Settings | File Templates.
 */
public class Search {

    private static IndexSearcher indexSearcher = null;

    private static void initSearcher(String indexDir) throws IOException {
        if(indexSearcher == null) {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
            indexSearcher = new IndexSearcher(reader);
        }
    }

    public static String flights(String queryStr) {
        return null;
    }

    public static String filters(String queryStr) {
        return null;
    }
}
