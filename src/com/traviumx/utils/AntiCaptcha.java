package com.traviumx.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class AntiCaptcha {

    private static final String urlCreateTask = "http://api.anti-captcha.com/createTask";
    private static final String urlGetTaskResult = "http://api.anti-captcha.com/getTaskResult";
    private static final int initInverval = 8000;
    private static final int checkInverval = 3000;

    public static String SolveNoCaptchaTaskProxyless(String clientKey, String websiteURL, String websiteKey, boolean isInvisible) throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setUserAgent("").setDefaultRequestConfig(
                        RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build())
                .build();
        try {
            JsonObject task = new JsonObject();
            task.addProperty("type", "NoCaptchaTaskProxyless");
            task.addProperty("websiteURL", websiteURL);
            task.addProperty("websiteKey", websiteKey);
            task.addProperty("isInvisible", isInvisible);
            JsonObject gson = new JsonObject();
            gson.addProperty("clientKey", clientKey);
            gson.add("task", task);
            HttpPost post = new HttpPost(urlCreateTask);
            post.setEntity(new StringEntity(gson.toString(), ContentType.APPLICATION_JSON));
            long taskID;
            while (true) {
                HttpResponse res = httpClient.execute(post);
                JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity(), "UTF-8")).getAsJsonObject();
                System.out.println(json.toString());
                if (json.get("errorId").getAsInt() > 0) {
                    if (json.get("errorCode").getAsString().equals("ERROR_NO_SLOT_AVAILABLE")) {
                        System.out.println("slot bekleniyor...");
                        Thread.sleep(checkInverval);
                    } else {
                        throw new Exception(json.get("errorCode").getAsString());
                    }
                } else {
                    taskID = json.get("taskId").getAsLong();
                    break;
                }
            }
            Thread.sleep(initInverval);
            while (true) {
                gson = new JsonObject();
                gson.addProperty("clientKey", clientKey);
                gson.addProperty("taskId", taskID);
                post = new HttpPost(urlGetTaskResult);
                post.setEntity(new StringEntity(gson.toString(), ContentType.APPLICATION_JSON));
                HttpResponse res = httpClient.execute(post);
                JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity(), "UTF-8")).getAsJsonObject();
                if (json.get("errorId").getAsInt() > 0) {
                    throw new Exception(json.get("errorCode").getAsString());
                } else if (json.get("status").getAsString().equals("ready")) {
                    return json.getAsJsonObject("solution").get("gRecaptchaResponse").getAsString();
                } else if (json.get("status").getAsString().equals("processing")) {
                    Thread.sleep(checkInverval);
                }
            }
        } finally {
            httpClient.close();
            System.out.println("finallendi");
        }
    }
}
