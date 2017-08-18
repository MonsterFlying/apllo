SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE `gfb_activity` DROP PRIMARY KEY;
ALTER TABLE `gfb_activity` ADD COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT FIRST , ADD PRIMARY KEY (`id`);
ALTER TABLE `gfb_activity` ADD COLUMN `title`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标题' AFTER `id`;
ALTER TABLE `gfb_activity` ADD COLUMN `is_open`  int(11) NULL DEFAULT 0 COMMENT '是否开启 0否 1是' AFTER `title`;
ALTER TABLE `gfb_activity` ADD COLUMN `begin_at`  datetime NULL DEFAULT NULL COMMENT '活动开启时间' AFTER `is_open`;
ALTER TABLE `gfb_activity` ADD COLUMN `end_at`  datetime NULL DEFAULT NULL COMMENT '活动结束时间' AFTER `begin_at`;
ALTER TABLE `gfb_activity` ADD COLUMN `del`  int(11) NULL DEFAULT 0 COMMENT '是否删除 0否 1是' AFTER `end_at`;
ALTER TABLE `gfb_activity` ADD COLUMN `create_at`  datetime NULL DEFAULT NULL AFTER `del`;
ALTER TABLE `gfb_activity` ADD COLUMN `create_by`  int(11) NULL DEFAULT NULL AFTER `create_at`;
ALTER TABLE `gfb_activity` ADD COLUMN `update_at`  datetime NULL DEFAULT NULL AFTER `create_by`;
ALTER TABLE `gfb_activity` ADD COLUMN `update_by`  int(11) NULL DEFAULT NULL AFTER `update_at`;
ALTER TABLE `gfb_activity` ADD COLUMN `iparam1`  int(11) NULL DEFAULT NULL AFTER `update_by`;
ALTER TABLE `gfb_activity` ADD COLUMN `iparam2`  int(11) NULL DEFAULT NULL AFTER `iparam1`;
ALTER TABLE `gfb_activity` ADD COLUMN `iparam3`  int(11) NULL DEFAULT NULL AFTER `iparam2`;
ALTER TABLE `gfb_activity` ADD COLUMN `vparam1`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `iparam3`;
ALTER TABLE `gfb_activity` ADD COLUMN `vparam2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `vparam1`;
ALTER TABLE `gfb_activity` ADD COLUMN `vparam3`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '红包类型 1：注册' AFTER `vparam2`;
ALTER TABLE `gfb_activity` ADD COLUMN `type`  int(11) NULL DEFAULT 0 COMMENT '1：注册；2：新手标' AFTER `vparam3`;
ALTER TABLE `gfb_activity` ADD COLUMN `min`  int(11) NULL DEFAULT 0 COMMENT '当type=1，它的基数为分（例如1元等于100）； 当type=2或者= 3， 基数为万分之一（例如万分之一=0.0001%;千分之五=0.0005%）' AFTER `type`;
ALTER TABLE `gfb_activity` ADD COLUMN `max`  int(110) NULL DEFAULT 0 COMMENT '当type=1，它的基数为分（例如1元等于100）； 当type=2或者= 3， 基数为万分之一（例如万分之一=0.0001%;千分之五=0.0005%）' AFTER `min`;
ALTER TABLE `gfb_activity` ADD COLUMN `banner_url`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'banner url' AFTER `max`;
ALTER TABLE `gfb_activity` ADD COLUMN `url`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '描述' AFTER `banner_url`;
ALTER TABLE `gfb_activity` ADD COLUMN `sort`  int(10) NOT NULL DEFAULT 0 COMMENT '排序' AFTER `url`;
ALTER TABLE `gfb_activity` ADD COLUMN `new_state`  int(1) NOT NULL DEFAULT 0 COMMENT '是否最新' AFTER `sort`;
ALTER TABLE `gfb_activity` ADD COLUMN `hot_state`  int(1) NOT NULL DEFAULT 0 COMMENT '是否热门' AFTER `new_state`;
ALTER TABLE `gfb_activity` ADD COLUMN `description`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' AFTER `hot_state`;
ALTER TABLE `gfb_activity` DROP COLUMN `ID`;
ALTER TABLE `gfb_activity` DROP COLUMN `TITLE`;
ALTER TABLE `gfb_activity` DROP COLUMN `IS_OPEN`;
ALTER TABLE `gfb_activity` DROP COLUMN `BEGIN_AT`;
ALTER TABLE `gfb_activity` DROP COLUMN `END_AT`;
ALTER TABLE `gfb_activity` DROP COLUMN `DEL`;
ALTER TABLE `gfb_activity` DROP COLUMN `CREATE_AT`;
ALTER TABLE `gfb_activity` DROP COLUMN `CREATE_BY`;
ALTER TABLE `gfb_activity` DROP COLUMN `UPDATE_AT`;
ALTER TABLE `gfb_activity` DROP COLUMN `UPDATE_BY`;
ALTER TABLE `gfb_activity` DROP COLUMN `IPARAM1`;
ALTER TABLE `gfb_activity` DROP COLUMN `IPARAM2`;
ALTER TABLE `gfb_activity` DROP COLUMN `IPARAM3`;
ALTER TABLE `gfb_activity` DROP COLUMN `VPARAM1`;
ALTER TABLE `gfb_activity` DROP COLUMN `VPARAM2`;
ALTER TABLE `gfb_activity` DROP COLUMN `VPARAM3`;
ALTER TABLE `gfb_activity` DROP COLUMN `TYPE`;
ALTER TABLE `gfb_activity` DROP COLUMN `MIN`;
ALTER TABLE `gfb_activity` DROP COLUMN `MAX`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `CREATE_UP`  int(11) NULL DEFAULT 0 AFTER `CREATE_TIME`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `UPDATE_UP`  int(11) NULL DEFAULT 0 AFTER `UPDATE_DATE`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `IPARAM1`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `DEL`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `IPARAM2`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `IPARAM1`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `IPARAM3`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `IPARAM2`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `VPARAM1`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `IPARAM3`;
ALTER TABLE `gfb_activity_red_packet` MODIFY COLUMN `VPARAM2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' AFTER `VPARAM1`;
ALTER TABLE `gfb_activity_red_packet_log` MODIFY COLUMN `CREATE_UP`  int(11) NULL DEFAULT 0 AFTER `CREATE_TIME`;
ALTER TABLE `gfb_activity_red_packet_log` MODIFY COLUMN `UPDARE_UP`  int(11) NULL DEFAULT 0 AFTER `UPDATE_TIME`;
ALTER TABLE `gfb_advance_log` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_advance_log` MODIFY COLUMN `status`  int(11) NULL DEFAULT 0 COMMENT '借款用户是否还款状态：0、未还款。1、还款；' AFTER `repayment_id`;
ALTER TABLE `gfb_advance_log` MODIFY COLUMN `repay_money_yes`  int(10) UNSIGNED NULL DEFAULT 0 COMMENT '实际还款金额（分）' AFTER `repay_at_yes`;
ALTER TABLE `gfb_article` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_article` ADD COLUMN `preview_img`  varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL AFTER `type`;
ALTER TABLE `gfb_article_type` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_asset` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_asset` MODIFY COLUMN `use_money`  bigint(20) NOT NULL DEFAULT 0 COMMENT '可用金额(分)' AFTER `user_id`;
ALTER TABLE `gfb_asset` MODIFY COLUMN `no_use_money`  bigint(20) NOT NULL DEFAULT 0 COMMENT '冻结金额(分)' AFTER `use_money`;
ALTER TABLE `gfb_asset` MODIFY COLUMN `virtual_money`  bigint(20) NOT NULL DEFAULT 0 COMMENT '体验金(分)' AFTER `no_use_money`;
ALTER TABLE `gfb_asset` MODIFY COLUMN `collection`  bigint(20) NOT NULL DEFAULT 0 COMMENT '代收金额(分)' AFTER `virtual_money`;
ALTER TABLE `gfb_asset` MODIFY COLUMN `payment`  bigint(20) NOT NULL DEFAULT 0 COMMENT '待还金额(分)' AFTER `collection`;
ALTER TABLE `gfb_asset` MODIFY COLUMN `updated_at`  timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间' AFTER `payment`;
ALTER TABLE `gfb_asset` ADD COLUMN `finance_plan_money`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '理财计划金额' AFTER `updated_at`;
ALTER TABLE `gfb_asset_log` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_asset_log` MODIFY COLUMN `money`  int(10) UNSIGNED NOT NULL COMMENT '操作金额（分）' AFTER `type`;
ALTER TABLE `gfb_asset_log` MODIFY COLUMN `use_money`  int(11) NOT NULL COMMENT '可用金额（分）' AFTER `money`;
ALTER TABLE `gfb_asset_log` MODIFY COLUMN `no_use_money`  int(11) NOT NULL COMMENT '冻结金额（分）' AFTER `use_money`;
ALTER TABLE `gfb_asset_log` MODIFY COLUMN `virtual_money`  int(11) NOT NULL COMMENT '体验金额（分）' AFTER `no_use_money`;
ALTER TABLE `gfb_asset_log` MODIFY COLUMN `collection`  int(11) NOT NULL COMMENT '代收金额（分）' AFTER `virtual_money`;
ALTER TABLE `gfb_asset_log` MODIFY COLUMN `payment`  int(11) NOT NULL COMMENT '待还金额（分）' AFTER `collection`;
ALTER TABLE `gfb_auto_tender` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_auto_tender` MODIFY COLUMN `tender_0`  int(11) NOT NULL DEFAULT 0 AFTER `borrow_types`;
ALTER TABLE `gfb_auto_tender` MODIFY COLUMN `tender_1`  int(11) NOT NULL DEFAULT 0 AFTER `tender_0`;
ALTER TABLE `gfb_auto_tender` MODIFY COLUMN `tender_3`  int(11) NOT NULL DEFAULT 0 AFTER `tender_1`;
ALTER TABLE `gfb_auto_tender` MODIFY COLUMN `tender_4`  int(11) NOT NULL DEFAULT 0 AFTER `tender_3`;
ALTER TABLE `gfb_auto_tender` MODIFY COLUMN `apr_first`  int(11) UNSIGNED NOT NULL COMMENT '年化率起始值' AFTER `timelimit_last`;
ALTER TABLE `gfb_auto_tender` MODIFY COLUMN `apr_last`  int(5) UNSIGNED NOT NULL COMMENT '年化率结束值' AFTER `apr_first`;
ALTER TABLE `gfb_auto_tender` ADD COLUMN `iparam1`  int(11) NULL DEFAULT NULL AFTER `updated_at`;
ALTER TABLE `gfb_auto_tender` ADD COLUMN `iparam2`  int(11) NULL DEFAULT NULL AFTER `iparam1`;
ALTER TABLE `gfb_auto_tender` ADD COLUMN `iparam3`  int(11) NULL DEFAULT NULL AFTER `iparam2`;
ALTER TABLE `gfb_auto_tender` ADD COLUMN `vparam1`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `iparam3`;
ALTER TABLE `gfb_auto_tender` ADD COLUMN `vparam2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `vparam1`;
ALTER TABLE `gfb_auto_tender` ADD COLUMN `vparam3`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `vparam2`;
ALTER TABLE `gfb_bank_account` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_banner` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_batch_asset_change` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `type`  int(11) NULL DEFAULT NULL COMMENT '操作（1.批次放款、2.批次还款、3.批次融资人还担保账户垫款 4.批次投资人购买债权 5.批次担保账户代偿）' ,
  `source_id`  int(11) NULL DEFAULT NULL COMMENT '资源id（borrowId,repaymentId等）' ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '状态：0.未完成 1.已完成' ,
  `batch_no`  varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '批次号' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_batch_asset_change_item` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `batch_asset_change_id`  int(11) NULL DEFAULT NULL COMMENT '批次资产变动id' ,
  `state`  int(11) NULL DEFAULT NULL COMMENT '状态 0.未操作  1.已操作 2.无效' ,
  `user_id`  int(11) NULL DEFAULT NULL COMMENT '资产变动会员id' ,
  `to_user_id`  int(11) NULL DEFAULT NULL COMMENT '交易对方ID' ,
  `money`  int(20) NULL DEFAULT NULL COMMENT '本比操作总金额 （分）' ,
  `principal`  int(20) NULL DEFAULT NULL COMMENT '操作本金（分）' ,
  `interest`  int(20) NULL DEFAULT NULL COMMENT '利息（分）' ,
  `asset`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资产操作' ,
  `type`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '变动类型' ,
  `send_red_packet`  int(11) NULL DEFAULT 0 COMMENT '是否需要发送存管红包 0否 1是' ,
  `remark`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  `source_id`  int(11) NULL DEFAULT 0 COMMENT '引用ID' ,
  `seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '交易流水' ,
  `group_seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '一组的交易流水' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_borrow` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_borrow` MODIFY COLUMN `type`  int(11) NULL DEFAULT 0 COMMENT '借款类型；0：车贷标；1：净值标；2：秒标；3.转让标4：渠道标；' AFTER `status`;
