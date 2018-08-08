package org.searcher.searchmodule.controller;

import org.searcher.searchmodule.service.SearchFilesService;

import java.util.Map;
import java.util.regex.Pattern;

public class SearchFilesController implements Runnable {

    private SearchFilesService searchFilesService;

    public void setVariablesToSearching (String directory, String findText, String findType) {
        searchFilesService = new SearchFilesService();
        searchFilesService.setFields(directory, findText, findType);
    }

    public Map<String, int[]> getSearchedFiles() {
        return searchFilesService.getSearchedFiles();
    }

    @Override
    public void run() {
        searchFilesService.startSearching();
    }

    public boolean getSearchIsAlive() {
        return searchFilesService.isSearchIsAlive();
    }

    public void setSearchIsAlive(boolean isAlive) {
        searchFilesService.setSearchIsAlive(isAlive);
    }

    public static boolean isFileTypeGood(String fileName, String findType) {
        return Pattern.compile(".+\\." + findType + "$")
                .matcher(fileName)
                .matches();
    }

}
