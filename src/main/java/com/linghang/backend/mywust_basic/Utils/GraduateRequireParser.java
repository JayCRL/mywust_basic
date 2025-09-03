package com.linghang.backend.mywust_basic.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraduateRequireParser {

    public static Map<String, Object> Parse(String html) {
        Map<String, Object> finalResult = new HashMap<>();

        Map<String, String> flatData = parseFlatData(html);
        Map<String, Object> courseStats = new LinkedHashMap<>();
        Map<String, Object> gradCredits = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : flatData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("课程统计")) {
                String subKey = key.replace("课程统计 - ", "");
                courseStats.put(subKey, value);
            } else {
                String cleanKey = key.endsWith(" - ") ? key.replace(" - ", "") : key;

                // 拆分学分
                if (value.contains("要求学分") && value.contains("已获得")) {
                    Map<String, String> creditInfo = new LinkedHashMap<>();
                    String[] parts = value.split(",");
                    for (String part : parts) {
                        String[] kv = part.split(":");
                        if (kv.length == 2) {
                            creditInfo.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                    gradCredits.put(cleanKey, creditInfo);
                }
            }
        }

        finalResult.put("课程统计", courseStats);
        finalResult.put("毕业学分完成情况", gradCredits);

        return finalResult;
    }

    // 简化实现，解析 byxf1 和 byxf3 表格
    private static Map<String, String> parseFlatData(String html) {
        Map<String, String> courseStatus = new LinkedHashMap<>();

        // byxf1 - 毕业学分完成情况
        String pattern1 = "<table[^>]*id=\"byxf1\"[^>]*>(.*?)</table>";
        Matcher matcher1 = Pattern.compile(pattern1, Pattern.DOTALL).matcher(html);
        if (matcher1.find()) {
            String tableContent = matcher1.group(1);
            Pattern rowPattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
            Matcher rowMatcher = rowPattern.matcher(tableContent);
            while (rowMatcher.find()) {
                String row = rowMatcher.group(1);
                Matcher cellMatcher = Pattern.compile("<td[^>]*>(.*?)</td>").matcher(row);
                List<String> cells = new ArrayList<>();
                while (cellMatcher.find()) {
                    cells.add(cellMatcher.group(1).trim().replaceAll("<[^>]+>", ""));
                }
                if (cells.size() == 4) {
                    String key = cells.get(0) + " - " + cells.get(1);
                    String value = "要求学分: " + cells.get(2) + ", 已获得: " + cells.get(3);
                    courseStatus.put(key, value);
                }
            }
        }

        // byxf3 - 课程统计
        String pattern2 = "<table[^>]*id=\"byxf3\"[^>]*>(.*?)</table>";
        Matcher matcher2 = Pattern.compile(pattern2, Pattern.DOTALL).matcher(html);
        if (matcher2.find()) {
            String tableContent = matcher2.group(1);
            Matcher rowMatcher = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL).matcher(tableContent);
            int rowCount = 0;
            while (rowMatcher.find()) {
                rowCount++;
                if (rowCount == 2) {
                    Matcher cellMatcher = Pattern.compile("<td[^>]*>(.*?)</td>").matcher(rowMatcher.group(1));
                    String[] headers = new String[]{
                            "学期数", "已修门数", "已修学分", "学期平均学分",
                            "不及格门数", "不及格学分", "未选门数", "未选学分",
                            "必修未获学分门数", "必修未获学分"
                    };
                    int i = 0;
                    while (cellMatcher.find() && i < headers.length) {
                        String cleanText = cellMatcher.group(1).trim().replaceAll("<[^>]+>", "");
                        courseStatus.put("课程统计 - " + headers[i], cleanText);
                        i++;
                    }
                }
            }
        }

        return courseStatus;
    }

    // 测试主方法
    public static void main(String[] args) throws Exception {
        // 示例 HTML，替换成你的真实HTML字符串
        String html = "<html>...你的HTML片段...</html>"; // 插入完整HTML

        Map<String, Object> result = Parse(html);

        // 打印 JSON 格式
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println(jsonResult);
    }
}
