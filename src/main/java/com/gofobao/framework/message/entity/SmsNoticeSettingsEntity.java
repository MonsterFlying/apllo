package com.gofobao.framework.message.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Max on 17/5/17.
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "gfb_sms_notice_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsNoticeSettingsEntity implements Serializable{
    @Id
    private Long userId ;
    @Basic
    private boolean receivedRepay;
    @Basic
    private boolean borrowSuccess;
}
