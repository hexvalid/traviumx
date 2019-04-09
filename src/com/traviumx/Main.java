package com.traviumx;

import com.traviumx.ui.home.Home;
import com.traviumx.utils.Database;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) throws Exception {
        Database.connect();

      //  Database.ReadObjectFromDB("gameworlds");

        Application.launch(Home.class, args);
    }

}
