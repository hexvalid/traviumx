package com.traviumx.bot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.traviumx.Pool;
import com.traviumx.utils.AntiCaptcha;
import com.traviumx.utils.Parser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class Account {
    private String id;
    private String username;
    private String password;
    private GameWorld gameWorld;
    private String userAgent;
    private String pax;
    private String owner;
    private Config config;
    private CloseableHttpClient httpClient;
    private BasicCookieStore cookieStore;


    private boolean isLoaded;

    public String PlayerName;
    public String Tribe; //1:roman, 2:teuton, 3:gaul, 5:natar, 6:egyptian 7:hun
    public String HeroStatus;
    public String HeroStatusText;
    public String HeroStatusTooltip;
    public Image HeroImage;
    public double Health;
    public String HealthTooltip;
    public double Experience;
    public String ExperienceTooltip;
    public ObservableList<Village> Villages;

    private static class Config {
        private String test;
    }


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public GameWorld getGameWorld() {
        return this.gameWorld;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getPax() {
        return this.pax;
    }

    public Config getConfig() {
        return this.config;
    }

    //from AddAccount Form
    public Account(String username, String password, GameWorld gameWorld, String userAgent, String pax) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.gameWorld = gameWorld;
        this.userAgent = userAgent;
        this.pax = pax;
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom()
                .setUserAgent(this.userAgent)
                .setDefaultHeaders(new ArrayList<>(Arrays.asList(
                        Vars.HTTP.HeaderAcceptEncoding,
                        new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, Vars.DefaultLanguage),
                        Vars.HTTP.HeaderUIR)))
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

    //from Database
    public Account(String id, String username, String password, String gameWorld,
                   String userAgent, String pax, String cookies, String config) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.gameWorld = new GameWorld(gameWorld);
        this.userAgent = userAgent;
        this.pax = pax;
        this.cookieStore = new BasicCookieStore();
       /* HttpHost proxy = new HttpHost("localhost", 8080, "http");
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);*/

        this.httpClient = HttpClients.custom()
                .setUserAgent(this.userAgent)
                .setDefaultHeaders(new ArrayList<>(Arrays.asList(
                        Vars.HTTP.HeaderAcceptEncoding,
                        new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, Vars.DefaultLanguage),
                        Vars.HTTP.HeaderUIR)))
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy())
                /*  .setRoutePlanner(routePlanner)
                  .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                  .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)*/
                .setDefaultCookieStore(cookieStore)
                .build();

        try {
            addCookiesFromJson(cookies);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.Villages = FXCollections.observableList(new ArrayList<>());
    }

    public String executeRequest(HttpRequestBase request) throws IOException {
        if (request.getHeaders(HttpHeaders.ACCEPT).length == 0) {
            request.setHeader(Vars.HTTP.HeaderAcceptDefault);
        }
        HttpResponse response = this.httpClient.execute(request);
        return new BasicResponseHandler().handleResponse(response);
    }

    private String getCookie(String cookieName) {
        for (Cookie cookie : this.cookieStore.getCookies()) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String getCookiesAsJson() {
        JsonArray arr = new JsonArray();
        for (Cookie cookie : this.cookieStore.getCookies()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", cookie.getName());
            obj.addProperty("value", cookie.getValue());
            obj.addProperty("domain", cookie.getDomain());
            obj.addProperty("path", cookie.getPath());
            obj.addProperty("version", cookie.getVersion());
            obj.addProperty("issecure", cookie.isSecure());
            if (cookie.getExpiryDate() != null) {
                Format formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
                //todo: global func
                obj.addProperty("expiry", formatter.format(cookie.getExpiryDate()));
            }
            arr.add(obj);
        }
        return arr.toString();
    }

    public void addCookiesFromJson(String json) throws ParseException {
        JsonArray jArr = new Gson().fromJson(String.valueOf(json), JsonArray.class);
        for (JsonElement o : jArr) {
            BasicClientCookie cookie = new BasicClientCookie(
                    o.getAsJsonObject().get("name").getAsString(),
                    o.getAsJsonObject().get("value").getAsString());
            cookie.setDomain(o.getAsJsonObject().get("domain").getAsString());
            cookie.setPath(o.getAsJsonObject().get("path").getAsString());
            cookie.setVersion(o.getAsJsonObject().get("version").getAsInt());
            cookie.setSecure(o.getAsJsonObject().get("issecure").getAsBoolean());
            System.out.println(o.getAsJsonObject().get("expiry"));
            if (o.getAsJsonObject().get("expiry") != null) {
                Date date = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").parse(o.getAsJsonObject().get("expiry").getAsString());
                //todo: global func
                cookie.setExpiryDate(date);
            }
            cookie.setVersion(1); // todo: maybe 1
            this.cookieStore.addCookie(cookie);
        }
    }

    public void Login() throws Exception {
        HttpRequestBase get = new HttpGet(gameWorld.getUrl());
        Document doc = Jsoup.parse(executeRequest(get));
        String s1 = doc.select(".loginButtonRow button").first().text();
        HttpPost post = new HttpPost(gameWorld.getUrl() + "dorf1.php");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("name", this.username));
        params.add(new BasicNameValuePair("password", this.password));
        params.add(new BasicNameValuePair("s1", s1));
        params.add(new BasicNameValuePair("w", "1920:1080"));
        //todo: login in html : name="login" value="
        params.add(new BasicNameValuePair("login", String.valueOf(Instant.now().toEpochMilli() / 1000)));
        post.setEntity(new UrlEncodedFormEntity(params));
        doc = Jsoup.parse(executeRequest(post));
        //System.out.println(doc.outerHtml());
        if (doc.select("#content.login").size() > 0) {
            if (doc.select(".g-recaptcha").size() > 0) {
                System.out.println("captcha tespit edildi");
                String siteKey = doc.select(".g-recaptcha").first().attr("data-sitekey");
                System.out.println(siteKey);
                String recaptchaResponse =
                        AntiCaptcha.SolveNoCaptchaTaskProxyless(Vars.AntiCaptchaKey, gameWorld.getUrl(), siteKey, false);
                params.add(new BasicNameValuePair("g-recaptcha-response", recaptchaResponse));
                post = new HttpPost(gameWorld.getUrl() + "dorf1.php");
                post.setEntity(new UrlEncodedFormEntity(params));
                doc = Jsoup.parse(executeRequest(post));
            }
            StringBuilder errorSb = new StringBuilder();
            Elements errors = doc.select("div.error");
            for (Element error : errors)
                errorSb.append(error.text());
            errorSb.append("!");
            throw new Exception(errorSb.toString());
        }
    }

    public void Load() throws IOException {
        HttpRequestBase get = new HttpGet(gameWorld.getUrl() + "dorf1.php");
        Document doc = Jsoup.parse(executeRequest(get));
        this.PlayerName = doc.select(".playerName").first().text();
        switch (doc.select("div.playerName i").first().attr("class")) {
            case "tribe1_medium":
                this.Tribe = "roman";
                break;
            case "tribe2_medium":
                this.Tribe = "teuton";
                break;
            case "tribe3_medium":
                this.Tribe = "gaul";
                break;
            case "tribe5_medium":
                this.Tribe = "natar";
                break;
            case "tribe6_medium":
                this.Tribe = "egyptian";
                break;
            case "tribe7_medium":
                this.Tribe = "hun";
                break;
        }

        get = new HttpGet(gameWorld.getUrl() + doc.select("img.heroImage").first().attr("src"));
        this.HeroImage = new Image(this.httpClient.execute(get).getEntity().getContent());

        switch (doc.select(".heroStatusMessage img").first().attr("class")) {
            case "heroStatus100":
                this.HeroStatus = "invillage";
                break;
            case "heroStatus101":
                this.HeroStatus = "dead";
                break;
            case "heroStatus101Regenerate":
                this.HeroStatus = "regenerate";
                break;
            case "heroStatus102":
                this.HeroStatus = "captured";
                break;
            case "heroStatus103":
                this.HeroStatus = "onsupply";
                break;
            default: //heroStatus3,heroStatus4,heroStatus5,heroStatus9,heroStatus40,heroStatus50
                this.HeroStatus = "ontheway";
                break;
        }
        this.HeroStatusText = doc.select(".heroStatusMessage").first().text();
        this.HeroStatusTooltip = doc.select(".heroStatusMessage").first().attr("title");
        this.Health = Double.valueOf(StringUtils.substringBetween(
                doc.select(".heroHealthBarBox .bar").first().attr("style"), "width:", "%")) / 100;
        this.HealthTooltip = doc.select(".heroHealthBarBox").first().attr("title");
        this.Experience = Double.valueOf(StringUtils.substringBetween(
                doc.select(".heroXpBarBox .bar").first().attr("style"), "width:", "%")) / 100;
        this.ExperienceTooltip = doc.select(".heroXpBarBox").first().attr("title");


        for (Element e : doc.select("#sidebarBoxVillagelist ul li")) {
            int id = Integer.parseInt(StringUtils.substringBetween(e.select("a").attr("href"), "id=", "&"));
            boolean containsInAccount = false;
            for (Village vl : this.Villages) {
                if (vl.id == id) {
                    containsInAccount = true;
                }
            }
            if (!containsInAccount) {
                Village v = new Village();
                v.id = id;
                v.name = e.select(".name").text();
                v.coordinateX = Parser.ParseInt(e.select(".coordinateX").text());
                v.coordinateY = Parser.ParseInt(e.select(".coordinateY").text());
                this.Villages.add(v);
            }
        }

    }

}

