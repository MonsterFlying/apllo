package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ImgHelper;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
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
    public String upload(Long userId, MultipartFile file) throws Exception {
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        //对上传的文件进行判断 png,jpg
        String imgPrefix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (file.getSize() >= 1024 * 1024 * 2) {
            throw new Exception("图片大小超过2M!");
        }
        if (!(imgPrefix.equals("png") || imgPrefix.equals("jpg"))) {
            throw new Exception("上传图片只支持png,jpg!");
        }
        //图片压缩处理
//                ImgHelper.scale("C:\\Users\\xin\\Desktop\\10.jpg",
//                "C:\\Users\\xin\\Desktop\\yasuo.jpg", 180, 240, true);//等比例缩放  输出缩放图片
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
            return putRet.key;

        } catch (QiniuException ex) {
            return null;
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

    @Override
    public List<String> multiUpload(@NotNull Long userId,
                                    @NotNull HttpServletRequest httpServletRequest,
                                    @NotNull String fileName) throws Exception {
        List<MultipartFile> files = ((MultipartHttpServletRequest) httpServletRequest).getFiles(fileName);
        MultipartFile file = null;
        boolean allSuccess = true;
        List<String> keys = new ArrayList<>(files.size());
        try {
            for (int i = 0; i < files.size(); ++i) {
                file = files.get(i);
                int looper = 5;
                boolean itemSuccess = false;
                do {
                    looper--;
                    String key = upload(userId, file);
                    if (!StringUtils.isEmpty(key)) {
                        // 成功
                        keys.add(key);
                        itemSuccess = true;
                        break;
                    }
                } while (looper > 0);

                if (!itemSuccess) {
                    allSuccess = false;
                    throw new Exception("个别文件上传异常");
                }
            }
        } finally {
            if (!allSuccess) {
                // 全部文件删除
                if (!CollectionUtils.isEmpty(keys)) {
                    for (String key : keys) {
                        int looper = 5;
                        do {
                            looper--;
                            try {
                                ResponseEntity<VoBaseResp> responseEntity = deleteFile(userId, key);
                                if (responseEntity.getBody().getState().getCode() == VoBaseResp.OK) {
                                    break;
                                }
                            } catch (Exception e) {
                                log.error("批处理文件删除:" + key, e);
                            }
                        } while (looper > 0);
                    }
                }
            }
        }

        return keys;
    }

    @Override
    public ResponseEntity<FileUploadResp> uploadByFileName(@NotNull Long userId,
                                                           @NotNull HttpServletRequest httpServletRequest,
                                                           @NotNull String fileName) {
        try {
            FileUploadResp fileUploadResp = VoBaseResp.ok("操作成功", FileUploadResp.class);
            List<String> keys = multiUpload(userId, httpServletRequest, fileName);
            FileUploadResp.ImageKey imageKey = null;
            for (String key : keys) {
                imageKey = new FileUploadResp.ImageKey();
                imageKey.setImagesUrl(qiNiuDomain + key);
                imageKey.setKey(key);
                fileUploadResp.getImages().add(imageKey);
            }

            return ResponseEntity.ok(fileUploadResp);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "上传失败", FileUploadResp.class));
        }
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


