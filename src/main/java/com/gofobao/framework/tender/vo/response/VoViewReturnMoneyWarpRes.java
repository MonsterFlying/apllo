package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewReturnMoneyWarpRes  extends VoBaseResp{
    private VoViewReturnedMoney voViewReturnedMoney=new VoViewReturnedMoney();
}
