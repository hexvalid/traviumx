package com.traviumx.utils;

import com.traviumx.bot.Account;

import java.io.*;
import java.sql.*;

public class Database {

    public static Connection conn;
    private static final String initQuery = "create table accounts(id TEXT,username TEXT not null,password TEXT not null," +
            "gameworld TEXT not null,useragent TEXT,pax TEXT,cookies TEXT,config TEXT,owner TEXT,added TIMESTAMP); " +
            "create table caches(key text not null primary key on conflict replace,date timestamp,value text)";
    private static final String insertAccountQuery = "insert into accounts(id,username,password,gameworld,useragent," +
            "pax,cookies,config,owner,added) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String selectAllAccountsQuery = "select id,username,password,gameworld,useragent,pax,cookies,config from accounts";
    private static final String updateCookiesQuery = "update accounts set cookies = ? where id = ?";
    public static final String insertCacheQuery = "insert into caches(key,date,value) values(?,?,?)";
    public static final String selectCacheQuery = "select date,value from caches where key = ?";
    public static final String updateCacheQuery = "update caches set value = ? where key = ?";

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
        try (PreparedStatement pstmt = conn.prepareStatement(insertAccountQuery)) {
            pstmt.setString(1, a.getId());
            pstmt.setString(2, a.getUsername());
            pstmt.setString(3, a.getPassword());
            pstmt.setString(4, a.getGameWorld().toJson());
            pstmt.setString(5, a.getUserAgent());
            pstmt.setString(6, a.getPax());
            pstmt.setString(7, a.getCookiesAsJson()); //a.getConfig().toJson()
            pstmt.setString(8, ""); //a.getConfig().toJson()
            pstmt.setString(9, Cache.loggedUser);
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
            for (Account pa : Cache.accountList) {
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
                Cache.accountList.add(a);
            }
        }
        stmt.close();
    }

}
