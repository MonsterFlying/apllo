package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.FileManagerBiz;
import com.gofobao.framework.system.vo.response.FileUploadResp;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 文件管理
 */
@Component
@Slf4j
public class FileManagerBizImpl implements FileManagerBiz {

    @Value("${qiniu.sk}")
    private String secretKey;

    @Value("${qiniu.ak}")
    private String accessKey;

    @Value("${qiniu.domain}")
    private String qiNiuDomain;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Override
    public ResponseEntity<FileUploadResp> upload(@NonNull Long userId, @NonNull MultipartFile file) throws Exception {
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        // 文件后缀名
        String extendType = getextendType(originalFilename);
        Preconditions.checkNotNull(extendType, "upload file extend type is empty");
        UploadManager uploadManager = getUploadManager();
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        String uuid = UUID.randomUUID().toString();
        String uploadFileName = String.format("%s-%s%s", userId, uuid, extendType);
        log.info(uploadFileName);
        try {
            Response response = uploadManager.put(file.getBytes(), uploadFileName, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            FileUploadResp fileUploadResp = VoBaseResp.ok("操作成功", FileUploadResp.class);
            fileUploadResp.setKey(putRet.key);
            fileUploadResp.setImageUrl(String.format("%s/%s", qiNiuDomain, putRet.key));
            return ResponseEntity.ok(fileUploadResp);
        } catch (QiniuException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(ex.code(), ex.response.toString(), FileUploadResp.class));
        }
    }

    @Override
    public ResponseEntity<VoBaseResp> deleteFile(@NonNull Long userId, @NonNull String key) {
        boolean state = key.startsWith(userId + "-");
        if (!state) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "无权删除该商品"));
        }

        Zone z = Zone.autoZone();
        Configuration cfg = new Configuration(z);
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, key);
        } catch (QiniuException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(ex.code(), ex.response.toString()));
        }

        return ResponseEntity.ok(VoBaseResp.ok("操作成功"));
    }

    /**
     * 获取七牛用上传管理类
     *
     * @return
     */
    private UploadManager getUploadManager() {
        Zone z = Zone.autoZone();
        Configuration cfg = new Configuration(z);
        return new UploadManager(cfg);
    }

    /**
     * 获取文件后缀
     *
     * @param originalFilename
     * @return
     */
    private String getextendType(String originalFilename) {
        if (ObjectUtils.isEmpty(originalFilename)) {
            return null;
        }

        String extend = originalFilename.substring(originalFilename.lastIndexOf('.'));
        return ObjectUtils.isEmpty(extend) ? null : extend;
    }
}
