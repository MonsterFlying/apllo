package com.gofobao.framework.system.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.vo.response.FileUploadResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileManagerBiz {
    /**
     *  文件上传
     * @param userId
     * @param file
     * @return
     */
    ResponseEntity<FileUploadResp> upload(Long userId, MultipartFile file) throws Exception;


    /**
     * 删除文件
     * @param userId
     * @param key
     * @return
     */
    ResponseEntity<VoBaseResp> deleteFile(Long userId, String key);
}
