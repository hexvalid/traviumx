package com.traviumx.bot;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.util.regex.Pattern;

public class Vars {
    public static final String AntiCaptchaKey = "5efd3fe7179bdf7d576bf709d4b698c6";
    static final String BaseURL = "https://www.travian.com/";
    public static final String DefaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36";
    public static final String DefaultLanguage = "en-US,en;q=0.9";
    static final Pattern WindowDataRegex = Pattern.compile("window.__data='(.*?)'");

    static final CloseableHttpClient DefaultHttpClient = HttpClientBuilder.create().setUserAgent(DefaultUserAgent).build();

    public static class HTTP {
        static final Header HeaderAcceptDefault = new BasicHeader(HttpHeaders.ACCEPT,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        public static final Header HeaderAcceptAll = new BasicHeader(HttpHeaders.ACCEPT, "*/*");
        public static final Header HeaderAcceptLanguageDefault = new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9");
        public static final Header HeaderCTypeJson = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        static final Header HeaderAcceptEncoding = new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        static final Header HeaderUIR = new BasicHeader("Upgrade-Insecure-Requests", "1");
    }
}

