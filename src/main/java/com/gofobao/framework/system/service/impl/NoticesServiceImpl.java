package com.gofobao.framework.system.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.repository.NoticesRepository;
import com.gofobao.framework.system.service.NoticesService;
import com.gofobao.framework.system.vo.request.VoNoticesReq;
import com.gofobao.framework.system.vo.response.NoticesInfo;
import com.gofobao.framework.system.vo.response.UserNotices;
import com.gofobao.framework.system.vo.response.VoViewUserNoticesWarpRes;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Max on 17/6/5.
 */
@Component
public class NoticesServiceImpl implements NoticesService {
    @Autowired
    private NoticesRepository noticesRepository;
    @Override
    public void save(Notices notices) {
        noticesRepository.save(notices);
    }

    @Override
    public List<UserNotices> list(VoNoticesReq voNoticesReq) {
        Specification specification = Specifications.<Notices>and()
                .eq("userId", voNoticesReq.getUserId())
                .build();

       // specification.toPredicate()
        Page<Notices> noticesPage=noticesRepository.findAll(specification,
                new PageRequest(voNoticesReq.getPageIndex(),
                        voNoticesReq.getPageSize(),
                        new Sort(Sort.Direction.DESC, "id")));
        List<Notices> noticesList=noticesPage.getContent();

       Integer pageCount= noticesPage.getTotalPages();
        if(CollectionUtils.isEmpty(noticesList)){
            return Collections.EMPTY_LIST;
        }
        List<UserNotices> userNotices=new ArrayList<>(voNoticesReq.getPageSize());
        final int[] num = {0};
        noticesList.stream().forEach(p->{
            UserNotices userNotice=new UserNotices();
            userNotice.setTitle(p.getName());
            userNotice.setId(p.getId());
            userNotice.setTime(DateHelper.dateToString(p.getCreatedAt()));
            if(num[0] ==0){
                userNotice.setPageCount(pageCount);
                num[0] =2;
            }
            userNotices.add(userNotice);

        });
        return userNotices;
    }

   @Override
    public List<VoViewUserNoticesWarpRes> pcList(VoNoticesReq voNoticesReq) {
       /* try {
             VoViewUserNoticesWarpRes warpRes= VoBaseResp.ok("查询成功",VoViewUserNoticesWarpRes.class);
            Specification specification = Specifications.<Notices>and()
                    .eq("userId", voNoticesReq.getUserId())
                    .build();
            Page<Notices> noticesPage = noticesRepository.findAll(specification,
                    new PageRequest(voNoticesReq.getPageIndex(),
                            voNoticesReq.getPageSize(),
                            new Sort(Sort.Direction.DESC, "id")));
            int totalPages=noticesPage.getTotalPages();
            List<Notices> notices=noticesPage.getContent();
            List<UserNotices> userNotices=new ArrayList<>(voNoticesReq.getPageSize());
            notices.stream().forEach(p->{
                UserNotices userNotice=new UserNotices();
                userNotice.setTitle(p.getName());
                userNotice.setId(p.getId());
                userNotice.setTime(DateHelper.dateToString(p.getCreatedAt()));
                userNotices.add(userNotice);
            });
            warpRes.setPageCount(totalPages);
            warpRes.setNotices(userNotices);
        }catch (Exception e){
            return  new ResponseEntity

        }*/

        return null;
    }

    @Override
    public NoticesInfo info(VoNoticesReq voNoticesReq) {
        Specification specification=Specifications.<Notices>and()
                .eq("userId",voNoticesReq.getUserId())
                .eq("id",voNoticesReq.getId())
                .build();
        Notices notices=noticesRepository.findOne(specification);
        NoticesInfo noticesInfo=new NoticesInfo();
        if(ObjectUtils.isEmpty(notices)){
            return noticesInfo;
        }
        noticesInfo.setTitle(notices.getName());
        String content= notices.getContent();
        if(content.contains("[")){
            content= content.replace("[","");
        }if(content.contains("[")){
            content= content.replace("[","");
        }
        noticesInfo.setContent(content);
        noticesInfo.setCreateTime(DateHelper.dateToString(notices.getCreatedAt()));
        return noticesInfo;
    }
}
