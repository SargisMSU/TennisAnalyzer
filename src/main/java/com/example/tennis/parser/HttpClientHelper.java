package com.example.tennis.parser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import static java.net.HttpURLConnection.HTTP_OK;

public class HttpClientHelper {
    private static HttpClientHelper instance;

    private HttpClientHelper() {
    }

    public static HttpClientHelper getInstance(){
        if (instance == null){
            instance = new HttpClientHelper();
        }
        return instance;
    }

    public String doGetRequest(String url) throws IOException, InterruptedException {
        System.out.println("url: " + url);
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("X-Requested-With", "XMLHttpRequest");
        headersMap.put("X-Fsign", "SW9D1eZo");
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).GET();
        headersMap.forEach(builder::setHeader);
        HttpResponse httpResponse = client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        if (httpResponse.statusCode() == HTTP_OK){
            return (String) httpResponse.body();
        }
        return null;
    }
}
