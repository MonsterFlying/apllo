package com.gofobao.framework.api.contants;

import com.gofobao.framework.member.entity.UserThirdAccount;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/17.
 */
public class IdTypeContant {
    public static final String ID_CARD = "01";//01-身份证（18位）

    public static String getIdTypeContant(UserThirdAccount userThirdAccount) {
        try {
            Integer idType = userThirdAccount.getIdType();
            if (ObjectUtils.isEmpty(idType)) {
                idType = 1;
            }

            String idTypeStr = idType.toString();
            if (idTypeStr.length() == 1) {
                idTypeStr = "0" + idTypeStr;
            }
            return idTypeStr;
        } catch (Exception e) {
            return ID_CARD;
        }
    }
}
