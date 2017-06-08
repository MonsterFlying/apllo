package com.gofobao.framework.award.service.impl;

import com.gofobao.framework.award.contants.RedPacketContants;
import com.gofobao.framework.award.entity.ActivityRedPacket;
import com.gofobao.framework.award.repository.RedPackageRepository;
import com.gofobao.framework.award.service.RedPackageService;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Component
public class RedPackageServiceImpl implements RedPackageService {
    @Autowired
    private RedPackageRepository redPackageRepository;

    @Override
    public List<RedPackageRes> list(VoRedPackageReq voRedPackageReq) {
        Page<ActivityRedPacket> packetPage;

        if (RedPacketContants.unUsed == voRedPackageReq.getStatus()) {//未使用
            packetPage = redPackageRepository.findByUserIdAndStatusIsAndEndAtGreaterThanEqual(voRedPackageReq.getUserId(),
                    voRedPackageReq.getStatus(),
                    new Date(),
                    new PageRequest(voRedPackageReq.getPageIndex(),
                            voRedPackageReq.getPageSize(),
                            new Sort(Sort.Direction.DESC, "id")
                    ));
        } else if (RedPacketContants.used == voRedPackageReq.getStatus()) { //已领取
            packetPage = redPackageRepository.findByUserIdAndStatusIs(voRedPackageReq.getUserId(),
                    voRedPackageReq.getStatus(),
                    new PageRequest(voRedPackageReq.getPageIndex(),
                            voRedPackageReq.getPageSize(),
                            new Sort(Sort.Direction.DESC, "updateDate")
                    ));
        } else if (RedPacketContants.Past == voRedPackageReq.getStatus()) { //已过期
            packetPage = redPackageRepository.findByUserIdAndStatusIsOrEndAtLessThanEqual(voRedPackageReq.getUserId(),
                    voRedPackageReq.getStatus(),
                    new Date(),
                    new PageRequest(voRedPackageReq.getPageIndex(),
                            voRedPackageReq.getPageSize(),
                            new Sort(Sort.Direction.DESC, "id")
                    ));
        } else {
            return Collections.EMPTY_LIST;
        }
        List<ActivityRedPacket> packetList = packetPage.getContent();
        if (CollectionUtils.isEmpty(packetList)) {
            return Collections.EMPTY_LIST;
        }

        List<RedPackageRes> redPackageRes = Lists.newArrayList();
        packetList.stream().forEach(p -> {
            RedPackageRes packageRes = new RedPackageRes();
            if (voRedPackageReq.getStatus() == RedPacketContants.unUsed) {
                packageRes.setRedPackageId(p.getId());
            }
            if (voRedPackageReq.getStatus() == RedPacketContants.used) {
                packageRes.setExpiryDate(DateHelper.dateToString(p.getUpdateDate()));
                packageRes.setMoney(StringHelper.toString(p.getMoney() / 100D));
            } else {
                packageRes.setExpiryDate(DateHelper.dateToString(p.getBeginAt()) + "~" + DateHelper.dateToString(p.getBeginAt()));
            }
            packageRes.setTitle(p.getActivityName());
            redPackageRes.add(packageRes);
        });
        return redPackageRes;
    }

}
