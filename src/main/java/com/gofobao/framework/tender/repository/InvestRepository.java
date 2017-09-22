package com.gofobao.framework.tender.repository;
import com.gofobao.framework.tender.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
/**
 * Created by admin on 2017/6/1.
 */
public interface InvestRepository extends JpaRepository<Tender,Long> ,JpaSpecificationExecutor <Tender> {

    /**
     * 根据tenderId和用户Id查询投标信息
     * @param tenderId
     * @param userId
     * @return
     */
    Tender findByIdAndUserId(Long tenderId,Long userId);

}
