package com.gofobao.framework.core.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * Created by Max on 17/5/16.
 */
@Slf4j
public class JWTHelper {

    public static final SignatureAlgorithm HS512 = SignatureAlgorithm.HS512;

    public static final String SECRET = "";


    /**
     * 生成JWT TOKEN
     *
     * @param claims 存储的用户信息
     * @param expirationDate 过期时间
     * @return token
     */
    public static String generateToken(Map<String, Object> claims, Date expirationDate) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(HS512, SECRET)
                .compact();
    }

    /**
     * 解析JWT TOKEN 里面的claims信息
     * @param token token
     * @return 储存的用户信息
     */
    public static Claims paserClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            log.error("");
        }

        return claims ;
    }

}
