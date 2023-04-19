// partially adapted from https://github.com/TobiasKain/Uhura/blob/master/src/main/java/at/tuwien/gui/TranslationTabController.java

package at.tuwien.gui.controller;

import at.tuwien.entity.SentencePattern;
import at.tuwien.entity.TestType;
import at.tuwien.gui.TestGenerationThread;
import at.tuwien.gui.service.IMainGuiService;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

public class SpecificationTabController implements Initializable {

    @FXML
    public TextArea errorText;
    @FXML
    public Button generateButton;
    @FXML
    public TabPane infoTabs;

    @FXML
    public Tab errorTab;
    @FXML
    public TableView<SentencePattern> sentencePatternTableView;
    @FXML
    public StackPane specificationStackPane;
    @FXML
    public StackPane testResultStackPane;

    public CodeArea specificationArea;
    public TableView<List<StringProperty>> testCaseTableView;

    private Tab tab;
    private File file;
    private String initialSpecContent = "";
    private String tabLabel;

    private IMainGuiService mainGuiService;
    private SpecificationTabController specificationTabController;

    public SpecificationTabController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        specificationTabController = this;
        specificationArea = new CodeArea();
        specificationArea.setParagraphGraphicFactory(LineNumberFactory.get(specificationArea));
        specificationArea.setOnKeyReleased(this::tfCnlOnKeyReleased);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem miTranslate = new MenuItem("manually translate sentence");

        contextMenu.getItems().add(miTranslate);
        specificationArea.setContextMenu(contextMenu);

        specificationStackPane.getChildren().add(0, new VirtualizedScrollPane<>(specificationArea));

        testCaseTableView = new TableView<>();
        testResultStackPane.getChildren().add(0, testCaseTableView);
    }

    public void generateButtonClicked(ActionEvent actionEvent) {
        generate();
    }

    public void tfCnlOnKeyReleased(KeyEvent keyEvent) {

        highlightTabLabel(!specificationArea.getText().equals(initialSpecContent) &&
                (!keyEvent.isShortcutDown() || (keyEvent.getText().equals("v") && (keyEvent.isMetaDown()))));

    }

    Thread thread;

    public void generate() {
        String specification = specificationArea.getText();
        startTestCaseGeneration();

        TestGenerationThread testGenerationThread = new TestGenerationThread(this, mainGuiService.getTestType(), mainGuiService.getTestStrength(), specification);

        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(testGenerationThread);
        thread.start();

    }

    public void updateTestCasesAsync(List<List<StringProperty>> testCaseList){
        Platform.runLater(() -> {
            testCaseTableView.getItems().clear();
            testCaseTableView.getColumns().clear();

            List<StringProperty> headers = testCaseList.get(0);
            for (int i = 0; i < headers.size(); i++) {
                TableColumn<List<StringProperty>, String> col = new TableColumn<>(headers.get(i).getValue());
                int finalI = i;
                col.setCellValueFactory(data -> data.getValue().get(finalI));
                if (i == 0) {
                    col.setMaxWidth(80);
                }
                testCaseTableView.getColumns().add(col);
            }

            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            for (int i = 1; i < testCaseList.size(); i++) {
                data.add(testCaseList.get(i));
            }
            testCaseTableView.setItems(data);
        });
    }

    public void updateErrorTextAsync(String s) {
        Platform.runLater(() -> {
            errorText.setStyle("-fx-text-fill: red ;");
            errorText.setText(s);
            infoTabs.getSelectionModel().select(0);
        });
    }

    public void appendErrorText(String s) {
        Platform.runLater(() -> {
            errorText.setStyle("-fx-text-fill: red ;");
            errorText.appendText(s);
        });
    }


    public void clearErrorTextAsync() {
        Platform.runLater(() -> {
            errorText.setStyle("-fx-text-fill: black ;");
            errorText.setText("");
        });
    }

    public void loadSentencePatterns() {
        sentencePatternTableView.getItems().clear();
        sentencePatternTableView.getColumns().clear();
        TableColumn<SentencePattern, String> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("patternNumber"));
        numberColumn.setSortable(false);
        numberColumn.setMaxWidth(40);
        TableColumn<SentencePattern, String> typeColumn = new TableColumn<>("Sentence Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("patternName"));
        typeColumn.setSortable(false);
        TableColumn<SentencePattern, String> patternColumn = new TableColumn<>("Sentence Pattern");
        patternColumn.setCellValueFactory(new PropertyValueFactory<>("pattern"));
        patternColumn.setSortable(false);
        TableColumn<SentencePattern, String> exampleColumn = new TableColumn<>("Example Sentence");
        exampleColumn.setCellValueFactory(new PropertyValueFactory<>("example"));
        exampleColumn.setSortable(false);

        sentencePatternTableView.getColumns().add(numberColumn);
        sentencePatternTableView.getColumns().add(typeColumn);
        sentencePatternTableView.getColumns().add(patternColumn);
        sentencePatternTableView.getColumns().add(exampleColumn);

        InputStream inputStream;

        if (mainGuiService == null || mainGuiService.getTestType().equals(TestType.INPUT_PARAMETER)) {
            inputStream = this.getClass().getResourceAsStream("/patternsInputParameterTesting.csv");
        } else {
            inputStream = this.getClass().getResourceAsStream("/patternsEventSequenceTesting.csv");
        }
        Scanner sentencePatternScanner = new Scanner(Objects.requireNonNull(inputStream));
        while (sentencePatternScanner.hasNext()) {
            String sentenceLine = sentencePatternScanner.nextLine();
            SentencePattern sentencePattern = new SentencePattern(sentenceLine);
            sentencePatternTableView.getItems().add(sentencePattern);
        }
    }


    public void loadEmptyTestCases() {
        testCaseTableView.setPlaceholder(new Label("No test cases have been generated yet"));
    }

    public void startTestCaseGeneration() {
            generateButton.setDisable(true);
            generateButton.setText("Generating ...");
    }

    public void endTestCaseGenerationAsync() {

        Platform.runLater(() -> {
            generateButton.setDisable(false);
            generateButton.setText("Generate");
        });

    }

    public void highlightTabLabel(boolean highlight) {
        if (!tab.getText().equals("")) {
            tabLabel = tab.getText();
            tab.setText("");
        }

        tab.setGraphic(new Label(tabLabel));
        if (highlight) {
            tab.getGraphic().setStyle("-fx-text-fill: #004aff;");
        } else {
            tab.getGraphic().setStyle("-fx-text-fill: black;");
        }
    }

    public boolean hasSpecificationContentChanged() {
        String currentSpecification = specificationArea.getText();
        return !currentSpecification.equals(initialSpecContent);
    }


    public void setMainGuiService(IMainGuiService mainGuiService) {
        this.mainGuiService = mainGuiService;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        initialSpecContent = specificationArea.getText();
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public Tab getTab() {
        return tab;
    }

    public void setInitialSpecContent(String initialSpecContent) {
        this.initialSpecContent = initialSpecContent;
    }
}
