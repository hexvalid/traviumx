package com.traviumx.utils;

import com.traviumx.Pool;
import com.traviumx.bot.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static Connection conn;
    private static final String initQuery = "create table accounts(id TEXT,username TEXT not null,password TEXT not null," +
            "gameworld TEXT not null,useragent TEXT,pax TEXT,cookies TEXT,config TEXT,owner TEXT,added TIMESTAMP); " +
            "create unique index accounts_id_uindex on accounts (id);";
    private static final String addAccountQuery = "insert into accounts(id,username,password,gameworld,useragent," +
            "pax,cookies,config,owner,added) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String selectAllAccountsQuery = "select id,username,password,gameworld,useragent,pax,cookies,config from accounts";
    private static final String updateCookiesQuery = "update accounts set cookies = ? where id = ?";


    public static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        boolean firstInit;
        File f = new File("config.dat");
        if (f.exists() && !f.isDirectory()) {
            firstInit = false;
        } else {
            firstInit = true;
        }
        String url = "jdbc:sqlite:config.dat";
        conn = DriverManager.getConnection(url);
        Statement stmt = conn.createStatement();
        if (firstInit) {
            stmt.execute(initQuery);
        }
        System.out.println("Connection to SQLite has been established.");
    }


    public static void AddAccountToDB(Account a) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(addAccountQuery)) {
            pstmt.setString(1, a.getId());
            pstmt.setString(2, a.getUsername());
            pstmt.setString(3, a.getPassword());
            pstmt.setString(4, a.getGameWorld().toJson());
            pstmt.setString(5, a.getUserAgent());
            pstmt.setString(6, a.getPax());
            pstmt.setString(7, a.getCookiesAsJson()); //a.getConfig().toJson()
            pstmt.setString(8, ""); //a.getConfig().toJson()
            pstmt.setString(9, Pool.loggedUser);
            pstmt.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public static void UpdateCookies(Account a) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(updateCookiesQuery)) {
            pstmt.setString(1, a.getCookiesAsJson());
            pstmt.setString(2, a.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public static void getAccountsToPool() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectAllAccountsQuery);
        while (rs.next()) {
            boolean containsInPool = false;
            String id = rs.getString("id");
            for (Account pa : Pool.accountList) {
                if (pa.getId().equals(id)) {
                    containsInPool = true;
                }
            }
            if (!containsInPool) {
                Account a = new Account(id,
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("gameworld"),
                        rs.getString("useragent"),
                        rs.getString("pax"),
                        rs.getString("cookies"),
                        rs.getString("config")
                );
                Pool.accountList.add(a);
            }
        }
        stmt.close();
    }
}
