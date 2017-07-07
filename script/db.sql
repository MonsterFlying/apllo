ALTER TABLE `gofobao0627`.`gfb_activity_red_packet`
  MODIFY COLUMN `CREATE_UP` int(11) NULL DEFAULT '0'
, MODIFY COLUMN `UPDATE_UP` int(11) NULL DEFAULT '0'
, MODIFY COLUMN `IP` varchar(24) NULL DEFAULT '' COMMENT '领取IP'
, MODIFY COLUMN `IPARAM1` varchar(50) NULL DEFAULT ''
, MODIFY COLUMN `IPARAM2` varchar(50) NULL DEFAULT ''
, MODIFY COLUMN `IPARAM3` varchar(50) NULL DEFAULT ''
, MODIFY COLUMN `VPARAM1` varchar(255) NULL DEFAULT ''
, MODIFY COLUMN `VPARAM2` varchar(255) NULL DEFAULT '';

ALTER TABLE `gofobao0627`.`gfb_activity_red_packet_log`
  MODIFY COLUMN `CREATE_UP` int(11) NULL DEFAULT '0'
, MODIFY COLUMN `UPDARE_UP` int(11) NULL DEFAULT '0'
, MODIFY COLUMN `VPARAM1` varchar(255) NULL DEFAULT ''
, MODIFY COLUMN `VPARAM2` varchar(255) NULL DEFAULT '';

