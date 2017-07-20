package com.gofobao.framework.helper;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

public class ThirdAccountHelper {

    /**
     * 用户教研
     *
     * @param userThirdAccount
     * @return
     */
    public static ResponseEntity<VoBaseResp> conditionCheck(UserThirdAccount userThirdAccount) {
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getAutoTransferState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动债权转让协议！", VoAutoTenderInfo.class));
        }


        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动投标协议！", VoAutoTenderInfo.class));
        }

        return ResponseEntity.ok(VoBaseResp.ok("搜索成功")) ;
    }
}
