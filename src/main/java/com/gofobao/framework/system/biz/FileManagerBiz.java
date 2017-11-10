package com.gofobao.framework.system.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.vo.response.FileUploadResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface FileManagerBiz {
    /**
     * 文件上传
     *
     * @param userId 用户id
     * @param file   上传文件包装类
     * @return
     */
    String upload(Long userId, MultipartFile file) throws Exception;


    /**
     * 删除文件
     *
     * @param userId 用户ID
     * @param key    七牛文件key
     * @return
     */
    ResponseEntity<VoBaseResp> deleteFile(Long userId, String key);


    /**
     * 批量上传
     *
     * @param userId             用户Id
     * @param httpServletRequest 网路请求
     * @param fileName           文件上传名称
     * @return
     */
    List<String> multiUpload(Long userId, HttpServletRequest httpServletRequest, String fileName) throws Exception;


    /**
     * 根据指定文件名长传图片(支持批处理)
     * @param userId
     * @param httpServletRequest
     * @param fileName
     * @return
     */
    ResponseEntity<FileUploadResp> uploadByFileName(Long userId, HttpServletRequest httpServletRequest, String fileName);

}
