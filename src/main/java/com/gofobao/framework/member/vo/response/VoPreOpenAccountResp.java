package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 17/5/22.
 */
@Data
@ApiModel
@AllArgsConstructor
@NoArgsConstructor
public class VoPreOpenAccountResp extends VoBaseResp {
    /**
     * 手机号
     */
    private String mobile ;
    /**
     * 证件类型（01- 身份证 18）
     */
    private String idType = "01" ;
    /**
     * 证件号码
     */
    private String idNo ;
    /**
     * 名称
     */
    private String name ;

    /**
     * 银行卡号列表
     */
    private List<VoBankResp> bankList = new ArrayList<>(0);
}
