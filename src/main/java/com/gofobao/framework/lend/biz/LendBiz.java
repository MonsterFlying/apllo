package com.gofobao.framework.lend.biz;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.lend.vo.request.*;
import com.gofobao.framework.lend.vo.response.*;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/6.
 */
public interface LendBiz {

    ResponseEntity<VoViewLendListWarpRes> list(Page page,Long userId);


    ResponseEntity<VoViewLendInfoWarpRes> info(Long userId, Long lendId);

    ResponseEntity<VoViewUserLendInfoWarpRes> byUserId(VoUserLendReq voUserLendReq);

    /**
     * 发布有草出借
     *
     * @param voCreateLend
     * @return
     */
    ResponseEntity<VoBaseResp> create(VoCreateLend voCreateLend);

    /**
     * 结束有草出借
     *
     * @param voEndLend
     * @return
     */
    ResponseEntity<VoBaseResp> end(VoEndLend voEndLend);

    /**
     * 有草出借摘草
     *
     * @param voLend
     * @return
     */
    ResponseEntity<VoBaseResp> lend(VoLend voLend);


    /**
     *
     * @param userId
     * @param lendId
     * @return
     */
    ResponseEntity<VoViewLendInfoListWarpRes> infoList(Long userId, Long lendId);


    /**
     * 获取当前用户黑名单列表
     *
     * @param voGetLendBlacklists
     * @return
     * @throws Exception
     */
    ResponseEntity<VoViewLendBlacklists> getLendBlacklists(VoGetLendBlacklists voGetLendBlacklists);

    /**
     * 添加有草出借黑名单
     *
     * @param voAddLendBlacklist
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> addLendBlacklist(VoAddLendBlacklist voAddLendBlacklist);

    /**
     * 移除有草出借黑名单
     *
     * @param voDelLendBlacklist
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> delLendBlacklist(VoDelLendBlacklist voDelLendBlacklist);

    /**
     * 获取有草出借借款列表
     *
     * @param voGetPickLendList
     * @return
     * @throws Exception
     */
    ResponseEntity<VoViewPickLendList> getPickLendList(VoGetPickLendList voGetPickLendList);





}
