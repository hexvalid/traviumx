package com.traviumx;

import com.traviumx.bot.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class Pool {
    public static ObservableList<Account> accountList = FXCollections.observableList(new ArrayList<>());
    public static String loggedUser = "root";
}
