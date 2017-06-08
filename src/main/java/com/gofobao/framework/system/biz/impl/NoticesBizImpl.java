package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.system.biz.NoticesBiz;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.service.NoticesService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * Created by Max on 17/6/5.
 */
@Component
@Slf4j
public class NoticesBizImpl implements NoticesBiz {

    @Autowired
    NoticesService noticesService ;

    @Autowired
    UserService userService ;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Notices notices) {
        Preconditions.checkNotNull(notices, "NoticesBizImpl.save: notices is empty") ;
        Preconditions.checkNotNull(notices.getUserId(), "NoticesBizImpl.save: userId is empty") ;

        Users users = userService.findByIdLock(notices.getUserId());
        if(ObjectUtils.isEmpty(users)){
            log.error("NoticesBizImpl.save: userId find null" );
            return false ;
        }

        Date now = new Date();
        if(ObjectUtils.isEmpty(notices.getCreatedAt())){
            notices.setCreatedAt(now);
        }

        if(ObjectUtils.isEmpty(notices.getUpdatedAt())){
            notices.setUpdatedAt(now);
        }

        noticesService.save(notices) ;
        Integer noticeCount = users.getNoticeCount();
        noticeCount = noticeCount <= 0 ? 0 : noticeCount ;
        users.setNoticeCount(noticeCount + 1);
        users.setUpdatedAt(now);
        userService.updUserById(users);
        return true ;
    }
}
