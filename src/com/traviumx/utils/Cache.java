package com.traviumx.utils;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.traviumx.bot.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import static com.traviumx.utils.Database.selectCacheQuery;
import static com.traviumx.utils.Database.updateCacheQuery;

public class Cache {
    public static ObservableList<Account> accountList = FXCollections.observableList(new ArrayList<>());
    public static String loggedUser = "root";


    public static void AddToCache(String key, Object o) throws SQLException {
        try (PreparedStatement pstmt = Database.conn.prepareStatement(Database.insertCacheQuery)) {
            pstmt.setString(1, key);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, JsonWriter.objectToJson(o));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public static Object GetFromCache(String key, int maxValidityHours) throws SQLException {
        try (PreparedStatement pstmt = Database.conn.prepareStatement(selectCacheQuery)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery(); //todo: hmm? not looks like so good
            if (rs.next() && rs.getTimestamp(1).after(new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * maxValidityHours)))) {
                return JsonReader.jsonToJava(rs.getString(2));
            }

        } catch (SQLException e) {
            throw e;
        }
        //todo wtf returns?
        return null;
    }

    public static Object UpdateFromCache(String key, Object o) throws SQLException {
        try (PreparedStatement pstmt = Database.conn.prepareStatement(updateCacheQuery)) {
            pstmt.setString(1, JsonWriter.objectToJson(o));
            pstmt.setString(2, key);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
        //todo wtf returns?
        return null;
    }
}
