package com.gofobao.framework.security;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.mail.internet.ContentType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class

JwtAuthenticationTokenBeforeFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(this.getClass());

    Gson GSON = new Gson() ;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.prefix}")
    private String prefix ;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authToken = request.getHeader(this.tokenHeader);
        if(!StringUtils.isEmpty(authToken) && (authToken.contains(prefix))){
            authToken = authToken.substring(7) ;
        }

        String username = jwtTokenHelper.getUsernameFromToken(authToken);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Long userId = jwtTokenHelper.getUserIdFromToken(authToken);
            request.setAttribute(SecurityContants.USERID_KEY, userId) ;

            // It is not compelling necessary to load the use details from the database. You could also store the information
            // in the captchaToken and read it from it. It's up to you ;)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // For simple validation it is completely sufficient to just check the captchaToken integrity. You don't have to call
            // the database compellingly. Again it's up to you ;)
            if (jwtTokenHelper.validateToken(authToken, userDetails)) {
//                //  判断用户TOKEN是否过期
//                try {
//                    jwtTokenHelper.validateSign(authToken) ;
//                } catch (Exception e) {
//                    response.setContentType("application/json");
//                    response.setCharacterEncoding("UTF-8");
//                    try(PrintWriter printWriter = response.getWriter()) {
//                        VoBaseResp error = VoBaseResp.error(VoBaseResp.RELOGIN, e.getMessage());
//                        printWriter.write(GSON.toJson(error));
//                        printWriter.flush();
//                    }catch (Exception ex){
//                        logger.error("权限验证异常") ;
//                    }
//                    return;
//                }
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                logger.info("authenticated user " + username + ", setting security context");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}