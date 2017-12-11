package com.gofobao.framework.member.enums;

import org.springframework.util.StringUtils;

/**
 * 注册
 * Created by Max on 17/6/1.
 */
public enum RegisterSourceEnum {
    PC("pc", 0),
    ANDROID("android", 1),
    IOS("ios", 2),
    LLB("llb", 3), // 流量宝
    ZCZJ("zczj", 4),  //  众筹之家
    DX("dianxin", 5),  //  电信
    WXGZH("wxgzh", 6),  // 微信公众号
    OTHER("qt", 7),  // 其他
    ZLP("zlp", 8), // 赚乐扒
    H5("h5", 9), // h5
    LLBANK("llbank", 10),// 流量银行
    WINDMILL("windmill",12),
    DT("offline",15);
    private String name;
    private int index;

    // 构造方法
    private RegisterSourceEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (RegisterSourceEnum c : RegisterSourceEnum.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    // 普通方法
    public static Integer getIndex(String name) {
        if (!StringUtils.isEmpty(name)) {
            for (RegisterSourceEnum c : RegisterSourceEnum.values()) {
                if (String.valueOf(c.getIndex()).equals(name.toLowerCase())) {
                    //当前是h5
                    if (c.index == 3) {
                        return H5.getIndex();
                    } else {
                        return c.index;
                    }
                }
            }
        }
        return OTHER.getIndex();
    }

    // get set 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
