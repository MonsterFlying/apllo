package com.gofobao.framework.message.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Max on 17/5/17.
 */
@Entity
@Table(name = "gfb_sms", schema = "gofobao9", catalog = "")
public class GfbSmsEntity {
    private int id;
    private String username;
    private String type;
    private String phone;
    private String content;
    private String ext;
    private String stime;
    private String rrid;
    private byte status;
    private String ip;
    private Timestamp createdAt;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "ext")
    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @Basic
    @Column(name = "stime")
    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    @Basic
    @Column(name = "rrid")
    public String getRrid() {
        return rrid;
    }

    public void setRrid(String rrid) {
        this.rrid = rrid;
    }

    @Basic
    @Column(name = "status")
    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Basic
    @Column(name = "ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Basic
    @Column(name = "created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GfbSmsEntity that = (GfbSmsEntity) o;

        if (id != that.id) return false;
        if (status != that.status) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (ext != null ? !ext.equals(that.ext) : that.ext != null) return false;
        if (stime != null ? !stime.equals(that.stime) : that.stime != null) return false;
        if (rrid != null ? !rrid.equals(that.rrid) : that.rrid != null) return false;
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        result = 31 * result + (stime != null ? stime.hashCode() : 0);
        result = 31 * result + (rrid != null ? rrid.hashCode() : 0);
        result = 31 * result + (int) status;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }
}
