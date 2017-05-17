package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.VoBorrowListReq;
import com.gofobao.framework.borrow.vo.VoViewBorrowListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 17/5/16.
 */

@RequestMapping("borrow")
@RestController
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/list")
    public List<VoViewBorrowListRes> borrowList(HttpServletRequest request, @RequestBody VoBorrowListReq voBorrowListReq) {
        List<VoViewBorrowListRes> listResList = new ArrayList<>();
        try {
            listResList = borrowService.findAll(voBorrowListReq);
            return listResList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listResList;
    }

}
