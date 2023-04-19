// partially adapted from https://github.com/TobiasKain/Uhura/blob/master/src/main/java/at/tuwien/gui/MainGuiController.java

package at.tuwien.gui.controller;

import at.tuwien.entity.TestStrength;
import at.tuwien.entity.TestType;
import at.tuwien.gui.service.IMainGuiService;
import at.tuwien.gui.service.MainGuiService;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class MainGuiController implements Initializable{

    @FXML
    public TabPane specificationTabPane;
    @FXML
    public MenuItem newTabMenuItem;
    @FXML
    public MenuItem generateMenuItem;

    @FXML
    public MenuItem openFileMenuItem;
    @FXML
    public MenuItem saveFileMenuItem;

    @FXML
    public MenuItem exportTestsMenuItem;

    private IMainGuiService mainGuiService;
    private MainGuiController mainGuiController;

    public HashMap<Tab, SpecificationTabController> specificationTabControllerHashMap;
    private static int tabCount = 1;
    private Scene scene;
    private boolean firstTabCreated = false;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainGuiController = this;
        specificationTabControllerHashMap = new HashMap<>();
        mainGuiService = new MainGuiService();

        createNewTab();
    }


    // HANDLE FILE MENU ACTIONS

    public void openFileClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CNL-CT test specification");
        FileChooser.ExtensionFilter textFileFilter = new FileChooser.ExtensionFilter("Text Document (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(textFileFilter);

        Stage stage = (Stage) specificationTabPane.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file) {
        SpecificationTabController specificationTabController = createNewTab();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            specificationTabController.specificationArea.replaceText("");

            for (String line: lines) {
                specificationTabController.specificationArea.appendText(line + "\n");
            }

            specificationTabController.getTab().setText(file.getName());

            specificationTabController.setFile(file);

        } catch (IOException e) {
            specificationTabController.errorText.appendText(e.getMessage());
        }

    }

    public void saveFileClicked() {
        saveSpecificationFile();
    }


    private void saveSpecificationFile() {
        if(getControllerOfSelectedTab().getFile() != null)
        {
            saveSpecification(getControllerOfSelectedTab().getFile());
        }
        else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CNL-CT test specification");
            FileChooser.ExtensionFilter txtFileFilter = new FileChooser.ExtensionFilter("Text Document (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(txtFileFilter);

            Stage stage = (Stage) specificationTabPane.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            saveSpecification(file);
        }
    }

    private void saveSpecification(File file){
        if (file != null) {

            String path = file.getPath();
            if(!file.getPath().endsWith(".txt"))
            {
                path += ".txt";
            }

            try(  PrintWriter out = new PrintWriter( path) ){
                out.println( getControllerOfSelectedTab().specificationArea.getText() );
            } catch (FileNotFoundException e) {
                getControllerOfSelectedTab().errorText.appendText(e.getMessage());
            }

            getControllerOfSelectedTab().setInitialSpecContent(getControllerOfSelectedTab().specificationArea.getText());
            getSelectedTab().setText(file.getName());
            getControllerOfSelectedTab().highlightTabLabel(false);
        }
    }

    public void exportTestsClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tests");
        FileChooser.ExtensionFilter csvExtensionFilter = new FileChooser.ExtensionFilter("CSV (Comma delimited) (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(csvExtensionFilter);

        Stage stage = (Stage) specificationTabPane.getScene().getWindow();

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {

            String path = file.getPath();
            if(!file.getPath().endsWith(".csv"))
            {
                path += ".csv";
            }

            try(  PrintWriter out = new PrintWriter( path) ){
                TableView<List<StringProperty>> tableView = getControllerOfSelectedTab().testCaseTableView;
                ObservableList<TableColumn<List<StringProperty>, ?>> columns = tableView.getColumns();
                List<String> columnNames = new ArrayList<>();
                for (TableColumn<List<StringProperty>, ?> column : columns) {
                    columnNames.add(column.getText());
                }
                out.println(String.join(";", columnNames));
                ObservableList<List<StringProperty>> testCaseList = getControllerOfSelectedTab().testCaseTableView.getItems();
                for (List<StringProperty> testCase : testCaseList) {
                    List<String> content = new ArrayList<>();
                    testCase.forEach(s -> content.add(s.getValue()));
                    out.println(String.join(";", content));
                }

            } catch (FileNotFoundException e) {
                getControllerOfSelectedTab().errorText.appendText(e.getMessage());
            }
        }

    }


    // HANDLE TEST CONFIGURATION MENU ACTIONS

    public void inputParameterTestingSelected() {
        mainGuiService.setTestType(TestType.INPUT_PARAMETER);
        getControllerOfSelectedTab().loadSentencePatterns();

    }

    public void eventSequenceTestingSelected() {
        mainGuiService.setTestType(TestType.EVENT_SEQUENCE);
        getControllerOfSelectedTab().loadSentencePatterns();
    }

    public void pairwiseTestingSelected() {
        mainGuiService.setTestStrength(TestStrength.PAIRWISE);
    }

    public void threeWayTestingSelected() {
        mainGuiService.setTestStrength(TestStrength.THREE_WAY);
    }

    public void fourWayTestingSelected() {
        mainGuiService.setTestStrength(TestStrength.FOUR_WAY);
    }


    public void addTabClicked() {

        if(firstTabCreated) {
            createNewTab();
        }
        firstTabCreated = true;
    }

    public SpecificationTabController createNewTab(){
        SpecificationTabController specificationTabController = null;

        try {
            Tab tab = new Tab(String.format("new Tab (%d)",tabCount++));

            FXMLLoader loader = new FXMLLoader();

            Node n = loader.load(MainGuiController.class.getResourceAsStream("/specification-tab.fxml"));

            specificationTabController = loader.getController();
            specificationTabController.setMainGuiService(mainGuiService);
            specificationTabController.setTab(tab);
            specificationTabController.loadSentencePatterns();
            specificationTabController.loadEmptyTestCases();

            SpecificationTabController finalSpecificationTabController = specificationTabController;
            tab.setOnSelectionChanged( event -> {
                finalSpecificationTabController.loadSentencePatterns();
            });
            tab.setOnCloseRequest(event -> {
                if (finalSpecificationTabController.hasSpecificationContentChanged()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Save changes");
                    alert.setHeaderText("Do you want to save your changes?");
                    alert.setContentText("Your changes will be lost if you don't save them.");

                    ButtonType buttonTypeYes = new ButtonType("Yes");
                    ButtonType buttonTypeNo = new ButtonType("No");

                    alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == buttonTypeYes) {
                        saveSpecificationFile();
                    }
                }
            });

            specificationTabControllerHashMap.put(tab,specificationTabController);

            tab.setContent(n);
            specificationTabPane.getTabs().add(specificationTabPane.getTabs().size()-1,tab);
            specificationTabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return specificationTabController;

    }

    private SpecificationTabController getControllerOfSelectedTab(){
        return specificationTabControllerHashMap.get(specificationTabPane.getSelectionModel().getSelectedItem());
    }

    private Tab getSelectedTab(){
        return specificationTabPane.getSelectionModel().getSelectedItem();
    }

    public void newTabClicked() {
        createNewTab();
    }

    public void generateClicked() {
        getControllerOfSelectedTab().generate();
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.isShortcutDown() && keyEvent.getCode().equals(KeyCode.N)){
                createNewTab();
            } else if(keyEvent.isShortcutDown() && keyEvent.getCode().equals(KeyCode.T)){
                getControllerOfSelectedTab().generate();
            }

        });
    }

}
