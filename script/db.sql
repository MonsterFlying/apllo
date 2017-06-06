ALTER TABLE `gfb_borrow` ADD COLUMN `tx_fee` int(11) DEFAULT NULL COMMENT '借款手续费(选填）';
ALTER TABLE `gfb_borrow` ADD COLUMN `iparam1` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow` ADD COLUMN `iparam2` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow` ADD COLUMN `iparam3` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow` ADD COLUMN `vparam1` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow` ADD COLUMN `vparam2` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow` ADD COLUMN `vparam3` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow` ADD COLUMN `t_user_id` int(11) DEFAULT NULL COMMENT '银行电子账户标 id';

ALTER TABLE `gfb_borrow_tender` ADD COLUMN `auth_code` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '即信债权授权码';
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `iparam1` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `iparam2` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `iparam3` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `vparam1` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `vparam2` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `vparam3` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_tender` ADD COLUMN `t_user_id` int(11) DEFAULT NULL COMMENT '银行电子账户标 id';

ALTER TABLE `gfb_borrow_collection` ADD COLUMN `borrow_id` int(11) DEFAULT NULL COMMENT '借款id',;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `user_id` int(11) DEFAULT NULL COMMENT '投标会员id';
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `iparam1` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `iparam2` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `iparam3` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `vparam1` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `vparam2` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `vparam3` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_collection` ADD COLUMN `t_user_id` int(11) DEFAULT NULL COMMENT '银行电子账户标 id';

ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `user_id` int(11) DEFAULT NULL COMMENT '借款人id';
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `iparam1` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `iparam2` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `iparam3` int(11) DEFAULT NULL;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `vparam1` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `vparam2` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `vparam3` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_borrow_repayment` ADD COLUMN `t_user_id` int(11) DEFAULT NULL COMMENT '银行电子账户标 id';

ALTER TABLE `gfb_lend` ADD COLUMN `iparam1` int(11) DEFAULT NULL;
ALTER TABLE `gfb_lend` ADD COLUMN `iparam2` int(11) DEFAULT NULL;
ALTER TABLE `gfb_lend` ADD COLUMN `iparam3` int(11) DEFAULT NULL;
ALTER TABLE `gfb_lend` ADD COLUMN `vparam1` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_lend` ADD COLUMN `vparam2` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_lend` ADD COLUMN `vparam3` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL;
ALTER TABLE `gfb_lend` ADD COLUMN `t_user_id` int(11) DEFAULT NULL COMMENT '银行电子账户标 id';


CREATE TABLE `gfb_user_third_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT '0' COMMENT '用户Id',
  `account_id` varchar(50) DEFAULT '' COMMENT '电子账户账号',
  `name` varchar(50) DEFAULT '' COMMENT '真实姓名',
  `acct_use` int(11) DEFAULT '0' COMMENT '0.普通用户；1.红包账户，2.企业账户',
  `card_no` varchar(50) DEFAULT '' COMMENT '银行卡',
  `id_type` int(11) DEFAULT '1' COMMENT '证件类型。 1身份证',
  `id_no` varchar(50) DEFAULT '' COMMENT '证件号码',
  `mobile` varchar(50) DEFAULT '' COMMENT '开户手机',
  `channel` int(11) DEFAULT '0' COMMENT '渠道',
  `password_state` int(11) DEFAULT '0' COMMENT '初始密码状态（0，未初始化，1.初始化）',
  `card_no_bind_state` int(11) DEFAULT '1' COMMENT '银行卡绑定状态（0，未绑定，1.已绑定）',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  `create_id` int(11) DEFAULT '0',
  `update_id` int(11) DEFAULT '0',
  `del` int(11) DEFAULT NULL COMMENT '0，有效， 1.无效',
  `auto_tender_order_id` varchar(255) DEFAULT NULL COMMENT '自动投标签约订单号',
  `auto_tender_tx_amount` int(12) DEFAULT '0' COMMENT '单笔投标金额的上限',
  `auto_tender_tot_amount` int(12) DEFAULT '0' COMMENT '自动投标总金额上限',
  `auto_transfer_bond_order_id` varchar(255) DEFAULT NULL COMMENT '自动债券转让签约单号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='银行电子账户标';

