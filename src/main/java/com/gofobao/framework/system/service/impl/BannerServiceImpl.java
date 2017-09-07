package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Banner;
import com.gofobao.framework.system.repository.BannerRepository;
import com.gofobao.framework.system.service.BannerService;
import com.gofobao.framework.system.vo.response.IndexBanner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2017/6/14.
 */
@Component
public class BannerServiceImpl implements BannerService {
    @Autowired
    private BannerRepository bannerRepository;

    @Value("${gofobao.imageDomain}")
    private String imageDomain;


    LoadingCache<String, List<IndexBanner>> bannerCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, List<IndexBanner>>() {
                @Override
                public List<IndexBanner> load(String s) throws Exception {
                    List<Banner> banners;
                    if ("pc".equals(s)) {  // pc
                        banners = bannerRepository.findByStatusAndTerminalOrderByIdDesc(new Byte("1"), 0);
                    } else {  // 移动端
                        banners = bannerRepository.findByStatusAndTerminalOrderByIdDesc(new Byte("1"), 1);
                    }
                    List<IndexBanner> bannerList = Lists.newArrayList();
                    banners.stream().forEach(p -> {
                        IndexBanner indexBanner = new IndexBanner();
                        indexBanner.setTitle(p.getTitle());
                        indexBanner.setImageUrl(p.getImgurl());
                        indexBanner.setClickUrl(p.getClickurl());
                        indexBanner.setMClickUrl(p.getMClickUrl());
                        bannerList.add(indexBanner);
                    });
                    return bannerList;
                }
            });


    @Override
    public List<IndexBanner> index(String terminal) {
        try {
            return bannerCache.get(terminal);
        } catch (ExecutionException e) {
            return Lists.newArrayList();
        }
    }


}
