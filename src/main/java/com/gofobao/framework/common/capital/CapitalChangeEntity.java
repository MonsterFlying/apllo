package com.gofobao.framework.common.capital;

/**
 * 资金变动类
 * Created by Max on 17/3/10.
 */
public class CapitalChangeEntity {
    /**
     * 用户Id
     */
    private int userId;

    /**
     * userID
     */
    private int toUserId;

    /**
     * 操作总金额
     */
    private int money;

    /**
     * 本金
     */
    private int principal;

    /**
     * 资产操作
     */
    private String asset;

    /**
     * 利息
     */
    private int interest;
    /**
     * 变动类型
     */
    private CapitalChangeEnum type;

    /**
     * 备注
     */
    private String remark;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public int getMoney() {
        return money;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getPrincipal() {
        return principal;
    }

    public void setPrincipal(int principal) {
        this.principal = principal;
    }

    public int getInterest() {
        return interest;
    }

    public void setInterest(int interest) {
        this.interest = interest;
    }

    public CapitalChangeEnum getType() {
        return type;
    }

    public void setType(CapitalChangeEnum type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
