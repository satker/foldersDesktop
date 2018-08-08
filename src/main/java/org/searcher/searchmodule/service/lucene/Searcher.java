package org.searcher.searchmodule.service.lucene;

import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Searcher {

    /**
     * Search in body using QueryParser
     *
     * @param toSearch string to search
     * @param limit how many results to return
     */
    private static final QueryParser queryParser = new QueryParser("body", new RussianAnalyzer());

    private static IndexReader reader;

    public static void setReader(IndexReader reader) {
        Searcher.reader = reader;
    }

    public static int[] searchInBody(final String toSearch, final int limit)
            throws IOException, ParseException {
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        final Query query = queryParser.parse(toSearch);
        final TopDocs search = indexSearcher.search(query, limit);
        final ScoreDoc[] hits = search.scoreDocs;
        return getMassiveIntFromScoreDoc(hits);
    }

    private static int[] getMassiveIntFromScoreDoc(ScoreDoc[] hits) {
        int[] indexStrWhichAreSearch = new int[0];
        if (hits.length != 0) {
            indexStrWhichAreSearch = Arrays.stream(hits)
                    .map(element -> {
                        try {
                            return reader.document(element.doc)
                                    .get("title");
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
        return indexStrWhichAreSearch;
    }
}
