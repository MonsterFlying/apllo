package com.gofobao.framework.contract.service.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.helper.ContractManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
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
import com.gofobao.framework.api.model.contract_sms.SendSmsRequest;
import com.gofobao.framework.api.model.contract_sms.SendSmsResponse;
import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.contract.entity.BorrowContract;
import com.gofobao.framework.contract.repository.BorrowContractRepository;
import com.gofobao.framework.contract.service.ContractService;
import com.gofobao.framework.contract.vo.request.ContractBorrowIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by master on 2017/11/14.
 */
@Component
public class ContractServiceImpl implements ContractService {


    @Autowired
    private ContractManager contractManager;

    @Autowired
    private BorrowContractRepository borrowContractRepository;

    /**
     * 3.4请求短信验证码接口
     *
     * @param phone
     * @param srvTxCode
     * @return
     */
    @Override
    public SendSmsResponse sendSms(String phone, String srvTxCode) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setMobile(phone);
        sendSmsRequest.setSrvTxCode(srvTxCode);
        sendSmsRequest.setChannel(ChannelContant.HTML);
        SendSmsResponse smsResponse = contractManager.send(JixinTxCodeEnum.CRTDATA_SENDSMS,
                sendSmsRequest,
                SendSmsResponse.class);
        return smsResponse;
    }

    /**
     * 3.1主动签署委托借款协议接口
     *
     * @param entrustProtocolRequest
     * @return
     */
    @Override
    public EntrustProtocolResponse applyForSignAuth(EntrustProtocolRequest entrustProtocolRequest) {
        EntrustProtocolResponse entrustProtocolResponse = contractManager.send(JixinTxCodeEnum.SIGN_CONTRACT,
                entrustProtocolRequest,
                EntrustProtocolResponse.class);
        return entrustProtocolResponse;
    }

    /**
     * @param entrustEnterRequest
     * @return
     */
    @Override
    public EntrustEnterResponse entrustEnter(EntrustEnterRequest entrustEnterRequest) {

        EntrustEnterResponse entrustProtocolResponse = contractManager.send(JixinTxCodeEnum.ENTER_ENTRUST,
                entrustEnterRequest,
                EntrustEnterResponse.class);
        return entrustProtocolResponse;
    }

    /**
     * 3.7实名认证
     *
     * @param realNameAuthRequest
     * @return
     */
    @Override
    public RealNameAuthResponse realNameAuth(RealNameAuthRequest realNameAuthRequest) {
        RealNameAuthResponse realNameAuthResponse = contractManager.send(JixinTxCodeEnum.REALNAME_AUTH,
                realNameAuthRequest,
                RealNameAuthResponse.class);
        return realNameAuthResponse;

    }

    /**
     * 3.3标的号绑定合同模板查询接口
     *
     * @param queryRequest
     * @return
     */
    @Override
    public TemplateQueryResponse templateQuery(TemplateQueryRequest queryRequest) {
        TemplateQueryResponse templateQueryResponse = contractManager.send(JixinTxCodeEnum.REALNAME_AUTH,
                queryRequest,
                TemplateQueryResponse.class);
        return templateQueryResponse;
    }

    /**
     * 3.3标的号绑定合同模板查询接口
     *
     * @param debtTemplateRequest
     * @return
     */
    @Override
    public DebtTemplateResponse debtTemplate(DebtTemplateRequest debtTemplateRequest) {
        DebtTemplateResponse debtTemplateResponse = contractManager.send(JixinTxCodeEnum.ADD_DEBTTEMPLATE,
                debtTemplateRequest,
                DebtTemplateResponse.class);
        return debtTemplateResponse;
    }

    /**
     * 3.6查看合同内容
     *
     * @param contractDetailsRequest
     * @return
     */
    @Override
    public ContractDetailsResponse contractDetails(ContractDetailsRequest contractDetailsRequest) {
        ContractDetailsResponse contractDetailsResponse = contractManager.send(JixinTxCodeEnum.CONTRACT_DETAILS,
                contractDetailsRequest,
                ContractDetailsResponse.class);
        return contractDetailsResponse;
    }

    /**
     * 3.5获取合同ID列表
     *
     * @param contractIdsRequest
     * @return
     */
    @Override
    public ContractIdsResponse contractIds(ContractIdsRequest contractIdsRequest) {
        ContractIdsResponse contractDetailsResponse = contractManager.send(JixinTxCodeEnum.FIND_CONTRACT_IDS,
                contractIdsRequest,
                ContractIdsResponse.class);
        return contractDetailsResponse;
    }


    @Transactional
    @Override
    public List<BorrowContract> bitchSave(List<BorrowContract> borrowContracts) {
        return borrowContractRepository.save(borrowContracts);
    }

    @Override
    public List<BorrowContract> findByBorrowId(Long borrowId, String batchNo, Boolean status) {
        return borrowContractRepository.findByBorrowIdAndBatchNoAndStatus(borrowId, batchNo, status);
    }


    @Override
    public void updateContractStatus(Long borrowId, String batchNo) {
        borrowContractRepository.updateContractStatus(borrowId, batchNo);
    }

    @Override
    public List<BorrowContract> findUserContracts(ContractBorrowIds contractBorrowIds, Page page) {
        return borrowContractRepository.findUserContracts(contractBorrowIds.getUserId(), contractBorrowIds.getType(),
                new PageRequest(page.getPageIndex(),page.getPageSize())
        );
    }

    @Override
    public List<BorrowContract> findList(Specification<BorrowContract> borrowContractSpecification) {
        return borrowContractRepository.findAll(borrowContractSpecification);
    }
}

