package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.system.repository.DictItemRepository;
import com.gofobao.framework.system.repository.DictValueRepository;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 数据字典服务
 * Created by Max on 17/3/1.
 */
@Service
public class DictServiceImpl {

    @Autowired
    private DictItemRepository dictItemRepository;

    @Autowired
    private DictValueRepository dictValueRepository;


    @Autowired
    private RedisHelper redisHelper;

    public List<Map<String, String>> queryDictList(String aliasCode) throws Exception {
        if (StringUtils.isEmpty(aliasCode)) {
            throw new Exception("aliasCode is empty");
        }

      Gson gson = new Gson();
        /*
        String cacheStr = redisHelper.get(SysCacheContants.DICT_ALIAS_CODE + aliasCode, null);
        if (StringUtils.isEmpty(cacheStr)) {
            DictItemExample dictItemExample = new DictItemExample();
            dictItemExample.or().andIsDelEqualTo(0).andAliasCodeEqualTo(aliasCode);
            List<DictItem> dictItems = dictItemRepository.selectByExample(dictItemExample);

            if (CollectionUtils.isEmpty(dictItems)) {
                throw new Exception("aliasCode is invalidate");
            }

            DictItem dictItem = dictItems.get(0);
            Integer dictItemId = dictItem.getId();

            DictValueExample dictValueExample = new DictValueExample();
            dictValueExample.or().andIsDelEqualTo(0).andItemIdEqualTo(dictItemId);
            List<DictValue> dictValues = dictValueRepository.selectByExample(dictValueExample);

            if (CollectionUtils.isEmpty(dictValues)) {
                throw new Exception("aliasCode values is empty");
            }

            cacheStr = gson.toJson(dictValues);
            redisHelper.put(SysCacheContants.DICT_ALIAS_CODE + aliasCode, cacheStr);
        }*/


        List<Map<String, String>> maps = gson.fromJson("", new TypeToken<List<Map<String, String>>>() {
        }.getType());
        return maps;
    }
}
