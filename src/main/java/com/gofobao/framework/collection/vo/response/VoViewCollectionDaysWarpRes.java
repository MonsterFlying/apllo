package com.gofobao.framework.collection.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Data
public class VoViewCollectionDaysWarpRes extends VoBaseResp{

    List<Integer> warpRes= Lists.newArrayList();
}
