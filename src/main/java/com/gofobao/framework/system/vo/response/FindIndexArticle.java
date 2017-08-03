package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class FindIndexArticle {
    private String imageUrl ;
    private Long id ;
    private String titel ;
    private String time ;
}
