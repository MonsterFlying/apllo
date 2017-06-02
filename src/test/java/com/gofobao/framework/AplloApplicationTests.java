package com.gofobao.framework;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.tender.repository.AutoTenderRepository;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AplloApplicationTests {

    @Autowired
    private AutoTenderRepository autoTenderRepository;
    @Autowired
    private AutoTenderService autoTenderService;


    @Autowired
    private AssetLogService assetLogService;

    @Autowired
    private AssetLogRepository assetLogRepository;


    @Autowired
    private BorrowCollectionService borrowCollectionService;


    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Test

    public void tt(){


        VoCollectionOrderReq voCollectionOrderReq=new VoCollectionOrderReq();
        voCollectionOrderReq.setUserId(901L);
        voCollectionOrderReq.setTime("2017-05-16 00:00:00");



        VoViewCollectionOrderListRes orderListRes=  orderList(voCollectionOrderReq);
        orderListRes.getOrderResList().stream().forEach(w->{
            System.out.println(w.getBorrowName());
        });

    }


    public VoViewCollectionOrderListRes orderList(VoCollectionOrderReq voCollectionOrderReq) {
        Date date = DateHelper.stringToDate(voCollectionOrderReq.getTime());

        Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("startAt",new Range<>(date,DateHelper.addDays(date,1)))
                .eq("transferFlag", BorrowCollectionContants.TRANSFER_FLAG_NO)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findAll(specification);
        if(CollectionUtils.isEmpty(borrowCollections)){
            return null;
        }
        Set borrowIdSet = borrowCollections.stream()
                .map(f -> f.getBorrowId())
                .collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(Lists.newArrayList(borrowIdSet));
        Map<Long, Borrow> borrowMap = borrowList
                .stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        VoViewCollectionOrderListRes voViewCollectionOrderListRes = new VoViewCollectionOrderListRes();

        List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

        borrowCollections.stream().forEach(p -> {
            VoViewCollectionOrderRes item = new VoViewCollectionOrderRes();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            item.setBorrowName(borrow.getName());
            item.setOrder(p.getOrder() + 1);
            item.setTimeLime(borrow.getTimeLimit());
            item.setCollectionMoney(NumberHelper.to2DigitString(p.getCollectionMoney() / 100));
            item.setCollectionMoneyYes(NumberHelper.to2DigitString(p.getCollectionMoneyYes() / 100));
            orderResList.add(item);
        });
        //回款列表
        voViewCollectionOrderListRes.setOrderResList(orderResList);
        //总回款期数
        voViewCollectionOrderListRes.setOrder(orderResList.size());
        //已回款金额
        Integer sumCollectionMoneyYes = borrowCollections.stream()
                .filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_YES)
                .mapToInt(w -> w.getCollectionMoneyYes())
                .sum();
        voViewCollectionOrderListRes.setSumCollectionMoneyYes(NumberHelper.to2DigitString(sumCollectionMoneyYes / 100));
        Optional<VoViewCollectionOrderListRes> orderListRes = Optional.ofNullable(voViewCollectionOrderListRes);
        return orderListRes.orElse(null);
    }
}
