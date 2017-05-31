package com.gofobao.framework.tender.biz.impl;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
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
        if (ObjectUtils.isEmpty(borrow)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "你看到的借款消失啦!"));
        }

        Users users = userService.findByIdLock(userId);
        if (ObjectUtils.isEmpty(users)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "用户不存在!"));
        }

        if (users.getIsLock()){ //判断当前会员是否锁定
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已被冻结操作，请联系客服人员!"));
        }

        if (!userService.checkRealname(users)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已被冻结操作，请联系客服人员!"));
        }



        return ResponseEntity.ok(VoBaseResp.ok("投标成功!"));
    }
}
