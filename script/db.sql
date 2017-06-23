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
  `auto_transfer_bond_order_id` varchar(255) DEFAULT NULL COMMENT '自动债权转让签约单号',
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

ALTER TABLE gfb_user_third_account ADD auto_tender_state INT DEFAULT 0 NULL;
ALTER TABLE gfb_user_third_account ADD auto_transfer_state INT DEFAULT 0 NULL;


#充值记录（新版）
CREATE TABLE gfb_recharge_detail_log
(
  id INT PRIMARY KEY COMMENT '唯一标示' AUTO_INCREMENT,
  user_id INT NOT NULL COMMENT '用户ID',
  seq_no VARCHAR(32) DEFAULT '' COMMENT '交易流水号',
  create_time DATETIME,
  callback_time DATETIME,
  state INT DEFAULT 0 COMMENT '充值状态：0：充值请求。1.充值成功。2.充值失败',
  del INT DEFAULT 0 COMMENT '0.有效记录 1.无效记录',
  recharge_type INT DEFAULT 0 COMMENT '充值类型： 0.渠道充值 1.线下转账',
  card_no VARCHAR(32) DEFAULT '' COMMENT '卡号',
  bank_name VARCHAR(32) DEFAULT '' COMMENT '充值银行',
  money INT DEFAULT 0 COMMENT '充值金额',
  recharge_source INT DEFAULT 0 COMMENT '充值来源： 0.pc 1.html5 2.android 3.ios',
  recharge_channel INT DEFAULT 0 COMMENT '充值渠道：0.江西银行（线上）1.其他',
  remark VARCHAR(255) DEFAULT '' COMMENT '备注',
  mobile VARCHAR(32) DEFAULT '' COMMENT '手机',
  update_time DATETIME COMMENT '更新时间',
  ip VARCHAR(32) DEFAULT '' COMMENT 'ip'
);

#修改第三方存管账户
ALTER TABLE gfb_user_third_account ADD bank_name VARCHAR(255) DEFAULT '其他' NULL;
ALTER TABLE gfb_user_third_account ADD bank_logo VARCHAR(255) DEFAULT '' NULL;


ALTER TABLE gfb_recharge_detail_log MODIFY bank_name VARCHAR(64) DEFAULT '' COMMENT '充值银行';
ALTER TABLE gfb_recharge_detail_log CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;


#创建提现表
create table gfb_cash_detail_log
(
	id int auto_increment
		primary key,
	user_id int default '0' not null comment '用户ID',
	third_account_id varchar(32) default '' null comment '银行存管账号',
	card_no varchar(32) default '' null comment '提现卡号',
	bank_name varchar(32) default '' null comment '提现银行名称',
	company_bank_no varchar(32) default '' null comment '联行号',
	money int default '0' null comment '提现金额',
	fee int default '0' null comment '费用',
	verify_user_id int default '0' null comment '审核人',
	verify_time datetime null comment '审核时间',
	verify_remark varchar(255) default '' null comment '审核备注',
	state int default '0' null comment '-1.取消提现.0:申请中,1.系统审核通过,2.系统审核不通过, 3.银行提现成功.4.银行提现失败.',
	create_time datetime null comment '提现申请时间',
	callbak_time datetime null comment '存管回调时间',
	cancel_time datetime null comment '取消时间',
	ip varchar(32) default '' null comment '提现IP',
	cash_type int default '0' null comment '0:渠道提现,1.人行提现'
);
ALTER TABLE gfb_cash_detail_log ADD seq_no VARCHAR(32) DEFAULT '' NULL;
ALTER TABLE gfb_cash_detail_log CHANGE callbak_time callback_time DATETIME COMMENT '存管回调时间';
ALTER TABLE gfb_recharge_detail_log ADD response_message VARCHAR(2048) NULL;


ALTER TABLE gfb_activity_red_packet ALTER COLUMN CREATE_UP SET DEFAULT 0;
ALTER TABLE gfb_activity_red_packet ALTER COLUMN UPDATE_UP SET DEFAULT 0;
ALTER TABLE gfb_activity_red_packet ALTER COLUMN IP SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN IPARAM1 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN IPARAM2 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN IPARAM3 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN VPARAM1 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN VPARAM2 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN VPARAM3 SET DEFAULT '';


ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN CREATE_UP SET DEFAULT 0;
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN UPDARE_UP SET DEFAULT 0;
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN VPARAM1 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN VPARAM2 SET DEFAULT '';


ALTER TABLE gofobao0524.gfb_borrow_tender MODIFY state INT(10) DEFAULT 1 COMMENT '1:投标中； 2:还款中 ;3:已结清';