package org.searcher.searchmodule.service;
/*
     Модуль поиска (мозг программы)
*/

import org.searcher.fxmlmanager.MainWindowController;
import org.searcher.searchmodule.model.SearchFilesModel;

import java.io.File;
import java.util.*;

import static org.searcher.searchmodule.controller.SearchFilesController.isFileTypeGood;

public class SearchFilesService {

    private String directory; // Искомая директория

    private String findText; // Искомый текст

    private String findType; // Искомое расширение

    private int id = 1; // ID результата

    private Map<String, int[]> searchedFiles = new HashMap<>();

    private volatile boolean searchIsAlive = false;

    public boolean isSearchIsAlive() {
        return searchIsAlive;
    }

    public void setSearchIsAlive(boolean isAlive) {
        this.searchIsAlive = isAlive;
    }

    public Map<String, int[]> getSearchedFiles() {
        return searchedFiles;
    }

    public void setFields(String directory, String findText, String findType) {
        this.directory = directory.trim()
                .equals("") ? "C:\\" : directory;
        this.findText = findText.equals("*") ? "" : findText.trim();
        // если ничего не ввели выставляем по-умолчанию .log
        this.findType = findType.equals("") ? "log" : findType.trim();
    }

    public void startSearching() {
        List<String> result = currentDirectories(directory);
        List<String> readyForAddingToResult = new ArrayList<>();
        searchIsAlive = true;
        while (!result.isEmpty()) {
            for (String currentDirectory : result) {
                if (currentDirectory != null) {
                    readyForAddingToResult.addAll(currentDirectories(currentDirectory));
                }
            }
            result.clear();
            result.addAll(readyForAddingToResult);
            readyForAddingToResult.clear();
        }
        searchIsAlive = false;
    }
    // Возвращает список директорий в папке

    private List<String> currentDirectories(String path) {
        List<String> result = new ArrayList<>();
        // Список файлов текущей директории
        String[] currentFiles = new File(path).list();
        if (currentFiles != null) {
            Arrays.stream(currentFiles)
                    .map(fileOrDirectoryName ->
                            getFileFromFullNameFileOrDirectory(path, fileOrDirectoryName))
                    .forEach(currentFile ->
                            isaRequiredFileOrDirectory(result, currentFile));
        }
        return result;
    }

    private void isaRequiredFileOrDirectory(List<String> result, File currentFile) {
        if (currentFile.isFile()) {
            String fileForAdd = getCorrectFile(currentFile);
            if (fileForAdd != null) {
                MainWindowController.resultFiles.add(new SearchFilesModel(id, fileForAdd));
                id++;
            }
        } else {
            result.add(currentFile.getAbsolutePath());
        }
    }

    private File getFileFromFullNameFileOrDirectory(String fileOrDirectoryPath,
                                                    String fileOrDirectoryName) {
        String fullNameFileOrDirectory = fileOrDirectoryPath + "\\" + fileOrDirectoryName;
        return new File(fullNameFileOrDirectory);
    }

    private String getCorrectFile(File file) {
        String resultFullName = null;
        String fullNameFile = file.getAbsolutePath();
        if (isaGoodFileAtAll(file)) {
            resultFullName = fullNameFile;
        }
        return resultFullName;
    }

    private boolean isaGoodFileAtAll(File file) {
        return file.canRead() && (findType.equals("") || isFileTypeGood(file.getName(), findType)) &&
                (findText.equals("") || isFileContainCurrentText(file));
    }
    // Проверка наличия искомого текста в файле

    private boolean isFileContainCurrentText(File file) {
        try {
            int[] findIndexesWithSearchedStrings = LuceneSearcherService.isaTextInFile(file, findText);
            searchedFiles.put(file.getAbsolutePath(), findIndexesWithSearchedStrings);
            return findIndexesWithSearchedStrings.length != 0;
        } catch (Exception e) {
            return false;
        }
    }
}