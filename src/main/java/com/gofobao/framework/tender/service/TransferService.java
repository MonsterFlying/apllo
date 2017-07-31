package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;


/**
 * Created by admin on 2017/6/12.
 */

public interface TransferService {

    List<Transfer> findList(Specification<Transfer> specification);

    List<Transfer> findList(Specification<Transfer> specification, Sort sort);

    List<Transfer> findList(Specification<Transfer> specification, Pageable pageable);

    long count(Specification<Transfer> specification);

    Transfer findById(long id);

    Map<String, Object> transferOfList(VoTransferReq voTransferReq);


    Map<String, Object> transferedList(VoTransferReq voTransferReq);

    Map<String, Object> transferMayList(VoTransferReq voTransferReq);

    Map<String, Object> transferBuyList(VoTransferReq voTransferReq);

}
