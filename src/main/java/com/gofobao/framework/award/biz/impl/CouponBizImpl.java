package com.gofobao.framework.award.biz.impl;

import com.gofobao.framework.award.biz.CouponBiz;
import com.gofobao.framework.award.contants.CouponContants;
import com.gofobao.framework.award.entity.Coupon;
import com.gofobao.framework.award.repository.CouponRepository;
import com.gofobao.framework.award.service.CouponService;
import com.gofobao.framework.award.vo.VoViewCouponWarpRes;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.request.VoTakeFlowReq;
import com.gofobao.framework.award.vo.response.CouponRes;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.OKHttpHelper;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class CouponBizImpl implements CouponBiz {
    @Autowired
    private CouponService couponService;

    /**
     * 易蜂享URL
     */
    @Value("${gofobao.flowConfig.url}")
    private String yiFengXiangFlowUrl;
    /**
     * 易蜂享ID
     */
    @Value("${gofobao.flowConfig.id}")
    private String yiFengXiangId;

    /**
     * 易蜂享URL
     */
    @Value("${gofobao.flowConfig.key}")
    private String yiFengXiangKey;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @Autowired
    private CouponRepository couponRepository;


    private static Integer[][] telCorpCodeList = {
            {134, 135, 136, 137, 138, 139, 147, 150, 151, 152, 154, 157, 158, 159, 1703, 1705, 1706, 178, 182, 183, 184, 187, 188}, // 移动
            {133, 153, 1700, 1701, 1702, 173, 177, 180, 181, 189}, // 电信
            {130, 131, 132, 145, 155, 156, 1707, 1708, 1709, 1718, 1719, 175, 176, 185, 186}, // 联通
    };

    private static List<Map<String, String>> productList = new ArrayList<>();

    static {
        Map<String, String> info = new HashMap<>();
        info.put("10M", "100010");
        info.put("30M", "100030");
        info.put("70M", "100070");
        info.put("150M", "100150");
        info.put("500M", "100500");
        info.put("1G", "101024");
        info.put("2G", "102048");
        info.put("3G", "103072");
        info.put("4G", "104096");
        info.put("6G", "106144");
        info.put("11G", "111264");
        productList.add(info);

        info = new HashMap<>();
        info.put("5M", "100005");
        info.put("10M", "100010");
        info.put("30M", "100030");
        info.put("50M", "100050");
        info.put("100M", "100100");
        info.put("200M", "100200");
        info.put("500M", "100500");
        info.put("1G", "101024");
        productList.add(info);

        info = new HashMap<>();
        info.put("20M", "100020");
        info.put("50M", "100050");
        info.put("100M", "100100");
        info.put("200M", "100200");
        info.put("500M", "100500");
        productList.add(info);
    }

    private static Integer getTelCorpCode(String phone) {
        Integer phonePre = Integer.parseInt(phone.substring(0, 3));

        if (170 == phonePre) {
            phonePre = Integer.parseInt(phone.substring(0, 4));
        }

        int index = 0;
        for (Integer[] bean : telCorpCodeList) {

            for (Integer phoneCdoe : bean) {
                if (phoneCdoe.equals(phonePre)) {
                    return index;
                }
            }

            index++;
        }
        return null;
    }

    public static String getBizcode(String phone, String size) {
        Integer index = getTelCorpCode(phone);

        if (ObjectUtils.isEmpty(index)) {
            return null;
        }

        Map<String, String> stringStringMap = productList.get(index);
        return stringStringMap.get(size);
    }


    @Override
    public ResponseEntity<VoViewCouponWarpRes> list(VoCouponReq voCouponReq) {
        try {
            List<CouponRes> resList = couponService.list(voCouponReq);
            VoViewCouponWarpRes voViewCouponWarpRes = VoBaseResp.ok("查询成功", VoViewCouponWarpRes.class);
            voViewCouponWarpRes.setCouponList(resList);
            return ResponseEntity.ok(voViewCouponWarpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp
                            .error(VoBaseResp.ERROR, "查询失败", VoViewCouponWarpRes.class));
        }
    }


    public ResponseEntity<VoBaseResp> exchange(VoTakeFlowReq takeFlowReq) {

        List<Coupon> couponList = couponService.takeFlow(takeFlowReq.getUserId(),takeFlowReq.getCouponId());
        if (CollectionUtils.isEmpty(couponList)) {
            VoBaseResp.error(VoBaseResp.ERROR, "");
        }
        Coupon coupon = couponList.get(0);
        if (coupon.getStatus() != CouponContants.VALID) {
            String message = null;
            if (coupon.getStatus() == CouponContants.LOCK) {
                message = CouponContants.LOCK_STR;
            } else if (coupon.getStatus() == CouponContants.USED) {
                message = CouponContants.USED_STR;
            } else if (coupon.getStatus() == CouponContants.FAILURE) {
                message = CouponContants.FAILURE_STR;
            }
            return ResponseEntity.badRequest()
                    .body(
                            VoBaseResp.error(
                                    VoBaseResp.ERROR,
                                    String.format("流量劵%s", message)));
        }
        String bizcode = getBizcode(coupon.getPhone(), coupon.getSize());
        if (ObjectUtils.isEmpty(bizcode)) {
            return ResponseEntity.badRequest()
                    .body(
                            VoBaseResp.error(
                                    VoBaseResp.ERROR, "操作失败"));
        }

        Map<String, String> params = new HashMap<>();
        params.put("custInteId", yiFengXiangId);
        params.put("orderId", RandomHelper.generateNumberCode(8) + coupon.getId());
        params.put("busiCode", "INTEGRAL");
        params.put("effectKind", "1");
        // params.put("notifyUrl",  "http://max.3w.dkys.org/coupon/takeFlowBackCall");
        params.put("notifyUrl", String.format("%s/pub/coupon/v2/takeFlowBackCall", webDomain));
        params.put("mobile", coupon.getPhone());
        params.put("packCode", bizcode);
        params.put("timestamp", DateHelper.dateToString(new Date()).replace("-", "").replace(":", "").replace(" ", ""));
        params.put("echo", RandomHelper.generateNumberCode(8));
        params.put("version", "1");
        String chargeSignStr = params.get("custInteId").toString() + params.get("timestamp").toString() + yiFengXiangKey + params.get("echo").toString();
        String chargeSign = DigestUtils.md5DigestAsHex(chargeSignStr.getBytes());
        params.put("chargeSign", chargeSign);
        log.info(String.format("请求参数信息params: %s", new Gson().toJson(params)));
        String result = OKHttpHelper.get(yiFengXiangFlowUrl, params, null);

        log.info(String.format("流量劵返回信息: %s", result));
        Response response = Response.convert(result);

        if (!response.getResult().equals(0)) {
            return ResponseEntity.badRequest()
                    .body(
                            VoBaseResp.error(
                                    VoBaseResp.ERROR, "操作失败"));
        }
        coupon.setStatus(CouponContants.LOCK);
        coupon.setUpdatedAt(new Date());

        try {
            couponRepository.save(coupon);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(
                            VoBaseResp.error(
                                    VoBaseResp.ERROR, "操作失败"));
        }
        return ResponseEntity.ok(VoBaseResp.ok(response.getRedirectUrl()));
    }


    @Override
    @Transactional
    public String takeFlowCallBack(String key) throws Exception {

        log.info(String.format("流量劵回调接口:%s ", key));
        TrafResult convert = TrafResult.convert(key);
        boolean verify = convert.verify(yiFengXiangId, yiFengXiangKey);
        if (!verify) {
            ReturnResponse returnResponse = new ReturnResponse();
            returnResponse.setResult(1);
            String s = returnResponse.toXml();
            return s;
        }

        List<Item> body = convert.getBody();
        if (ObjectUtils.isEmpty(body)) {
            return ReturnResponse.FAILUE.toXml();
        }
        for (Item item : body) {
            Integer couponId =  Integer.valueOf(item.getOrderId().substring(8));
            List<Coupon> coupons = couponRepository.findById(couponId);
            if (ObjectUtils.isEmpty(coupons)) {
                return ReturnResponse.FAILUE.toXml();
            }
            Coupon coupon = coupons.get(0);
            if (!coupon.getStatus().equals(2)) {
                ReturnResponse returnResponse = new ReturnResponse();
                returnResponse.setResult(1);
                String s = returnResponse.toXml();
                return s;
            }

            coupon.setUpdatedAt(new Date());
            if (1 == item.getResult()) {
                log.info(String.format("########## 流量券兑换成功 Id:%s phone: %s", coupon.getId(), coupon.getPhone()));
                coupon.setStatus(CouponContants.USED);
            } else {
                log.info(String.format("########## 流量券兑换失败，解锁流量券 Id:%s phone: %s", coupon.getId(), coupon.getPhone()));
                coupon.setStatus(CouponContants.VALID);
            }
            try {
                couponRepository.save(coupon);
                return ReturnResponse.SUCCESS.toXml();

            } catch (Exception e) {
                return ReturnResponse.FAILUE.toXml();
            }

        }

        return ReturnResponse.FAILUE.toXml();

    }
}

