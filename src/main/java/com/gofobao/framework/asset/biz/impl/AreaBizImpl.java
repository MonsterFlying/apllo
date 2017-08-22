package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.AreaBiz;
import com.gofobao.framework.asset.entity.Area;
import com.gofobao.framework.asset.service.AreaService;
import com.gofobao.framework.asset.vo.response.pc.AreaRes;
import com.gofobao.framework.asset.vo.response.pc.VoAreaWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.RedisHelper;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/8/19.
 */

@Service
@Slf4j
public class AreaBizImpl implements AreaBiz {

    @Autowired
    private AreaService areaService;
    @Autowired
    private RedisHelper redisHelper;

    /**
     * 获取地区
     *
     * @param id
     * @return
     */
    @Override
    public ResponseEntity<VoAreaWarpRes> list(Integer id) {

        id = StringUtils.isEmpty(id) ? 0 : id;

        VoAreaWarpRes warpRes = VoBaseResp.ok("查询成功", VoAreaWarpRes.class);
        try {
            List<AreaRes> areaResList;
            //从redis中获取地区缓存
            String area = redisHelper.get("area", null);
            if (!StringUtils.isEmpty(area)) {
                areaResList = new Gson().fromJson(area, new TypeToken<List<AreaRes>>() {
                }.getType());
                Integer finalId = id;
                List<AreaRes>  areaRes = areaResList.stream().
                        filter(p -> p.getPid().intValue() == finalId.intValue())
                        .collect(Collectors.toList());
                warpRes.setAreaRes(areaRes);
                return ResponseEntity.ok(warpRes);
            }
        } catch (Exception e) {
            log.error("获取redis中地区异常");
        }
        List<Area> areas = areaService.findAll();
        if (CollectionUtils.isEmpty(areas)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法查询", VoAreaWarpRes.class));
        }
        List<AreaRes> areaResList = Lists.newArrayList();
        //装配结果集
        areas.forEach(p -> {
            AreaRes item = new AreaRes();
            item.setId(p.getId());
            item.setAreaName(p.getName());
            item.setPid(p.getPid());
            areaResList.add(item);
        });

        //将结果存放redis中
        String areaJson = new Gson().toJson(areas);
        try {
            redisHelper.put("area", areaJson);
        } catch (Exception e) {
            log.error("地区添加到redis报错", areaJson);
        }

        Integer finalId1 = id;
/*        List<AreaRes> areaRes=areas.stream()
                .filter(w-> w.getPid().intValue()== finalId1.intValue())
                .collect(Collectors.toList());
        warpRes.setAreaRes(areas);*/
        return ResponseEntity.ok(warpRes);
    }
}