CREATE TABLE `gofobao0627`.`gfb_cash_detail_log` (
  `id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '用户ID',
  `third_account_id` varchar(32) NULL DEFAULT '' COMMENT '银行存管账号',
  `card_no` varchar(32) NULL DEFAULT '' COMMENT '提现卡号',
  `bank_name` varchar(32) NULL DEFAULT '' COMMENT '提现银行名称',
  `company_bank_no` varchar(32) NULL DEFAULT '' COMMENT '联行号',
  `money` int(11) NULL DEFAULT '0' COMMENT '提现金额',
  `fee` int(11) NULL DEFAULT '0' COMMENT '费用',
  `verify_user_id` int(11) NULL DEFAULT '0' COMMENT '审核人',
  `verify_time` datetime NULL COMMENT '审核时间',
  `verify_remark` varchar(255) NULL DEFAULT '' COMMENT '审核备注',
  `state` int(11) NULL DEFAULT '0' COMMENT '-1.取消提现.0:申请中,1.系统审核通过,2.系统审核不通过, 3.银行提现成功.4.银行提现失败.',
  `create_time` datetime NULL COMMENT '提现申请时间',
  `callback_time` datetime NULL COMMENT '存管回调时间',
  `cancel_time` datetime NULL COMMENT '取消时间',
  `ip` varchar(32) NULL DEFAULT '' COMMENT '提现IP',
  `cash_type` int(11) NULL DEFAULT '0' COMMENT '0:渠道提现,1.人行提现',
  `seq_no` varchar(32) NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

ALTER TABLE `gofobao0627`.`gfb_permissions`
  MODIFY COLUMN `name` varchar(255) NOT NULL
, MODIFY COLUMN `display_name` varchar(255) NULL
, MODIFY COLUMN `description` varchar(255) NULL
, MODIFY COLUMN `created_at` timestamp NULL
, MODIFY COLUMN `updated_at` timestamp NULL;

ALTER TABLE `gofobao0627`.`gfb_roles`
  MODIFY COLUMN `name` varchar(255) NOT NULL
, MODIFY COLUMN `display_name` varchar(255) NULL
, MODIFY COLUMN `description` varchar(255) NULL
, MODIFY COLUMN `created_at` timestamp NULL
, MODIFY COLUMN `updated_at` timestamp NULL;

ALTER TABLE `gofobao0627`.`gfb_dict_item`
  DROP COLUMN `IS_DEL`
, ADD COLUMN `DEL` int(11) NULL DEFAULT '0' COMMENT '是否删除：0.存活，1.删除';

ALTER TABLE `gofobao0627`.`gfb_dict_value`
  DROP COLUMN `IS_DEL`
, ADD COLUMN `DEL` int(11) NULL DEFAULT '0' COMMENT '是否删除：0.存活，1.删除';

ALTER TABLE `gofobao0627`.`gfb_permission_role`
  MODIFY COLUMN `permission_id` int(10) unsigned NOT NULL
, MODIFY COLUMN `role_id` int(10) unsigned NOT NULL;

CREATE TABLE `gofobao0627`.`gfb_recharge_detail_log` (
  `id` int(11) NOT NULL auto_increment COMMENT '唯一标示',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `seq_no` varchar(32) NULL DEFAULT '' COMMENT '交易流水号',
  `create_time` datetime NULL,
  `callback_time` datetime NULL,
  `state` int(11) NULL DEFAULT '0' COMMENT '充值状态：0：充值请求。1.充值成功。2.充值失败',
  `del` int(11) NULL DEFAULT '0' COMMENT '0.有效记录1.无效记录',
  `recharge_type` int(11) NULL DEFAULT '0' COMMENT '充值类型：0.渠道充值1.线下转账',
  `card_no` varchar(32) NULL DEFAULT '' COMMENT '卡号',
  `bank_name` varchar(64) NULL DEFAULT '' COMMENT '充值银行',
  `money` int(11) NULL DEFAULT '0' COMMENT '充值金额',
  `recharge_source` int(11) NULL DEFAULT '0' COMMENT '充值来源：0.pc1.html52.android3.ios',
  `recharge_channel` int(11) NULL DEFAULT '0' COMMENT '充值渠道：0.江西银行（线上）1.其他',
  `remark` varchar(255) NULL DEFAULT '' COMMENT '备注',
  `mobile` varchar(32) NULL DEFAULT '' COMMENT '手机',
  `update_time` datetime NULL COMMENT '更新时间',
  `ip` varchar(32) NULL DEFAULT '' COMMENT 'ip',
  `response_message` varchar(2048) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

ALTER TABLE `gofobao0627`.`gfb_users`
  MODIFY COLUMN `password` varchar(60) NOT NULL DEFAULT '' COMMENT '登录密码（AES-256-CBC）'
, MODIFY COLUMN `pay_password` varchar(60) NOT NULL DEFAULT '' COMMENT '支付密码（AES-256-CBC）'
, MODIFY COLUMN `realname` varchar(10) NOT NULL DEFAULT '' COMMENT '真实姓名'
, MODIFY COLUMN `type` varchar(10) NOT NULL DEFAULT 'borrower' COMMENT '用户类型（manager：管理员；borrower：浏览者；）'
, ADD COLUMN `avatar_path` varchar(60) NULL DEFAULT '' COMMENT '头像地址';

ALTER TABLE `gofobao0627`.`gfb_role_user`
  MODIFY COLUMN `user_id` int(10) unsigned NOT NULL
, MODIFY COLUMN `role_id` int(10) unsigned NOT NULL;

CREATE TABLE `gofobao0627`.`gfb_third_batch_log` (
  `ID` int(11) NOT NULL auto_increment,
  `BATCH_NO` varchar(255) NULL,
  `TYPE` int(11) NULL,
  `SOURCE_ID` int(11) NULL,
  `REMARK` varchar(1024) NULL,
  `IPARAM1` int(11) NULL,
  `IPARAM2` int(11) NULL,
  `IPARAM3` int(11) NULL,
  `VPARAM1` varchar(255) NULL,
  `VPARAM2` varchar(255) NULL,
  `VPARAM3` varchar(255) NULL,
  `CREATE_AT` datetime NULL,
  `UPDATE_AT` datetime NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB;

CREATE TABLE `gofobao0627`.`gfb_user_third_account` (
  `id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NULL DEFAULT '0' COMMENT '用户Id',
  `account_id` varchar(50) NULL DEFAULT '' COMMENT '电子账户账号',
  `name` varchar(50) NULL DEFAULT '' COMMENT '真实姓名',
  `acct_use` int(11) NULL DEFAULT '0' COMMENT '0.普通用户；1.红包账户，2.企业账户',
  `card_no` varchar(50) NULL DEFAULT '' COMMENT '银行卡',
  `id_type` int(11) NULL DEFAULT '1' COMMENT '证件类型。 1身份证',
  `id_no` varchar(50) NULL DEFAULT '' COMMENT '证件号码',
  `mobile` varchar(50) NULL DEFAULT '' COMMENT '开户手机',
  `channel` int(11) NULL DEFAULT '0' COMMENT '渠道',
  `password_state` int(11) NULL DEFAULT '0' COMMENT '初始密码状态（0，未初始化，1.初始化）',
  `card_no_bind_state` int(11) NULL DEFAULT '1' COMMENT '银行卡绑定状态（0，未绑定，1.已绑定）',
  `create_at` datetime NULL,
  `update_at` datetime NULL,
  `create_id` int(11) NULL DEFAULT '0',
  `update_id` int(11) NULL DEFAULT '0',
  `del` int(11) NULL COMMENT '0，有效， 1.无效',
  `auto_tender_order_id` varchar(255) NULL COMMENT '自动投标签约订单号',
  `auto_tender_tx_amount` int(12) NULL DEFAULT '0' COMMENT '单笔投标金额的上限',
  `auto_tender_tot_amount` int(12) NULL DEFAULT '0' COMMENT '自动投标总金额上限',
  `auto_transfer_bond_order_id` varchar(255) NULL COMMENT '自动债券转让签约单号',
  `auto_tender_state` int(11) NULL DEFAULT '0',
  `auto_transfer_state` int(11) NULL DEFAULT '0',
  `bank_name` varchar(255) NULL DEFAULT '',
  `bank_logo` varchar(255) NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='银行电子账户标';

ALTER TABLE `gofobao0627`.`gfb_lend`
  ADD COLUMN `iparam1` int(11) NULL
, ADD COLUMN `iparam2` int(11) NULL
, ADD COLUMN `iparam3` int(11) NULL
, ADD COLUMN `vparam1` varchar(255) NULL
, ADD COLUMN `vparam2` varchar(255) NULL
, ADD COLUMN `vparam3` varchar(255) NULL
, ADD COLUMN `t_user_id` int(11) NULL COMMENT '银行电子账户标 id';

ALTER TABLE `gofobao0627`.`gfb_borrow_tender`
  MODIFY COLUMN `transfer_flag` int(11) NULL DEFAULT '0' COMMENT '转让标识（0：未转让；1：转让中；2：已转让）'
, MODIFY COLUMN `created_at` datetime NULL COMMENT '创建时间'
, MODIFY COLUMN `updated_at` datetime NULL COMMENT '更新时间'
, ADD COLUMN `auth_code` varchar(255) NULL COMMENT '即信债权授权码'
, ADD COLUMN `iparam1` int(11) NULL DEFAULT '0'
, ADD COLUMN `iparam2` int(11) NULL DEFAULT '0'
, ADD COLUMN `iparam3` int(11) NULL DEFAULT '0'
, ADD COLUMN `vparam1` varchar(255) NULL DEFAULT ''
, ADD COLUMN `vparam2` varchar(255) NULL DEFAULT ''
, ADD COLUMN `vparam3` varchar(255) NULL DEFAULT ''
, ADD COLUMN `t_user_id` int(11) NULL DEFAULT '0' COMMENT '银行电子账户标 id'
, ADD COLUMN `state` int(10) NULL DEFAULT '0' COMMENT '1:投标中； 2:还款中 ;3:已结清'
, ADD COLUMN `third_tender_order_id` varchar(255) NULL DEFAULT '' COMMENT '第三方订单号'
, ADD COLUMN `third_transfer_order_id` varchar(255) NULL DEFAULT '' COMMENT '购买债券转让编号'
, ADD COLUMN `is_third_register` int(11) NULL DEFAULT '0' COMMENT '是否在存管进行登记 0否 1.是否';

ALTER TABLE `gofobao0627`.`gfb_borrow`
  ADD COLUMN `tx_fee` int(11) NULL COMMENT '借款手续费(选填）'
, ADD COLUMN `iparam1` int(11) NULL
, ADD COLUMN `iparam2` int(11) NULL
, ADD COLUMN `iparam3` int(11) NULL
, ADD COLUMN `vparam1` varchar(255) NULL
, ADD COLUMN `vparam2` varchar(255) NULL
, ADD COLUMN `vparam3` varchar(255) NULL
, ADD COLUMN `t_user_id` int(11) NULL COMMENT '银行电子账户标 id'
, ADD COLUMN `bail_account_id` varchar(255) NULL COMMENT '担保存管账号'
, ADD COLUMN `take_user_id` int(11) NULL COMMENT '收款人id  目前针对于官标';

ALTER TABLE `gofobao0627`.`gfb_asset`
  MODIFY COLUMN `use_money` int(11) NOT NULL DEFAULT '0' COMMENT '可用金额(分)'
, MODIFY COLUMN `no_use_money` int(11) NOT NULL DEFAULT '0' COMMENT '冻结金额(分)'
, MODIFY COLUMN `virtual_money` int(11) NOT NULL DEFAULT '0' COMMENT '体验金(分)'
, MODIFY COLUMN `collection` int(11) NOT NULL DEFAULT '0' COMMENT '代收金额(分)'
, MODIFY COLUMN `payment` int(11) NOT NULL DEFAULT '0' COMMENT '待还金额(分)'
, MODIFY COLUMN `updated_at` timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间';

ALTER TABLE `gofobao0627`.`gfb_auto_tender`
  ADD COLUMN `iparam1` int(11) NULL
, ADD COLUMN `iparam2` int(11) NULL
, ADD COLUMN `iparam3` int(11) NULL
, ADD COLUMN `vparam1` varchar(255) NULL
, ADD COLUMN `vparam2` varchar(255) NULL
, ADD COLUMN `vparam3` varchar(255) NULL;

ALTER TABLE `gofobao0627`.`gfb_borrow_collection`
  MODIFY COLUMN `start_at` datetime NULL COMMENT '理论开始计息时间'
, MODIFY COLUMN `start_at_yes` datetime NULL COMMENT '实际开始计息时间'
, MODIFY COLUMN `collection_at` datetime NULL COMMENT '理论结束计息时间'
, MODIFY COLUMN `collection_at_yes` datetime NULL COMMENT '实际结束计息时间'
, MODIFY COLUMN `transfer_flag` int(11) NULL DEFAULT '0' COMMENT '转让标识（0：未转让；1：已转让）'
, MODIFY COLUMN `created_at` datetime NULL COMMENT '创建时间'
, MODIFY COLUMN `updated_at` datetime NULL COMMENT '更新时间'
, ADD COLUMN `borrow_id` int(11) NULL COMMENT '借款id'
, ADD COLUMN `user_id` int(11) NULL COMMENT '投标会员id'
, ADD COLUMN `iparam1` int(11) NULL
, ADD COLUMN `iparam2` int(11) NULL
, ADD COLUMN `iparam3` int(11) NULL
, ADD COLUMN `vparam1` varchar(255) NULL
, ADD COLUMN `vparam2` varchar(255) NULL
, ADD COLUMN `vparam3` varchar(255) NULL
, ADD COLUMN `t_user_id` int(11) NULL COMMENT '银行电子账户标 id'
, ADD COLUMN `t_repay_order_id` varchar(255) NULL COMMENT '还款order'
, ADD COLUMN `t_bail_repay_order_id` varchar(255) NULL COMMENT '垫付订单号'
, ADD COLUMN `t_repay_bail_order_id` varchar(255) NULL COMMENT '借款人还垫付订单号'
, ADD COLUMN `t_bail_auth_code` varchar(255) NULL COMMENT '垫付即信授权码';

ALTER TABLE `gofobao0627`.`gfb_borrow_repayment`
  ADD COLUMN `user_id` int(11) NULL COMMENT '借款人id'
, ADD COLUMN `iparam1` int(11) NULL
, ADD COLUMN `iparam2` int(11) NULL
, ADD COLUMN `iparam3` int(11) NULL
, ADD COLUMN `vparam1` varchar(255) NULL
, ADD COLUMN `vparam2` varchar(255) NULL
, ADD COLUMN `vparam3` varchar(255) NULL
, ADD COLUMN `t_user_id` int(11) NULL COMMENT '银行电子账户标 id';

ALTER TABLE `gofobao0627`.`gfb_borrow_virtual_collection`
  MODIFY COLUMN `status` int(10) NOT NULL DEFAULT '0'
, MODIFY COLUMN `order` int(10) NOT NULL
, MODIFY COLUMN `collection_at` datetime NULL
, MODIFY COLUMN `collection_at_yes` datetime NULL
, MODIFY COLUMN `created_at` datetime NULL
, MODIFY COLUMN `updated_at` datetime NULL;

ALTER TABLE `gofobao0627`.`gfb_currency`
  MODIFY COLUMN `use_currency` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '广富币金额'
, MODIFY COLUMN `no_use_currency` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '冻结广富币金额';

ALTER TABLE `gofobao0627`.`gfb_integral`
  MODIFY COLUMN `use_integral` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '用户积分'
, MODIFY COLUMN `no_use_integral` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '冻结积分';

ALTER TABLE `gofobao0627`.`gfb_notices`
  MODIFY COLUMN `read` int(11) NOT NULL DEFAULT '0' COMMENT '是否阅读（0、未读；1、已读）';

ALTER TABLE gofobao0627.gfb_statistic ADD id INT NULL PRIMARY KEY AUTO_INCREMENT;

ALTER TABLE `gofobao0627`.`gfb_user_cache`
  MODIFY COLUMN `income_interest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '已赚利息'
, MODIFY COLUMN `income_award` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '已转奖励'
, MODIFY COLUMN `income_overdue` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '逾期收入'
, MODIFY COLUMN `income_integral_cash` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '积分折现'
, MODIFY COLUMN `income_bonus` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '提成收入（推荐人）'
, MODIFY COLUMN `income_other` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '其他收入'
, MODIFY COLUMN `wait_collection_principal` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '待收本金'
, MODIFY COLUMN `wait_collection_interest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '待收利息'
, MODIFY COLUMN `tj_wait_collection_principal` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '车贷标代收本金'
, MODIFY COLUMN `tj_wait_collection_interest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '车贷标代收利息'
, MODIFY COLUMN `qd_wait_collection_principal` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '渠道标代收本金'
, MODIFY COLUMN `qd_wait_collection_interest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '渠道标代收利息'
, MODIFY COLUMN `expenditure_interest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '利息支出'
, MODIFY COLUMN `expenditure_interest_manage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '利息管理费支出'
, MODIFY COLUMN `expenditure_manage` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '账户管理费支出'
, MODIFY COLUMN `expenditure_fee` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '费用支出'
, MODIFY COLUMN `expenditure_overdue` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '逾期支出'
, MODIFY COLUMN `expenditure_other` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '其他支出'
, MODIFY COLUMN `wait_repay_principal` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '待还本金'
, MODIFY COLUMN `wait_repay_interest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '待还利息'
, MODIFY COLUMN `first_tender_award` smallint(5) unsigned NOT NULL DEFAULT 0 COMMENT '首投奖励'
, MODIFY COLUMN `award_virtual_money` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '赠送体验金'
, MODIFY COLUMN `recharge_total` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '充值总额'
, MODIFY COLUMN `yesterday_use_money` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '昨日可用余额'
, MODIFY COLUMN `cash_total` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '提现总额';

ALTER TABLE `gfb_user_info`
  MODIFY COLUMN `realname` varchar(10) NOT NULL DEFAULT '' COMMENT '用户真实姓名'
, MODIFY COLUMN `card_pic1` varchar(100) NOT NULL DEFAULT '' COMMENT '身份证图片地址'
, MODIFY COLUMN `card_pic2` varchar(100) NOT NULL DEFAULT '' COMMENT '身份证图片地址'
, MODIFY COLUMN `qq` varchar(20) NOT NULL DEFAULT '' COMMENT 'QQ号码'
, MODIFY COLUMN `graduated_school` varchar(255) NOT NULL DEFAULT '' COMMENT '毕业学校'
, MODIFY COLUMN `address` varchar(255) NOT NULL DEFAULT '' COMMENT '常住地'
, MODIFY COLUMN `birthday_y` smallint(4) NOT NULL DEFAULT 0 COMMENT '生日月'
, MODIFY COLUMN `birthday_md` smallint(4) NOT NULL DEFAULT 0 COMMENT '生日日';


INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '0', '中国工商银行', '/images/bankLogo/bank_fazhan_icon.png', '5万,50000', '5万,50000', '20万,200000', '2017-03-01 13:02:25', '2017-03-01 13:02:43', 0, 0, 0, '中国工商银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '1', '中国银行', '/images/bankLogo/bank_zhongguo_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:27', '2017-03-01 13:02:44', 0, 0, 0, '中国银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '2', '中国建设银行', '/images/bankLogo/bank_jianshe_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:27', '2017-03-01 13:02:45', 0, 0, 0, '中国建设银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '3', '中国农业银行', '/images/bankLogo/bank_nongye_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:28', '2017-03-01 13:02:46', 0, 0, 0, '中国农业银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '4', '交通银行', '/images/bankLogo/bank_jiaotong_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:31', '2017-03-01 13:02:46', 0, 0, 0, '交通银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '5', '广东发展银行', '/images/bankLogo/bank_fazhan_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:32', '2017-03-01 13:02:47', 0, 0, 0, '广东发展银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '6', '招商银行', '/images/bankLogo/bank_zhaoshang_icon.png', '5万,50000', '5万,50000', '20万,200000', '2017-03-01 13:02:33', '2017-03-01 13:02:48', 0, 0, 0, '招商银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '7', '平安银行', '/images/bankLogo/bank_pingan_icon.png', '5万,50000', '5万,50000', '20万,200000', '2017-03-01 13:02:34', '2017-03-01 13:02:49', 0, 0, 0, '平安银行股份有限公司');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '8', '兴业银行', '/images/bankLogo/bank_xingye_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:35', '2017-03-01 13:02:49', 0, 0, 0, '兴业银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '9', '中国民生银行', '/images/bankLogo/bank_minsheng_icon.png', '5万,50000', '20万,200000', '20万,200000', '2017-03-01 13:02:36', '2017-03-01 13:02:50', 0, 0, 0, '中国民生银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '10', '华夏银行', '/images/bankLogo/bank_huaxia_icon.png', '5万,50000', '20万,200000', '20万,200000', '2017-03-01 13:02:37', '2017-03-01 13:02:51', 0, 0, 0, '华夏银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '11', '上海浦东发展银行', '/images/bankLogo/bank_pufa_icon.png', '49000元,49000', '49000元,49000', '20万,200000', '2017-03-01 13:02:38', '2017-03-01 13:02:52', 0, 0, 0, '上海浦发发展银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '12', '中信银行', '/images/bankLogo/bank_zhongxing_icon.png', '1万,10000', '2万,20000', '4万,40000', '2017-03-01 13:02:39', '2017-03-01 13:02:53', 0, 0, 0, '中信银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '13', '中国光大银行', '/images/bankLogo/bank_guangda_icon.png', '5万,50000', '10万,100000', '20万,200000', '2017-03-01 13:02:40', '2017-03-01 13:02:54', 0, 0, 0, '中国光大银行');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (1, '15', '中国邮政储蓄银行', '/images/bankLogo/bank_youzheng_icon.png', '5万,50000', '20万,200000', '20万,200000', '2017-03-01 13:02:41', '2017-03-01 13:02:54', 0, 0, 0, '中国邮政储蓄银行股份有限公司');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (3, '14', 'annualized', '', '', '', '', null, null, 0, 0, 0, '年化率');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (3, '50', 'startMoney', '', '', '', '', null, null, 0, 0, 0, '起投金额');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (4, 'SERVICE_PLATFORM', '服务平台', '深圳市广富宝金融信息服务有限公司', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (4, 'REGISTER_NUM', '注册编号', '440301107333144', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (4, 'BORROW_CONTRACT', '合同内容', '乙方有一定的借款需求，有合理的借款用途；甲方有一定的资金实力，丙方拥有一套成熟的借贷管理服务平台，并基于此平台开展借款咨询及管理服务。现甲乙双方希望丙方为其提供借款与出借咨询、信用评审、出借人推荐等系列借款管理服务，并且委托其提供帐户管理、划扣款、本息管理、进行催收，贷后管理一系列活动。以保证公平的促成此交易。于此，甲、乙、丙三方依照我国有关法律、法规，经协商一致，订立本协议。', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'servicePhoneHide', '客服电话', '400-839-6696', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'workday', '服务热线', '工作日9：00-20：00', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'serviceQQ', '客服QQ', '3808988573、944270204', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'serviceEmail', '客服邮箱', 'kefu@gofobao.com', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'wechatCode', '微信公众号', 'gofubao、gofobaocom', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'weiboCode', '官方微博', '广富宝', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'qqGroup', '官方QQ群', '318195664、108655342', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '0', '20000', '0.45', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '20001', '50000', '0.51', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '50001', '150000', '0.57', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '150001', '300000', '0.63', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '300001', '500000', '0.69', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '500001', '800000', '0.75', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '0', '50000', '800001', '99999999', '0.81', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '0', '20000', '0.47', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '20001', '50000', '0.53', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '50001', '150000', '0.59', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '150001', '300000', '0.65', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '300001', '500000', '0.71', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '500001', '800000', '0.77', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '50001', '120000', '800001', '99999999', '0.83', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '0', '20000', '0.49', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '20001', '50000', '0.55', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '50001', '150000', '0.61', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '150001', '300000', '0.67', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '300001', '500000', '0.73', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '500001', '800000', '0.79', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '120001', '250000', '800001', '99999999', '0.85', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '0', '20000', '0.51', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '20001', '50000', '0.57', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '50001', '150000', '0.63', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '150001', '300000', '0.69', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '300001', '500000', '0.75', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '500001', '800000', '0.81', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '250001', '400000', '800001', '99999999', '0.87', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '0', '20000', '0.53', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '20001', '50000', '0.59', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '50001', '150000', '0.65', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '150001', '300000', '0.71', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '300001', '500000', '0.77', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '500001', '800000', '0.83', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '400001', '680000', '800001', '99999999', '0.89', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '0', '20000', '0.55', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '20001', '50000', '0.61', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '50001', '150000', '0.67', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '150001', '300000', '0.73', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '300001', '500000', '0.79', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '500001', '800000', '0.85', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '680001', '980000', '800001', '99999999', '0.91', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '0', '20000', '0.57', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '20001', '50000', '0.63', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '50001', '150000', '0.69', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '150001', '300000', '0.75', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '300001', '500000', '0.81', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '500001', '800000', '0.87', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '980001', '1280000', '800001', '99999999', '0.93', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '0', '20000', '0.59', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '20001', '50000', '0.65', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '50001', '150000', '0.71', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '150001', '300000', '0.77', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '300001', '500000', '0.83', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '500001', '800000', '0.89', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (6, '1280001', '99999999', '800001', '99999999', '0.95', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (5, 'servicePhoneView', '客服电话', '400-839-6696', '', '', '', null, null, 0, 0, 0, '');
INSERT INTO gfb_dict_value (ITEM_ID, VALUE01, VALUE02, VALUE03, VALUE04, VALUE05, VALUE06, CREATE_TIME, UPDATE_TIME, CREATE_ID, UPDATE_ID, DEL, NAME) VALUES (7, 'firstCreateAt', '批次编号第一次更新时间', '1498530243166', '', '', '', null, null, 0, 0, 0, '');


ALTER TABLE `gfb_auto_tender`
   MODIFY COLUMN `tender_0` int(11) NOT NULL DEFAULT '0',
   MODIFY COLUMN `tender_1` int(11) NOT NULL DEFAULT '0',
   MODIFY COLUMN `tender_3` int(11) NOT NULL DEFAULT '0',
   MODIFY COLUMN `tender_4` int(11) NOT NULL DEFAULT '0',
   MODIFY COLUMN  `apr_first` int(11) unsigned NOT NULL COMMENT '年化率起始值',
   MODIFY COLUMN  `apr_last` int(5) unsigned NOT NULL COMMENT '年化率结束值';

ALTER TABLE `gfb_auto_tender`
   MODIFY COLUMN `third_tender_cancel_order_id` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '第三方取消投标订单号';

CREATE TABLE gfb_task_scheduler
(
  id INT PRIMARY KEY COMMENT '唯一标示' AUTO_INCREMENT,
  type INT DEFAULT 0 COMMENT '调度类型: 0:委托申请状态查询',
  task_num INT DEFAULT 10 COMMENT '调度次数',
  state INT DEFAULT 0 COMMENT '0:未成功 1.成功',
  task_data VARCHAR(1024) DEFAULT '' COMMENT '调度任务数据',
  del INT DEFAULT 0 COMMENT '是否有效: 0有效 ,1 无效',
  do_task_num INT DEFAULT 0 COMMENT '已经执行次数',
  do_task_data VARCHAR(10240) DEFAULT '' COMMENT '每次执行结果累加',
  create_at DATETIME COMMENT '创建时间',
  update_at DATETIME COMMENT '修改时间'
);



CREATE TABLE gfb_asset_change_log
(
  id INT PRIMARY KEY COMMENT '自增类型' AUTO_INCREMENT,
  user_id INT DEFAULT 0 NOT NULL,
  moeny BIGINT DEFAULT 0 COMMENT '更改金额',
  type INT DEFAULT 0 COMMENT '资金变动类型',
  available_money BIGINT DEFAULT 0 COMMENT '可用金额',
  fee_money BIGINT DEFAULT 0 COMMENT '冻结金额',
  virtual_money BIGINT DEFAULT 0 COMMENT '体验金',
  collection_money BIGINT DEFAULT 0 COMMENT '待收金额',
  payment_money BIGINT DEFAULT 0 COMMENT '待还金额',
  for_user_id INT DEFAULT 0 COMMENT '对手账户',
  remark VARCHAR(255) DEFAULT '' COMMENT '备注',
  synchronize_at DATETIME COMMENT '同步时间',
  synchronize_state INT DEFAULT 0 COMMENT '同步状态(0. 未同步, 1. 已同步)',
  jixin_seq_no VARCHAR(255) DEFAULT '' COMMENT '即信资金序列号',
  jixin_tx_type VARCHAR(255) DEFAULT '' COMMENT '即信交易类型',
  jixin_tx_time TIME COMMENT '交易时间',
  jixin_tx_date DATE COMMENT '交易时间',
  create_at DATETIME COMMENT '创建时间',
  update_at DATETIME COMMENT '修改时间',
  ref_id INT DEFAULT 0 COMMENT '引用类型',
  extend_info VARCHAR(255) DEFAULT '' COMMENT '扩展信息JSON格式'
);


ALTER TABLE `gfb_auto_tender`
  MODIFY COLUMN `repay_money_yes` int(10) unsigned DEFAULT '0' COMMENT '实际还款金额（分）',
  MODIFY COLUMN `status` int(11) DEFAULT '0' COMMENT '借款用户是否还款状态：0、未还款。1、还款；';