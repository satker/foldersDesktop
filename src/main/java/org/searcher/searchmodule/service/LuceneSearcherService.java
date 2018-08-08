package org.searcher.searchmodule.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.searcher.fxmlmanager.MainWindowController;
import org.searcher.openfile.GetTextFromFile;
import org.searcher.searchmodule.service.lucene.FileIndexer;
import org.searcher.searchmodule.service.lucene.FileToDocument;
import org.searcher.searchmodule.service.lucene.Searcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class LuceneSearcherService {

    static int[] isaTextInFile(File file, String whatSearch)
            throws Exception {
        FileIndexer fileIndexer = new FileIndexer();

        List<Document> documents = new ArrayList<>();
        List<String> lines = GetTextFromFile.getTextFromFile(file.getAbsolutePath());

        int count = 0;
        for (String line : lines) {
            documents.add(FileToDocument.createWith(String.valueOf(count), line));
            count++;
        }

        fileIndexer.index(false, documents);

        Searcher.setReader(DirectoryReader.open(FileIndexer.directory));
        int[] result = Searcher.searchInBody(whatSearch, 10);
        if (result.length != 0) {
            MainWindowController.fileAndLines.put(file.getAbsolutePath(), lines);
        }
        return result;
    }

}
