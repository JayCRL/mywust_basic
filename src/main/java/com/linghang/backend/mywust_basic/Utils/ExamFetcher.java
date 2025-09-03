package com.linghang.backend.mywust_basic.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ExamFetcher {
   public static String GetExamPage(String term,String cookie) throws IOException, InterruptedException {
        // 构造请求体参数
        String postData = "xqlbmc=&sxxnxq=&dqxnxq=&ckbz=&xnxqid=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&xqlb=";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://bkjx.wust.edu.cn/jsxsd/xsks/xsksap_list"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Origin", "https://bkjx.wust.edu.cn")
                .header("Referer", "https://bkjx.wust.edu.cn/jsxsd/xsks/xsksap_query")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                .header("Cookie", cookie)
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        System.out.println("状态码: " + response.statusCode());
//        System.out.println("响应内容: ");
        return  response.body();
    }
}
