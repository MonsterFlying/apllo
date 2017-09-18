package com.gofobao.framework.helper;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.UserThirdAccount;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

public class ThirdAccountHelper {

    /**
     * 校验存管全部条件
     * 1. 是否开户
     * 2. 密码是否初始化
     * 3. 是否签订自动投标协议
     * 4. 是否签订债权转让协议
     * 5. 银行卡是否绑定
     *
     * @param userThirdAccount
     * @return
     */
    public static ResponseEntity<VoBaseResp> allConditionCheck(UserThirdAccount userThirdAccount) {
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_OPEN_ACCOUNT, "你还没有开通江西银行存管，请前往开通！", VoBaseResp.class));
        }
        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoBaseResp.class));
        }

        if (userThirdAccount.getAutoTransferState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动债权转让协议！", VoBaseResp.class));
        }


        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动投标协议！", VoBaseResp.class));
        }

        return ResponseEntity.ok(VoBaseResp.ok("搜索成功")) ;
    }

}
