package com.gofobao.framework.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity
@Table(name = "gfb_users")
@Data
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class Users implements Serializable{
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    private String username;

    private String phone;

    private String email;

    private String password;

    private String payPassword;

    private String realname;

    private String cardId;

    private Boolean isLock;

    private String type;

    @Column(name = "`branch`")
    private Integer branch;

    private Integer noticeCount;

    private String inviteCode;

    private Long parentId;

    private Integer source;

    private Integer parentAward;

    private String rememberToken;

    private Date createdAt;

    private Date updatedAt;

    private Integer pushState ;

    private String pushId ;

    private Integer platform ;

    private String ip ;

    private String windmillId;
    private Date loginTime ;

    private String avatarPath;
}
