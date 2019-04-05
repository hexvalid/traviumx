package com.traviumx.ui.home;

import com.traviumx.bot.Account;
import com.traviumx.utils.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Home extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //        ResourceBundle resources = ResourceBundle.getBundle("com.hexvalid.travium.ui.l10n");
        Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
        root.getStylesheets().add(getClass().getResource("/com/traviumx/ui/img/main.css").toExternalForm());
        primaryStage.setTitle("Travium X");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


}
