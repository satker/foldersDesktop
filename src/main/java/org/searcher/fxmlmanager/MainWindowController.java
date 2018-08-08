package org.searcher.fxmlmanager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.searcher.openfile.GetTextFromFile;
import org.searcher.searchmodule.controller.SearchFilesController;
import org.searcher.searchmodule.model.SearchFilesModel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class MainWindowController implements Initializable {

    public static final Map<String, List<String>> fileAndLines = new HashMap<>();

    private static final String[] TEXT_TYPES = (
            "RTF 1ST ABW ACL AFP AMI ANS ASC AWW CCF CSV CWK DBK DITA DOC" +
                    " DOCM DOCX DOT DOTX EGT EPUB EZW FDX FTM FTX GDOC HTML HWP HWPML LOG LWP" +
                    " MBP MD ME MCW Mobi NB NBP NEIS ODM ODOC ODT OSHEET OTT OMM PAGES PAP PDAX" +
                    " PDF QUOX Radix-64RTF RPT SDW SE STW Sxw TeX INFO Troff TXT UOF UOML VIA" +
                    " WPD WPS WPT WRD WRF WRI XHTML XML XPS").toLowerCase()
            .split(" ");

    public static volatile ObservableList<SearchFilesModel> resultFiles = FXCollections.observableArrayList();

    public static Stage primaryStage;

    private static String currentFilePath;

    private static ObservableList textOpenFile = FXCollections.observableArrayList();

    private Thread mainThread;

    private SearchFilesController searchFilesController;

    @FXML
    private TableView<SearchFilesModel> resultFinder;

    @FXML
    private TableColumn<SearchFilesModel, Integer> idFind;

    @FXML
    private TableColumn<SearchFilesModel, String> nameFile;

    @FXML
    private TextField innerFinder;

    @FXML
    private TextField findType;

    @FXML
    private TextField whatFindText;

    @FXML
    private Label changeName;

    @FXML
    private Button startSearch;

    @FXML
    private ProgressIndicator progressSearching;

    @FXML
    private ListView textOutputFile;

    @FXML
    private Button stopSearch;

    @FXML
    private Button openFile;

    @FXML
    private Button chooseDirectory;

    @FXML
    private void startFind() {
        changeName.setText("");
        textOpenFile.clear();
        resultFiles.clear();

        searchFilesController = new SearchFilesController();
        searchFilesController.setVariablesToSearching(innerFinder.getText(),
                whatFindText.getText(),
                findType.getText());
        mainThread = new Thread(searchFilesController);
        mainThread.start();
    }

    @FXML
    public void stopSearchAction() {
        if (mainThread != null && mainThread.isAlive()) {
            mainThread.stop();
            searchFilesController.setSearchIsAlive(false);
        }
    }

    @FXML
    private void openFile() {
        Platform.runLater(() -> {
            textOpenFile.clear();
            getTextFromFile(currentFilePath);
        });
    }

    @FXML
    private void chooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        File defaultDirectory = new File("C:/");
        directoryChooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            innerFinder.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        innerFinder.setDisable(true);

        TextFields.bindAutoCompletion(findType, TEXT_TYPES);

        stopSearch.setDisable(true);

        initializeSearchingResultRow();

        initializeResultFinder();

        textOutputFile.setItems(textOpenFile);

        primaryStage.setOnCloseRequest(event -> stopSearchAction());

        EventListener eventListener = new EventListener();
        eventListener.createAndStartThreadListener();
    }

    private void writeNameFileToLabelChangeName(TableRow<SearchFilesModel> row, MouseEvent event) {
        if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                && event.getClickCount() == 1) {
            SearchFilesModel clickedRow = row.getItem();
            currentFilePath = clickedRow.getName();
            /////// Вывод имени
            changeName.setText(new File(currentFilePath).getName());
        }
    }

    private void getTextFromFile(String pathOfFile) {
        try {
            int[] searchedStrings = searchFilesController.getSearchedFiles()
                    .get(pathOfFile);
            List<String> linesCurrentFile;
            if (fileAndLines.containsKey(pathOfFile)) {
                linesCurrentFile = fileAndLines.get(pathOfFile);
            } else {
                linesCurrentFile = GetTextFromFile.getTextFromFile(pathOfFile);
            }
            StringBuilder currentLines = new StringBuilder();
            if (searchedStrings != null) {
                enterTextToListViewIfTextPresent(searchedStrings, linesCurrentFile, currentLines);
            } else {
                for (String lineOfCurrentFile : linesCurrentFile) {
                    currentLines.append(lineOfCurrentFile)
                            .append('\n');
                    textOpenFile.add(lineOfCurrentFile);
                }
            }
            fileAndLines.put(pathOfFile, linesCurrentFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enterTextToListViewIfTextPresent(int[] searchedStrings,
                                                  List<String> linesCurrentFile,
                                                  StringBuilder currentLines) {
        mainLoop:
        for (int i = 0; i < linesCurrentFile.size(); i++) {
            String currentLine = linesCurrentFile.get(i);
            currentLines.append(currentLine)
                    .append('\n');
            for (int j = 0; j < searchedStrings.length; j++) {
                if (searchedStrings[j] != -1) {
                    if (searchedStrings[j] == i) {
                        Label label = new Label();
                        label.setText(currentLine);
                        label.setTextFill(Color.RED);
                        textOpenFile.add(label);
                        searchedStrings[j] = -1;
                        continue mainLoop;
                    }
                }
            }
            textOpenFile.add(currentLine);
        }
    }

    private void initializeSearchingResultRow() {
        textOutputFile.setEditable(true);
        idFind.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameFile.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    private void initializeResultFinder() {
        resultFinder.setItems(resultFiles);
        resultFinder.setRowFactory(tv -> {
            TableRow<SearchFilesModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    if (!isTextTypeOfFIle()) {
                        openFile();
                    }
                } else {
                    writeNameFileToLabelChangeName(row, event);
                }
            });
            return row;
        });
    }

    private boolean isTextTypeOfFIle() {
        return Arrays.stream(TEXT_TYPES)
                .noneMatch(
                        type -> searchFilesController.isFileTypeGood(
                                changeName.getText(),
                                type.toLowerCase()));
    }

    private class EventListener {
        private void createAndStartThreadListener() {
            Thread listenerThread = new Thread(() -> {
                final int[] count = {1};
                while (true) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    checkDisableOrEnableElements(count);
                    if (changeName.getText()
                            .equals("") || isTextTypeOfFIle()) {
                        openFile.setDisable(true);
                    } else {
                        openFile.setDisable(false);
                    }
                    count[0]++;
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
        }

        private void checkDisableOrEnableElements(int[] count) {
            if (searchFilesController != null && searchFilesController.getSearchIsAlive()) {
                if (progressSearching.getProgress() != -1) {
                    disableAllElementsBeforeSearching(count);
                }
            } else {
                if (progressSearching.getProgress() != 0) {
                    enableAllElementsAfterSearch(count[0]);
                }
            }
        }

        private void disableAllElementsBeforeSearching(int[] count) {
            Platform.runLater(() -> {
                count[0] = 0;
                stopSearch.setDisable(false);
                progressSearching.setProgress(-1);
                progressSearching.setVisible(true);
                findType.setDisable(true);
                whatFindText.setDisable(true);
                chooseDirectory.setDisable(true);
                startSearch.setDisable(true);
            });
        }

        private void enableAllElementsAfterSearch(int x) {
            Platform.runLater(() -> {
                System.out.println(x / 2 + " сек.");
                stopSearch.setDisable(true);
                progressSearching.setVisible(false);
                progressSearching.setProgress(0);
                findType.setDisable(false);
                chooseDirectory.setDisable(false);
                whatFindText.setDisable(false);
                startSearch.setDisable(false);
            });
        }
    }
}
