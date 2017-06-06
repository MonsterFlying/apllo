package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.member.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class BankAccountBizImpl implements BankAccountBiz{

    @Autowired
    private BankAccountService bankAccountService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserCacheService userCacheService;


}
