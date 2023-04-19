
package at.tuwien;

import at.tuwien.gui.controller.MainGuiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage initialStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/main-gui.fxml"));

        Stage stage = new Stage();
        Scene scene = new Scene(loader.load(), 1500, 850);
        stage.setScene(scene);
        stage.setTitle("CNL-CT System");
        stage.show();

        MainGuiController mainGuiController = loader.getController();
        mainGuiController.setScene(scene);
    }
}
