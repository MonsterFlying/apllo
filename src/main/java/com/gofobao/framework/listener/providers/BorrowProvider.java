package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    public boolean doFirstVerify(Long borrowId){
        return false;
    }

    public boolean doAgainVerify(Long borrowId){
        return false;
    }
}
