package com.linghang.backend.mywust_basic.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CreditStatusPageGet {
    public static String GetPage(String cookie) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://bkjx.wust.edu.cn/jsxsd/xxwcqk/xxwcqk_idxOnzh.do"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Origin", "https://bkjx.wust.edu.cn")
                .header("Referer", "https://bkjx.wust.edu.cn/jsxsd/xsks/xsksap_query")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                .header("Cookie", cookie)
                .GET()
                    .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("状态码: " + response.statusCode());
        return response.body();
    }
}
