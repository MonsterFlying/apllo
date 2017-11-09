package com.gofobao.framework.system.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.system.biz.FileManagerBiz;
import com.gofobao.framework.system.vo.response.FileUploadResp;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理
 */
@RestController
@Slf4j
public class FileController {

    @Autowired
    FileManagerBiz fileManagerBiz;

    @ApiOperation(value = "文件上传")
    @PostMapping("file/upload")
    public ResponseEntity<FileUploadResp> upload(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                 @RequestParam("file") MultipartFile file) throws Exception {
        try {
            return fileManagerBiz.upload(userId, file);
        } catch (Exception e) {
            log.error("文件上传异常", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不可用, 请稍后再试!", FileUploadResp.class));
        }
    }

    @ApiOperation(value = "文件上传")
    @GetMapping("file/delete/{key}")
    public ResponseEntity<VoBaseResp> delete(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                             @PathVariable String key) throws Exception {
        try {
            return fileManagerBiz.deleteFile(userId, key);
        } catch (Exception e) {
            log.error("文件删除异常异常", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前网络不可用, 请稍后再试!"));
        }
    }
}
