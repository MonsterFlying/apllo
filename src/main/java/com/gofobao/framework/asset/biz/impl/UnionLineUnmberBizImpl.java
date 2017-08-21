package com.gofobao.framework.asset.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.biz.UnionLineNumberBiz;
import com.gofobao.framework.asset.entity.UnionLineNumber;
import com.gofobao.framework.asset.service.UnionLineNumberService;
import com.gofobao.framework.asset.vo.request.VoUnionLineNoReq;
import com.gofobao.framework.asset.vo.response.pc.UnionLineNo;
import com.gofobao.framework.asset.vo.response.pc.UnionLineNoWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by admin on 2017/8/21.
 */
@Service
public class UnionLineUnmberBizImpl implements UnionLineNumberBiz {

    @Autowired
    private UnionLineNumberService unionLineNumberService;


    @Override
    public ResponseEntity<UnionLineNoWarpRes> list(VoUnionLineNoReq unionLineNoReq) {
        UnionLineNoWarpRes warpRes = VoBaseResp.ok("查询成功", UnionLineNoWarpRes.class);
        Specification<UnionLineNumber> specification = Specifications.<UnionLineNumber>and()
                .eq(!StringUtils.isEmpty(unionLineNoReq.getCityId()), "city", unionLineNoReq.getCityId())
                .eq(!StringUtils.isEmpty(unionLineNoReq.getProvinceId()), "province", unionLineNoReq.getProvinceId())
                .like(!StringUtils.isEmpty(unionLineNoReq.getKeyword()), "bankName", "%"+unionLineNoReq.getKeyword()+"%")
                .build();
        Page<UnionLineNumber> unionLineNumbers = unionLineNumberService.findAll(specification, new PageRequest(unionLineNoReq.getPageIndex(), unionLineNoReq.getPageSize(), new Sort(Sort.Direction.DESC, "id")));
        List<UnionLineNumber> lineNumbers = unionLineNumbers.getContent();
        if (CollectionUtils.isEmpty(lineNumbers)) {
            return ResponseEntity.ok(warpRes);
        }
        warpRes.setTotalCount(unionLineNumbers.getTotalElements());
        List<UnionLineNo> unionLineNos = Lists.newArrayList();
        lineNumbers.forEach(p -> {
            UnionLineNo unionLineNo = new UnionLineNo();
            unionLineNo.setAddress(p.getAddress());
            unionLineNo.setBankName(p.getBankName());
            unionLineNo.setNumber(p.getNumber());
            unionLineNo.setId(p.getId());
            unionLineNos.add(unionLineNo);
        });
        warpRes.setUnionLineNos(unionLineNos);
        return ResponseEntity.ok(warpRes);
    }
}
