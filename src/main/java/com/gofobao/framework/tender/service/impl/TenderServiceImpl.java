package com.gofobao.framework.tender.service.impl;
import com.gofobao.framework.borrow.vo.request.VoBorrowByIdReq;
import com.gofobao.framework.borrow.vo.response.VoBorrowTenderUserRes;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.UserHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.tender.service.TenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by admin on 2017/5/19.
 */
@Service
@Slf4j
public class TenderServiceImpl implements TenderService {

    @Autowired
    private TenderRepository tenderRepository;


    @Autowired
    private UsersRepository usersRepository;

    /**
     * 投标用户列表
     * @param req
     * @return
     */
    @Override
    public List<VoBorrowTenderUserRes> findBorrowTenderUser(VoBorrowByIdReq req) {
        List<VoBorrowTenderUserRes> tenderUserResList = new ArrayList<>();
        Tender tender = new Tender();
        tender.setBorrowId(req.getBorrowId());
        tender.setStatus(TenderConstans.SUCCESS);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("isAuto");
        Example<Tender> ex = Example.of(tender, matcher);
        List<Tender> tenderList = tenderRepository.findAll(ex);
        Optional<List<Tender>> listOptional = Optional.ofNullable(tenderList);

        listOptional.ifPresent(items -> items.forEach(item -> {
            VoBorrowTenderUserRes tenderUserRes = new VoBorrowTenderUserRes();
            tenderUserRes.setMoney(NumberHelper.to2DigitString(item.getValidMoney() / 100d) + MoneyConstans.RMB);
            tenderUserRes.setDate(DateHelper.dateToString(item.getCreatedAt(), DateHelper.DATE_FORMAT_YMDHMS));
            tenderUserRes.setType(item.getIsAuto() == 0 ? TenderConstans.MANUAL : TenderConstans.AUTO);
            Users user=usersRepository.findOne(new Long(item.getUserId()));
            String userName=StringUtils.isEmpty(user.getUsername())?
                    UserHelper.hideChar(user.getPhone(),UserHelper.PHONE_NUM):
                    UserHelper.hideChar(user.getUsername(),UserHelper.USERNAME_NUM);
            tenderUserRes.setUserName(userName);
            tenderUserResList.add(tenderUserRes);
        }));
        return  Optional.empty().ofNullable(tenderUserResList).orElse(Collections.emptyList());
    }

    public boolean insert(Tender tender){
        if (ObjectUtils.isEmpty(tender)){
            return false;
        }
        tender.setId(null);
        return !ObjectUtils.isEmpty(tenderRepository.save(tender));
    }

    public boolean updateById(Tender tender){
        if (ObjectUtils.isEmpty(tender) || ObjectUtils.isEmpty(tender.getId())){
            return false;
        }
        return !ObjectUtils.isEmpty(tenderRepository.save(tender));
    }
}