ALTER TABLE `gfb_borrow` MODIFY COLUMN `release_at`  timestamp NULL DEFAULT NULL COMMENT '平台发布时间' AFTER `award`;
ALTER TABLE `gfb_borrow` ADD COLUMN `tx_fee`  int(11) NULL DEFAULT NULL COMMENT '借款手续费(选填）' AFTER `updated_at`;
ALTER TABLE `gfb_borrow` ADD COLUMN `t_user_id`  int(11) NULL DEFAULT NULL COMMENT '银行电子账户标 id' AFTER `tx_fee`;
ALTER TABLE `gfb_borrow` ADD COLUMN `bail_account_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '担保存管账号' AFTER `t_user_id`;
ALTER TABLE `gfb_borrow` ADD COLUMN `titular_borrow_account_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `bail_account_id`;
ALTER TABLE `gfb_borrow` ADD COLUMN `take_user_id`  int(11) NULL DEFAULT NULL COMMENT '收款人id  目前针对于官标' AFTER `titular_borrow_account_id`;
ALTER TABLE `gfb_borrow` ADD COLUMN `product_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '即信标的号' AFTER `take_user_id`;
ALTER TABLE `gfb_borrow` ADD COLUMN `third_transfer_flag`  int(11) NULL DEFAULT 0 COMMENT '当借款是转让标时，标识否与存管通信， 0否 1是' AFTER `product_id`;
ALTER TABLE `gfb_borrow` ADD COLUMN `is_windmill`  tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否推送到风车理财 0：不是 ;1:是' AFTER `third_transfer_flag`;
ALTER TABLE `gfb_borrow_collection` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `actual_interest`  int(11) NULL DEFAULT NULL AFTER `interest`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `start_at`  datetime NULL DEFAULT NULL COMMENT '理论开始计息时间' AFTER `actual_interest`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `start_at_yes`  datetime NULL DEFAULT NULL COMMENT '实际开始计息时间' AFTER `start_at`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `collection_at`  datetime NULL DEFAULT NULL COMMENT '理论结束计息时间' AFTER `start_at_yes`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `collection_at_yes`  datetime NULL DEFAULT NULL COMMENT '实际结束计息时间' AFTER `collection_at`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `transfer_flag`  int(11) NULL DEFAULT 0 COMMENT '转让标识（0：未转让；1：已转让）' AFTER `late_interest`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `created_at`  datetime NULL DEFAULT NULL COMMENT '创建时间' AFTER `transfer_flag`;
ALTER TABLE `gfb_borrow_collection` MODIFY COLUMN `updated_at`  datetime NULL DEFAULT NULL COMMENT '更新时间' AFTER `created_at`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `borrow_id`  int(11) NULL DEFAULT NULL COMMENT '借款id' AFTER `updated_at`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `user_id`  int(11) NULL DEFAULT NULL COMMENT '投标会员id' AFTER `borrow_id`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `t_user_id`  int(11) NULL DEFAULT NULL COMMENT '银行电子账户标 id' AFTER `user_id`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `t_repay_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '还款order' AFTER `t_user_id`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `t_credit_end_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `t_repay_order_id`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `t_transfer_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '部分债权转让order_id' AFTER `t_credit_end_order_id`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `third_repay_flag`  int(11) NULL DEFAULT 0 COMMENT '第三方是否登记还款 0否 1是' AFTER `t_transfer_order_id`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `third_credit_end_flag`  int(11) NULL DEFAULT NULL AFTER `third_repay_flag`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `third_transfer_flag`  int(11) NULL DEFAULT 0 COMMENT '第三方是否登记部分债权转让 0否 1是' AFTER `third_credit_end_flag`;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `transfer_auth_code`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '即信部分债权转让授权码' AFTER `third_transfer_flag`;
ALTER TABLE `gfb_borrow_repayment` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `user_id`  int(11) NULL DEFAULT NULL COMMENT '借款人id' AFTER `updated_at`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `iparam1`  int(11) NULL DEFAULT NULL AFTER `user_id`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `iparam2`  int(11) NULL DEFAULT NULL AFTER `iparam1`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `iparam3`  int(11) NULL DEFAULT NULL AFTER `iparam2`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `vparam1`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `iparam3`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `vparam2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `vparam1`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `vparam3`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `vparam2`;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `t_user_id`  int(11) NULL DEFAULT NULL COMMENT '银行电子账户标 id' AFTER `vparam3`;
ALTER TABLE `gfb_borrow_tender` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_borrow_tender` MODIFY COLUMN `status`  int(11) NULL DEFAULT 0 COMMENT '状态；0：失败；1：成功；2.取消' AFTER `user_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `type`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '类型；0：普通；1：理财计划；' AFTER `status`;
ALTER TABLE `gfb_borrow_tender` MODIFY COLUMN `transfer_flag`  int(11) NULL DEFAULT 0 COMMENT '转让标识（0：未转让；1：转让中；2：全部已转让 3.部分转让）' AFTER `valid_money`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `auth_code`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '即信债权授权码' AFTER `transfer_flag`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `t_user_id`  int(11) NULL DEFAULT 0 COMMENT '银行电子账户标 id' AFTER `auth_code`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `state`  int(10) NULL DEFAULT 1 COMMENT '1:投标中； 2:还款中 ;3:已结清' AFTER `t_user_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `parent_id`  int(11) NULL DEFAULT 0 COMMENT '父级投标id（默认为0，最顶级记录）' AFTER `state`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `transfer_buy_id`  int(11) NULL DEFAULT NULL COMMENT '购买债权记录id' AFTER `parent_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `already_interest`  int(11) NULL DEFAULT 0 COMMENT '付给债权转让人的当期应计算利息，（债权转让时使用） ' AFTER `transfer_buy_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_tender_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT '第三方投标订单号' AFTER `already_interest`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_tender_cancel_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '第三方取消投标订单号' AFTER `third_tender_order_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_transfer_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT '购买债券转让编号' AFTER `third_tender_cancel_order_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_lend_pay_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `third_transfer_order_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_credit_end_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '结束债权orderid' AFTER `third_lend_pay_order_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `is_third_register`  int(11) NULL DEFAULT 0 COMMENT '是否在存管进行登记 0否 1.是否' AFTER `third_credit_end_order_id`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_transfer_flag`  int(11) NULL DEFAULT 0 COMMENT '当投标记录是转让标投标时，标识是否购买债权， 0否 1是' AFTER `is_third_register`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_tender_flag`  int(11) NULL DEFAULT 0 COMMENT '当投标记录是非转让标投标时，标识是否放款， 0否 1是' AFTER `third_transfer_flag`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `third_credit_end_flag`  int(11) NULL DEFAULT 0 COMMENT '当前投标记录在是否结束存管债权，0否 1是' AFTER `third_tender_flag`;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `transfer_auth_code`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '即信债权转让授权码' AFTER `third_credit_end_flag`;
ALTER TABLE `gfb_borrow_tender` MODIFY COLUMN `created_at`  datetime NULL DEFAULT NULL COMMENT '创建时间' AFTER `transfer_auth_code`;
ALTER TABLE `gfb_borrow_tender` MODIFY COLUMN `updated_at`  datetime NULL DEFAULT NULL COMMENT '更新时间' AFTER `created_at`;
ALTER TABLE `gfb_borrow_virtual` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_borrow_virtual_collection` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_borrow_virtual_collection` MODIFY COLUMN `status`  int(10) NOT NULL DEFAULT 0 AFTER `id`;
ALTER TABLE `gfb_borrow_virtual_collection` MODIFY COLUMN `order`  int(10) NOT NULL AFTER `status`;
ALTER TABLE `gfb_borrow_virtual_collection` MODIFY COLUMN `collection_at`  datetime NULL DEFAULT NULL AFTER `tender_id`;
ALTER TABLE `gfb_borrow_virtual_collection` MODIFY COLUMN `collection_at_yes`  datetime NULL DEFAULT NULL AFTER `collection_at`;
ALTER TABLE `gfb_borrow_virtual_collection` MODIFY COLUMN `created_at`  datetime NULL DEFAULT NULL AFTER `interest`;
ALTER TABLE `gfb_borrow_virtual_collection` MODIFY COLUMN `updated_at`  datetime NULL DEFAULT NULL AFTER `created_at`;
ALTER TABLE `gfb_borrow_virtual_tender` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_broker_bouns` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_cash_detail_log` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `user_id`  int(11) NOT NULL DEFAULT 0 COMMENT '用户ID' ,
  `third_account_id`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '银行存管账号' ,
  `card_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '提现卡号' ,
  `bank_name`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '提现银行名称' ,
  `company_bank_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '联行号' ,
  `money`  int(11) NULL DEFAULT 0 COMMENT '提现金额' ,
  `fee`  int(11) NULL DEFAULT 0 COMMENT '费用' ,
  `verify_user_id`  int(11) NULL DEFAULT 0 COMMENT '审核人' ,
  `verify_time`  datetime NULL DEFAULT NULL COMMENT '审核时间' ,
  `verify_remark`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '审核备注' ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '-1.取消提现.0:申请中,1.系统审核通过,2.系统审核不通过, 3.银行提现成功.4.银行提现失败.' ,
  `create_time`  datetime NULL DEFAULT NULL COMMENT '提现申请时间' ,
  `callback_time`  datetime NULL DEFAULT NULL COMMENT '存管回调时间' ,
  `cancel_time`  datetime NULL DEFAULT NULL COMMENT '取消时间' ,
  `ip`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '提现IP' ,
  `cash_type`  int(11) NULL DEFAULT 0 COMMENT '0:渠道提现,1.人行提现' ,
  `seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' ,
  `query_seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '实时查产生的交易流水(只有成功才能产生)' ,
  `query_callback_time`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '查询交易记录时间' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_cash_log` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_coupon` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_currency` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_currency` MODIFY COLUMN `use_currency`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '广富币金额' AFTER `user_id`;
