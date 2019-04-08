package com.traviumx;

import com.traviumx.bot.Account;
import com.traviumx.bot.Raid;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class Pool {
    public static ObservableList<Account> accountList = FXCollections.observableList(new ArrayList<>());
    public static List<Raid.TargetVillage> targetVillages = new ArrayList<>();

    public static String loggedUser = "root";
}
