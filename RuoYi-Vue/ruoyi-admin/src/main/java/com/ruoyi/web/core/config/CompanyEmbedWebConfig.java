package com.ruoyi.web.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ruoyi.web.core.interceptor.CompanyEmbedAppKeyInterceptor;

/**
 * 保险公司嵌入接口配置。
 */
@Configuration
public class CompanyEmbedWebConfig implements WebMvcConfigurer
{
    private final CompanyEmbedAppKeyInterceptor appKeyInterceptor;

    public CompanyEmbedWebConfig(CompanyEmbedAppKeyInterceptor appKeyInterceptor)
    {
        this.appKeyInterceptor = appKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(appKeyInterceptor)
                .addPathPatterns("/company/embed/medical", "/company/embed/medical/**");
    }
}
