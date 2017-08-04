package com.gofobao.framework.windmill.borrow.biz.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.award.service.RedPackageService;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.windmill.borrow.biz.WindmillStatisticsBiz;
import com.gofobao.framework.windmill.borrow.service.WindmillStatisticsService;
import com.gofobao.framework.windmill.borrow.vo.response.ByDayStatistics;
import com.gofobao.framework.windmill.borrow.vo.response.UserAccountStatistics;
import com.gofobao.framework.windmill.util.WrbCoopDESUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by admin on 2017/8/3.
 */
@Service
public class WindmillStatisticsBizImpl implements WindmillStatisticsBiz {


    @Value("${windmill.des-key}")
    private String desKey;

    private static final Gson GSON = new Gson();

    @Autowired
    private WindmillStatisticsService windmillStatisticsService;

    @Autowired
    private RedPackageService redPackageService;


    @Autowired
    private AssetService assetService;

    /**
     * 查询每日的汇总数据
     *
     * @param request
     * @return
     */
    @Override
    public ByDayStatistics byDayStatistics(HttpServletRequest request) {

        ByDayStatistics byDayStatistics = new ByDayStatistics();
        Map<String, String> paramMap;
        try {
            String paramStr = WrbCoopDESUtil.desDecrypt(desKey, request.getParameter("param"));
            paramMap = GSON.fromJson(paramStr, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (Exception e) {
            byDayStatistics.setRetcode(VoBaseResp.ERROR);
            byDayStatistics.setRetmsg("平台转json失败");
            return byDayStatistics;
        }
        return windmillStatisticsService.bySomeDayStatistics(paramMap.get("date"));
    }



    /**
     * 5.5账户信息查询接口
     * @param request
     * @return
     */
    @Override
    public UserAccountStatistics userStatistics(HttpServletRequest request) {

        UserAccountStatistics accountStatistics = new UserAccountStatistics();
        Map<String, String> paramMap;
        Long userId;
        try {
            String paramStr = WrbCoopDESUtil.desDecrypt(desKey, request.getParameter("param"));
            paramMap = GSON.fromJson(paramStr, new TypeToken<Map<String, String>>() {
            }.getType());
            userId=Long.valueOf(paramMap.get("pf_user_id"));
        } catch (Exception e) {
            accountStatistics.setRetcode(VoBaseResp.ERROR);
            accountStatistics.setRetmsg("平台转json失败");
            return accountStatistics;
        }
        Asset asset=assetService.findByUserId(userId);


        accountStatistics.setCurrent_money(StringHelper.formatDouble(asset.getUseMoney()/100D,false));
        accountStatistics.setFrozen_money(StringHelper.formatDouble(asset.getNoUseMoney()/100D,false));


        return null;
    }
}