ALTER TABLE `gfb_currency` MODIFY COLUMN `no_use_currency`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '冻结广富币金额' AFTER `use_currency`;
ALTER TABLE `gfb_currency_log` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_current_income_log` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `user_id`  int(11) NULL DEFAULT 0 ,
  `create_at`  datetime NULL DEFAULT NULL COMMENT '创建时间' ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '是否成功' ,
  `seq_no`  varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' ,
  `money`  bigint(20) NULL DEFAULT 0 COMMENT '活期金额' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_daily_asset` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_daily_asset` MODIFY COLUMN `use_money`  int(11) NOT NULL COMMENT '用户余额（分）' AFTER `user_id`;
ALTER TABLE `gfb_daily_asset` MODIFY COLUMN `no_use_money`  int(11) NOT NULL COMMENT '冻结金额（分）' AFTER `use_money`;
ALTER TABLE `gfb_daily_asset` MODIFY COLUMN `virtual_money`  int(11) NOT NULL COMMENT '体验金额（分）' AFTER `no_use_money`;
ALTER TABLE `gfb_daily_asset` MODIFY COLUMN `collection`  int(11) NOT NULL COMMENT '代收金额（分）' AFTER `virtual_money`;
ALTER TABLE `gfb_daily_asset` MODIFY COLUMN `payment`  int(11) NOT NULL COMMENT '代付金额（分）' AFTER `collection`;
ALTER TABLE `gfb_dict_item` ADD COLUMN `DEL`  int(11) NULL DEFAULT 0 COMMENT '是否删除：0.存活，1.删除' AFTER `UPDATE_ID`;
ALTER TABLE `gfb_dict_item` DROP COLUMN `IS_DEL`;
ALTER TABLE `gfb_dict_value` ADD COLUMN `DEL`  int(11) NULL DEFAULT 0 COMMENT '是否删除：0.存活，1.删除' AFTER `NAME`;
ALTER TABLE `gfb_dict_value` DROP COLUMN `IS_DEL`;
ALTER TABLE `gfb_failed_jobs` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_finance_plan` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `status`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态; 0：待审；1：购买中；2：初审不通过；3：结束购买；4：复审不通过；5：已取消；' ,
  `name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '计划名称' ,
  `money`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '计划金额（分）' ,
  `money_yes`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '加入总金额' ,
  `base_apr`  int(5) UNSIGNED NOT NULL DEFAULT 0 COMMENT '预期年化利率（不代表实际利息收益）' ,
  `time_limit`  int(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '期限' ,
  `lock_period`  int(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定期' ,
  `lowest`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个用户加入金额最小阈值' ,
  `append_multiple_amount`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '加入金额必须为该值的整数倍递增' ,
  `most`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个用户加入金额最大阈值' ,
  `success_at`  datetime NULL DEFAULT NULL COMMENT '结束购买时间' ,
  `end_lock_at`  datetime NULL DEFAULT NULL COMMENT '退出日期' ,
  `finished_state`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '结束状态' ,
  `total_sub_point`  int(5) UNSIGNED NOT NULL DEFAULT 0 COMMENT '加入总人次' ,
  `sub_point_count`  int(5) UNSIGNED NOT NULL DEFAULT 0 COMMENT '加入人数（同一用户多次加入进行合并）' ,
  `create_id`  int(10) UNSIGNED NOT NULL COMMENT '创建用户' ,
  `update_id`  int(10) UNSIGNED NOT NULL COMMENT '更新用户' ,
  `description`  text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '计划简介' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`),
  INDEX `finance_plan_finish_state_index` (`finished_state`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_finance_plan_buyer` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `user_id`  int(10) UNSIGNED NOT NULL COMMENT '用户id' ,
  `plan_id`  int(10) UNSIGNED NOT NULL COMMENT '计划id' ,
  `status`  int(11) NULL DEFAULT 0 COMMENT '状态；0：失败；1：成功；2.取消' ,
  `base_apr`  int(5) UNSIGNED NOT NULL DEFAULT 0 COMMENT '预期年化利率（不代表实际利息收益）' ,
  `apr`  int(5) UNSIGNED NOT NULL DEFAULT 0 COMMENT '实际年化利率' ,
  `money`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '加入金额（分）' ,
  `valid_money`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '有效金额（分）' ,
  `right_money`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '已匹配金额' ,
  `left_money`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '剩余金额' ,
  `end_lock_at`  datetime NULL DEFAULT NULL COMMENT '退出日期' ,
  `finished_state`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '结束状态' ,
  `source`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '来源；0：PC；1：ANDROID；2：IOS；3：H5' ,
  `remark`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '备注' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  `freeze_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '冻结资产orderid' ,
  PRIMARY KEY (`id`),
  CONSTRAINT `finance_plan_buyer_plan_id_foreign` FOREIGN KEY (`plan_id`) REFERENCES `gfb_finance_plan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `finance_plan_buyer_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `gfb_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `finance_plan_buyer_plan_id_foreign` (`plan_id`) USING BTREE ,
  INDEX `finance_plan_buyer_user_id_foreign` (`user_id`) USING BTREE ,
  INDEX `finance_plan_buyer_finish_state_end_lock_at_index` (`finished_state`, `end_lock_at`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_finance_plan_collection` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `user_id`  int(10) UNSIGNED NOT NULL COMMENT '用户id' ,
  `plan_id`  int(10) UNSIGNED NOT NULL COMMENT '计划id' ,
  `buyer_id`  int(10) UNSIGNED NOT NULL COMMENT '购买记录id' ,
  `order_num`  int(2) UNSIGNED NOT NULL COMMENT '期数' ,
  `status`  int(11) NULL DEFAULT 0 COMMENT '状态；0：计息中；1：已结息' ,
  `principal`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '本金（分）' ,
  `interest`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '利息（分）' ,
  `start_at`  datetime NULL DEFAULT NULL COMMENT '起息时间' ,
  `collection_at`  datetime NULL DEFAULT NULL COMMENT '结息时间' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`),
  CONSTRAINT `finance_plan_collection_buyer_id_foreign` FOREIGN KEY (`buyer_id`) REFERENCES `gfb_finance_plan_buyer` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `finance_plan_collection_plan_id_foreign` FOREIGN KEY (`plan_id`) REFERENCES `gfb_finance_plan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `finance_plan_collection_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `gfb_users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `finance_plan_collection_buyer_id_foreign` (`buyer_id`) USING BTREE ,
  INDEX `finance_plan_collection_plan_id_foreign` (`plan_id`) USING BTREE ,
  INDEX `finance_plan_collection_user_id_foreign` (`user_id`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_finance_plan_pre_tender_log` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `status`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态；0：等待匹配；1：已匹配；2：匹配失败' ,
  `borrow_id`  int(10) UNSIGNED NOT NULL COMMENT '借款id' ,
  `tender_id`  int(10) UNSIGNED NOT NULL COMMENT '投标id' ,
  `plan_id`  int(10) UNSIGNED NOT NULL COMMENT '计划id' ,
  `money`  int(10) UNSIGNED NOT NULL COMMENT '匹配金额' ,
  `close_at`  datetime NULL DEFAULT NULL COMMENT '结标日期' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_finance_plan_tender_log` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `borrow_id`  int(10) UNSIGNED NOT NULL COMMENT '借款id' ,
  `tender_id`  int(10) UNSIGNED NOT NULL COMMENT '投标id' ,
  `plan_id`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '计划id' ,
  `buyer_id`  int(10) UNSIGNED NOT NULL COMMENT '购买记录id' ,
  `money`  int(10) UNSIGNED NOT NULL COMMENT '匹配金额' ,
  `left_money`  int(10) UNSIGNED NOT NULL COMMENT '剩余未回本金' ,
  `transfer_buy_id`  int(10) UNSIGNED NOT NULL COMMENT '债转购买记录' ,
  `transfer_flag`  tinyint(1) UNSIGNED NOT NULL COMMENT '转让标识' ,
  `finnished_state`  tinyint(1) UNSIGNED NOT NULL COMMENT '结束状态（所有本金已回款或已转让）' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_find` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `status`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0：隐藏；1：显示' ,
  `title`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' ,
  `icon`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' ,
  `url`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' ,
  `order`  int(10) NOT NULL DEFAULT 0 COMMENT '排序；降序' ,
  `create_id`  int(10) UNSIGNED NOT NULL COMMENT '创建用户' ,
  `update_id`  int(10) UNSIGNED NOT NULL COMMENT '更新用户' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_incr_statistic` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `no_use_money_sum`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `use_money_sum`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `tj_sum_publish`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `no_use_money_sum`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `jz_sum_publish`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `tj_sum_publish`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `lz_sum_publish`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `jz_sum_publish`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `qd_sum_publish`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `lz_sum_publish`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `miao_sum_publish`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `qd_sum_publish`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `tj_sum_success`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `miao_sum_publish`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `jz_sum_success`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `tj_sum_success`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `lz_sum_success`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `jz_sum_success`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `qd_sum_success`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `lz_sum_success`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `miao_sum_success`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `qd_sum_success`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `tj_sum_repay`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `miao_sum_success`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `tj_sum_repay_principal`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `tj_sum_repay`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `jz_sum_repay`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `tj_sum_repay_principal`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `qd_sum_repay`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `jz_sum_repay`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `qd_sum_repay_principal`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `qd_sum_repay`;
ALTER TABLE `gfb_incr_statistic` MODIFY COLUMN `jz_sum_repay_principal`  int(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `qd_sum_repay_principal`;
ALTER TABLE `gfb_integral` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_integral` MODIFY COLUMN `use_integral`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户积分' AFTER `user_id`;
ALTER TABLE `gfb_integral` MODIFY COLUMN `no_use_integral`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '冻结积分' AFTER `use_integral`;
ALTER TABLE `gfb_integral_log` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_jixin_tx_log` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `seq_no`  varchar(225) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' ,
  `body`  varchar(10240) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '请求体或者响应体' ,
  `create_at`  datetime NULL DEFAULT NULL ,
  `tx_type`  varchar(225) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '交易类型' ,
  `tx_type_desc`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '交易类型描述' ,
  `type`  int(11) NULL DEFAULT 0 COMMENT '日志类型: 0: 请求 1.响应' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_jobs` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_lend` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_lend` ADD COLUMN `iparam1`  int(11) NULL DEFAULT NULL AFTER `updated_at`;
ALTER TABLE `gfb_lend` ADD COLUMN `iparam2`  int(11) NULL DEFAULT NULL AFTER `iparam1`;
ALTER TABLE `gfb_lend` ADD COLUMN `iparam3`  int(11) NULL DEFAULT NULL AFTER `iparam2`;
ALTER TABLE `gfb_lend` ADD COLUMN `vparam1`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `iparam3`;
ALTER TABLE `gfb_lend` ADD COLUMN `vparam2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `vparam1`;
ALTER TABLE `gfb_lend` ADD COLUMN `vparam3`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL AFTER `vparam2`;
ALTER TABLE `gfb_lend` ADD COLUMN `t_user_id`  int(11) NULL DEFAULT NULL COMMENT '银行电子账户标 id' AFTER `vparam3`;
ALTER TABLE `gfb_lend_blacklist` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_lianhanghao_area` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `pid`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级ID' ,
  `name`  varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '名称' ,
  `level`  int(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '层级' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_lianhanghao_bank` (
  `id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name`  varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '名称' ,
  `sort`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序（升序）' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_marketing` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `marketing_type`  int(11) NULL DEFAULT 0 COMMENT '促销类型: 1.红包, 2.积分, 3.现金券' ,
  `titel`  varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' ,
  `introduction`  varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '简介' ,
  `targer_url`  varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '活动展示页面' ,
  `view_url`  varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '预览图url' ,
  `begin_time`  datetime NULL DEFAULT NULL COMMENT '活动开始时间' ,
  `end_time`  datetime NULL DEFAULT NULL COMMENT '活动结束时间' ,
  `open_state`  int(11) NULL DEFAULT 0 COMMENT '启用状态(关闭, 开启)' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '记录有效状态(0,有效, 1.无效)' ,
  `create_time`  datetime NULL DEFAULT NULL ,
  `update_time`  datetime NULL DEFAULT NULL ,
  `parent_state`  int(11) NULL DEFAULT 0 COMMENT '是否针对邀请人' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_marketing_condition` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `marketing_id`  int(11) NULL DEFAULT 0 COMMENT '活动类型表' ,
  `tender_money_min`  bigint(20) NULL DEFAULT 0 COMMENT '最小投标金额' ,
  `register_min_time`  datetime NULL DEFAULT NULL COMMENT '注册时间' ,
  `recharge_money_min`  bigint(20) NULL DEFAULT 0 COMMENT '充值金额' ,
  `open_account_min_time`  datetime NULL DEFAULT NULL COMMENT '开户时间' ,
  `login_min_time`  datetime NULL DEFAULT NULL COMMENT '登录时间' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '有效状态: 0.有效, 1.无效' ,
  `create_time`  datetime NULL DEFAULT NULL COMMENT '创建时间' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_marketing_dimension` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `marketing_id`  int(11) NULL DEFAULT 0 COMMENT '营销活动ID' ,
  `platform`  varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '活动的平台类型, 可以使用多个(0,pc, 1.android, 2, ios, 3.h5)' ,
  `borrow_type`  varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '标的类型:(1.车贷标, 2.渠道标, 3.流转表, 净值标,-2.新手标)' ,
  `member_type`  int(11) NULL DEFAULT 0 COMMENT '0.不选, 1.新用户, 2.老用户' ,
  `channel_type`  varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '渠道用户类型(0.pc, 1.android, 2.ios, 3.h5, 4.类型)' ,
  `parent_state`  int(11) NULL DEFAULT 0 COMMENT '被邀请人:0, 赠送被邀请人, 1.赠送邀请人' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '有效状态: 0.有效, 1.无效' ,
  `create_time`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_marketing_redpack_record` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `marketing_id`  int(11) NULL DEFAULT 0 ,
  `redpack_rule_id`  int(11) NULL DEFAULT 0 ,
  `user_id`  int(11) NULL DEFAULT 0 COMMENT '用户ID' ,
  `source_id`  int(11) NULL DEFAULT 0 COMMENT '来源ID' ,
  `money`  bigint(20) NULL DEFAULT 0 COMMENT '红包金额' ,
  `markeing_titel`  varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '红包状态,0.待开启, 1.已开, 2.作废' ,
  `publish_time`  datetime NULL DEFAULT NULL COMMENT '发放时间' ,
  `open_time`  datetime NULL DEFAULT NULL COMMENT '开启红包时间' ,
  `cancel_time`  datetime NULL DEFAULT NULL COMMENT '作废时间' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '有效状态: 0.有效, 1.无效' ,
  `remark`  varchar(225) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '活动类型' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_marketing_redpack_rule` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `marketing_id`  int(11) NULL DEFAULT 0 ,
  `rule_type`  int(11) NULL DEFAULT 0 COMMENT '红包类型: 1.投资金额随机百分比,2.投资金额规定百分比, 3.随机金额, 4.规定金额, 5.年化率' ,
  `tender_money_min`  decimal(10,6) NULL DEFAULT 0.000000 COMMENT '投标金额*随机最小值' ,
  `tender_money_max`  decimal(10,6) NULL DEFAULT 0.000000 COMMENT '投标金额*随机最大值' ,
  `money_min`  bigint(20) NULL DEFAULT 0 COMMENT '固定金额最小值' ,
  `money_max`  bigint(20) NULL DEFAULT 0 COMMENT '固定金额最大金额' ,
  `apr`  decimal(10,6) NULL DEFAULT 0.000000 COMMENT '年化收益' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '有效状态(0.有效, 1.无效)' ,
  `create_time`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_migrations` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_new_asset_log` (
  `id`  bigint(20) NOT NULL AUTO_INCREMENT ,
  `op_name`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '操作名称' ,
  `op_money`  bigint(20) NULL DEFAULT 0 COMMENT '操作金额' ,
  `use_money`  bigint(20) NULL DEFAULT 0 COMMENT '可用金额' ,
  `no_use_money`  bigint(20) NULL DEFAULT 0 COMMENT '冻结金额' ,
  `user_id`  int(11) NULL DEFAULT 0 COMMENT '操作人ID' ,
  `for_user_id`  int(11) NULL DEFAULT 0 COMMENT '对手账户ID' ,
  `platform_type`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '存管平台类型' ,
  `local_type`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '本地交易类型' ,
  `tx_flag`  varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'D' COMMENT '交易金额符号: 小于零等于C；大于零等于D；' ,
  `local_seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '本地交易流水' ,
  `curr_money`  bigint(20) NULL DEFAULT 0 COMMENT '当前用户账户余额(可用+ 冻结)' ,
  `source_id`  int(11) NULL DEFAULT 0 COMMENT '来源ID' ,
  `create_time`  datetime NULL DEFAULT NULL COMMENT '创建时间' ,
  `remark`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '标识' ,
  `group_op_seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '同组操作标识' ,
  `syn_state`  int(11) NULL DEFAULT 0 COMMENT '对账标识, 0.未同步, 1.同步' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '有效状态标识: 0.有效, 1.无效' ,
  `syn_time`  datetime NULL DEFAULT NULL COMMENT '对账时间' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_notices` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_notices` MODIFY COLUMN `read`  int(11) NOT NULL DEFAULT 0 COMMENT '是否阅读（0、未读；1、已读）' AFTER `user_id`;
ALTER TABLE `gfb_password_resets` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_permission_role` MODIFY COLUMN `permission_id`  int(10) UNSIGNED NOT NULL FIRST ;
ALTER TABLE `gfb_permission_role` MODIFY COLUMN `role_id`  int(10) UNSIGNED NOT NULL AFTER `permission_id`;
ALTER TABLE `gfb_permissions` MODIFY COLUMN `name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `id`;
ALTER TABLE `gfb_permissions` MODIFY COLUMN `display_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `name`;
ALTER TABLE `gfb_permissions` MODIFY COLUMN `description`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `display_name`;
ALTER TABLE `gfb_permissions` MODIFY COLUMN `created_at`  timestamp NULL DEFAULT NULL AFTER `description`;
ALTER TABLE `gfb_permissions` MODIFY COLUMN `updated_at`  timestamp NULL DEFAULT NULL AFTER `created_at`;
CREATE TABLE `gfb_recharge_detail_log` (
  `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标示' ,
  `user_id`  int(11) NOT NULL COMMENT '用户ID' ,
  `seq_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '交易流水号' ,
  `create_time`  datetime NULL DEFAULT NULL ,
  `callback_time`  datetime NULL DEFAULT NULL ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '充值状态：0：充值请求。1.充值成功。2.充值失败' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '0.有效记录1.无效记录' ,
  `recharge_type`  int(11) NULL DEFAULT 0 COMMENT '充值类型：0.渠道充值1.线下转账' ,
  `card_no`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '卡号' ,
  `bank_name`  varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '充值银行' ,
  `money`  int(11) NULL DEFAULT 0 COMMENT '充值金额' ,
  `recharge_source`  int(11) NULL DEFAULT 0 COMMENT '充值来源：0.pc1.html52.android3.ios' ,
  `recharge_channel`  int(11) NULL DEFAULT 0 COMMENT '充值渠道：0.江西银行（线上）1.其他' ,
  `remark`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '备注' ,
  `mobile`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '手机' ,
  `update_time`  datetime NULL DEFAULT NULL COMMENT '更新时间' ,
  `ip`  varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT 'ip' ,
  `response_message`  varchar(2048) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_recharge_log` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_role_user` MODIFY COLUMN `user_id`  int(10) UNSIGNED NOT NULL FIRST ;
ALTER TABLE `gfb_role_user` MODIFY COLUMN `role_id`  int(10) UNSIGNED NOT NULL AFTER `user_id`;
ALTER TABLE `gfb_roles` MODIFY COLUMN `name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `id`;
ALTER TABLE `gfb_roles` MODIFY COLUMN `display_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `name`;
ALTER TABLE `gfb_roles` MODIFY COLUMN `description`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `display_name`;
ALTER TABLE `gfb_roles` MODIFY COLUMN `created_at`  timestamp NULL DEFAULT NULL AFTER `description`;
ALTER TABLE `gfb_roles` MODIFY COLUMN `updated_at`  timestamp NULL DEFAULT NULL AFTER `created_at`;
ALTER TABLE `gfb_sms_notice_settings` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_statistic` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_statistic` ADD COLUMN `id`  int(11) NOT NULL AUTO_INCREMENT AFTER `updated_at`, ADD PRIMARY KEY (`id`);
CREATE TABLE `gfb_suggest` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `content`  varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '意见建议内容' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `gfb_suggest_id_uindex` (`id`) USING BTREE
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_task_scheduler` (
  `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标示' ,
  `type`  int(11) NULL DEFAULT 0 COMMENT '调度类型: 0:委托申请状态查询, 1.提现状态调度' ,
  `task_num`  int(11) NULL DEFAULT 10 COMMENT '调度次数' ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '0:未成功 1.成功' ,
  `task_data`  varchar(1024) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '调度任务数据' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '是否有效: 0有效 ,1 无效' ,
  `do_task_num`  int(11) NULL DEFAULT 0 COMMENT '已经执行次数' ,
  `do_task_data`  varchar(10240) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT '' COMMENT '每次执行结果累加' ,
  `create_at`  datetime NULL DEFAULT NULL COMMENT '创建时间' ,
  `update_at`  datetime NULL DEFAULT NULL COMMENT '修改时间' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_third_batch_log` (
  `ID`  int(11) NOT NULL AUTO_INCREMENT ,
  `BATCH_NO`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  `TYPE`  int(11) NULL DEFAULT NULL COMMENT '1.投资人批次购买债权 2.即信批次放款 3.批次即信批次还款 4.批次担保人垫付 5.批次融资人还担保账户垫款 6.批次结束投资人债权 7.提前结清批次还款' ,
  `SOURCE_ID`  int(11) NULL DEFAULT NULL ,
  `REMARK`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  `STATE`  int(11) NULL DEFAULT 0 COMMENT '批次状态 0未处理 1参数校验通过 2参数校验不通过 3已处理  4.已处理存在失败批次' ,
  `ACQ_RES`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '批次请求保留域' ,
  `IPARAM1`  int(11) NULL DEFAULT NULL ,
  `IPARAM2`  int(11) NULL DEFAULT NULL ,
  `IPARAM3`  int(11) NULL DEFAULT NULL ,
  `VPARAM1`  varchar(2048) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  `VPARAM2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  `VPARAM3`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
  `CREATE_AT`  datetime NULL DEFAULT NULL ,
  `UPDATE_AT`  datetime NULL DEFAULT NULL ,
  PRIMARY KEY (`ID`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_ticheng_user` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
CREATE TABLE `gfb_transfer` (
  `id`  int(11) NOT NULL AUTO_INCREMENT COMMENT 'id' ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '0.待审核 1.转让中 2.已转让 3.审核未通过 4.已取消' ,
  `type`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '类型；0：普通；1：理财计划 2.垫付；' ,
  `title`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转让标题' ,
  `transfer_money`  int(255) NULL DEFAULT NULL COMMENT '转让总金额（本金+当期应计利息）' ,
  `principal`  int(20) NULL DEFAULT 0 COMMENT '转让金额' ,
  `already_interest`  int(20) NULL DEFAULT 0 COMMENT '当期应计利息' ,
  `transfer_money_yes`  int(11) NULL DEFAULT NULL COMMENT '已购买金额' ,
  `time_limit`  int(11) NULL DEFAULT 0 COMMENT '剩余期数' ,
  `start_order`  int(11) NULL DEFAULT NULL COMMENT '开始转让期数' ,
  `end_order`  int(11) NULL DEFAULT NULL COMMENT '结束转让期数' ,
  `apr`  int(11) NULL DEFAULT NULL COMMENT '年利率' ,
  `tender_count`  int(11) NULL DEFAULT 0 COMMENT '投标次数' ,
  `is_lock`  int(11) NULL DEFAULT 0 COMMENT '是否锁定 0否 1是' ,
  `lowest`  int(11) NULL DEFAULT NULL ,
  `repay_at`  datetime NULL DEFAULT NULL COMMENT '下一个还款日' ,
  `tender_id`  int(11) NULL DEFAULT NULL COMMENT '投资人投标id' ,
  `borrow_id`  int(11) NULL DEFAULT NULL COMMENT '投资借款id' ,
  `user_id`  int(11) NULL DEFAULT NULL COMMENT '转让人id' ,
  `is_all`  int(11) NULL DEFAULT 0 COMMENT '是否是全部期数转让 0否 1是' ,
  `borrow_collection_ids`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '部分转让时候 转让期数集合' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '0.未删除 1.已删除' ,
  `release_at`  datetime NULL DEFAULT NULL COMMENT '发布时间' ,
  `verify_at`  datetime NULL DEFAULT NULL COMMENT '初审时间' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  `success_at`  datetime NULL DEFAULT NULL COMMENT '满标时间' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
CREATE TABLE `gfb_transfer_buy_log` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `state`  int(11) NULL DEFAULT 0 COMMENT '状态： 0.购买中  1.成功购买 2.失败购买, 3.取消购买' ,
  `transfer_id`  int(11) NULL DEFAULT NULL ,
  `user_id`  int(11) NULL DEFAULT NULL COMMENT '购买债权用户id' ,
  `buy_money`  int(11) NULL DEFAULT NULL ,
  `valid_money`  int(11) NULL DEFAULT NULL COMMENT '有效金额（本金+当期应计利息）' ,
  `auto_order`  int(11) NULL DEFAULT 0 COMMENT '自动购买债权转让 order' ,
  `principal`  int(11) NULL DEFAULT NULL COMMENT '购买转让本金' ,
  `already_interest`  int(11) NULL DEFAULT NULL COMMENT '当期应计利息' ,
  `source`  int(11) NULL DEFAULT 0 COMMENT '投标来源；0 pc 1：android；2：ios 3：H5' ,
  `auto`  int(11) NULL DEFAULT 0 COMMENT '是否自动购买债权转让 0否 1是' ,
  `del`  int(11) NULL DEFAULT 0 COMMENT '是否删除 0否 1是' ,
  `freeze_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '冻结资产orderid' ,
  `created_at`  datetime NULL DEFAULT NULL ,
  `updated_at`  datetime NULL DEFAULT NULL ,
  `third_transfer_flag`  int(11) NULL DEFAULT 0 COMMENT '标识是否在存管系统登记购买债权， 0否 1是' ,
  `third_transfer_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT '购买债券转让编号' ,
  `transfer_auth_code`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '即信债权转让授权码' ,
  `type`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '类型；0：普通；1：理财计划；' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_user_attachment` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_user_cache` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `income_interest`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '已赚利息' AFTER `user_id`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `income_award`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '已转奖励' AFTER `income_interest`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `income_overdue`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '逾期收入' AFTER `income_award`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `income_integral_cash`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '积分折现' AFTER `income_overdue`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `income_bonus`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '提成收入（推荐人）' AFTER `income_integral_cash`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `income_other`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '其他收入' AFTER `income_bonus`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `wait_collection_principal`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '待收本金' AFTER `income_other`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `wait_collection_interest`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '待收利息' AFTER `wait_collection_principal`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tj_wait_collection_principal`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '车贷标代收本金' AFTER `wait_collection_interest`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tj_wait_collection_interest`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '车贷标代收利息' AFTER `tj_wait_collection_principal`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `qd_wait_collection_principal`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '渠道标代收本金' AFTER `tj_wait_collection_interest`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `qd_wait_collection_interest`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '渠道标代收利息' AFTER `qd_wait_collection_principal`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `expenditure_interest`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '利息支出' AFTER `qd_wait_collection_interest`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `expenditure_interest_manage`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '利息管理费支出' AFTER `expenditure_interest`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `expenditure_manage`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '账户管理费支出' AFTER `expenditure_interest_manage`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `expenditure_fee`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '费用支出' AFTER `expenditure_manage`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `expenditure_overdue`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '逾期支出' AFTER `expenditure_fee`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `expenditure_other`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '其他支出' AFTER `expenditure_overdue`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `wait_repay_principal`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '待还本金' AFTER `expenditure_other`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `wait_repay_interest`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '待还利息' AFTER `wait_repay_principal`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tender_tuijian`  int(10) NOT NULL DEFAULT 0 COMMENT '首投车贷标' AFTER `wait_repay_interest`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tender_jingzhi`  int(10) NOT NULL DEFAULT 0 COMMENT '首投净值标' AFTER `tender_tuijian`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tender_miao`  int(10) NOT NULL DEFAULT 0 COMMENT '首投秒标' AFTER `tender_jingzhi`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tender_transfer`  int(10) NOT NULL DEFAULT 0 COMMENT '首投转让标' AFTER `tender_miao`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `tender_qudao`  int(10) NULL DEFAULT 0 COMMENT '首投渠道标' AFTER `tender_transfer`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `first_tender_award`  smallint(5) UNSIGNED NOT NULL DEFAULT 0 COMMENT '首投奖励' AFTER `tender_qudao`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `award_virtual_money`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '赠送体验金' AFTER `first_tender_award`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `recharge_total`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '充值总额' AFTER `award_virtual_money`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `yesterday_use_money`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '昨日可用余额' AFTER `updated_at`;
ALTER TABLE `gfb_user_cache` MODIFY COLUMN `cash_total`  bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '提现总额' AFTER `yesterday_use_money`;
ALTER TABLE `gfb_user_info` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_user_info` MODIFY COLUMN `education`  int(11) NULL DEFAULT 0 COMMENT '最高学历 0:小学；1：初中;2:高中;3:中专;4:专科;5:本科;6:研究生;7:硕士;8:博士;9:博士后' AFTER `qq`;
ALTER TABLE `gfb_user_info` MODIFY COLUMN `industry`  int(11) NULL DEFAULT 0 COMMENT '专业' AFTER `housing`;
ALTER TABLE `gfb_user_info` MODIFY COLUMN `graduation`  int(11) NULL DEFAULT 0 COMMENT '月收入 0：1000元以下;1：1001-2000;2:2001-3000;3:3001-4000;4:4001-5000;5:5001-8000;6:8001-10000;7:10001-30000;8:30001-50000;9:5万以上' AFTER `industry`;
ALTER TABLE `gfb_user_info` MODIFY COLUMN `birthday_y`  smallint(4) NOT NULL DEFAULT 0 COMMENT '生日月' AFTER `updated_at`;
ALTER TABLE `gfb_user_info` MODIFY COLUMN `birthday_md`  smallint(4) NOT NULL DEFAULT 0 COMMENT '生日日' AFTER `birthday_y`;
CREATE TABLE `gfb_user_third_account` (
  `id`  int(11) NOT NULL AUTO_INCREMENT ,
  `user_id`  int(11) NULL DEFAULT 0 COMMENT '用户Id' ,
  `account_id`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '电子账户账号' ,
  `name`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '真实姓名' ,
  `acct_use`  int(11) NULL DEFAULT 0 COMMENT '0.普通用户；1.红包账户，2.企业账户' ,
  `card_no`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '银行卡' ,
  `id_type`  int(11) NULL DEFAULT 1 COMMENT '证件类型。 1身份证' ,
  `id_no`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '证件号码' ,
  `mobile`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '开户手机' ,
  `channel`  int(11) NULL DEFAULT 0 COMMENT '渠道' ,
  `password_state`  int(11) NULL DEFAULT 0 COMMENT '初始密码状态（0，未初始化，1.初始化）' ,
  `card_no_bind_state`  int(11) NULL DEFAULT 1 COMMENT '银行卡绑定状态（0，未绑定，1.已绑定）' ,
  `create_at`  datetime NULL DEFAULT NULL ,
  `update_at`  datetime NULL DEFAULT NULL ,
  `create_id`  int(11) NULL DEFAULT 0 ,
  `update_id`  int(11) NULL DEFAULT 0 ,
  `del`  int(11) NULL DEFAULT NULL COMMENT '0，有效， 1.无效' ,
  `auto_tender_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '自动投标签约订单号' ,
  `auto_tender_tx_amount`  int(12) NULL DEFAULT 0 COMMENT '单笔投标金额的上限' ,
  `auto_tender_tot_amount`  int(12) NULL DEFAULT 0 COMMENT '自动投标总金额上限' ,
  `auto_transfer_bond_order_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '自动债券转让签约单号' ,
  `auto_tender_state`  int(11) NULL DEFAULT 0 ,
  `auto_transfer_state`  int(11) NULL DEFAULT 0 ,
  `bank_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' ,
  `bank_logo`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' ,
  PRIMARY KEY (`id`)
)
  ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
  ROW_FORMAT=Compact
;
ALTER TABLE `gfb_users` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_users` MODIFY COLUMN `realname`  varchar(25) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '真实姓名' AFTER `pay_password`;
ALTER TABLE `gfb_users` MODIFY COLUMN `type`  varchar(10) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT 'borrower' COMMENT '用户类型（manager：管理员；borrower：浏览者；financer:理财用户）' AFTER `is_lock`;
ALTER TABLE `gfb_users` ADD COLUMN `avatar_path`  varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT '头像地址' AFTER `updated_at`;
ALTER TABLE `gfb_users` ADD COLUMN `push_state`  int(11) NULL DEFAULT 1 AFTER `avatar_path`;
ALTER TABLE `gfb_users` ADD COLUMN `push_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' AFTER `push_state`;
ALTER TABLE `gfb_users` ADD COLUMN `platform`  int(11) NULL DEFAULT '-1' COMMENT '最近登录的平台' AFTER `push_id`;
ALTER TABLE `gfb_users` ADD COLUMN `ip`  varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT '' AFTER `platform`;
ALTER TABLE `gfb_users` ADD COLUMN `login_time`  datetime NULL DEFAULT NULL COMMENT '最近一次登录时间' AFTER `ip`;
ALTER TABLE `gfb_users` ADD COLUMN `windmill_id`  varchar(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '风车理财id' AFTER `login_time`;
ALTER TABLE `gfb_vip` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_yesterday_asset` DEFAULT CHARACTER SET=utf8 COLLATE=utf8_unicode_ci;
ALTER TABLE `gfb_yesterday_asset` MODIFY COLUMN `use_money`  bigint(20) NOT NULL COMMENT '可用金额（分）' AFTER `user_id`;
ALTER TABLE `gfb_yesterday_asset` MODIFY COLUMN `no_use_money`  bigint(20) NOT NULL COMMENT '冻结金额（分）' AFTER `use_money`;
ALTER TABLE `gfb_yesterday_asset` MODIFY COLUMN `virtual_money`  bigint(20) NOT NULL COMMENT '体验金（分）' AFTER `no_use_money`;
ALTER TABLE `gfb_yesterday_asset` MODIFY COLUMN `collection`  bigint(20) NOT NULL COMMENT '代收金额（分）' AFTER `virtual_money`;
ALTER TABLE `gfb_yesterday_asset` MODIFY COLUMN `payment`  bigint(20) NOT NULL COMMENT '待还金额（分）' AFTER `collection`;
ALTER TABLE gfb_sms RENAME TO gfb_sms_log;
SET FOREIGN_KEY_CHECKS=1;