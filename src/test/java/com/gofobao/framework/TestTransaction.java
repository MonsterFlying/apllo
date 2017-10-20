package com.gofobao.framework;

import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.NewAssetLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Zeke on 2017/10/20.
 */
@Component
public class TestTransaction {
    @Autowired
    NewAssetLogService newAssetLogService;

    @Transactional(rollbackFor = Exception.class)
    public void test() throws Exception {
        NewAssetLog assetLog = new NewAssetLog();
        assetLog.setRemark("测试回滚!");
        newAssetLogService.save(assetLog);
    }
}
