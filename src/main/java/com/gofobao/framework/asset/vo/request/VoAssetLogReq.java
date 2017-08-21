package com.gofobao.framework.asset.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/22.
 */
@Data
public class VoAssetLogReq extends Page {

    @ApiModelProperty("不传为全部 资金变换类型（online_recharge：充值；cash：提现；frozen：冻结资金；unfrozen：解除冻结；tender：投标;collection_add：添加待收;award：奖励;borrow：借款；payment_add：添加待还；manager：账户管理费；fee:费用;repayment:还款;payment_lower:扣除待还;overdue:逾期费;income_repayment:回款;collection_lower:扣除待收;interest_manager:利息管理费;integral_cash:积分折现;bonus:提成;income_other:其他收入;expenditure_other:其他支出;award_virtual_money:奖励体验金;virtual_tender:投资体验标;correct:数据修正;income_overdue:逾期收入")
    private String type;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")

    private String endTime;
    @ApiModelProperty(hidden = true)
    private Long userId;
}
