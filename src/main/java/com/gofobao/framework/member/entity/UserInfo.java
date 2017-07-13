package com.gofobao.framework.member.entity;

import com.gofobao.framework.member.vo.response.pc.UserInfoExt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Max on 17/6/1.
 */
@Entity
@Table(name = "gfb_user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class UserInfo extends UserInfoExt {
    @Id
    @Column(name = "user_id")
    // @GeneratedValue
    private Long userId ;

    @Basic
    @Column(name = "email_status")
    private boolean emailStatus ;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "real_status")
    private Integer realStatus ;

    @Basic
    @Column(name = "realname")
    private String realname ;

    @Basic
    @Column(name = "card_id")
    private String cardId ;

    @Basic
    @Column(name = "card_pic1")
    private String cardPic1 ;

    @Basic
    @Column(name = "card_pic2")
    private String cardPic2;

    @Basic
    @Column(name = "vip_expire_at")
    private Date vipExpireAt ;

    @Basic
    @Column(name = "sex")
    private Integer sex ;

    @Basic
    @Column(name = "birthday")
    private Date birthday ;

    @Basic
    @Column(name = "qq")
    private String qq ;

    @Basic
    @Column(name = "education")
    private Integer education ;

    @Basic
    @Column(name = "graduated_school")
    private String graduatedSchool ;

    @Basic
    @Column(name = "marital")
    private Integer marital ;

    @Basic
    @Column(name = "address")
    private String address ;

    @Basic
    @Column(name = "housing")
    private Integer housing ;

    @Basic
    @Column(name = "industry")
    private Integer industry ;

    @Basic
    @Column(name = "graduation")
    private Integer graduation ;

    @Basic
    @Column(name = "updated_at")
    private Date updatedAt ;

    @Basic
    @Column(name = "birthday_y")
    private Integer birthdayY ;

    @Basic
    @Column(name = "birthday_md")
    private Integer birthdayMd ;

}
