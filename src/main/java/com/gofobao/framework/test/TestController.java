package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.vo.request.VoDelAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoGetAutoTenderList;
import com.gofobao.framework.tender.vo.request.VoOpenAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoSaveAutoTenderReq;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoViewAutoTenderList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/6/21.
 */
@RestController
@Api(description = "自动投标规则控制器")
@RequestMapping
public class TestController {

    @Autowired
    private AutoTenderBiz autoTenderBiz;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AssetChangeProvider assetChangeProvider;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private NewAssetLogService newAssetLogService;

    @ApiOperation("获取自动投标列表")
    @PostMapping("/pub/lend/payment/repair")
    public void lendPaymentRepair1() throws Exception{
        String sql = "\n" +
                "SELECT t.*,t8.username,t8.realname, concat('\\'',t9.account_id) from (\n" +
                "\n" +
                "SELECT t3.`id` as borrowId ,(t3.`op_money` - t4.`op_money`) as money,t3.`user_id` as userId FROM \n" +
                "\n" +
                "(\n" +
                "SELECT \n" +
                "t.`source_id` as id,\n" +
                "t.`user_id`,\n" +
                "       t.`op_money`\n" +
                "        from ( SELECT * from `gfb_new_asset_log` where `source_id` in\n" +
                "                                           ( SELECT `source_id`  FROM `gfb_third_batch_log` where `BATCH_NO`  in ('112424','103523','103244','100841')) and `local_type` in \n" +
                "                                                                                                                ('borrow')\n" +
                "               )t LEFT JOIN `gfb_users` t1 on t.user_id = t1.`id` \n" +
                "LEFT JOIN `gfb_user_third_account` t2 on t.user_id = t2.`user_id` \n" +
                ")\n" +
                "t3,(\n" +
                "    SELECT \n" +
                "t.`source_id` as id,\n" +
                "t.`user_id`,\n" +
                "       t.`op_money`\n" +
                "        from ( SELECT * from `gfb_new_asset_log` where `source_id` in\n" +
                "                                           ( SELECT `source_id`  FROM `gfb_third_batch_log` where `BATCH_NO`  in ('112424','103523','103244','100841')) and `local_type` in \n" +
                "                                                                                                                ('financingManagementFee')\n" +
                "               )t LEFT JOIN `gfb_users` t1 on t.user_id = t1.`id` \n" +
                "LEFT JOIN `gfb_user_third_account` t2 on t.user_id = t2.`user_id` \n" +
                "    \n" +
                "    \n" +
                " ) t4 where t3.`id` = t4.id\n" +
                ")\n" +
                "\n" +
                "t LEFT JOIN `gfb_users` t8 on t.userId = t8.`id` \n" +
                "LEFT JOIN `gfb_user_third_account` t9 on t.userId = t9.`user_id` ";

        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString());
        long redId = assetChangeProvider.getRedpackAccountId();
        UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        for (Map<String, Object> resultMap : resultList) {
            long userId = NumberHelper.toLong(resultMap.get("userId"));
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
            long borrowId = NumberHelper.toLong(resultMap.get("borrowId"));
            long money = NumberHelper.toLong(resultMap.get("money"));

            Specification<NewAssetLog> nals = Specifications
                    .<NewAssetLog>and()
                    .eq("userId", userId)
                    .eq("localType", AssetChangeTypeEnum.freeze.lendPaymentRepair.getLocalType())
                    .eq("borrowId",borrowId)
                    .build();
            long count = newAssetLogService.count(nals);
            if (count > 0) {
                continue;
            }

            // 净值标借款入账
            AssetChange redpackPublish = new AssetChange();
            redpackPublish.setMoney(money);
            redpackPublish.setType(AssetChangeTypeEnum.lendPaymentRepair);  //  净值标借款入账
            redpackPublish.setUserId(userId);
            redpackPublish.setForUserId(redId);
            redpackPublish.setRemark(String.format("净值标借款入账 %s元", StringHelper.formatDouble(money / 100D, true)));
            redpackPublish.setGroupSeqNo(groupSeqNo);
            redpackPublish.setSeqNo(assetChangeProvider.getSeqNo());
            redpackPublish.setForUserId(userId);
            redpackPublish.setSourceId(borrowId);
            assetChangeProvider.commonAssetChange(redpackPublish);

            //请求即信红包
            VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
            voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
            voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
            voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
            voucherPayRequest.setDesLine(String.format("净值标借款入账 %s元", StringHelper.formatDouble(money / 100D, true)));
            voucherPayRequest.setChannel(ChannelContant.HTML);
            VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                throw new Exception(String.format("净值标借款入账失败:%s,borrowId->%s", msg, borrowId));
            }
        }
    }

    /**
     * 获取自动投标列表
     *
     * @return
     * @throws Exception
     */
    @ApiOperation("获取自动投标列表")
    @PostMapping("/pub/lend/payment/repair")
    public void lendPaymentRepair() throws Exception {
        String sql = "\n" +
                "\n" +
                "SELECT t3.`id` as borrowId ,(t3.`op_money` - t4.`op_money`) as money,t3.`user_id` as userId FROM \n" +
                "\n" +
                "(SELECT t2.`id` ,t1.`op_money`  ,t2.`user_id` \n" +
                "  FROM `gfb_new_asset_log` t1\n" +
                "  LEFT JOIN `gfb_borrow` t2 on t1.`source_id`= t2.`id`\n" +
                " where t1.`user_id`= 22002\n" +
                "   and t1.`create_time`> '2017-09-07 00:00:00'\n" +
                "   and local_type IN  ('borrow')\n" +
                "   and t2.`type`= 1\n" +
                "   and t2.`status`= 3\n" +
                "GROUP BY t2.`id` \n" +
                " ORDER BY t1.id)\n" +
                "t3,(SELECT t2.`id` ,t1.`op_money` ,t2.`user_id` \n" +
                "  FROM `gfb_new_asset_log` t1\n" +
                "  LEFT JOIN `gfb_borrow` t2 on t1.`source_id`= t2.`id`\n" +
                " where t1.`user_id`= 22002\n" +
                "   and t1.`create_time`> '2017-09-07 00:00:00'\n" +
                "   and local_type IN  ('financingManagementFee')\n" +
                "   and t2.`type`= 1\n" +
                "   and t2.`status`= 3\n" +
                "GROUP BY t2.`id`) t4 where t3.`id` = t4.id\n";
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString());
        long redId = assetChangeProvider.getRedpackAccountId();
        UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        for (Map<String, Object> resultMap : resultList) {
            long userId = NumberHelper.toLong(resultMap.get("userId"));
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
            long borrowId = NumberHelper.toLong(resultMap.get("borrowId"));
            long money = NumberHelper.toLong(resultMap.get("money"));

            Specification<NewAssetLog> nals = Specifications
                    .<NewAssetLog>and()
                    .eq("userId", userId)
                    .eq("localType", AssetChangeTypeEnum.freeze.lendPaymentRepair.getLocalType())
                    .eq("borrowId",borrowId)
                    .build();
            long count = newAssetLogService.count(nals);
            if (count > 0) {
                continue;
            }

            // 净值标借款入账
            AssetChange redpackPublish = new AssetChange();
            redpackPublish.setMoney(money);
            redpackPublish.setType(AssetChangeTypeEnum.lendPaymentRepair);  //  净值标借款入账
            redpackPublish.setUserId(userId);
            redpackPublish.setForUserId(redId);
            redpackPublish.setRemark(String.format("净值标借款入账 %s元", StringHelper.formatDouble(money / 100D, true)));
            redpackPublish.setGroupSeqNo(groupSeqNo);
            redpackPublish.setSeqNo(assetChangeProvider.getSeqNo());
            redpackPublish.setForUserId(userId);
            redpackPublish.setSourceId(borrowId);
            assetChangeProvider.commonAssetChange(redpackPublish);

            //请求即信红包
            VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
            voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
            voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
            voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
            voucherPayRequest.setDesLine(String.format("净值标借款入账 %s元", StringHelper.formatDouble(money / 100D, true)));
            voucherPayRequest.setChannel(ChannelContant.HTML);
            VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                throw new Exception(String.format("净值标借款入账失败:%s,borrowId->%s", msg, borrowId));
            }
        }
    }
}
