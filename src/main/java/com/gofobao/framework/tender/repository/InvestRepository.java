package com.gofobao.framework.tender.repository;
import com.gofobao.framework.tender.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
/**
 * Created by admin on 2017/6/1.
 */
public interface InvestRepository extends JpaRepository<Tender,Long> ,JpaSpecificationExecutor <Tender> {


}
