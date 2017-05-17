package com.gofobao.framework.message.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by Max on 17/5/17.
 */
@Entity
@Table(name = "gfb_sms_notice_settings", schema = "gofobao9", catalog = "")
public class GfbSmsNoticeSettingsEntity {
    private byte receivedRepay;
    private byte borrowSuccess;

    @Basic
    @Column(name = "received_repay")
    public byte getReceivedRepay() {
        return receivedRepay;
    }

    public void setReceivedRepay(byte receivedRepay) {
        this.receivedRepay = receivedRepay;
    }

    @Basic
    @Column(name = "borrow_success")
    public byte getBorrowSuccess() {
        return borrowSuccess;
    }

    public void setBorrowSuccess(byte borrowSuccess) {
        this.borrowSuccess = borrowSuccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GfbSmsNoticeSettingsEntity that = (GfbSmsNoticeSettingsEntity) o;

        if (receivedRepay != that.receivedRepay) return false;
        if (borrowSuccess != that.borrowSuccess) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) receivedRepay;
        result = 31 * result + (int) borrowSuccess;
        return result;
    }
}
