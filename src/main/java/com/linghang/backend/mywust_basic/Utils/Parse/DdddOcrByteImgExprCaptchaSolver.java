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

public class DdddOcrByteImgExprCaptchaSolver implements CaptchaSolver<byte[]> {
    private final Tesseract tesseract;

    /**
     * 构造方法，初始化 Tesseract OCR 引擎
     * @param tessDataPath tessdata 文件夹路径（包含 eng.traineddata）
     */
    public DdddOcrByteImgExprCaptchaSolver(String tessDataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath); // 设置 OCR 数据路径
        this.tesseract.setLanguage("eng");        // 设置语言为英文
    }

    /**
     * 默认构造方法，使用默认的 tessdata 路径
     */
    public DdddOcrByteImgExprCaptchaSolver() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("mywust/mywust-core/src/main/tessdata"); // 设置 OCR 数据路径
        this.tesseract.setLanguage("eng");        // 设置语言为英文
    }

    /**
     * 核心方法：解决验证码（处理byte[]类型图片）
     * @param unsolvedImageCaptcha 未解决的图片验证码（byte[]类型图片数据）
     * @return 已解决的图片验证码（带上计算结果）
     * @throws ApiException 自定义异常，OCR 或计算失败时抛出
     */
    @Override
    public SolvedImageCaptcha<byte[]> solve(UnsolvedImageCaptcha<byte[]> unsolvedImageCaptcha) throws ApiException {
        try {
            // 创建结果对象，并设置原始图片
            SolvedImageCaptcha<byte[]> solvedImageCaptcha = new SolvedImageCaptcha<>(unsolvedImageCaptcha);
            // 执行 OCR 和表达式求值（直接传入byte[]图片数据）
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
     * @param imageBytes 图片字节数组（直接处理原始图片数据）
     * @return 表达式计算结果（字符串形式）
     */
    private String ocrAndEvaluate(byte[] imageBytes) throws IOException, TesseractException, ApiException {
        // 直接将字节数组转为 BufferedImage 对象（无需Base64解码）
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new ApiException(ApiException.Code.CAPTCHA_WRONG, "无法解析图片数据");
        }

        // 使用 Tesseract 进行 OCR 识别，去除识别结果中的空白字符
        String rawExpression = tesseract.doOCR(image).replaceAll("\\s+", "");
        System.out.println("识别结果：" + rawExpression); // 调试输出

        // 计算表达式值
        return rawExpression;
    }

    /**
     * 对识别到的表达式进行清洗并求值（逻辑保持不变）
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

            // 解析运算符和操作数
            int left, right;
            char op;

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

            // 计算结果
            int result;
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
