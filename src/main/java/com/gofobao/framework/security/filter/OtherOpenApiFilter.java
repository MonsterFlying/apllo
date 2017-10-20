package com.gofobao.framework.security.filter;

import com.alibaba.druid.support.http.util.IPAddress;
import com.gofobao.framework.helper.IpHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 第三方接口访问过滤
 * Created by master on 2017/10/19.
 */
@Slf4j
public class OtherOpenApiFilter implements Filter {
    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestUrl = httpServletRequest.getRequestURI();
        String requestIp =IpHelper.getIpAddress(httpServletRequest);
        String passUrl = config.getInitParameter("passUrl");
        /*if (requestUrl.contains("starfire")) {

            log.info("=============进入过滤器中==============");
            log.info("===========访问进入星火接口==============");
            String starFireStr = config.getInitParameter("starFireIps");
            List<String> ips = Lists.newArrayList(starFireStr.split(","));
            log.info("打印当前访问信息: ip" + requestIp + ",访问接口url:" + requestUrl);
            if (!ips.contains(requestIp)) {
                if (!requestUrl.contains(passUrl)) {
                    log.info("当前ip非方访问星火接口");
                    return;
                }
            }
        } else if (requestUrl.contains("windmill")) {
            log.info("=============进入过滤器中==============");
            log.info("===========访问进入风车接口==============");
            String windmillIps = config.getInitParameter("windmillIps");
            List<String> ips = Lists.newArrayList(windmillIps.split(","));
            log.info("打印当前访问信息: ip:" + requestIp + ",访问接口url:" + requestUrl);
            if (!ips.contains(requestIp)) {
                if (!requestUrl.contains(passUrl)) {
                    log.info("当前ip非方访问风车接口");
                    return;
                }
            }
        }*/
        filterChain.doFilter(servletRequest, servletResponse);
        return;
    }

    @Override
    public void destroy() {

    }
}
