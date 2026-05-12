package com.ruoyi.business.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.business.domain.BizInsuranceCompany;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;

@Component
public class CompanyTokenService
{
    @Value("${token.secret:abcdefghijklmnopqrstuvwxyz}")
    private String secret;

    private static final long EXPIRE_MINUTES = 480; // 8小时

    private static final String CACHE_PREFIX = "company_token:";

    @Resource
    private RedisCache redisCache;

    public String createToken(BizInsuranceCompany company)
    {
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        String cacheKey = CACHE_PREFIX + token;
        redisCache.setCacheObject(cacheKey, company, (int) EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        claims.put("token", token);
        claims.put("company_id", company.getId());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new java.util.Date())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public BizInsuranceCompany getCompanyFromToken(String jwtToken)
    {
        try
        {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
            String token = (String) claims.get("token");
            return redisCache.getCacheObject(CACHE_PREFIX + token);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void delLoginUser(String jwtToken)
    {
        try
        {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
            String token = (String) claims.get("token");
            redisCache.deleteObject(CACHE_PREFIX + token);
        }
        catch (Exception ignored) {}
    }
}
