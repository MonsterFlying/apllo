package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.system.biz.CaptchaBiz;
import com.gofobao.framework.system.vo.VoCaptchaImageResp;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by Max on 17/5/18.
 */
@Service
@Slf4j
public class CaptchaBizImpl implements CaptchaBiz {

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    private DefaultKaptcha kaptcha;


    @Override
    public ResponseEntity<VoCaptchaImageResp> drawImageByAnon() {
        UUID uuid = UUID.randomUUID();

        String capText = kaptcha.createText();
        log.info(String.format("图形验证码: %s", capText));
        try {
            redisHelper.put(String.format("%s%s", CaptchaHelper.REDIS_CAPTCHA_PREFIX_KEY, uuid), capText, 15 * 60);
            BufferedImage bi = kaptcha.createImage(capText); // create the image with the text
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", os);
            String contect = Base64.getEncoder().encodeToString(os.toByteArray());
            VoCaptchaImageResp response = new VoCaptchaImageResp(contect, uuid.toString());
            return ResponseEntity.ok(response) ;
        } catch (Throwable e) {
            log.error("CaptchaBizImpl drawImageByAnon excption :", e);
            return ResponseEntity.badRequest().body(null);
        }

    }
}
