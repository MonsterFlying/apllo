package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by master on 2017/9/15.
 */
@Data
public class SmsNoticeListRes extends VoBaseResp {

    private List<SmsNoticeSettingsEntity> settingsEntities = Lists.newArrayList();

}
