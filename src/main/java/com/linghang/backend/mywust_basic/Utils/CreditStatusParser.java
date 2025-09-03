package com.linghang.backend.mywust_basic.Utils;

import java.util.*;
import java.util.regex.*;

public class CreditStatusParser {
    public static class CourseInfo {
        public String name;
        public String property;
        public String type;
        public String status;

        public CourseInfo(String name, String property, String type, String status) {
            this.name = name;
            this.property = property;
            this.type = type;
            this.status = status;
        }

        @Override
        public String toString() {
            return "课程名称: " + name + ", 课程性质: " + property + ", 课程属性: " + type + ", 状态: " + status;
        }
    }

    public static Map<String, CourseInfo> Parse(String html) {
        Map<String, CourseInfo> courseStatus = new LinkedHashMap<>();

        // 精准匹配课程表格 <table id="pyfakcxdmx">
        Pattern tablePattern = Pattern.compile("<table[^>]*id=\"pyfakcxdmx\"[^>]*>(.*?)</table>", Pattern.DOTALL);
        Matcher tableMatcher = tablePattern.matcher(html);

        if (tableMatcher.find()) {
            String tableContent = tableMatcher.group(1); // 只解析课程明细部分

            // 匹配每行数据
            String rowPattern = "<tr>\\s*<td align=\"center\">[^<]*</td>\\s*" +
                    "<td align=\"center\">[^<]*</td>\\s*" +
                    "<td align=\"center\">(.*?)</td>\\s*" +
                    "<td align=\"center\">[^<]*</td>\\s*" +
                    "<td align=\"center\">(.*?)</td>\\s*" +
                    "<td align=\"center\">(.*?)</td>\\s*" +
                    "<td[^>]*>\\s*(?:<font[^>]*>)?\\s*([^<]*)\\s*(?:</font>)?\\s*</td>";

            Pattern rowRegex = Pattern.compile(rowPattern);
            Matcher matcher = rowRegex.matcher(tableContent);

            while (matcher.find()) {
                String name = matcher.group(1).trim();
                String property = matcher.group(2).trim();
                String type = matcher.group(3).trim();
                String status = matcher.group(4).trim()
                        .replace("&nbsp;", "")
                        .replace("\u00A0", "");

                // 检查课程名称是否有效
                if (!isValidCourseName(name)) {
                    continue; // 跳过无效课程名
                }

                courseStatus.put(name, new CourseInfo(name, property, type, status));
            }
        }

        return courseStatus;
    }

    private static boolean isValidCourseName(String name) {
        if (name == null || name.trim().isEmpty()) return false;

        // 排除纯数字、过短名称、无中文
        if (name.matches("^\\d+$")) return false;
        if (name.length() < 2) return false;
        if (!name.matches(".*[\u4e00-\u9fa5]+.*")) return false; // 至少包含一个中文字符

        return true;
    }
}
