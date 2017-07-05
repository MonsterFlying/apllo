package com.gofobao.framework.collection.vo.response.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class VoViewCollectionWarpRes  extends VoBaseResp{

    private List<Collection> collections= Lists.newArrayList();

    private Integer totalCount=0;
}
