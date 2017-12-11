package com.gofobao.framework.contract.service;

import com.gofobao.framework.api.model.contract_auth_realname.RealNameAuthRequest;
import com.gofobao.framework.api.model.contract_auth_realname.RealNameAuthResponse;
import com.gofobao.framework.api.model.contract_bind_template.DebtTemplateRequest;
import com.gofobao.framework.api.model.contract_bind_template.DebtTemplateResponse;
import com.gofobao.framework.api.model.contract_debttemplate_query.TemplateQueryRequest;
import com.gofobao.framework.api.model.contract_debttemplate_query.TemplateQueryResponse;
import com.gofobao.framework.api.model.contract_details.ContractDetailsRequest;
import com.gofobao.framework.api.model.contract_details.ContractDetailsResponse;
import com.gofobao.framework.api.model.contract_entrust_enter.EntrustEnterRequest;
import com.gofobao.framework.api.model.contract_entrust_enter.EntrustEnterResponse;
import com.gofobao.framework.api.model.contract_entrust_protocol.EntrustProtocolRequest;
import com.gofobao.framework.api.model.contract_entrust_protocol.EntrustProtocolResponse;
import com.gofobao.framework.api.model.contract_get_contracts.ContractIdsRequest;
import com.gofobao.framework.api.model.contract_get_contracts.ContractIdsResponse;
import com.gofobao.framework.api.model.contract_sms.SendSmsResponse;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.contract.entity.BorrowContract;
import com.gofobao.framework.contract.vo.request.ContractBorrowIds;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * @author master
 * @date 2017/11/14
 */
public interface ContractService {


    /**
     * 发送短信获取授权码
     *
     * @param phone
     * @param srvTxCode
     * @return
     */
    SendSmsResponse sendSms(String phone, String srvTxCode);

    /**
     * 申请签署委托协议
     *
     * @param entrustProtocolRequest
     * @return
     */
    EntrustProtocolResponse applyForSignAuth(EntrustProtocolRequest entrustProtocolRequest);

    /**
     * 确认签署委托协议
     *
     * @param entrustEnterRequest
     * @return
     */
    EntrustEnterResponse entrustEnter(EntrustEnterRequest entrustEnterRequest);


    /**
     * 用户实名
     *
     * @param realNameAuthRequest
     * @return
     */
    RealNameAuthResponse realNameAuth(RealNameAuthRequest realNameAuthRequest);

    /**
     * 3.3标的号绑定合同模板查询接口
     *
     * @param queryRequest
     * @return
     */
    TemplateQueryResponse templateQuery(TemplateQueryRequest queryRequest);


    /**
     * 标的号绑定合同模板
     *
     * @param debtTemplateRequest
     * @return
     */
    DebtTemplateResponse debtTemplate(DebtTemplateRequest debtTemplateRequest);

    /**
     * 3.5获取合同ID列表
     *
     * @param contractIdsRequest
     * @return
     */
    ContractIdsResponse contractIds(ContractIdsRequest contractIdsRequest);

    /**
     * 查看合同内容
     *
     * @param contractDetailsRequest
     * @return
     */
    ContractDetailsResponse contractDetails(ContractDetailsRequest contractDetailsRequest);

    /**
     * @param borrowContracts
     * @return
     */
    List<BorrowContract> bitchSave(List<BorrowContract> borrowContracts);


    List<BorrowContract> findByBorrowId(Long borrowId, String bitchNo, Boolean status);

    /**
     * @param borrowId
     */
    void updateContractStatus(Long borrowId, String batchNo);

    /**
     * @param contractBorrowIds
     * @return
     */
    List<BorrowContract> findUserContracts(ContractBorrowIds contractBorrowIds,Page page);

    /**
     *
     * @param borrowContractSpecification
     * @return
     */
    List<BorrowContract> findList(Specification<BorrowContract> borrowContractSpecification);


}
