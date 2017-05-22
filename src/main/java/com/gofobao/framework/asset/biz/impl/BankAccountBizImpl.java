package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BankAccount;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BankAccountService;
import com.gofobao.framework.asset.vo.request.VoUserBankListReq;
import com.gofobao.framework.asset.vo.response.VoUserBankListResp;
import com.gofobao.framework.asset.vo.response.VoUserBankResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MaskHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.system.contants.DictAliasCodeContants;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
