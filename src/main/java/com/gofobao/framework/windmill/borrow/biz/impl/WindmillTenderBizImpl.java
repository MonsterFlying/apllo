package com.gofobao.framework.windmill.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.windmill.borrow.biz.WindmillTenderBiz;
import com.gofobao.framework.windmill.borrow.service.WindmillTenderService;
import com.gofobao.framework.windmill.borrow.vo.request.BackRecordsReq;
import com.gofobao.framework.windmill.borrow.vo.request.UserTenderLogReq;
import com.gofobao.framework.windmill.borrow.vo.response.BackRecords;
import com.gofobao.framework.windmill.borrow.vo.response.BackRecordsRes;
import com.gofobao.framework.windmill.borrow.vo.response.InvestRecords;
import com.gofobao.framework.windmill.borrow.vo.response.InvestRecordsRes;
import com.gofobao.framework.windmill.util.StrToJsonStrUtil;
import com.gofobao.framework.windmill.util.WrbCoopDESUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by admin on 2017/8/4.
 */
@SuppressWarnings("all")
@Slf4j
@Service
public class WindmillTenderBizImpl implements WindmillTenderBiz {
    @Autowired
    private WindmillTenderService windmillTenderService;

    @Value("${windmill.des-key}")
    private String desKey;

    @Value("${windmill.local-des-key}")
    private String localDesKey;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${windmill.short-name}")
    private String shortName;

    @Value("${windmill.request-address}")
    private String requestAddress;

    @Value("${windmill.notify-address}")
    private String notifyAddress;

    @Autowired
    private UsersRepository userService;

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private BorrowRepository borrowService;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowCollectionService borrowCollectionService;

    private static final Gson GSON = new Gson();

