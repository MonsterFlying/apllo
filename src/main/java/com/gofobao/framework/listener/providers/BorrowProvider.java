package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;

    public boolean doFirstVerify(Long borrowId) {
        return false;
    }

    /**
     * 车贷标、净值标、渠道标初审
     *
     * @return
     */
    private boolean cqjBorrow(Long borrowId) {
        boolean bool = false;
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Borrow borrow = borrowService.findByIdLock(borrowId);
            if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
                break;
            }

            //更新借款状态
            borrow.setIsLock(true);
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);
            }
            borrowService.updateById(borrow);

            Date releaseDate = borrow.getReleaseAt();

            //====================================
            //延时投标
            //====================================
            if (borrow.getIsNovice()) {//判断是否是新手标
                Date tempDate = DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20);
                releaseDate = DateHelper.max(tempDate, releaseDate);
            }

            //触发自动投标队列

        } while (false);
        return bool;
    }

    /**
     * 转让标初审
     * @param borrowId
     * @return
     * @throws Exception
     */
    private boolean lendBorrow(Long borrowId) throws Exception{
        do {
            Date nowDate = DateHelper.subSeconds(new Date(), 10);

            Borrow borrow = borrowService.findByIdLock(borrowId);
            if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
                break;
            }

            //更新借款状态
            borrow.setStatus(1);
            borrow.setVerifyAt(nowDate);
            Date releaseAt = borrow.getReleaseAt();
            if (ObjectUtils.isEmpty(releaseAt)) {
                borrow.setReleaseAt(nowDate);

            }
            borrowService.updateById(borrow);

            Long lendId = borrow.getLendId();
            if (!ObjectUtils.isEmpty(lendId)) {
                Lend lend = lendService.findById(lendId);
                VoCreateTenderReq voCreateTenderReq = new VoCreateTenderReq();
                voCreateTenderReq.setUserId(lend.getUserId());
                voCreateTenderReq.setBorrowId(borrowId);
                voCreateTenderReq.setTenderMoney(borrow.getMoney());
                Map<String, Object> rsMap = tenderBiz.createTender(voCreateTenderReq);

                Object msg = rsMap.get("msg");
                if (ObjectUtils.isEmpty(msg)){
                    log.error(StringHelper.toString(msg));
                }
            }
        } while (false);
        return false;
    }

    private boolean miaoBorrow() {
        return false;
    }

    public boolean doAgainVerify(Long borrowId) {
        return false;
    }
}
