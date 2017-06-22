package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Banner;
import com.gofobao.framework.system.repository.BannerRepository;
import com.gofobao.framework.system.service.BannerService;
import com.gofobao.framework.system.vo.response.IndexBanner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Component
public class BannerServiceImpl implements BannerService {
    @Autowired
    private BannerRepository bannerRepository;

    @Value("${gofobao.imageDomain}")
    private String imageDomain;

    @Override
    public List<IndexBanner> index() {
        Cache<String, List<IndexBanner>> cache2 = CacheBuilder.newBuilder().maximumSize(1000).build();
        List<IndexBanner> bannerList = Lists.newArrayList();
        try {
            cache2.get("banner", () -> {
                List<Banner> banners = bannerRepository.findByStatusAndTerminal(new Byte("1"), 1);
                banners.stream().forEach(p -> {
                    IndexBanner indexBanner = new IndexBanner();
                    indexBanner.setTitle(p.getTitle());
                    indexBanner.setImageUrl(imageDomain+p.getImgurl());
                    indexBanner.setClickUrl(p.getClickurl());
                    bannerList.add(indexBanner);
                });
                return bannerList;
            });
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
        return bannerList;
    }


}
