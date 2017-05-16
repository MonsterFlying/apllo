package com.gofobao.framework.borrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity
@Table(name = "gfb_borrow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrow implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;

}
