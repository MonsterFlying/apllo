package com.gofobao.framework.system.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.repository.NoticesRepository;
import com.gofobao.framework.system.service.NoticesService;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.request.VoNoticesTranReq;
import com.gofobao.framework.system.vo.response.NoticesInfo;
import com.gofobao.framework.system.vo.response.UserNotices;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Max on 17/6/5.
 */
@Component
public class NoticesServiceImpl implements NoticesService {
    @Autowired
    private NoticesRepository noticesRepository;

    @Override
    public Notices save(Notices notices) {
        return noticesRepository.save(notices);
    }

    @Override
    public List<UserNotices> list(VoNoticesReq voNoticesReq) {
        Specification specification = Specifications.<Notices>and()
                .eq("userId", voNoticesReq.getUserId())
                .eq("deletedAt", null)
                .build();
        Page<Notices> noticesPage = noticesRepository.findAll(specification,
                new PageRequest(voNoticesReq.getPageIndex(),
                        voNoticesReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));
        List<Notices> noticesList = noticesPage.getContent();

        Long pageCount = noticesPage.getTotalElements();
        if (CollectionUtils.isEmpty(noticesList)) {
            return Collections.EMPTY_LIST;
        }
        List<UserNotices> userNotices = new ArrayList<>(voNoticesReq.getPageSize());
        final int[] num = {0};
        noticesList.stream().forEach(p -> {
            UserNotices userNotice = new UserNotices();
            String title=p.getName();
            userNotice.setTitle( title.replaceAll("<[^>]+>", ""));
            userNotice.setStauts(p.getRead());
            userNotice.setId(p.getId());
            userNotice.setTime(DateHelper.dateToString(p.getCreatedAt()));
            if (num[0] == 0) {
                userNotice.setTotalCount(pageCount.intValue());
                num[0] = 2;
            }
            userNotices.add(userNotice);

        });
        return userNotices;
    }


    @Override
    public NoticesInfo info(VoNoticesReq voNoticesReq) {
        Specification specification = Specifications.<Notices>and()
                .eq("userId", voNoticesReq.getUserId())
                .eq("id", voNoticesReq.getId())
                .eq("deletedAt", null)
                .build();
        Notices notices = noticesRepository.findOne(specification);
        NoticesInfo noticesInfo = new NoticesInfo();
        if (ObjectUtils.isEmpty(notices)) {
            return noticesInfo;
        }
        noticesInfo.setTitle(notices.getName());

        String content = notices.getContent();
        if (StringUtils.isEmpty(content)) {
            content = noticesInfo.getTitle();
        } else {
            if (voNoticesReq.getType() == 0) {//移動端讀取
                if (content.contains("[")) {
                    StringBuffer stringBuffer = new StringBuffer(content);
                    content = content.replaceAll("<[^>]+>", "");
                }
            }
        }
        noticesInfo.setRead(notices.getRead());
        noticesInfo.setContent(content);
        noticesInfo.setCreateTime(DateHelper.dateToString(notices.getCreatedAt()));
        return noticesInfo;
    }

    /**
     * 未读消息数量
     *
     * @param userId
     * @return
     */
    @Override
    public Long unread(Long userId) {
        Specification specification = Specifications.<Notices>and()
                .eq("userId", userId)
                .eq("deletedAt", null)
                .eq("read", false)
                .build();
        return noticesRepository.count(specification);
    }

    @Transactional
    @Override
    public boolean delete(VoNoticesTranReq voNoticesTranReq) {
        Date now = new Date();
        return noticesRepository.delete(now,
                voNoticesTranReq.getUserId(),
                Lists.newArrayList(voNoticesTranReq.getNoticesIds())) <= 0 ? false : true;
    }

    @Transactional
    @Override
    public boolean update(VoNoticesTranReq voNoticesTranReq) {
        return noticesRepository.update(voNoticesTranReq.getUserId(),
                Lists.newArrayList(voNoticesTranReq.getNoticesIds())) <= 0 ? false : true;
    }
}
