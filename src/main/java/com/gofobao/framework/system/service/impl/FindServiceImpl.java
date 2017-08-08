package com.gofobao.framework.system.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.system.entity.Find;
import com.gofobao.framework.system.repository.FindRepository;
import com.gofobao.framework.system.service.FindService;
import com.gofobao.framework.system.vo.response.FindIndexItem;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class FindServiceImpl implements FindService {

    @Autowired
    FindRepository findRepository;

    LoadingCache<String, List<FindIndexItem>> itemCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, List<FindIndexItem>>() {
                @Override
                public List<FindIndexItem> load(String s) throws Exception {
                    Specification<Find> specification = Specifications.<Find>and()
                            .eq("status", "1")
                            .build();
                    List<Find> resule = findRepository.findAll(specification,
                            new Sort(new Sort.Order(Sort.Direction.DESC, "order"),
                                    new Sort.Order(Sort.Direction.ASC, "id")));

                    if (CollectionUtils.isEmpty(resule)) {
                        return Lists.newArrayList();
                    } else {
                        List<FindIndexItem> findIndexItems = new ArrayList<>();

                        resule.forEach(item -> {
                            FindIndexItem indexItem = new FindIndexItem();
                            findIndexItems.add(indexItem);
                            indexItem.setIconUrl(item.getIcon());
                            indexItem.setTitel(item.getTitle());
                            indexItem.setTargetUrl(item.getUrl());
                        });
                        return findIndexItems;
                    }
                }
            });


    @Override
    public List<FindIndexItem> findIndex() {
        try {
            return itemCache.get("item");
        } catch (ExecutionException e) {
            return Lists.newArrayList();
        }
    }
}

