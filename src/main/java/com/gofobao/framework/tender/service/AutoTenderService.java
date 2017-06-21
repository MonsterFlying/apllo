package com.gofobao.framework.tender.service;

import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.vo.VoFindAutoTenderList;
import com.gofobao.framework.tender.vo.response.UserAutoTender;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/27.
 */
public interface AutoTenderService {

    boolean insert(AutoTender autoTender);

    boolean updateById(AutoTender autoTender);

    boolean updateByExample(AutoTender autoTender, Example<AutoTender> example);

    List<Map<String, Object>> findQualifiedAutoTenders(VoFindAutoTenderList voFindAutoTenderList);

    boolean updateAutoTenderOrder();

    List<UserAutoTender> list(Long userId);

    List<AutoTender> findList(Specification<AutoTender> specification);

    List<AutoTender> findList(Specification<AutoTender> specification, Sort sort);

    List<AutoTender> findList(Specification<AutoTender> specification, Pageable pageable);

    long count(Specification<AutoTender> specification);

    void delete(long id);
}
