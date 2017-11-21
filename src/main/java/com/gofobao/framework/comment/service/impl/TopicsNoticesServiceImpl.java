package com.gofobao.framework.comment.service.impl;

import com.gofobao.framework.comment.entity.TopicsNotices;
import com.gofobao.framework.comment.repository.TopicsNoticesRepository;
import com.gofobao.framework.comment.service.TopicsNoticesService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class TopicsNoticesServiceImpl implements TopicsNoticesService {
    @Autowired
    TopicsNoticesRepository topicsNoticesRepository;

    @Override
    public List<TopicsNotices> findAll(Specification<TopicsNotices> topicsReplyNoticesSpecification) {
        List<TopicsNotices> all = topicsNoticesRepository.findAll(topicsReplyNoticesSpecification);
        Optional<List<TopicsNotices>> optional = Optional.ofNullable(all);
        return optional.orElse(Lists.newArrayList());
    }

    @Override
    public Long count(Specification<TopicsNotices> topicsReplyNoticesSpecification) {
        return topicsNoticesRepository.count(topicsReplyNoticesSpecification);
    }

    @Override
    @Transactional
    public void batchUpdateRedundancy(Long userId, String username, String avatar) throws Exception {
        if (StringUtils.isEmpty(username) && StringUtils.isEmpty(avatar)) {
            throw new Exception("参数错误!");
        }
        if (!StringUtils.isEmpty(username)) {
            topicsNoticesRepository.batchUpateUsernameByForUserId(userId, username);
        } else {
            topicsNoticesRepository.batchUpateAvatarByForUserId(userId, avatar);
        }
    }

    @Override
    public TopicsNotices save(TopicsNotices notices) {
        return topicsNoticesRepository.save(notices);
    }

}