    /**
     * 5.6投资记录查询接口
     *
     * @param request
     * @return
     */
    @Override
    public InvestRecordsRes investRecordList(HttpServletRequest request) {

        InvestRecordsRes investRecordsRes = new InvestRecordsRes();
        UserTenderLogReq userTenderLogReq;
        try {
            Map<String, Object> paramMap = StrToJsonStrUtil.commonUrlParamToMap(request.getParameter("param"), desKey);
            userTenderLogReq = GSON.fromJson(GSON.toJson(paramMap), new TypeToken<UserTenderLogReq>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            investRecordsRes.setRetcode(VoBaseResp.ERROR);
            investRecordsRes.setRetmsg("平台转json失败");
            return investRecordsRes;
        }
        try {
            List<Tender> tenders = windmillTenderService.userTenderLog(userTenderLogReq);
            if (CollectionUtils.isEmpty(tenders)) {
                investRecordsRes.setRetcode(VoBaseResp.OK);
                investRecordsRes.setRetmsg("用戶投資記錄為空");
                return investRecordsRes;
            }
            Set<Long> borrowIds = tenders.stream()
                    .map(p -> p.getBorrowId())
                    .collect(Collectors.toSet());
            List<Borrow> borrows = borrowRepository.findByIdIn(new ArrayList<>(borrowIds));

            Map<Long, Borrow> borrowMap = borrows.stream()
                    .collect(Collectors.toMap(Borrow::getId,
                            Function.identity()));
            List<Long> tenderIds = tenders.stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toList());
            Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                    .in("tenderId", tenderIds.toArray())
                    .build();
            List<BorrowCollection> borrowCollections = borrowCollectionService.findList(specification, new Sort(Sort.Direction.ASC, "id"));
            Map<Long, List<BorrowCollection>> borrowCollectionMap = borrowCollections.stream().collect(groupingBy(BorrowCollection::getTenderId));
            List<InvestRecords> invest_records = Lists.newArrayList();
            tenders.forEach(w -> {
                InvestRecords investRecords = new InvestRecords();
                try {
                    Borrow borrow = borrowMap.get(w.getBorrowId());
                    //投资时间
                    investRecords.setInvest_time(DateHelper.dateToString(w.getCreatedAt()));
                    //投资金额
                    investRecords.setInvest_money(StringHelper.formatDouble(w.getValidMoney() / 100D, false));
                    //投资记录id
                    investRecords.setInvest_record_id( w.getId());
                    //项目id
                    investRecords.setProject_id(borrow.getId());
                    //项目标题
                    investRecords.setProject_title(borrow.getName());
                    //项目url
                    investRecords.setProject_url(h5Domain + "#borrowId/" + borrow.getId());
                    //项目利率
                    investRecords.setProject_rate(StringHelper.formatDouble(borrow.getApr() / 100D, false));
                    //项目期限描述
                    investRecords.setProject_timelimit_desc(borrow.getDescription());
                    //还款方式
                    if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                        investRecords.setProject_timelimit(borrow.getTimeLimit());
                        investRecords.setProject_timelimit_desc(borrow.getTimeLimit() + BorrowContants.DAY);
                    } else {
                        investRecords.setProject_timelimit(borrow.getTimeLimit() * 30);
                        investRecords.setProject_timelimit_desc(borrow.getTimeLimit() + BorrowContants.MONTH);
                    }
                    if (StringUtils.isEmpty(userTenderLogReq.getInvest_status())) {
                        //投标中
                        if (w.getState() == 1) {
                            investRecords.setInvest_status(-1);
                        }
                       // 0:收益中
                        else if(w.getState() == 2){
                            investRecords.setInvest_status(0);
                        }
                        // 0:收益中
                        else if(w.getState() == 3){
                            investRecords.setInvest_status(1);
                        }

                    } else {
                        investRecords.setInvest_status(userTenderLogReq.getInvest_status());
                    }

                    //是否自动投标
                    investRecords.setIs_auto_bid0(borrow.getMostAuto());
                    //还款方式
                    if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                        investRecords.setPayback_way(BorrowContants.REPAY_FASHION_ONCE_STR);
                    }
                    if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_MONTH) {
                        investRecords.setPayback_way(BorrowContants.REPAY_FASHION_MONTH_STR);
                    }
                    if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
                        investRecords.setPayback_way(BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR);
                    }
                    //预期收益
                    BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(w.getValidMoney()), new Double(borrow.getApr()), borrow.getTimeLimit(), borrow.getSuccessAt());
                    Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                    Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));
                    investRecords.setAll_interest(StringHelper.formatDouble(earnings / 100D, false));

                    //转让状态
                    if (w.getTransferFlag() == TenderConstans.TRANSFER_ING ||
                            w.getTransferFlag() == TenderConstans.TRANSFER_NO) {
                        investRecords.setAttorn_state(0);
                    } else {
                        investRecords.setAttorn_state(1);
                    }
                    //如果不是投标中
                    if (StringUtils.isEmpty(userTenderLogReq.getInvest_status()) || userTenderLogReq.getInvest_status() != -1) {

                        List<BorrowCollection> tempCollections = borrowCollectionMap.get(w.getId());
                        //已回款本金
                        Long principal = borrowCollections.stream().filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_YES).mapToLong(p -> p.getPrincipal()).sum();
                        //已回款利息
                        Long interest = borrowCollections.stream().filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_YES).mapToLong(p -> p.getInterest()).sum();
                        investRecords.setAll_back_interest(StringHelper.formatDouble(interest / 100D, false));
                        investRecords.setAll_back_principal(StringHelper.formatDouble(principal / 100D, false));
                        //货款中 ||预期中
                        if (w.getState() == TenderConstans.BACK_MONEY) {
                            //过滤掉已回款的期数 获取第一条 就是下个还款日
                            BorrowCollection borrowCollection = borrowCollections.stream().filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_NO).collect(Collectors.toList()).get(0);
                            investRecords.setNext_back_date(DateHelper.dateToString(borrowCollection.getCollectionAt()));
                            investRecords.setNext_back_money(StringHelper.formatDouble(borrowCollection.getCollectionMoney() / 100D, false));
                            investRecords.setNext_back_principal(StringHelper.formatDouble(borrowCollection.getPrincipal() / 100D, false));
                            investRecords.setNext_back_interest(StringHelper.formatDouble(borrowCollection.getInterest() / 100D, false));
                            investRecords.setMonthly_back_date(DateHelper.getDay(borrowCollection.getCollectionAt()));
                        }
                    }
                    invest_records.add(investRecords);

                } catch (Exception e) {
                    investRecordsRes.setRetcode(VoBaseResp.ERROR);
                    investRecordsRes.setRetmsg("平台数据装配异常");
                    return;
                }
            });
            investRecordsRes.setPf_user_id(tenders.get(0).getUserId());
            investRecordsRes.setRetcode(VoBaseResp.OK);
            investRecordsRes.setRetmsg("查询成功");
            investRecordsRes.setInvest_records(invest_records);
        } catch (Exception e) {
            e.printStackTrace();
            investRecordsRes.setRetcode(VoBaseResp.ERROR);
            investRecordsRes.setRetmsg("平台查询异常");
        }
        return investRecordsRes;
    }

    /**
     * 5.7投资记录回款计划
     *
     * @param request
     * @return
     */
    @Override
    public BackRecordsRes backRecordList(HttpServletRequest request) {

        BackRecordsRes backRecordsRes = new BackRecordsRes();
        BackRecordsReq backRecordsReq;
        try {
            String paramSt = request.getParameter("param");
            Map<String, Object> paramMap = StrToJsonStrUtil.commonUrlParamToMap(paramSt, desKey);
            backRecordsReq = GSON.fromJson(GSON.toJson(paramMap), new TypeToken<BackRecordsReq>() {
            }.getType());
        } catch (Exception e) {
            backRecordsRes.setRetcode(VoBaseResp.ERROR);
            backRecordsRes.setRetmsg("平台转json失败");
            return backRecordsRes;
        }
        try {
            //解密
            backRecordsReq.setPf_user_id( backRecordsReq.getPf_user_id());
            backRecordsReq.setInvest_record_id( backRecordsReq.getInvest_record_id());
            //查询
            List<BorrowCollection> borrowCollections = windmillTenderService.backCollectionList(backRecordsReq);
            List<BackRecords> back_records = Lists.newArrayList();
            borrowCollections.forEach(p -> {
                BackRecords backRecords = new BackRecords();
                backRecords.setBack_date(DateHelper.dateToString(p.getCollectionAt()));
                backRecords.setBack_money(StringHelper.formatDouble(p.getCollectionMoney() / 100D, false));
                backRecords.setBack_interest(StringHelper.formatDouble(p.getInterest() / 100D, false));
                backRecords.setBack_principal(StringHelper.formatDouble(p.getPrincipal() / 100D, false));
                backRecords.setBack_status(p.getStatus());
                back_records.add(backRecords);
            });

            backRecordsRes.setRetcode(VoBaseResp.OK);
            backRecordsRes.setRetmsg("查询成功");
            return backRecordsRes;
        } catch (Exception e) {
            backRecordsRes.setRetcode(VoBaseResp.OK);
            backRecordsRes.setRetmsg("查询失败");
            return backRecordsRes;

        }

    }

    /**
     * 4.1投资通知
     *
     * @param paramMap
     * @return
     */
    @Override
    public void tenderNotify(Tender tender) {
        log.info("================进入用戶投资成功平台通知到风车理财================");

        log.info("打印当前用户的投资信息:" + GSON.toJson(tender));
        //拼接请求参数
        String requestParamStr = "";
        do {
            try {
                Borrow borrow = borrowService.findById(tender.getBorrowId());
                requestParamStr = "pf_user_id=" + tender.getUserId() +
                        "&invest_time=" + DateHelper.dateToString(tender.getCreatedAt()) +
                        "&invest_sno=" +  tender.getId() +
                        "&invest_money=" + StringHelper.formatDouble(tender.getValidMoney() / 100D, false) +
                        "&invest_limit=" + (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE ? borrow.getTimeLimit() : (borrow.getTimeLimit() * 30)) +
                        "&invest_rate=" + borrow.getApr() +
                        "&back_way=" + getBorrowBackWay(borrow.getRepayFashion()) +
                        "&invest_title=" + borrow.getName();
                requestParamStr = WrbCoopDESUtil.desEncrypt(desKey, requestParamStr);
            } catch (Exception e) {
                log.info("风车理财用户投资成功通知:失败 通知发送请求 封装参数时失败");
                break;
            }
            //封装请求参数
            Map<String, String> paramMap = Maps.newHashMap();
            paramMap.put("from", shortName);
            paramMap.put("param", requestParamStr);
            //发送通知请求
            String reulstStr = OKHttpHelper.get(notifyAddress, paramMap, null);
            log.info("打印通知风车返回的结果:" + reulstStr);
            Map<String, String> resultMap = GSON.fromJson(reulstStr, new TypeToken<Map<String, String>>() {
            }.getType());
            if (Integer.valueOf(resultMap.get("retcode")) == VoBaseResp.OK)
                log.info("平台通知风车理财成功");
            else
                log.info("平台通知风车理财失败");
        } while (false);
    }


    /**
     * 回款通知
     *
     * @param paramMap
     */
    @Override
    public void backMoneyNotify(List<BorrowCollection> borrowCollections) {
        log.info("================进入用户回款成功 平台通知到风车理财================");
        try {
            do {
                //过滤不是风车理财的用户
                Set<Long> userIds = borrowCollections.stream().map(m -> m.getUserId()).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(userIds)) {
                    log.info("当前批量中没有风车理财的用户");
                    log.info("回款信息:" + JacksonHelper.obj2json(borrowCollections));
                    break;
                }
                //要通知风车理财的回款集合
                List<BorrowCollection> borrowCollectionList = borrowCollections.stream().filter(m -> userIds.contains(m.getUserId())).collect(Collectors.toList());

                //标集合
                Set<Long> borrowIds = borrowCollectionList.stream().map(b -> b.getBorrowId()).collect(Collectors.toSet());
                List<Borrow> borrowList = borrowService.findByIdIn(new ArrayList<>(borrowIds));
                Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));

                //投标记录
                Set<Long> tenderIds = borrowCollectionList.stream().map(p -> p.getTenderId()).collect(Collectors.toSet());
                List<Tender> tenders = tenderRepository.findByIdIn(new ArrayList<>(tenderIds));
                Map<Long, Tender> tenderMap = tenders.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
                //通知风车理财
                borrowCollectionList.forEach(p -> {
                    Tender tender = tenderMap.get(p.getTenderId());
                    Borrow borrow = borrowMap.get(p.getBorrowId());
                    //拼接参数并加密
                    String requestParamStr = "";
                    try {
                        requestParamStr = "pf_user_id=" + p.getUserId() +
                                "&invest_time=" + DateHelper.dateToString(tender.getCreatedAt()) +
                                "&invest_sno=" + tender.getId() +
                                "&invest_money" + StringHelper.formatDouble(tender.getValidMoney() / 100D, false) +
                                "&invest_limit=" + (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE ? borrow.getTimeLimit() : (borrow.getTimeLimit() * 30)) +
                                "&invest_rate=" + StringHelper.formatDouble(borrow.getApr() / 100D, false) +
                                "&back_way=" + getBorrowBackWay(borrow.getRepayFashion()) +
                                "&invest_title=" + borrow.getName();
                        requestParamStr = WrbCoopDESUtil.desEncrypt(desKey, URLEncoder.encode(requestParamStr, "UTF-8"));
                    } catch (Exception e) {
                        log.info("风车理财用户回款通知:失败 通知发送请求 封装参数时失败");
                        return;
                    }
                    //封装请求参数
                    Map<String, String> paramMap = Maps.newHashMap();
                    paramMap.put("from", shortName);
                    paramMap.put("param", requestParamStr);
                    log.info("参数 param:" + GSON.toJson(paramMap));
                    //发送通知请求
                    String result = OKHttpHelper.get(notifyAddress, paramMap, null);
                    log.info("打印通知风车返回的结果:" + result);
                    Map<String, String> resultMap = GSON.fromJson(result, new TypeToken<Map<String, String>>() {
                    }.getType());
                    //通知判断
                    if (Long.valueOf(resultMap.get("retcode")) != VoBaseResp.OK)
                        log.info("平台通知风车理财成功");
                    else
                        log.info("平台通知风车理财失败");
                });

            } while (false);

        } catch (Exception e) {
            log.info("用户回款成功,平台通知风车理财异常");
        }
    }


    private String getBorrowBackWay(Integer borrowType) {
        String back_way = "";
        if (borrowType == BorrowContants.REPAY_FASHION_ONCE) {
            back_way = BorrowContants.REPAY_FASHION_ONCE_STR;
        }
        if (borrowType == BorrowContants.REPAY_FASHION_MONTH) {
            back_way = BorrowContants.REPAY_FASHION_MONTH_STR;
        }
        if (borrowType == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
            back_way = BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR;
        }
        return back_way;
    }
}
