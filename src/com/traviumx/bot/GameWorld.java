package com.traviumx.bot;

import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

public class GameWorld {


    private String uuid;
    private String shortcut;
    private String name;
    private String url;
    private int status;
    private boolean registrationKeyRequired;
    private Date start; //todo: +gmt3


    //From JSON
    public GameWorld(String json) {
        GameWorld gw = new Gson().fromJson(json, GameWorld.class);
        this.uuid = gw.uuid;
        this.shortcut = gw.shortcut;
        this.name = gw.name;
        this.url = gw.url;

    }

    public GameWorld() {
        super();
    }


    //for from msgpack
    public GameWorld(String uuid, String shortcut, String name, String url, int status,
                     boolean registrationKeyRequired, long start) {
        this.uuid = uuid;
        this.shortcut = shortcut;
        this.name = name;
        this.url = url;
        this.status = status;
        this.registrationKeyRequired = registrationKeyRequired;
        this.start = new java.util.Date(start * 1000);
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getShortcut() {
        return shortcut;
    }

    public String getUrl() {
        return url;
    }

    public String getPrettyUrl() {
        String prettyUrl = url.replaceAll("http://", "").replaceAll("https://", "");
        if (prettyUrl.substring(prettyUrl.length() - 1).equals("/")) {
            prettyUrl = prettyUrl.substring(0, prettyUrl.length() - 1);
        }
        return prettyUrl;
    }


    public int getStatus() {
        return status;
    }

    public boolean isRegistrationKeyRequired() {
        return registrationKeyRequired;
    }

    public Date getStart() {
        return start;
    }

    public String toJson() {
        return new GsonBuilder().registerTypeAdapter(GameWorld.class, new GameWorld.GameWorldSerializer()).create().toJson(this);
    }

    public static class GameWorldSerializer implements JsonSerializer<GameWorld> {
        public JsonElement serialize(final GameWorld gw, final Type type, final JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add("uuid", new JsonPrimitive(gw.uuid));
            result.add("shortcut", new JsonPrimitive(gw.shortcut));
            result.add("name", new JsonPrimitive(gw.name));
            result.add("url", new JsonPrimitive(gw.url));
            return result;
        }
    }

    public static List<GameWorld> GetGameWorlds(String priority, boolean getAll) throws IOException {
        List<GameWorld> list = new ArrayList<>();
        HttpResponse response = Vars.DefaultHttpClient.execute(new HttpGet(Vars.BaseURL + priority));
        String responseString = new BasicResponseHandler().handleResponse(response);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Matcher m = Vars.WindowDataRegex.matcher(responseString);
        if (m.find()) {
            IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(m.group(1)))), out);
        } else {
            throw new IOException("windowData regex not found");
        }
        Value jsonData = new MessagePack().read(out.toByteArray());
        out.close();
        JsonObject jObj = new Gson().fromJson(String.valueOf(jsonData), JsonObject.class);
        JsonObject gameWorlds = jObj.getAsJsonObject("gameWorlds");
        JsonArray jsonArray = new JsonArray();
        jsonArray.addAll(gameWorlds.getAsJsonArray("list"));
        if (getAll) {
            jsonArray.addAll(gameWorlds.getAsJsonArray("forGtl"));
        }
        for (JsonElement je : jsonArray) {
            JsonObject jgw = je.getAsJsonObject();
            GameWorld gw = new GameWorld(
                    jgw.get("uuid").getAsString(),
                    jgw.get("shortcut").getAsString(),
                    jgw.get("name").getAsString(),
                    jgw.get("url").getAsString(),
                    jgw.get("status").getAsInt(),
                    jgw.get("registrationKeyRequired").getAsBoolean(),
                    jgw.get("start").getAsLong());
            list.add(gw);
        }

        return list;
    }


}


