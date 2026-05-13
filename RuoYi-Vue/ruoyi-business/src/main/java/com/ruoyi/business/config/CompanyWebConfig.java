package com.ruoyi.business.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CompanyWebConfig implements WebMvcConfigurer
{
    @Autowired
    private CompanyAuthInterceptor companyAuthInterceptor;

    @Autowired
    private SignAuthInterceptor signAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(companyAuthInterceptor)
                .addPathPatterns("/company/api/**");
        registry.addInterceptor(signAuthInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
