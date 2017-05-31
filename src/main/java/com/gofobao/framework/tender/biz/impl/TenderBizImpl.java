package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * Created by Zeke on 2017/5/31.
 */
public class TenderBizImpl implements TenderBiz {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private UserService userService;

    public ResponseEntity<VoBaseResp> createTender(VoCreateTenderReq voCreateTenderReq) {
        Long userId = voCreateTenderReq.getUserId();
        Long borrowId = voCreateTenderReq.getBorrowId();
        boolean isAutoTender = voCreateTenderReq.getIsAutoTender();
        Date nowDate = new Date();

        Borrow borrow = borrowService.findByIdLock(borrowId);//投标锁定借款
        Preconditions.checkNotNull(borrow,"你看到的借款消失啦!");

        Users users = userService.findByIdLock(userId);
        Preconditions.checkNotNull(users,"用户不存在!");

        if (users.getIsLock()){ //判断当前会员是否锁定
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已被冻结操作，请联系客服人员!"));
        }

        if (!userService.checkRealname(users)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户未实名认证!"));
        }

        if (!borrowService.checkBidding(borrow)){ //检查借款状态是否实在招标中
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前借款不在招标中!"));
        }

        if (!borrowService.checkReleaseAt(borrow)){ //检查借款是否到发布时间
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前借款未到招标时间!"));
        }

        if (!borrowService.checkValidDay(borrow)){ //检查是否是有效的招标时间
            /**
             * @// TODO: 2017/5/31 调用取消借款函数
             */
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前借款不在有效招标时间中!"));
        }


        return ResponseEntity.ok(VoBaseResp.ok("投标成功!"));
    }
}
