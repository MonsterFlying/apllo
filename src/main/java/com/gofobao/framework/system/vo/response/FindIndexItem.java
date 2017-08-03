package com.gofobao.framework.system.vo.response;


import lombok.Data;

@Data
public class FindIndexItem {
    /**
     * 跳转链接
     */
    private String targetUrl ;
    /**
     * 标题
     */
    private String titel ;

    /**
     * 图标url
     */
    private String iconUrl ;
}
