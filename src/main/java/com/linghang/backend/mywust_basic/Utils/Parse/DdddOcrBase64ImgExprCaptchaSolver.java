package com.linghang.backend.mywust_basic.Utils.Parse;

import cn.wustlinghang.mywust.captcha.SolvedImageCaptcha;
import cn.wustlinghang.mywust.captcha.UnsolvedImageCaptcha;
import cn.wustlinghang.mywust.core.request.service.captcha.solver.CaptchaSolver;
import cn.wustlinghang.mywust.exception.ApiException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class DdddOcrBase64ImgExprCaptchaSolver implements CaptchaSolver<String> {
    private final Tesseract tesseract;

    /**
     * 构造方法，初始化 Tesseract OCR 引擎
     * @param tessDataPath tessdata 文件夹路径（包含 eng.traineddata）
     */
    public DdddOcrBase64ImgExprCaptchaSolver(String tessDataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath); // 设置 OCR 数据路径
        this.tesseract.setLanguage("eng");        // 设置语言为英文
    }
    public DdddOcrBase64ImgExprCaptchaSolver() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("mywust/mywust-core/src/main/tessdata"); // 设置 OCR 数据路径
        this.tesseract.setLanguage("eng");        // 设置语言为英文
    }

    /**
     * 核心方法：解决验证码
     * @param unsolvedImageCaptcha 未解决的图片验证码（Base64编码）
     * @return 已解决的图片验证码（带上计算结果）
     * @throws ApiException 自定义异常，OCR 或计算失败时抛出
     */
    @Override
    public SolvedImageCaptcha<String> solve(UnsolvedImageCaptcha<String> unsolvedImageCaptcha) throws ApiException {
        try {
            // 创建结果对象，并设置原始图片
            SolvedImageCaptcha<String> solvedImageCaptcha = new SolvedImageCaptcha<>(unsolvedImageCaptcha);
            // 执行 OCR 和表达式求值
            String result = this.ocrAndEvaluate(unsolvedImageCaptcha.getImage());
            // 设置计算结果
            solvedImageCaptcha.setResult(result);
            return solvedImageCaptcha;
        } catch (IOException | TesseractException e) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG); // 捕获异常，统一抛出为 API 异常
        }
    }

    /**
     * 识别图片中的验证码表达式，并计算其结果
     * @param base64URIData Base64 编码的图片数据（可能带 data:image/png;base64, 前缀）
     * @return 表达式计算结果（字符串形式）
     */
    private String ocrAndEvaluate(String base64URIData) throws IOException, TesseractException, ApiException {
        // 去除 data URI 前缀，只保留 Base64 数据部分
        String base64Image = base64URIData.replaceFirst("^data:image/[^;]+;base64,", "");

        // 解码 Base64 得到图片字节数组
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // 将字节流转为 BufferedImage 对象
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

        // 使用 Tesseract 进行 OCR 识别，去除识别结果中的空白字符
        String rawExpression = tesseract.doOCR(image).replaceAll("\\s+", "");
        System.out.println("识别结果：" + rawExpression); // 可选：控制台输出调试

        // 计算表达式值
        return evaluateExpression(rawExpression);
    }

    /**
     * 对识别到的表达式进行清洗并求值
     * @param expression OCR 识别出来的字符串
     * @return 表达式的求值结果
     */
    private String evaluateExpression(String expression) throws ApiException {
        try {
            // 清洗字符串：替换常见 OCR 错误字符
            expression = expression.replace('×', '*')
                    .replace('÷', '/')
                    .replace('＝', '=')
                    .replaceAll("[^0-9\\+\\-\\*/=]", "");

            // 去掉等号后面的内容（如 1+1=）
            if (expression.contains("=")) {
                expression = expression.substring(0, expression.indexOf("="));
            }

            // 匹配并提取表达式：格式必须为 “数字 运算符 数字”
            if (!expression.matches("\\d+[\\+\\-\\*/]\\d+")) {
                throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            // 解析
            int left, right;
            char op;

            // 找出运算符
            if (expression.contains("+")) {
                op = '+';
            } else if (expression.contains("-")) {
                op = '-';
            } else if (expression.contains("*")) {
                op = '*';
            } else if (expression.contains("/")) {
                op = '/';
            } else {
                throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            // 拆分左右数字
            String[] parts = expression.split("\\" + op);
            left = Integer.parseInt(parts[0]);
            right = Integer.parseInt(parts[1]);

            int result;

            // 计算
            switch (op) {
                case '+': result = left + right; break;
                case '-': result = left - right; break;
                case '*': result = left * right; break;
                case '/':
                    if (right == 0) throw new ArithmeticException("除数为0");
                    result = left / right;
                    break;
                default: throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
            }

            return String.valueOf(result);

        } catch (Exception e) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG);
        }
    }

}
