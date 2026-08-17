package com.ruoyi.framework.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 通用配置
 * 
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    private static final String COMPANY_EMBED_PATH = "/company/embed/medical";

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Value("${embedded.cors.allowed-origin-patterns:http://localhost:[*],http://127.0.0.1:[*]}")
    private String embeddedAllowedOriginPatterns;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        /** 本地文件上传路径 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations("file:" + RuoYiConfig.getProfile() + "/");

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration defaultConfig = new CorsConfiguration();
        // 设置访问源地址
        defaultConfig.addAllowedOriginPattern("*");
        // 设置访问源请求头
        defaultConfig.addAllowedHeader("*");
        // 设置访问源请求方法
        defaultConfig.addAllowedMethod("*");
        // 有效期 1800秒
        defaultConfig.setMaxAge(1800L);

        CorsConfiguration embeddedConfig = new CorsConfiguration();
        embeddedConfig.setAllowedOriginPatterns(parseOriginPatterns(embeddedAllowedOriginPatterns));
        embeddedConfig.setAllowedHeaders(Arrays.asList("X-App-Key", "Content-Type", "Accept"));
        embeddedConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS"));
        embeddedConfig.setExposedHeaders(List.of("Content-Disposition"));
        embeddedConfig.setAllowCredentials(false);
        embeddedConfig.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource defaultSource = new UrlBasedCorsConfigurationSource();
        defaultSource.registerCorsConfiguration("/**", defaultConfig);
        CorsConfigurationSource source = request -> isCompanyEmbedRequest(request.getRequestURI(), request.getContextPath())
                ? embeddedConfig : defaultSource.getCorsConfiguration(request);
        return new CorsFilter(source);
    }

    private List<String> parseOriginPatterns(String value)
    {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList();
    }

    private boolean isCompanyEmbedRequest(String requestUri, String contextPath)
    {
        String path = requestUri.substring(contextPath.length());
        return path.equals(COMPANY_EMBED_PATH) || path.startsWith(COMPANY_EMBED_PATH + "/");
    }
}
