package vaccine.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //start
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Main.fxml")));
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon32.png")));
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon128.png")));

        primaryStage.setTitle("Clinic Assistant");
        primaryStage.setOnCloseRequest(event ->{
            GoogleManager.closeThreads();
            AIRAutomation.controller.closeThreads();

            primaryStage.close();
        });
        primaryStage.setScene(new Scene(root, 600, 400));
        //background tasks
        primaryStage.show();
    }


}