package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.response.VoViewReturnedMoney;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class VoViewFinanceReturnMoneyWarpRes extends VoBaseResp{
    private VoViewFinanceReturnedMoney voViewReturnedMoney=new VoViewFinanceReturnedMoney();
}
