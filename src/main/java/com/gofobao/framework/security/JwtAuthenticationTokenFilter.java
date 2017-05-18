package com.gofobao.framework.security;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(this.getClass());

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
        // authToken.startsWith("Bearer ")
        // String authToken = header.substring(7);
        String username = jwtTokenHelper.getUsernameFromToken(authToken);

        logger.info("checking authentication für user " + username);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // add userinfo indentify to head
            Long userId = jwtTokenHelper.getUserIdFromToken(authToken);
            request.setAttribute(SecurityContants.USERID_KEY, userId) ;

            // It is not compelling necessary to load the use details from the database. You could also store the information
            // in the captchaToken and read it from it. It's up to you ;)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // For simple validation it is completely sufficient to just check the captchaToken integrity. You don't have to call
            // the database compellingly. Again it's up to you ;)
            if (jwtTokenHelper.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                logger.info("authenticated user " + username + ", setting security context");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}