package com.gofobao.framework.borrow.biz.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoAddNetWorthBorrow;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.service.AutoTenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Service
@Slf4j
public class BorrowBizImpl implements BorrowBiz{



    @Autowired
    private UserService userService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AutoTenderService autoTenderService;

    /**
     * 新增净值借款
     * @param voAddNetWorthBorrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> addNetWorth(VoAddNetWorthBorrow voAddNetWorthBorrow){
        Long userId = voAddNetWorthBorrow.getUserId();
        String releaseAtStr = voAddNetWorthBorrow.getReleaseAt();
        Integer money = voAddNetWorthBorrow.getMoney();
        boolean closeAuto = voAddNetWorthBorrow.isCloseAuto();

        Asset asset = assetService.findByUserIdLock(userId);
        if (ObjectUtils.isEmpty(asset)){
            log.info("新增净值借款：用户asset未被查询得到。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"系统开小差了，请稍候重试！"));
        }

        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users.getCardId())){
            log.info("新增净值借款：当前用户未实名。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"当前用户未实名认证!"));
        }

        Date releaseAt = DateHelper.stringToDate(releaseAtStr,DateHelper.DATE_FORMAT_YMDHMS);
        if (releaseAt.getTime() > DateHelper.addDays(new Date(),1).getTime()){
            log.info("新增净值借款：发布时间必须在24小时内。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"发布时间必须在24小时内!"));
        }

        UserCache userCache = userCacheService.findById(userId);
        if (ObjectUtils.isEmpty(userCache)){
            log.info("新增净值借款：用户usercache未被查询得到。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"系统开小差了，请稍候重试！"));
        }

        double totalMoney = (asset.getUseMoney() + userCache.getWaitCollectionPrincipal()) * 0.8 - asset.getPayment();
        if (totalMoney < money){
            log.info("新增净值借款：借款金额大于净值额度。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"借款金额大于净值额度!"));
        }

        long count = borrowService.countByUserIdAndStatusIn(userId, Arrays.asList(0,1));
        if (count > 0){
            log.info("新增净值借款：您已经有一个进行中的借款标。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"您已经有一个进行中的借款标!"));
        }

        if (closeAuto){ //关闭用户自动投标
            AutoTender saveAutoTender = new AutoTender();
            saveAutoTender.setStatus(false);
            saveAutoTender.setUpdatedAt(new Date());

            AutoTender condAutoTender = new AutoTender();
            condAutoTender.setUserId(userId);
            Example<AutoTender> autoTenderExample = Example.of(condAutoTender);

            if (!autoTenderService.updateByExample(saveAutoTender,autoTenderExample)){
                log.info("新增净值借款：自动投标关闭失败。");
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR,"自动投标关闭失败!"));
            }
        }

        Long borrowId = null;
        try {
            borrowId = insertBorrow(voAddNetWorthBorrow, userId);  // 插入标
        } catch (Exception e) {
            log.error("新增净值借款异常：",e);
        }

        if (borrowId<=0){
            log.info("新增净值借款：净值标插入失败。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"净值标插入失败!"));
        }

        /**
         * @// TODO: 2017/5/27 初审逻辑
         */

        return  ResponseEntity.ok(VoBaseResp.ok("净值借款成功!"));
    }

    private long insertBorrow(VoAddNetWorthBorrow voAddNetWorthBorrow, Long userId) throws Exception {
        Borrow borrow = new Borrow();
        borrow.setType(BorrowContants.JING_ZHI); // 净值标
        borrow.setUserId(userId);
        borrow.setUse(0);
        borrow.setIsLock(false);
        borrow.setIsImpawn(false);
        borrow.setIsContinued(false);
        borrow.setIsConversion(false);
        borrow.setIsNovice(false);
        borrow.setIsVouch(false);
        borrow.setIsMortgage(false);
        borrow.setName(voAddNetWorthBorrow.getName());
        borrow.setMoney(voAddNetWorthBorrow.getMoney());
        borrow.setRepayFashion(1);//voAddNetWorthBorrow.getRepayFashion()
        borrow.setTimeLimit(voAddNetWorthBorrow.getTimeLimit());
        borrow.setApr(voAddNetWorthBorrow.getApr());
        borrow.setLowest(50 * 100);
        borrow.setMost(0);
        borrow.setMostAuto(0);
        borrow.setValidDay(voAddNetWorthBorrow.getValidDay());
        borrow.setAward(0);
        borrow.setAwardType(0);
        String releaseAt = voAddNetWorthBorrow.getReleaseAt();
        if (!ObjectUtils.isEmpty(releaseAt)) {
            borrow.setReleaseAt(DateHelper.stringToDate(releaseAt, "yyyy-MM-dd HH:mm:ss"));
        }
        borrow.setDescription("");
        borrow.setPassword("");
        borrow.setMoneyYes(0);
        borrow.setTenderCount(0);
        borrow.setCreatedAt(new Date());
        borrow.setUpdatedAt(new Date());
        boolean rs = borrowService.insert(borrow);
        if (rs) {
            return borrow.getId();
        } else {
            return 0;
        }
    }

    /**
     * 取消借款
     * @param voCancelBorrow
     * @return
     */
    public ResponseEntity<VoBaseResp> cancelBorrow(VoCancelBorrow voCancelBorrow){
        return null;
    }
}
