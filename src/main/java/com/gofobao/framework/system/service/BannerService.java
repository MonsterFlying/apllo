package com.gofobao.framework.system.service;

import com.gofobao.framework.system.vo.response.IndexBanner;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by admin on 2017/6/14.
 */
public interface BannerService {

    List<IndexBanner> index(String terminal);
}
