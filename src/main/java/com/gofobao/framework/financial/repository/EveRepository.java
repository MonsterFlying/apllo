package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.Eve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EveRepository extends JpaRepository<Eve, Long> , JpaSpecificationExecutor<Eve>{
    List<Eve> findByRetseqnoAndSeqno(String retseqno, String seqno);
}