INSERT INTO `gfb_user_third_account` VALUES ('1', '901', '6212462040000050015', '崔灿', '1', '6222988812340046', '1', '342224198405191617', '18949830519', '2', '0', '1', '2017-05-23 14:15:07', '2017-05-23 14:15:07', '901', null, '0', null, null, null, null);

# 统计表添加自增字段
ALTER TABLE gfb_statistic ADD id INT NULL PRIMARY KEY AUTO_INCREMENT;

#修改gfb_user_info默认值
ALTER TABLE gfb_user_info ALTER COLUMN realname SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN card_pic1 SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN card_pic2 SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN qq SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN graduated_school SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN address SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN birthday_y SET DEFAULT 0;
ALTER TABLE gfb_user_info ALTER COLUMN birthday_md SET DEFAULT 0;

#修改gfb_asset默认值
ALTER TABLE gfb_asset ALTER COLUMN use_money SET DEFAULT 0;
ALTER TABLE gfb_asset ALTER COLUMN no_use_money SET DEFAULT 0;
ALTER TABLE gfb_asset ALTER COLUMN virtual_money SET DEFAULT 0;
ALTER TABLE gfb_asset ALTER COLUMN collection SET DEFAULT 0;
ALTER TABLE gfb_asset ALTER COLUMN payment SET DEFAULT 0;
ALTER TABLE gfb_asset ALTER COLUMN updated_at SET DEFAULT 0;


#修改gfb_user_cache默认值
ALTER TABLE gfb_user_cache ALTER COLUMN income_interest SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN income_award SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN income_overdue SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN income_integral_cash SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN income_bonus SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN income_other SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN wait_collection_principal SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN wait_collection_interest SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tj_wait_collection_principal SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tj_wait_collection_interest SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN qd_wait_collection_principal SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN qd_wait_collection_interest SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN expenditure_interest SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN expenditure_interest_manage SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN expenditure_manage SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN expenditure_fee SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN expenditure_overdue SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN expenditure_other SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN wait_repay_principal SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN wait_repay_interest SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tender_tuijian SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tender_jingzhi SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tender_miao SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tender_transfer SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN tender_qudao SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN first_tender_award SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN award_virtual_money SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN recharge_total SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN yesterday_use_money SET DEFAULT 0;
ALTER TABLE gfb_user_cache ALTER COLUMN cash_total SET DEFAULT 0;

#修改gfb_integral默认值
ALTER TABLE gfb_integral ALTER COLUMN use_integral SET DEFAULT 0;
ALTER TABLE gfb_integral ALTER COLUMN no_use_integral SET DEFAULT 0;

#修改gfb_currency默认值
ALTER TABLE gfb_currency ALTER COLUMN use_currency SET DEFAULT 0;
ALTER TABLE gfb_currency ALTER COLUMN no_use_currency SET DEFAULT 0;

#修改gfb_users默认值
ALTER TABLE gfb_users ALTER COLUMN password SET DEFAULT '';
ALTER TABLE gfb_users ALTER COLUMN pay_password SET DEFAULT '';
ALTER TABLE gfb_users ALTER COLUMN realname SET DEFAULT '';
ALTER TABLE gfb_users ALTER COLUMN type SET DEFAULT 'borrower';

#修改数字字典
ALTER TABLE gfb_dict_item CHANGE IS_DEL DEL INT(11) DEFAULT '0' COMMENT '是否删除：0.存活，1.删除';
ALTER TABLE gfb_dict_value CHANGE IS_DEL DEL INT(11) DEFAULT '0' COMMENT '是否删除：0.存活，1.删除';