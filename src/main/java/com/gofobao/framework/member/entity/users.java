package com.gofobao.framework.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity
@Table(name = "gfb_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class users {
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "username")
    private String username;
    @Basic
    @Column(name = "phone")
    private String phone;
    @Basic
    @Column(name = "email")
    private String email;
    @Basic
    @Column(name = "password")
    private String password;
    @Basic
    @Column(name = "pay_password")
    private String payPassword;
    @Basic
    @Column(name = "realname")
    private String realname;
    @Basic
    @Column(name = "card_id")
    private String cardId;
    @Basic
    @Column(name = "is_lock")
    private byte isLock;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "branch")
    private Integer branch;
    @Basic
    @Column(name = "notice_count")
    private short noticeCount;
    @Basic
    @Column(name = "invite_code")
    private String inviteCode;
    @Basic
    @Column(name = "parent_id")
    private int parentId;
    @Basic
    @Column(name = "source")
    private byte source;
    @Basic
    @Column(name = "parent_award")
    private int parentAward;
    @Basic
    @Column(name = "remember_token")
    private String rememberToken;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
