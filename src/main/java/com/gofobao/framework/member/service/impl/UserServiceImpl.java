package com.gofobao.framework.member.service.impl;

import com.gofobao.framework.api.contants.AcctUseContant;
import com.gofobao.framework.api.contants.IdTypeContant;
import com.gofobao.framework.api.contants.SeqNoContant;
import com.gofobao.framework.api.model.openusers.AccountOpenRequest;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoRegisterCallReq;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoRegisterCallResp;
import com.gofobao.framework.member.vo.response.VoRegisterResp;
import com.gofobao.framework.security.entity.JwtUserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户实体类
 * Created by Max on 20.03.16.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService, UserService{
    @Autowired
    private UsersRepository userRepository;

    // @Value("${gofobao.callBack}")
    public String callBack;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = findByAccount(username);

        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return JwtUserFactory.create(user);
        }
    }

    @Override
    public List<Users> listUser(Users users) {
        Example<Users> usersExample = Example.of(users);
        List<Users> usersList = userRepository.findAll(usersExample);
        return Optional.ofNullable(usersList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public Users findByAccount(String account) {
        List<Users> usersList = userRepository.findByUsernameOrPhoneOrEmail(account, account, account);

        if (!CollectionUtils.isEmpty(usersList)) {
            return usersList.get(0);
        }
        return null;
    }


    /**
     * 注册用户
     * @param voRegisterReq
     * @return
     */
    public VoRegisterResp register(VoRegisterReq voRegisterReq){
        AccountOpenRequest request = new AccountOpenRequest();
        request.setIdType(IdTypeContant.ID_CARD);
        request.setChannel(voRegisterReq.getChannel());
        request.setSeqNo(RandomHelper.generateNumberCode(6));
        request.setIdType(IdTypeContant.ID_CARD);
        request.setCardNo(voRegisterReq.getCardNo());
        request.setIdNo(voRegisterReq.getCardId());
        request.setName(voRegisterReq.getUsername());
        request.setMobile(voRegisterReq.getMobile());
        request.setAcctUse(AcctUseContant.GENERAL_ACCOUNT);
        request.setNotifyUrl(callBack+"/pub/user/reg/registerCallBack");
        try {
            //openHttp.postForm(OpenMethodContant.OPEN_USER,request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注册用户回调
     * @param voRegisterCallReq
     * @return
     */
    public VoRegisterCallResp registerCallBack(VoRegisterCallReq voRegisterCallReq){
        return null;
    }

    @Override
    public boolean notExistsByPhone(String phone) {
        List<Users> usersList = userRepository.findByPhone(phone);
        return CollectionUtils.isEmpty(usersList);
    }
}
