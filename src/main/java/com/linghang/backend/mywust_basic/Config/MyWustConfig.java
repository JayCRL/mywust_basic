package com.linghang.backend.mywust_basic.Config;
import cn.wustlinghang.mywust.core.request.service.auth.UndergraduateLogin;
import cn.wustlinghang.mywust.core.request.service.captcha.solver.builtin.DdddOcrBase64ImgExprCaptchaSolver;
import cn.wustlinghang.mywust.core.util.WustRequester;
import cn.wustlinghang.mywust.network.RequestClientOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyWustConfig {

    @Bean
    public WustRequester wustRequester() {
        return new WustRequester();
    }

    @Bean
    public DdddOcrBase64ImgExprCaptchaSolver captchaSolver() {
        return new DdddOcrBase64ImgExprCaptchaSolver();
    }

    @Bean
    public UndergraduateLogin undergraduateLogin(WustRequester wustRequester,
                                                 DdddOcrBase64ImgExprCaptchaSolver captchaSolver) {
        return new UndergraduateLogin(wustRequester, captchaSolver);
    }

    @Bean
    public RequestClientOption requestClientOption() {
        RequestClientOption option = new RequestClientOption();
        option.setTimeout(10);
        option.setFollowUrlRedirect(false);
        option.setRetryable(true);
        option.setMaxRetryTimes(3);
        option.setIgnoreSSLError(true);
        return option;
    }
}