@XStreamAlias("response")
class ReturnResponse {
    @XStreamAlias("result")
    private Integer result;

    @XStreamAlias("desc")
    private String desc;

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String toXml() {
        XStream xStream = new XStream();
        xStream.autodetectAnnotations(true);
        String s = xStream.toXML(this);
        return s;
    }

    public ReturnResponse(Integer result, String desc) {
        this.result = result;
        this.desc = desc;
    }

    public ReturnResponse() {
    }

    public static ReturnResponse SUCCESS = new ReturnResponse(0, "");
    public static ReturnResponse FAILUE = new ReturnResponse(1, "数据有误");
}

@XStreamAlias("response")
class Response {
    private Integer result;

    private String desc;

    private String redirectUrl;

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public static Response convert(String msg) {
        XStream xStream = new XStream();
        xStream.setClassLoader(Response.class.getClassLoader());
        xStream.alias("response", Response.class);
        Response response = (Response) xStream.fromXML(msg);

        return response;
    }
}


class TrafResult {
    private static final Logger logger = LoggerFactory.getLogger(TrafResult.class);

    private Head head;

    private List<Item> body;

    public boolean verify(String id, String key) {
        String result = DigestUtils.md5DigestAsHex((id + head.getTimestamp() + key + head.getEcho()).getBytes());
        return result.toLowerCase().equals(head.getChargeSign().toLowerCase());
    }

    public static TrafResult convert(String msg) {
        XStream xstream = new XStream();
        xstream.setClassLoader(TrafResult.class.getClassLoader());
        xstream.alias("traf-result", TrafResult.class);
        xstream.alias("head", Head.class);
        xstream.alias("item", Item.class);
        TrafResult trafResult = null;
        try {
            trafResult = (TrafResult) xstream.fromXML(msg);
        } catch (Exception e) {
            logger.error(String.format("流量券强转失败：%s", msg), e);
        }
        return trafResult;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public List<Item> getBody() {
        return body;
    }

    public void setBody(List<Item> body) {
        this.body = body;
    }
}


class Head {
    private Integer custInteId;

    private String timestamp;

    private String echo;

    private String chargeSign;

    public Integer getCustInteId() {
        return custInteId;
    }

    public void setCustInteId(Integer custInteId) {
        this.custInteId = custInteId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEcho() {
        return echo;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public String getChargeSign() {
        return chargeSign;
    }

    public void setChargeSign(String chargeSign) {
        this.chargeSign = chargeSign;
    }
}


class Item {

    private String orderId;


    private Integer result;

    private String desc;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
