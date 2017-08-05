package com.gofobao.framework.windmill.user.vo.respones;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/8/5.
 */
@Data
public class VoNoticesRes  {

    private List<Notices> all_notices = Lists.newArrayList();

    private String retmsg;

    private Long retcode;

}
