package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@ApiModel
public class VoUserBankListResp extends VoBaseResp{
    List<VoUserBankResp> bankList;
}
