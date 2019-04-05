package com.traviumx;

import com.traviumx.ui.home.Home;
import com.traviumx.utils.Database;
import javafx.application.Application;

import javax.xml.crypto.Data;

public class Main {

    public static void main(String[] args) throws Exception {
        Database.connect();
        Application.launch(Home.class, args);
    }


}
