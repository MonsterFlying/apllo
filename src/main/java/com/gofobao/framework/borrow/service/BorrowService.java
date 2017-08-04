package com.gofobao.framework.borrow.service;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.response.BorrowStatistics;
import com.gofobao.framework.borrow.vo.response.VoBorrowDescRes;
import com.gofobao.framework.borrow.vo.response.VoPcBorrowList;
import com.gofobao.framework.borrow.vo.response.VoViewBorrowList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/5/17.
 */
public interface BorrowService {

    List<VoViewBorrowList> findNormalBorrow(VoBorrowListReq voBorrowListReq);

    VoPcBorrowList pcFindAll(VoBorrowListReq voBorrowListReq);

    Borrow findByBorrowId(Long borrowId);

    long countByUserIdAndStatusIn(Long userId, List<Integer> statusList);

    boolean insert(Borrow borrow);

    boolean updateById(Borrow borrow);

    Borrow findByIdLock(Long borrowId);

    Borrow findById(Long borrowId);

    public Borrow save(Borrow borrow);

    public List<Borrow> save(List<Borrow> borrowList);

    /**
     * 检查是否招标中
     *
     * @param borrow
     * @return
     */
    boolean checkBidding(Borrow borrow);

    /**
     * 检查是否在发布时间内
     *
     * @param borrow
     * @return
     */
    boolean checkReleaseAt(Borrow borrow);

    /**
     * 检查招标时间是否有效
     *
     * @param borrow
     * @return
     */
    boolean checkValidDay(Borrow borrow);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<Borrow> findList(Specification<Borrow> specification);

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<Borrow> findList(Specification<Borrow> specification, Sort sort);


    /**
     * 招标统计
     *
     * @param
     * @return
     */
    List<BorrowStatistics> statistics();

    /**
     * 查询列表
     *
     * @param specification
     * @return
     */
    List<Borrow> findList(Specification<Borrow> specification, Pageable pageable);

    long count(Specification<Borrow> specification);


    VoBorrowDescRes desc(Long borrowId);

    Map<String, Object> contract(Long borrowId, Long userId);


    Map<String, Object> pcContract(Long borrowId, Long userId);

    Borrow getLastBorrowLock();

    Borrow flushSave(Borrow borrow);

    /**
     * 查找新手标
     * @return
     */
    Borrow findNoviceBorrow();

}
