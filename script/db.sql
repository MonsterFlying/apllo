ALTER TABLE gfb_activity ADD banner_url VARCHAR(255) DEFAULT '' NOT NULL COMMENT 'banner url';
ALTER TABLE gfb_activity ADD description VARCHAR(255) DEFAULT '' NOT NULL;
ALTER TABLE gfb_activity ADD hot_state INT(1) DEFAULT '0' NOT NULL COMMENT '是否热门';
ALTER TABLE gfb_activity CHANGE MAX max INT(110) DEFAULT '0' COMMENT '当type=1，它的基数为分（例如1元等于100）； 当type=2或者= 3， 基数为万分之一（例如万分之一=0.0001%;千分之五=0.0005%）';
ALTER TABLE gfb_activity CHANGE MIN min INT(11) DEFAULT '0' COMMENT '当type=1，它的基数为分（例如1元等于100）； 当type=2或者= 3， 基数为万分之一（例如万分之一=0.0001%;千分之五=0.0005%）';
ALTER TABLE gfb_activity ADD new_state INT(1) DEFAULT '0' NOT NULL COMMENT '是否最新';
ALTER TABLE gfb_activity ADD sort INT(10) DEFAULT '0' NOT NULL COMMENT '排序';
ALTER TABLE gfb_activity ADD url VARCHAR(255) DEFAULT '' NOT NULL COMMENT '描述';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN CREATE_UP SET DEFAULT '0';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN IP SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet MODIFY IPARAM1 VARCHAR(50) DEFAULT '';
ALTER TABLE gfb_activity_red_packet MODIFY IPARAM2 VARCHAR(50) DEFAULT '';
ALTER TABLE gfb_activity_red_packet MODIFY IPARAM3 VARCHAR(50) DEFAULT '';
ALTER TABLE gfb_activity_red_packet ALTER COLUMN UPDATE_UP SET DEFAULT '0';
ALTER TABLE gfb_activity_red_packet MODIFY VPARAM1 VARCHAR(255) DEFAULT '';
ALTER TABLE gfb_activity_red_packet MODIFY VPARAM2 VARCHAR(255) DEFAULT '';
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN CREATE_UP SET DEFAULT '0';
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN UPDARE_UP SET DEFAULT '0';
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN VPARAM1 SET DEFAULT '';
ALTER TABLE gfb_activity_red_packet_log ALTER COLUMN VPARAM2 SET DEFAULT '';
ALTER TABLE gfb_advance_log MODIFY repay_money_yes INT(10) unsigned DEFAULT '0' COMMENT '实际还款金额（分）';
ALTER TABLE gfb_advance_log MODIFY status INT(11) DEFAULT '0' COMMENT '借款用户是否还款状态：0、未还款。1、还款；';
ALTER TABLE gfb_article ADD preview_img VARCHAR(100) NOT NULL;
ALTER TABLE gfb_asset ADD finance_plan_money INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '理财计划金额';
ALTER TABLE gfb_asset ALTER COLUMN updated_at SET DEFAULT '0000-00-00 00:00:00';
ALTER TABLE gfb_asset_log MODIFY collection BIGINT(20) NOT NULL COMMENT '代收金额（分）';
ALTER TABLE gfb_asset_log ALTER COLUMN money DROP DEFAULT;
ALTER TABLE gfb_asset_log ALTER COLUMN no_use_money DROP DEFAULT;
ALTER TABLE gfb_asset_log ALTER COLUMN payment DROP DEFAULT;
ALTER TABLE gfb_asset_log ALTER COLUMN use_money DROP DEFAULT;
ALTER TABLE gfb_asset_log MODIFY virtual_money BIGINT(20) NOT NULL COMMENT '体验金额（分）';
ALTER TABLE gfb_auto_tender MODIFY apr_first INT(11) unsigned NOT NULL COMMENT '年化率起始值';
ALTER TABLE gfb_auto_tender MODIFY apr_last INT(5) unsigned NOT NULL COMMENT '年化率结束值';
ALTER TABLE gfb_auto_tender ADD iparam1 INT(11) NULL;
ALTER TABLE gfb_auto_tender ADD iparam2 INT(11) NULL;
ALTER TABLE gfb_auto_tender ADD iparam3 INT(11) NULL;
ALTER TABLE gfb_auto_tender MODIFY tender_0 INT(11) NOT NULL DEFAULT '0';
ALTER TABLE gfb_auto_tender MODIFY tender_1 INT(11) NOT NULL DEFAULT '0';
ALTER TABLE gfb_auto_tender MODIFY tender_3 INT(11) NOT NULL DEFAULT '0';
ALTER TABLE gfb_auto_tender MODIFY tender_4 INT(11) NOT NULL DEFAULT '0';
ALTER TABLE gfb_auto_tender ADD vparam1 VARCHAR(255) NULL;
ALTER TABLE gfb_auto_tender ADD vparam2 VARCHAR(255) NULL;
ALTER TABLE gfb_auto_tender ADD vparam3 VARCHAR(255) NULL;
CREATE TABLE gfb_batch_asset_change
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  type INT(11) COMMENT '操作（1.批次放款、2.批次还款、3.批次融资人还担保账户垫款 4.批次投资人购买债权 5.批次担保账户代偿）',
  source_id INT(11) COMMENT '资源id（borrowId,repaymentId等）',
  state INT(11) DEFAULT '0' COMMENT '状态：0.未完成 1.已完成',
  batch_no VARCHAR(25) COMMENT '批次号',
  created_at DATETIME,
  updated_at DATETIME
);
CREATE TABLE gfb_batch_asset_change_item
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  batch_asset_change_id INT(11) COMMENT '批次资产变动id',
  state INT(11) COMMENT '状态 0.未操作  1.已操作 2.无效',
  user_id INT(11) COMMENT '资产变动会员id',
  to_user_id INT(11) COMMENT '交易对方ID',
  money INT(20) COMMENT '本比操作总金额 （分）',
  principal INT(20) COMMENT '操作本金（分）',
  interest INT(20) COMMENT '利息（分）',
  asset VARCHAR(255) COMMENT '资产操作',
  type VARCHAR(255) COMMENT '变动类型',
  send_red_packet INT(11) DEFAULT '0' COMMENT '是否需要发送存管红包 0否 1是',
  remark VARCHAR(1024),
  created_at DATETIME,
  updated_at DATETIME,
  source_id INT(11) DEFAULT '0' COMMENT '引用ID',
  seq_no VARCHAR(32) DEFAULT '' COMMENT '交易流水',
  group_seq_no VARCHAR(32) DEFAULT '' COMMENT '一组的交易流水'
);
ALTER TABLE gfb_borrow ADD bail_account_id VARCHAR(255) NULL COMMENT '担保存管账号';
ALTER TABLE gfb_borrow ADD is_windmill TINYINT(1) DEFAULT '0' NOT NULL COMMENT '是否推送到风车理财 0：不是 ;1:是';
ALTER TABLE gfb_borrow ADD product_id VARCHAR(255) NULL COMMENT '即信标的号';
ALTER TABLE gfb_borrow ADD t_user_id INT(11) NULL COMMENT '银行电子账户标 id';
ALTER TABLE gfb_borrow ADD take_user_id INT(11) NULL COMMENT '收款人id  目前针对于官标';
ALTER TABLE gfb_borrow ADD third_transfer_flag INT(11) DEFAULT '0' NULL COMMENT '当借款是转让标时，标识否与存管通信， 0否 1是';
ALTER TABLE gfb_borrow ADD titular_borrow_account_id VARCHAR(255) NULL;
ALTER TABLE gfb_borrow ADD tx_fee INT(11) NULL COMMENT '借款手续费(选填）';
ALTER TABLE gfb_borrow_collection ADD actual_interest INT(11) NULL;
ALTER TABLE gfb_borrow_collection ADD borrow_collection_ids VARCHAR(255) NULL COMMENT '部分转让时候 转让期数集合';
ALTER TABLE gfb_borrow_collection ADD borrow_id INT(11) NULL COMMENT '借款id';
ALTER TABLE gfb_borrow_collection MODIFY collection_at DATETIME COMMENT '理论结束计息时间';
ALTER TABLE gfb_borrow_collection MODIFY collection_at_yes DATETIME COMMENT '实际结束计息时间';
ALTER TABLE gfb_borrow_collection MODIFY created_at DATETIME COMMENT '创建时间';
ALTER TABLE gfb_borrow_collection MODIFY start_at DATETIME COMMENT '理论开始计息时间';
ALTER TABLE gfb_borrow_collection MODIFY start_at_yes DATETIME COMMENT '实际开始计息时间';
ALTER TABLE gfb_borrow_collection ADD t_credit_end_order_id VARCHAR(255) NULL;
ALTER TABLE gfb_borrow_collection ADD t_repay_order_id VARCHAR(255) NULL COMMENT '还款order';
ALTER TABLE gfb_borrow_collection ADD t_transfer_order_id VARCHAR(255) NULL COMMENT '部分债权转让order_id';
ALTER TABLE gfb_borrow_collection ADD t_user_id INT(11) NULL COMMENT '银行电子账户标 id';
ALTER TABLE gfb_borrow_collection ADD third_credit_end_flag INT(11) NULL;
ALTER TABLE gfb_borrow_collection ADD third_repay_flag INT(11) DEFAULT '0' NULL COMMENT '第三方是否登记还款 0否 1是';
ALTER TABLE gfb_borrow_collection ADD third_transfer_flag INT(11) DEFAULT '0' NULL COMMENT '第三方是否登记部分债权转让 0否 1是';
ALTER TABLE gfb_borrow_collection ADD transfer_auth_code VARCHAR(255) NULL COMMENT '即信部分债权转让授权码';
ALTER TABLE gfb_borrow_collection MODIFY transfer_flag INT(11) DEFAULT '0' COMMENT '转让标识（0：未转让；1：已转让）';
ALTER TABLE gfb_borrow_collection MODIFY updated_at DATETIME COMMENT '更新时间';
ALTER TABLE gfb_borrow_collection ADD user_id INT(11) NULL COMMENT '投标会员id';
ALTER TABLE gfb_borrow_repayment ADD iparam1 INT(11) NULL;
ALTER TABLE gfb_borrow_repayment ADD `is_advance` int(11) DEFAULT '0' COMMENT '是否垫付 0否1是' AFTER `late_interest`;
ALTER TABLE gfb_borrow_repayment ADD iparam2 INT(11) NULL;
ALTER TABLE gfb_borrow_repayment ADD iparam3 INT(11) NULL;
ALTER TABLE gfb_borrow_repayment ADD t_user_id INT(11) NULL COMMENT '银行电子账户标 id';
ALTER TABLE gfb_borrow_repayment ADD user_id INT(11) NULL COMMENT '借款人id';
ALTER TABLE gfb_borrow_repayment ADD vparam1 VARCHAR(255) NULL;
ALTER TABLE gfb_borrow_repayment ADD vparam2 VARCHAR(255) NULL;
ALTER TABLE gfb_borrow_repayment ADD vparam3 VARCHAR(255) NULL;
ALTER TABLE gfb_borrow_tender ADD already_interest INT(11) DEFAULT '0' NULL COMMENT '付给债权转让人的当期应计算利息，（债权转让时使用） ';
ALTER TABLE gfb_borrow_tender ADD auth_code VARCHAR(255) NULL COMMENT '即信债权授权码';
ALTER TABLE gfb_borrow_tender MODIFY created_at DATETIME COMMENT '创建时间';
ALTER TABLE gfb_borrow_tender ADD is_third_register INT(11) DEFAULT '0' NULL COMMENT '是否在存管进行登记 0否 1.是否';
ALTER TABLE gfb_borrow_tender ADD parent_id INT(11) DEFAULT '0' NULL COMMENT '父级投标id（默认为0，最顶级记录）';
ALTER TABLE gfb_borrow_tender ADD state INT(10) DEFAULT '1' NULL COMMENT '1:投标中； 2:还款中 ;3:已结清';
ALTER TABLE gfb_borrow_tender ADD t_user_id INT(11) DEFAULT '0' NULL COMMENT '银行电子账户标 id';
ALTER TABLE gfb_borrow_tender ADD third_credit_end_flag INT(11) DEFAULT '0' NULL COMMENT '当前投标记录在是否结束存管债权，0否 1是';
ALTER TABLE gfb_borrow_tender ADD third_credit_end_order_id VARCHAR(255) NULL COMMENT '结束债权orderid';
ALTER TABLE gfb_borrow_tender ADD third_lend_pay_order_id VARCHAR(255) NULL;
ALTER TABLE gfb_borrow_tender ADD third_tender_cancel_order_id VARCHAR(255) NULL COMMENT '第三方取消投标订单号';
ALTER TABLE gfb_borrow_tender ADD third_tender_flag INT(11) DEFAULT '0' NULL COMMENT '当投标记录是非转让标投标时，标识是否放款， 0否 1是';
ALTER TABLE gfb_borrow_tender ADD third_tender_order_id VARCHAR(255) DEFAULT '' NULL COMMENT '第三方投标订单号';
ALTER TABLE gfb_borrow_tender ADD third_transfer_flag INT(11) DEFAULT '0' NULL COMMENT '当投标记录是转让标投标时，标识是否购买债权， 0否 1是';
ALTER TABLE gfb_borrow_tender ADD third_transfer_order_id VARCHAR(255) DEFAULT '' NULL COMMENT '购买债券转让编号';
ALTER TABLE gfb_borrow_tender ADD transfer_auth_code VARCHAR(255) NULL COMMENT '即信债权转让授权码';
ALTER TABLE gfb_borrow_tender ADD transfer_buy_id INT(11) NULL COMMENT '购买债权记录id';
ALTER TABLE gfb_borrow_tender MODIFY transfer_flag INT(11) DEFAULT '0' COMMENT '转让标识（0：未转让；1：转让中；2：全部已转让 3.部分转让）';
ALTER TABLE gfb_borrow_tender ADD type TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '类型；0：普通；1：理财计划 ；';
ALTER TABLE gfb_borrow_tender MODIFY updated_at DATETIME COMMENT '更新时间';
ALTER TABLE gfb_borrow_virtual_collection MODIFY collection_at DATETIME;
ALTER TABLE gfb_borrow_virtual_collection MODIFY collection_at_yes DATETIME;
ALTER TABLE gfb_borrow_virtual_collection MODIFY created_at DATETIME;
ALTER TABLE gfb_borrow_virtual_collection MODIFY `order` INT(10) NOT NULL;
ALTER TABLE gfb_borrow_virtual_collection MODIFY status INT(10) NOT NULL DEFAULT '0';
ALTER TABLE gfb_borrow_virtual_collection MODIFY updated_at DATETIME;
CREATE TABLE gfb_cash_detail_log
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id INT(11) DEFAULT '0' NOT NULL COMMENT '用户ID',
  third_account_id VARCHAR(32) DEFAULT '' COMMENT '银行存管账号',
  card_no VARCHAR(32) DEFAULT '' COMMENT '提现卡号',
  bank_name VARCHAR(32) DEFAULT '' COMMENT '提现银行名称',
  company_bank_no VARCHAR(32) DEFAULT '' COMMENT '联行号',
  money INT(11) DEFAULT '0' COMMENT '提现金额',
  fee INT(11) DEFAULT '0' COMMENT '费用',
  verify_user_id INT(11) DEFAULT '0' COMMENT '审核人',
  verify_time DATETIME COMMENT '审核时间',
  verify_remark VARCHAR(255) DEFAULT '' COMMENT '审核备注',
  state INT(11) DEFAULT '0' COMMENT '-1.取消提现.0:申请中,1.系统审核通过,2.系统审核不通过, 3.银行提现成功.4.银行提现失败.',
  create_time DATETIME COMMENT '提现申请时间',
  callback_time DATETIME COMMENT '存管回调时间',
  cancel_time DATETIME COMMENT '取消时间',
  ip VARCHAR(32) DEFAULT '' COMMENT '提现IP',
  cash_type INT(11) DEFAULT '0' COMMENT '0:渠道提现,1.人行提现',
  seq_no VARCHAR(32) DEFAULT '',
  query_seq_no VARCHAR(32) DEFAULT '' COMMENT '实时查产生的交易流水(只有成功才能产生)',
  query_callback_time VARCHAR(32) DEFAULT '' COMMENT '查询交易记录时间'
);
ALTER TABLE gfb_currency ALTER COLUMN no_use_currency SET DEFAULT '0';
ALTER TABLE gfb_currency ALTER COLUMN use_currency SET DEFAULT '0';
CREATE TABLE gfb_current_income_log
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id INT(11) DEFAULT '0',
  create_at DATETIME COMMENT '创建时间',
  state INT(11) DEFAULT '0' COMMENT '是否成功',
  seq_no VARCHAR(32) DEFAULT '',
  money BIGINT(20) DEFAULT '0' COMMENT '活期金额'
);
ALTER TABLE gfb_daily_asset MODIFY collection INT(11) NOT NULL COMMENT '代收金额（分）';
ALTER TABLE gfb_daily_asset MODIFY no_use_money INT(11) NOT NULL COMMENT '冻结金额（分）';
ALTER TABLE gfb_daily_asset MODIFY payment INT(11) NOT NULL COMMENT '代付金额（分）';
ALTER TABLE gfb_daily_asset MODIFY use_money INT(11) NOT NULL COMMENT '用户余额（分）';
ALTER TABLE gfb_daily_asset MODIFY virtual_money INT(11) NOT NULL COMMENT '体验金额（分）';
ALTER TABLE gfb_dict_item ADD DEL INT(11) DEFAULT '0' NULL COMMENT '是否删除：0.存活，1.删除';
ALTER TABLE gfb_dict_item DROP IS_DEL;
ALTER TABLE gfb_dict_value ADD DEL INT(11) DEFAULT '0' NULL COMMENT '是否删除：0.存活，1.删除';
ALTER TABLE gfb_dict_value DROP IS_DEL;
CREATE TABLE gfb_finance_plan
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  status TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '状态; 0：待审；1：购买中；2：初审不通过；3：结束购买；4：复审不通过；5：已取消；',
  name VARCHAR(255) NOT NULL COMMENT '计划名称',
  money INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '计划金额（分）',
  money_yes INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '加入总金额',
  base_apr INT(5) unsigned DEFAULT '0' NOT NULL COMMENT '预期年化利率（不代表实际利息收益）',
  time_limit INT(2) unsigned DEFAULT '0' NOT NULL COMMENT '期限',
  lock_period INT(2) unsigned DEFAULT '0' NOT NULL COMMENT '锁定期',
  lowest INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '单个用户加入金额最小阈值',
  append_multiple_amount INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '加入金额必须为该值的整数倍递增',
  most INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '单个用户加入金额最大阈值',
  success_at DATETIME COMMENT '结束购买时间',
  end_lock_at DATETIME COMMENT '退出日期',
  finished_state TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '结束状态',
  total_sub_point INT(5) unsigned DEFAULT '0' NOT NULL COMMENT '加入总人次',
  sub_point_count INT(5) unsigned DEFAULT '0' NOT NULL COMMENT '加入人数（同一用户多次加入进行合并）',
  create_id INT(10) unsigned NOT NULL COMMENT '创建用户',
  update_id INT(10) unsigned NOT NULL COMMENT '更新用户',
  description TEXT NOT NULL COMMENT '计划简介',
  created_at DATETIME,
  updated_at DATETIME
);
CREATE INDEX finance_plan_finish_state_index ON gfb_finance_plan (finished_state);
CREATE TABLE gfb_finance_plan_buyer
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id INT(10) unsigned NOT NULL COMMENT '用户id',
  plan_id INT(10) unsigned NOT NULL COMMENT '计划id',
  status INT(11) DEFAULT '0' COMMENT '状态；0：失败；1：成功；2.取消',
  base_apr INT(5) unsigned DEFAULT '0' NOT NULL COMMENT '预期年化利率（不代表实际利息收益）',
  apr INT(5) unsigned DEFAULT '0' NOT NULL COMMENT '实际年化利率',
  money INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '加入金额（分）',
  valid_money INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '有效金额（分）',
  right_money INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '已匹配金额',
  left_money INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '剩余金额',
  end_lock_at DATETIME COMMENT '退出日期',
  finished_state TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '结束状态',
  source TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '来源；0：PC；1：ANDROID；2：IOS；3：H5',
  remark VARCHAR(255) DEFAULT '' NOT NULL COMMENT '备注',
  created_at DATETIME,
  updated_at DATETIME,
  freeze_order_id VARCHAR(255) COMMENT '冻结资产orderid',
  CONSTRAINT finance_plan_buyer_user_id_foreign FOREIGN KEY (user_id) REFERENCES gfb_users (id) ON UPDATE CASCADE,
  CONSTRAINT finance_plan_buyer_plan_id_foreign FOREIGN KEY (plan_id) REFERENCES gfb_finance_plan (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX finance_plan_buyer_finish_state_end_lock_at_index ON gfb_finance_plan_buyer (finished_state, end_lock_at);
CREATE TABLE gfb_finance_plan_collection
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id INT(10) unsigned NOT NULL COMMENT '用户id',
  plan_id INT(10) unsigned NOT NULL COMMENT '计划id',
  buyer_id INT(10) unsigned NOT NULL COMMENT '购买记录id',
  order_num INT(2) unsigned NOT NULL COMMENT '期数',
  status INT(11) DEFAULT '0' COMMENT '状态；0：计息中；1：已结息',
  principal INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '本金（分）',
  interest INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '利息（分）',
  start_at DATETIME COMMENT '起息时间',
  collection_at DATETIME COMMENT '结息时间',
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT finance_plan_collection_user_id_foreign FOREIGN KEY (user_id) REFERENCES gfb_users (id) ON UPDATE CASCADE,
  CONSTRAINT finance_plan_collection_plan_id_foreign FOREIGN KEY (plan_id) REFERENCES gfb_finance_plan (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT finance_plan_collection_buyer_id_foreign FOREIGN KEY (buyer_id) REFERENCES gfb_finance_plan_buyer (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE gfb_finance_plan_pre_tender_log
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  status TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '状态；0：等待匹配；1：已匹配；2：匹配失败',
  borrow_id INT(10) unsigned NOT NULL COMMENT '借款id',
  tender_id INT(10) unsigned NOT NULL COMMENT '投标id',
  plan_id INT(10) unsigned NOT NULL COMMENT '计划id',
  money INT(10) unsigned NOT NULL COMMENT '匹配金额',
  close_at DATETIME COMMENT '结标日期',
  created_at DATETIME,
  updated_at DATETIME
);
CREATE TABLE gfb_finance_plan_tender_log
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  borrow_id INT(10) unsigned NOT NULL COMMENT '借款id',
  tender_id INT(10) unsigned NOT NULL COMMENT '投标id',
  plan_id INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '计划id',
  buyer_id INT(10) unsigned NOT NULL COMMENT '购买记录id',
  money INT(10) unsigned NOT NULL COMMENT '匹配金额',
  left_money INT(10) unsigned NOT NULL COMMENT '剩余未回本金',
  transfer_buy_id INT(10) unsigned NOT NULL COMMENT '债转购买记录',
  transfer_flag TINYINT(1) unsigned NOT NULL COMMENT '转让标识',
  finnished_state TINYINT(1) unsigned NOT NULL COMMENT '结束状态（所有本金已回款或已转让）',
  created_at DATETIME,
  updated_at DATETIME
);
CREATE TABLE gfb_find
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  status TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '0：隐藏；1：显示',
  title VARCHAR(20) DEFAULT '' NOT NULL,
  icon VARCHAR(100) DEFAULT '' NOT NULL,
  url VARCHAR(100) DEFAULT '' NOT NULL,
  `order` INT(10) DEFAULT '0' NOT NULL COMMENT '排序；降序',
  create_id INT(10) unsigned NOT NULL COMMENT '创建用户',
  update_id INT(10) unsigned NOT NULL COMMENT '更新用户',
  created_at DATETIME,
  updated_at DATETIME
);
ALTER TABLE gfb_incr_statistic MODIFY jz_sum_publish INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY jz_sum_repay INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY jz_sum_repay_principal INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY jz_sum_success INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY lz_sum_publish INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY lz_sum_success INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY miao_sum_publish INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY miao_sum_success INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY no_use_money_sum INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY qd_sum_publish INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY qd_sum_repay INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY qd_sum_repay_principal INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY qd_sum_success INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY tj_sum_publish INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY tj_sum_repay INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY tj_sum_repay_principal INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_incr_statistic MODIFY tj_sum_success INT(10) unsigned NOT NULL DEFAULT '0';
ALTER TABLE gfb_integral ALTER COLUMN no_use_integral SET DEFAULT '0';
ALTER TABLE gfb_integral ALTER COLUMN use_integral SET DEFAULT '0';
CREATE TABLE gfb_jixin_tx_log
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  seq_no VARCHAR(225) DEFAULT '',
  body VARCHAR(10240) DEFAULT '' COMMENT '请求体或者响应体',
  create_at DATETIME,
  tx_type VARCHAR(225) DEFAULT '' COMMENT '交易类型',
  tx_type_desc VARCHAR(255) DEFAULT '' COMMENT '交易类型描述',
  type INT(11) DEFAULT '0' COMMENT '日志类型: 0: 请求 1.响应'
);
ALTER TABLE gfb_lend ADD iparam1 INT(11) NULL;
ALTER TABLE gfb_lend ADD iparam2 INT(11) NULL;
ALTER TABLE gfb_lend ADD iparam3 INT(11) NULL;
ALTER TABLE gfb_lend ADD t_user_id INT(11) NULL COMMENT '银行电子账户标 id';
ALTER TABLE gfb_lend ADD vparam1 VARCHAR(255) NULL;
ALTER TABLE gfb_lend ADD vparam2 VARCHAR(255) NULL;
ALTER TABLE gfb_lend ADD vparam3 VARCHAR(255) NULL;
CREATE TABLE gfb_lianhanghao_bank
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(60) DEFAULT '' NOT NULL COMMENT '名称',
  sort INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '排序（升序）',
  created_at DATETIME,
  updated_at DATETIME
);
CREATE TABLE gfb_lianhanghao
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  number VARCHAR(60) DEFAULT '' NOT NULL COMMENT '联行号',
  bank INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '银行ID',
  bankname VARCHAR(60) DEFAULT '' NOT NULL COMMENT '开户行名称',
  tel VARCHAR(255) DEFAULT '' NOT NULL COMMENT '银行网点电话',
  province INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '身份ID',
  city INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '城市ID',
  address VARCHAR(255) DEFAULT '' NOT NULL COMMENT '银行网点地址',
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT lianhanghao_bank_foreign FOREIGN KEY (bank) REFERENCES gfb_lianhanghao_bank (id)
);
CREATE INDEX lianhanghao_province_city_index ON gfb_lianhanghao (province, city);
CREATE TABLE gfb_lianhanghao_area
(
  id INT(10) unsigned PRIMARY KEY NOT NULL AUTO_INCREMENT,
  pid INT(10) unsigned DEFAULT '0' NOT NULL COMMENT '上级ID',
  name VARCHAR(60) DEFAULT '' NOT NULL COMMENT '名称',
  level INT(2) unsigned DEFAULT '0' NOT NULL COMMENT '层级',
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE gfb_marketing
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  marketing_type INT(11) DEFAULT '0' COMMENT '促销类型: 1.红包, 2.积分, 3.现金券',
  titel VARCHAR(255) DEFAULT '',
  introduction VARCHAR(255) DEFAULT '' COMMENT '简介',
  targer_url VARCHAR(255) DEFAULT '' COMMENT '活动展示页面',
  view_url VARCHAR(255) DEFAULT '' COMMENT '预览图url',
  begin_time DATETIME COMMENT '活动开始时间',
  end_time DATETIME COMMENT '活动结束时间',
  open_state INT(11) DEFAULT '0' COMMENT '启用状态(关闭, 开启)',
  del INT(11) DEFAULT '0' COMMENT '记录有效状态(0,有效, 1.无效)',
  create_time DATETIME,
  update_time DATETIME,
  parent_state INT(11) DEFAULT '0' COMMENT '是否针对邀请人'
);
CREATE TABLE gfb_marketing_condition
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  marketing_id INT(11) DEFAULT '0' COMMENT '活动类型表',
  tender_money_min BIGINT(20) DEFAULT '0' COMMENT '最小投标金额',
  register_min_time DATETIME COMMENT '注册时间',
  recharge_money_min BIGINT(20) DEFAULT '0' COMMENT '充值金额',
  open_account_min_time DATETIME COMMENT '开户时间',
  login_min_time DATETIME COMMENT '登录时间',
  del INT(11) DEFAULT '0' COMMENT '有效状态: 0.有效, 1.无效',
  create_time DATETIME COMMENT '创建时间'
);
CREATE TABLE gfb_marketing_dimension
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  marketing_id INT(11) DEFAULT '0' COMMENT '营销活动ID',
  platform VARCHAR(32) DEFAULT '' COMMENT '活动的平台类型, 可以使用多个(0,pc, 1.android, 2, ios, 3.h5)',
  borrow_type VARCHAR(32) DEFAULT '' COMMENT '标的类型:(1.车贷标, 2.渠道标, 3.流转表, 信用标,-2.新手标)',
  member_type INT(11) DEFAULT '0' COMMENT '0.不选, 1.新用户, 2.老用户',
  channel_type VARCHAR(32) DEFAULT '' COMMENT '渠道用户类型(0.pc, 1.android, 2.ios, 3.h5, 4.类型)',
  parent_state INT(11) DEFAULT '0' COMMENT '被邀请人:0, 赠送被邀请人, 1.赠送邀请人',
  del INT(11) DEFAULT '0' COMMENT '有效状态: 0.有效, 1.无效',
  create_time DATETIME
);
CREATE TABLE gfb_marketing_redpack_record
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  marketing_id INT(11) DEFAULT '0',
  redpack_rule_id INT(11) DEFAULT '0',
  user_id INT(11) DEFAULT '0' COMMENT '用户ID',
  source_id INT(11) DEFAULT '0' COMMENT '来源ID',
  money BIGINT(20) DEFAULT '0' COMMENT '红包金额',
  markeing_titel VARCHAR(255),
  state INT(11) DEFAULT '0' COMMENT '红包状态,0.待开启, 1.已开, 2.作废',
  publish_time DATETIME COMMENT '发放时间',
  open_time DATETIME COMMENT '开启红包时间',
  cancel_time DATETIME COMMENT '作废时间',
  del INT(11) DEFAULT '0' COMMENT '有效状态: 0.有效, 1.无效',
  remark VARCHAR(225) DEFAULT '' COMMENT '活动类型'
);
CREATE TABLE gfb_marketing_redpack_rule
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  marketing_id INT(11) DEFAULT '0',
  rule_type INT(11) DEFAULT '0' COMMENT '红包类型: 1.投资金额随机百分比,2.投资金额规定百分比, 3.随机金额, 4.规定金额, 5.年化率',
  tender_money_min DECIMAL(10,6) DEFAULT '0.000000' COMMENT '投标金额*随机最小值',
  tender_money_max DECIMAL(10,6) DEFAULT '0.000000' COMMENT '投标金额*随机最大值',
  money_min BIGINT(20) DEFAULT '0' COMMENT '固定金额最小值',
  money_max BIGINT(20) DEFAULT '0' COMMENT '固定金额最大金额',
  apr DECIMAL(10,6) DEFAULT '0.000000' COMMENT '年化收益',
  del INT(11) DEFAULT '0' COMMENT '有效状态(0.有效, 1.无效)',
  create_time DATETIME
);
CREATE TABLE gfb_new_asset_log
(
  id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  op_name VARCHAR(128) DEFAULT '' COMMENT '操作名称',
  op_money BIGINT(20) DEFAULT '0' COMMENT '操作金额',
  use_money BIGINT(20) DEFAULT '0' COMMENT '可用金额',
  no_use_money BIGINT(20) DEFAULT '0' COMMENT '冻结金额',
  user_id INT(11) DEFAULT '0' COMMENT '操作人ID',
  for_user_id INT(11) DEFAULT '0' COMMENT '对手账户ID',
  platform_type VARCHAR(32) DEFAULT '' COMMENT '存管平台类型',
  local_type VARCHAR(32) DEFAULT '' COMMENT '本地交易类型',
  tx_flag VARCHAR(8) DEFAULT 'D' COMMENT '交易金额符号: 小于零等于C；大于零等于D；',
  local_seq_no VARCHAR(32) DEFAULT '' COMMENT '本地交易流水',
  curr_money BIGINT(20) DEFAULT '0' COMMENT '当前用户账户余额(可用+ 冻结)',
  source_id INT(11) DEFAULT '0' COMMENT '来源ID',
  create_time DATETIME COMMENT '创建时间',
  remark VARCHAR(1024) DEFAULT '' COMMENT '标识',
  group_op_seq_no VARCHAR(32) DEFAULT '' COMMENT '同组操作标识',
  syn_state INT(11) DEFAULT '0' COMMENT '对账标识, 0.未同步, 1.同步',
  del INT(11) DEFAULT '0' COMMENT '有效状态标识: 0.有效, 1.无效',
  syn_time DATETIME COMMENT '对账时间'
);
ALTER TABLE gfb_notices MODIFY `read` INT(11) NOT NULL DEFAULT '0' COMMENT '是否阅读（0、未读；1、已读）';
CREATE TABLE gfb_recharge_detail_log
(
  id INT(11) PRIMARY KEY NOT NULL COMMENT '唯一标示' AUTO_INCREMENT,
  user_id INT(11) NOT NULL COMMENT '用户ID',
  seq_no VARCHAR(32) DEFAULT '' COMMENT '交易流水号',
  create_time DATETIME,
  callback_time DATETIME,
  state INT(11) DEFAULT '0' COMMENT '充值状态：0：充值请求。1.充值成功。2.充值失败',
  del INT(11) DEFAULT '0' COMMENT '0.有效记录1.无效记录',
  recharge_type INT(11) DEFAULT '0' COMMENT '充值类型：0.渠道充值1.线下转账',
  card_no VARCHAR(32) DEFAULT '' COMMENT '卡号',
  bank_name VARCHAR(64) DEFAULT '' COMMENT '充值银行',
  money INT(11) DEFAULT '0' COMMENT '充值金额',
  recharge_source INT(11) DEFAULT '0' COMMENT '充值来源：0.pc1.html52.android3.ios',
  recharge_channel INT(11) DEFAULT '0' COMMENT '充值渠道：0.江西银行（线上）1.其他',
  remark VARCHAR(255) DEFAULT '' COMMENT '备注',
  mobile VARCHAR(32) DEFAULT '' COMMENT '手机',
  update_time DATETIME COMMENT '更新时间',
  ip VARCHAR(32) DEFAULT '' COMMENT 'ip',
  response_message VARCHAR(2048)
);
ALTER TABLE gfb_sms RENAME TO gfb_sms_log;
ALTER TABLE gfb_statistic ADD id INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT;
CREATE TABLE gfb_suggest
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  content VARCHAR(500) COMMENT '意见建议内容',
  created_at DATETIME
);
CREATE TABLE gfb_task_scheduler
(
  id INT(11) PRIMARY KEY NOT NULL COMMENT '唯一标示' AUTO_INCREMENT,
  type INT(11) DEFAULT '0' COMMENT '调度类型: 0:委托申请状态查询, 1.提现状态调度',
  task_num INT(11) DEFAULT '10' COMMENT '调度次数',
  state INT(11) DEFAULT '0' COMMENT '0:未成功 1.成功',
  task_data VARCHAR(1024) DEFAULT '' COMMENT '调度任务数据',
  del INT(11) DEFAULT '0' COMMENT '是否有效: 0有效 ,1 无效',
  do_task_num INT(11) DEFAULT '0' COMMENT '已经执行次数',
  do_task_data VARCHAR(10240) DEFAULT '' COMMENT '每次执行结果累加',
  create_at DATETIME COMMENT '创建时间',
  update_at DATETIME COMMENT '修改时间'
);
CREATE TABLE gfb_third_batch_log
(
  ID INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  BATCH_NO VARCHAR(255),
  TYPE INT(11) COMMENT '1.投资人批次购买债权 2.即信批次放款 3.批次即信批次还款 4.批次担保人垫付 5.批次融资人还担保账户垫款 6.批次结束投资人债权 7.提前结清批次还款',
  SOURCE_ID INT(11),
  REMARK VARCHAR(1024),
  STATE INT(11) DEFAULT '0' COMMENT '批次状态 0未处理 1参数校验通过 2参数校验不通过 3已处理  4.已处理存在失败批次',
  ACQ_RES VARCHAR(1024) COMMENT '批次请求保留域',
  IPARAM1 INT(11),
  IPARAM2 INT(11),
  IPARAM3 INT(11),
  VPARAM1 VARCHAR(2048),
  VPARAM2 VARCHAR(255),
  VPARAM3 VARCHAR(255),
  CREATE_AT DATETIME,
  UPDATE_AT DATETIME
);
CREATE TABLE gfb_transfer
(
  id INT(11) PRIMARY KEY NOT NULL COMMENT 'id' AUTO_INCREMENT,
  state INT(11) DEFAULT '0' COMMENT '0.待审核 1.转让中 2.已转让 3.审核未通过 4.已取消',
  type TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '类型；0：普通；1：理财计划 2.垫付；',
  title VARCHAR(255) COMMENT '转让标题',
  transfer_money INT(255) COMMENT '转让总金额（本金+当期应计利息）',
  principal INT(20) DEFAULT '0' COMMENT '转让金额',
  already_interest INT(20) DEFAULT '0' COMMENT '当期应计利息',
  transfer_money_yes INT(11) COMMENT '已购买金额',
  time_limit INT(11) DEFAULT '0' COMMENT '剩余期数',
  start_order INT(11) COMMENT '开始转让期数',
  end_order INT(11) COMMENT '结束转让期数',
  apr INT(11) COMMENT '年利率',
  tender_count INT(11) DEFAULT '0' COMMENT '投标次数',
  is_lock INT(11) DEFAULT '0' COMMENT '是否锁定 0否 1是',
  lowest INT(11),
  repay_at DATETIME COMMENT '下一个还款日',
  tender_id INT(11) COMMENT '投资人投标id',
  borrow_id INT(11) COMMENT '投资借款id',
  user_id INT(11) COMMENT '转让人id',
  is_all INT(11) DEFAULT '0' COMMENT '是否是全部期数转让 0否 1是',
  borrow_collection_ids VARCHAR(255) COMMENT '部分转让时候 转让期数集合',
  del INT(11) DEFAULT '0' COMMENT '0.未删除 1.已删除',
  release_at DATETIME COMMENT '发布时间',
  verify_at DATETIME COMMENT '初审时间',
  created_at DATETIME,
  updated_at DATETIME,
  success_at DATETIME COMMENT '满标时间'
);
CREATE TABLE gfb_transfer_buy_log
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  state INT(11) DEFAULT '0' COMMENT '状态： 0.购买中  1.成功购买 2.失败购买, 3.取消购买',
  transfer_id INT(11),
  user_id INT(11) COMMENT '购买债权用户id',
  buy_money INT(11),
  valid_money INT(11) COMMENT '有效金额（本金+当期应计利息）',
  auto_order INT(11) DEFAULT '0' COMMENT '自动购买债权转让 order',
  principal INT(11) COMMENT '购买转让本金',
  already_interest INT(11) COMMENT '当期应计利息',
  source INT(11) DEFAULT '0' COMMENT '投标来源；0 pc 1：android；2：ios 3：H5',
  auto INT(11) DEFAULT '0' COMMENT '是否自动购买债权转让 0否 1是',
  del INT(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  freeze_order_id VARCHAR(255) COMMENT '冻结资产orderid',
  created_at DATETIME,
  updated_at DATETIME,
  third_transfer_flag INT(11) DEFAULT '0' COMMENT '标识是否在存管系统登记购买债权， 0否 1是',
  third_transfer_order_id VARCHAR(255) DEFAULT '' COMMENT '购买债券转让编号',
  transfer_auth_code VARCHAR(255) COMMENT '即信债权转让授权码',
  type TINYINT(1) unsigned DEFAULT '0' NOT NULL COMMENT '类型；0：普通；1：理财计划 2.垫付；',
  t_credit_end_order_id VARCHAR(255),
  third_credit_end_flag INT(11)
);
ALTER TABLE gfb_user_cache MODIFY award_virtual_money BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '赠送体验金';
ALTER TABLE gfb_user_cache MODIFY cash_total BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '提现总额';
ALTER TABLE gfb_user_cache MODIFY expenditure_fee BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '费用支出';
ALTER TABLE gfb_user_cache MODIFY expenditure_interest BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '利息支出';
ALTER TABLE gfb_user_cache MODIFY expenditure_interest_manage BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '利息管理费支出';
ALTER TABLE gfb_user_cache MODIFY expenditure_manage BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '账户管理费支出';
ALTER TABLE gfb_user_cache MODIFY expenditure_other BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '其他支出';
ALTER TABLE gfb_user_cache MODIFY expenditure_overdue BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '逾期支出';
ALTER TABLE gfb_user_cache ALTER COLUMN first_tender_award SET DEFAULT '0';
ALTER TABLE gfb_user_cache MODIFY income_award BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '已转奖励';
ALTER TABLE gfb_user_cache MODIFY income_bonus BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '提成收入（推荐人）';
ALTER TABLE gfb_user_cache MODIFY income_integral_cash BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '积分折现';
ALTER TABLE gfb_user_cache MODIFY income_interest BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '已赚利息';
ALTER TABLE gfb_user_cache MODIFY income_other BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '其他收入';
ALTER TABLE gfb_user_cache MODIFY income_overdue BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '逾期收入';
ALTER TABLE gfb_user_cache MODIFY qd_wait_collection_interest BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '渠道标代收利息';
ALTER TABLE gfb_user_cache MODIFY qd_wait_collection_principal BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '渠道标代收本金';
ALTER TABLE gfb_user_cache MODIFY recharge_total BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '充值总额';
ALTER TABLE gfb_user_cache MODIFY tender_jingzhi INT(10) NOT NULL DEFAULT '0' COMMENT '首投信用标';
ALTER TABLE gfb_user_cache MODIFY tender_miao INT(10) NOT NULL DEFAULT '0' COMMENT '首投秒标';
ALTER TABLE gfb_user_cache MODIFY tender_qudao INT(10) DEFAULT '0' COMMENT '首投渠道标';
ALTER TABLE gfb_user_cache MODIFY tender_transfer INT(10) NOT NULL DEFAULT '0' COMMENT '首投转让标';
ALTER TABLE gfb_user_cache MODIFY tender_tuijian INT(10) NOT NULL DEFAULT '0' COMMENT '首投车贷标';
ALTER TABLE gfb_user_cache MODIFY tj_wait_collection_interest BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '车贷标代收利息';
ALTER TABLE gfb_user_cache MODIFY tj_wait_collection_principal BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '车贷标代收本金';
ALTER TABLE gfb_user_cache MODIFY wait_collection_interest BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '待收利息';
ALTER TABLE gfb_user_cache MODIFY wait_collection_principal BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '待收本金';
ALTER TABLE gfb_user_cache MODIFY wait_repay_interest BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '待还利息';
ALTER TABLE gfb_user_cache MODIFY wait_repay_principal BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '待还本金';
ALTER TABLE gfb_user_cache MODIFY yesterday_use_money BIGINT(20) unsigned NOT NULL DEFAULT '0' COMMENT '昨日可用余额';
ALTER TABLE gfb_user_info ALTER COLUMN address SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN birthday_md SET DEFAULT '0';
ALTER TABLE gfb_user_info ALTER COLUMN birthday_y SET DEFAULT '0';
ALTER TABLE gfb_user_info ALTER COLUMN card_pic1 SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN card_pic2 SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN graduated_school SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN qq SET DEFAULT '';
ALTER TABLE gfb_user_info ALTER COLUMN realname SET DEFAULT '';
CREATE TABLE gfb_user_third_account
(
  id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  user_id INT(11) DEFAULT '0' COMMENT '用户Id',
  account_id VARCHAR(50) DEFAULT '' COMMENT '电子账户账号',
  name VARCHAR(50) DEFAULT '' COMMENT '真实姓名',
  acct_use INT(11) DEFAULT '0' COMMENT '0.普通用户；1.红包账户，2.企业账户',
  card_no VARCHAR(50) DEFAULT '' COMMENT '银行卡',
  id_type INT(11) DEFAULT '1' COMMENT '证件类型。 1身份证',
  id_no VARCHAR(50) DEFAULT '' COMMENT '证件号码',
  mobile VARCHAR(50) DEFAULT '' COMMENT '开户手机',
  channel INT(11) DEFAULT '0' COMMENT '渠道',
  password_state INT(11) DEFAULT '0' COMMENT '初始密码状态（0，未初始化，1.初始化）',
  card_no_bind_state INT(11) DEFAULT '1' COMMENT '银行卡绑定状态（0，未绑定，1.已绑定）',
  create_at DATETIME,
  update_at DATETIME,
  create_id INT(11) DEFAULT '0',
  update_id INT(11) DEFAULT '0',
  del INT(11) COMMENT '0，有效， 1.无效',
  auto_tender_order_id VARCHAR(255) COMMENT '自动投标签约订单号',
  auto_tender_tx_amount INT(12) DEFAULT '0' COMMENT '单笔投标金额的上限',
  auto_tender_tot_amount INT(12) DEFAULT '0' COMMENT '自动投标总金额上限',
  auto_transfer_bond_order_id VARCHAR(255) COMMENT '自动债券转让签约单号',
  auto_tender_state INT(11) DEFAULT '0',
  auto_transfer_state INT(11) DEFAULT '0',
  bank_name VARCHAR(255) DEFAULT '',
  bank_logo VARCHAR(255) DEFAULT ''
);
ALTER TABLE gfb_users ADD avatar_path VARCHAR(100) DEFAULT '' NULL COMMENT '头像地址';
ALTER TABLE gfb_users ADD ip VARCHAR(255) DEFAULT '' NULL;
ALTER TABLE gfb_users ADD login_time DATETIME NULL COMMENT '最近一次登录时间';
ALTER TABLE gfb_users ALTER COLUMN password SET DEFAULT '';
ALTER TABLE gfb_users ALTER COLUMN pay_password SET DEFAULT '';
ALTER TABLE gfb_users ADD platform INT(11) DEFAULT '-1' NULL COMMENT '最近登录的平台';
ALTER TABLE gfb_users ADD push_id VARCHAR(255) DEFAULT '' NULL;
ALTER TABLE gfb_users ADD push_state INT(11) DEFAULT '1' NULL;
ALTER TABLE gfb_users MODIFY realname VARCHAR(25) NOT NULL DEFAULT '' COMMENT '真实姓名';
ALTER TABLE gfb_users MODIFY type VARCHAR(10) NOT NULL DEFAULT 'borrower' COMMENT '用户类型（manager：管理员；borrower：浏览者；financer:理财用户）';
ALTER TABLE gfb_users ADD windmill_id VARCHAR(16) NULL COMMENT '风车理财id';
ALTER TABLE gfb_yesterday_asset MODIFY collection BIGINT(20) NOT NULL COMMENT '代收金额（分）';
ALTER TABLE gfb_yesterday_asset MODIFY no_use_money BIGINT(20) NOT NULL COMMENT '冻结金额（分）';
ALTER TABLE gfb_yesterday_asset MODIFY payment BIGINT(20) NOT NULL COMMENT '待还金额（分）';
ALTER TABLE gfb_yesterday_asset MODIFY use_money BIGINT(20) NOT NULL COMMENT '可用金额（分）';
ALTER TABLE gfb_yesterday_asset MODIFY virtual_money BIGINT(20) NOT NULL COMMENT '体验金（分）';


CREATE TABLE `gfb_third_error_remark` (
  `id` int(11) NOT NULL,
  `state` int(11) DEFAULT '0' COMMENT '0未解决 1已解决',
  `user_id` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `source_id` int(11) DEFAULT NULL,
  `to_user_id` int(11) DEFAULT NULL,
  `third_req_str` text,
  `third_resp_str` text,
  `error_msg` text COMMENT '异常信息',
  `remark` varchar(2048) DEFAULT NULL,
  `is_del` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#资金类型字段长度扩容
ALTER TABLE gfb_new_asset_log MODIFY local_type VARCHAR(50) DEFAULT '' COMMENT '本地交易类型';

CREATE TABLE gfb_eve
(
  id INT PRIMARY KEY AUTO_INCREMENT,
  acqcode VARCHAR(11) DEFAULT '' COMMENT '受理方标识码',
  seqno VARCHAR(6) DEFAULT '' COMMENT '系统跟踪号',
  sendt VARCHAR(11) DEFAULT '' COMMENT '交易传输时间',
  cardnbr VARCHAR(19) DEFAULT '' COMMENT '主账号',
  amount VARCHAR(12) DEFAULT '' COMMENT '交易金额',
  crflag VARCHAR(1) DEFAULT '' COMMENT '交易金额符号',
  msgtype VARCHAR(4) DEFAULT '' COMMENT '消息类型',
  proccode VARCHAR(6) DEFAULT '' COMMENT '交易类型码',
  mertype VARCHAR(4) DEFAULT '' COMMENT '商户类型',
  term VARCHAR(8) DEFAULT '' COMMENT '受卡机终端标识码',
  retseqno VARCHAR(12) DEFAULT '' COMMENT '检索参考号',
  conmode VARCHAR(2) DEFAULT '' COMMENT '服务点条件码',
  autresp VARCHAR(6) DEFAULT '' COMMENT '授权应答码',
  forcode VARCHAR(11) DEFAULT '' COMMENT '发送方标识码',
  clrdate VARCHAR(4) DEFAULT '' COMMENT '清算日期',
  oldseqno VARCHAR(6) DEFAULT '' COMMENT '原始交易的系统跟踪号',
  openbrno VARCHAR(6) DEFAULT '' COMMENT '发卡网点号',
  tranbrno VARCHAR(6) DEFAULT '' COMMENT '交易网点',
  ervind VARCHAR(1) DEFAULT '' COMMENT '冲正、撤销标志',
  transtype VARCHAR(4) DEFAULT '' COMMENT '主机交易类型',
  create_at DATETIME COMMENT '创建时间'
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE gfb_eve COMMENT = '存管系统交易明细';

CREATE TABLE gfb_aleve
(
  id INT PRIMARY KEY AUTO_INCREMENT,
  bank VARCHAR(4) DEFAULT '' COMMENT '银行号',
  cardnbr VARCHAR(19) DEFAULT '' COMMENT '电子账号',
  amount VARCHAR(17) DEFAULT '' COMMENT '交易金额',
  cur_num VARCHAR(3) DEFAULT '' COMMENT '货币代码',
  crflag VARCHAR(1) DEFAULT '' COMMENT '交易金额符号',
  valdate VARCHAR(8) DEFAULT '' COMMENT '入帐日期',
  inpdate VARCHAR(8) DEFAULT '' COMMENT '交易日期',
  reldate VARCHAR(8) DEFAULT '' COMMENT '自然日期',
  inptime VARCHAR(8) DEFAULT '' COMMENT '交易时间',
  tranno VARCHAR(6) DEFAULT '' COMMENT '交易流水号',
  ori_tranno VARCHAR(6) DEFAULT '' COMMENT '关联交易流水号',
  transtype VARCHAR(4) DEFAULT '' COMMENT '交易类型',
  desline VARCHAR(42) DEFAULT '' COMMENT '交易描述',
  curr_bal VARCHAR(17) DEFAULT '' COMMENT '交易后余额',
  forcardnbr VARCHAR(19) DEFAULT '' COMMENT '对手交易帐号',
  revind VARCHAR(1) DEFAULT '' COMMENT '冲正、撤销标志',
  resv VARCHAR(200) DEFAULT '' COMMENT '保留域',
  create_at DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE gfb_aleve COMMENT = '交易明细全流水' ;

DROP TABLE IF EXISTS `gfb_third_error_remark`;
CREATE TABLE `gfb_third_error_remark` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `state` int(11) DEFAULT '0' COMMENT '0未解决 1已解决',
  `user_id` int(11) DEFAULT NULL COMMENT '冗余',
  `type` int(11) DEFAULT NULL COMMENT '批次类型 ''1.投资人批次购买债权 2.即信批次放款 3.批次即信批次还款 4.批次担保人垫付 5.批次融资人还担保账户垫款 6.批次结束投资人债权 7.提前结清批次还款''',
  `source_id` int(11) DEFAULT NULL,
  `old_batch_no` varchar(255) DEFAULT NULL,
  `to_user_id` int(11) DEFAULT NULL,
  `third_req_str` text,
  `third_resp_str` text,
  `third_error_msg` text COMMENT '第三方异常信息',
  `error_msg` text COMMENT '本地异常信息',
  `remark` varchar(2048) DEFAULT NULL,
  `is_del` int(11) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=873 DEFAULT CHARSET=utf8;

create table gfb_financial_scheduler
(
  id int auto_increment primary key,
  name varchar(255) default '' null comment '调度名称',
  data varchar(1024) default '' null comment '调度数据',
  do_num int default '0' null comment '调度此处',
  res_msg varchar(1024) default '' null comment '结果',
  create_at datetime null comment '调度创建时间',
  update_at datetime null comment '更新时间',
  state int default '0' null comment '调度状态(0, 失败, 1.成功)'
)ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
ALTER TABLE gfb_financial_scheduler COMMENT = '对账系统调度';

INSERT INTO gfb_dict_value ( `ITEM_ID`, `VALUE01`, `VALUE02`, `VALUE03`, `VALUE04`, `VALUE05`, `VALUE06`, `CREATE_TIME`, `UPDATE_TIME`, `CREATE_ID`, `UPDATE_ID`, `NAME`, `DEL`) VALUES ('7', 'takeUserId', '实际收款人', '45184', '', '', '', '2017-09-01 14:37:17', '2017-09-01 14:37:20', '0', '0', '', '0');

ALTER TABLE gfb_eve ADD query_date VARCHAR(8) DEFAULT '' NULL COMMENT '查询时间';
ALTER TABLE gfb_aleve ADD query_date VARCHAR(8) DEFAULT '' NULL COMMENT '查询时间';

alter table gfb_batch_asset_change_item change  column to_user_id for_user_id int(11) DEFAULT NULL COMMENT '交易对方ID';

INSERT INTO `gfb_sms_template` ( `ALIAS_CODE`, `TEMPLATE`, `IS_DEL`, `IS_ACTIVE`, `TYPE`, `CREATE_TIME`, `CREATE_ID`, `UPDATE_TIME`, `UPDATE_ID`) VALUES ('SMS_BORROW_CANCEL_TENDER', '【广富宝】你所投资的[编号：{id}][借款：{name}]在{timestamp}已取消', '0', '1', '0', '2017-09-12 16:07:32', '0', '2017-09-12 16:07:40', '0');
INSERT INTO `gfb_sms_template` (`ALIAS_CODE`, `TEMPLATE`, `IS_DEL`, `IS_ACTIVE`, `TYPE`, `CREATE_TIME`, `CREATE_ID`, `UPDATE_TIME`, `UPDATE_ID`) VALUES ('SMS_BORROW_CANCEL_BORROW', '【广富宝】你发布的[编号：{id}][借款：{name}]，在{timestamp}停止募集，已取消', '0', '1', '0', '2017-09-12 16:07:36', '0', '2017-09-12 16:07:44', '0');

ALTER TABLE gfb_borrow ADD `lend_repay_status` int(11) DEFAULT '0' COMMENT '放款即信通信状态 0.未处理 1.处理中 2.处理失败 3.处理成功';

ALTER TABLE gfb_borrow ADD `first_most` int(10) DEFAULT '0' COMMENT '第一笔限额';

ALTER TABLE gfb_borrow_repayment ADD   `repay_status` int(11) DEFAULT '0' COMMENT '还款即信通信状态 0.未处理 1.处理中 2.处理失败 3.处理成功';


CREATE TABLE `gfb_third_batch_deal_log` (
  `id` int(11) NOT NULL,
  `batch_id` int(11) DEFAULT NULL COMMENT '批次号',
  `state` int(11) DEFAULT '0' COMMENT '0待处理 1.未通过 2.已通过',
  `error_msg` varchar(2048) DEFAULT NULL COMMENT '错误信息',
  `status` int(11) DEFAULT '1' COMMENT '状态：0.失败 1.成功',
  `type` int(11) DEFAULT NULL COMMENT '1.投资人批次购买债权 2.即信批次放款 3.批次即信批次还款 4.批次担保人垫付 5.批次融资人还担保账户垫款 6.批次结束投资人债权 7.提前结清批次还款',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE gfb_transfer ADD   `recheck_at` datetime DEFAULT NULL COMMENT '即信复审时间';


ALTER TABLE gfb_transfer ADD   `right_money` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '已匹配金额';

ALTER TABLE gfb_finance_plan ADD   `left_money` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '剩余未匹配金额';

ALTER TABLE gfb_borrow_tender ADD   `finance_buy_id` int(11) DEFAULT NULL COMMENT '理财计划购买id';
ALTER TABLE gfb_borrow ADD   `is_finance` tinyint(1) DEFAULT '0' COMMENT '是否是理财计划 0否 1是';
ALTER TABLE gfb_borrow_repayment ADD  `repay_trigger_at` datetime DEFAULT NULL COMMENT '还款触发时间';

ALTER TABLE gfb_third_batch_log ADD `TX_DATE` varchar(11) DEFAULT NULL COMMENT '即信交易日期';
ALTER TABLE gfb_third_batch_log ADD `TX_TIME` varchar(11) DEFAULT NULL COMMENT '即信交易时间';
ALTER TABLE gfb_third_batch_log ADD `SEQ_NO` varchar(11) DEFAULT NULL COMMENT '即信交易时间';
ALTER TABLE gfb_transfer_buy_log ADD  `finance_buy_id` int(11) DEFAULT NULL COMMENT '理财计划购买id';


# 2017-09-29 新版new_aleve
create table gfb_new_aleve
(
  id int auto_increment
    primary key,
  bank  varchar(4) default '' null  comment '银行号',
  cardnbr  varchar(19) default '' null  comment '电子账号',
  amount  varchar(17) default '' null  comment '交易金额',
  cur_num  varchar(3) default '' null  comment '货币代码',
  crflag  varchar(1) default '' null  comment '交易金额符号',
  valdate  varchar(8) default '' null  comment '入帐日期',
  inpdate  varchar(8) default '' null  comment '交易日期',
  reldate  varchar(8) default '' null  comment '自然日期',
  inptime  varchar(8) default '' null  comment '交易时间',
  tranno  varchar(6) default '' null  comment '交易流水号',
  ori_tranno  varchar(6) default '' null  comment '关联交易流水号',
  transtype  varchar(4) default '' null  comment '交易类型',
  desline  varchar(42) default '' null  comment '交易描述',
  curr_bal  varchar(17) default '' null  comment '交易后余额',
  forcardnbr  varchar(19) default '' null  comment '对手交易帐号',
  revind  varchar(1) default '' null  comment '冲正、撤销标志',
  accchg  varchar(1) default '' null  comment '交易标识',
  seqno  varchar(6) default '' null  comment '系统跟踪号',
  ori_num  varchar(6) default '' null  comment '原交易流水号',
  resv  varchar(187) default '' null  comment '保留域',
  query_time varchar(8) default '' null  comment '入库时间'
) comment '新版ALEVE日志' engine=InnoDB charset=utf8;

# 2017-09-29 新版new_aleve
create table gfb_new_eve
(
  id int auto_increment
    primary key,
  forcode varchar(11) default '' null  comment '发送方标识码',
  seqno varchar(6) default '' null  comment '系统跟踪号',
  cendt varchar(10) default '' null  comment '交易传输时间',
  cardnbr varchar(19) default '' null  comment '主账号',
  amount varchar(12) default '' null  comment '交易金额',
  crflag varchar(1) default '' null  comment '交易金额符号',
  msgtype varchar(4) default '' null  comment '消息类型',
  proccode varchar(6) default '' null  comment '交易类型码',
  orderno varchar(40) default '' null  comment '订单号',
  tranno varchar(6) default '' null  comment '内部交易流水号',
  reserved varchar(19) default '' null  comment '内部保留域',
  ervind varchar(1) default '' null  comment '冲正、撤销标志',
  transtype varchar(4) default '' null  comment '主机交易类型',
  query_time varchar(8) default '' null  comment '创建时间'
) comment '新版EVE日志' engine=InnoDB charset=utf8;

ALTER TABLE gfb_finance_plan_buyer ADD   `state` int(10) DEFAULT '1' COMMENT '1:投标中； 2:还款中 ;3:已结清';
ALTER TABLE gfb_new_asset_log ADD  `state` int(11) NOT NULL DEFAULT '0' COMMENT '记录状态：0普通 1.理财计划';

#2017-09-29
ALTER TABLE gfb_users ADD starfire_register_token VARCHAR(100) NULL comment '星火智投token';
ALTER TABLE gfb_users ADD starfire_user_id VARCHAR(50) NULL COMMENT '星火平台id';
ALTER TABLE gfb_users ADD starfire_bind_date DATETIME NULL COMMENT '星火用户绑定时间';
ALTER TABLE gfb_borrow ADD is_starfire tinyint(1)  DEFAULT 0 COMMENT '是否推星火平台';


create table jixin_asset(
  id int primary key auto_increment,
  account_id varchar(64) null default '' comment '电子账户',
  user_id int null default 0 comment '用户ID',
  update_time datetime null comment '修改时间',
  curr_money bigint null default 0 comment '本地金额'
) engine=innodb , charset =utf8;

ALTER TABLE gfb_new_eve ADD mertype VARCHAR(4) DEFAULT '' NULL  COMMENT '商户类型';
ALTER TABLE gfb_new_eve ADD term VARCHAR(8) DEFAULT '' NULL  COMMENT '受卡机终端标识码';
ALTER TABLE gfb_new_eve ADD retseqno VARCHAR(12) DEFAULT '' NULL  COMMENT '检索参考号';
ALTER TABLE gfb_new_eve ADD conmode VARCHAR(2) DEFAULT '' NULL  COMMENT '服务点条件码';
ALTER TABLE gfb_new_eve ADD autresp VARCHAR(6) DEFAULT '' NULL  COMMENT '授权应答码';
ALTER TABLE gfb_new_eve ADD clrdate VARCHAR(4) DEFAULT '' NULL  COMMENT '清算日期';
ALTER TABLE gfb_new_eve ADD oldseqno VARCHAR(6) DEFAULT '' NULL  COMMENT '原始交易的系统跟踪号';
ALTER TABLE gfb_new_eve ADD openbrno VARCHAR(6) DEFAULT '' NULL  COMMENT '发卡网点号';
ALTER TABLE gfb_new_eve ADD tranbrno VARCHAR(6) DEFAULT '' NULL  COMMENT '交易网点';
ALTER TABLE gfb_new_eve ADD acqcode VARCHAR(11) DEFAULT '' NULL  COMMENT '受理方标识码';

ALTER TABLE jixin_asset ADD INDEX `jixin_asset_account_id_index` (`account_id` ASC);
ALTER TABLE jixin_asset ADD INDEX `jixin_asset_user_id` (`user_id` ASC);
ALTER TABLE gfb_new_aleve  ADD INDEX `gfb_new_aleve_reldateAndInputtimeAndTranno` (`reldate` ASC, `inptime` ASC, `tranno` ASC);
ALTER TABLE gfb_new_aleve ADD INDEX `gfb_new_aleve_cardnbr` (`cardnbr` ASC);

ALTER TABLE gfb_new_eve
  ADD INDEX `gfb_new_eve_orderno` (`orderno` ASC),
  ADD INDEX `gfb_new_eve_cardnbr` (`cardnbr` ASC);


ALTER TABLE  gfb_user_third_account
  ADD INDEX `gfb_user_third_account_user_id` (`user_id` ASC),
  ADD INDEX `gfb_user_third_account_account_id` (`account_id` ASC);

ALTER TABLE gfb_new_asset_log ADD `type` int(11) DEFAULT '0' COMMENT '资金变动类型：0基本 1.理财计划 默认为0';

ALTER TABLE gfb_batch_asset_change_item ADD `asset_type` int(11) DEFAULT '0' COMMENT '资金变动类型：0基本 1.金服理财计划 2.理财计划，默认为0';


DROP TABLE IF EXISTS `gfb_application`;
CREATE TABLE `gfb_application` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) DEFAULT NULL COMMENT '应用名',
  `sketch` varchar(200) DEFAULT NULL COMMENT '应用简介',
  `created_at` datetime DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL COMMENT 'logo',
  `qrode_url` varchar(255) DEFAULT NULL COMMENT '应用二维码地址',
  `alias_name` varchar(20) DEFAULT NULL,
  `update_at` datetime DEFAULT NULL COMMENT '更新时间',
  `terminal` int(11) DEFAULT '0' COMMENT '终端：1:andrion ; 2:ISO;3:H5',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gfb_application_id_uindex` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for gfb_application_version
-- ----------------------------
DROP TABLE IF EXISTS `gfb_application_version`;
CREATE TABLE `gfb_application_version` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `application_id` int(10) DEFAULT NULL COMMENT '应用id',
  `terminal` int(2) DEFAULT NULL COMMENT '终端: ',
  `version_id` int(2) DEFAULT NULL COMMENT '版本id',
  `view_version` varchar(10) CHARACTER SET latin1 DEFAULT NULL COMMENT '展示描述版本id\n',
  `description` varchar(500) DEFAULT NULL COMMENT '版本描述',
  `force` tinyint(4) DEFAULT '0' COMMENT '是否强制更新',
  `application_url` varchar(255) CHARACTER SET latin1 DEFAULT NULL COMMENT '版本地址\n',
  `update_at` datetime DEFAULT NULL COMMENT '更新时间',
  `created_at` datetime DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `gfb_application_version_id_uindex` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE gfb_users ADD wheel_id VARCHAR(50) NULL;
ALTER TABLE gfb_users ADD bind_wheel_date DATETIME NULL;



CREATE TABLE `gfb_realtime_asset` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NULL COMMENT '用户ID',
  `account_id` INT NULL COMMENT '存管账户ID',
  `username` VARCHAR(45) NULL COMMENT '用户昵称',
  `phone` VARCHAR(45) NULL COMMENT '手机',
  `jixin_total_amount` DECIMAL(16,2) NULL COMMENT '存管账户总额',
  `local_total_amount` DECIMAL(16,2) NULL COMMENT '本地总金额',
  `jixin_use_amount` DECIMAL(16,2) NULL COMMENT '存管可用金额',
  `local_use_amount` DECIMAL(16,2) NULL COMMENT '本地可用金额',
  `inteval_money` DECIMAL(16,2) NULL COMMENT '相差金额(存管账户总额-本地总金额)',
  `create_time` DATETIME NULL COMMENT '查询时间',
  `batch_no` BIGINT NULL COMMENT '查询批次(每一次调用资金比对, 生成最新批次)',
  PRIMARY KEY (`id`))
  COMMENT = '实时查询存管金额记录表' , charset="utf8";


# 账户中添加用户活跃状态
ALTER TABLE `gfb_user_third_account`
  ADD COLUMN `active_state` int(11) NULL DEFAULT 0 COMMENT '用户活跃状态: 1.活跃账户, 0, 僵尸用户' AFTER `bank_logo`;



ALTER TABLE gfb_user_cache
  ADD wait_expenditure_interest_manage INT(10) UNSIGNED NOT NULL DEFAULT 0
COMMENT '待付利息管理费'
  AFTER expenditure_interest_manage;


DROP TABLE IF EXISTS `gfb_user_address`;
CREATE TABLE `gfb_user_address` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` int(11) DEFAULT '0' COMMENT '地址类型：0.收货地址',
  `name` varchar(255) DEFAULT NULL COMMENT '收件人姓名',
  `phone` varchar(25) DEFAULT NULL COMMENT '收货号码',
  `country` varchar(255) DEFAULT NULL COMMENT '国家',
  `province` varchar(255) DEFAULT NULL COMMENT '身份',
  `city` varchar(255) DEFAULT NULL COMMENT '城市',
  `district` varchar(255) DEFAULT NULL COMMENT '地区（市区/县）',
  `detailed_address` varchar(1024) DEFAULT NULL COMMENT '详细地址',
  `default` int(11) DEFAULT '0' COMMENT '是否默认地址：0否，1是',
  `del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `update_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 主题类型表
CREATE TABLE gfb_topic_type (
  id INT AUTO_INCREMENT COMMENT '主题唯一标识',
  topic_type_name VARCHAR(32) DEFAULT '未命名' COMMENT '主题类型名称',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序, 数字越大越靠前',
  hot_state INT NOT NULL DEFAULT 0 COMMENT '最热标识',
  new_state INT NOT NULL DEFAULT 0 COMMENT '最新标识',
  topic_total_num INT NOT NULL DEFAULT 0 COMMENT '帖子总数数量',
  icon_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '主题类型icon',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  admin_id INT NOT NULL DEFAULT 0 COMMENT '主题管理员',
  del INT NOT NULL DEFAULT 0 COMMENT '记录有效状态, 1 .删除',
  PRIMARY KEY (id)
)  CHARSET=UTF8MB4 , COMMENT '主题类型表';


-- 主题表
CREATE TABLE gfb_topics (
  id BIGINT AUTO_INCREMENT COMMENT '主题唯一标识',
  titel VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标的',
  topic_type_id INT NOT NULL DEFAULT 0 COMMENT '主题类型Id',
  user_id INT NOT NULL DEFAULT 0 COMMENT '发帖用户ID',
  user_name VARCHAR(36) NOT NULL DEFAULT '' COMMENT '用户名-此处冗余',
  user_icon_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '用户头像-此处冗余',
  sort INT NOT NULL DEFAULT 0 COMMENT '(只有设置顶置状态, 此字段才有效果)排序, 数字越大越靠前',
  fix_state INT NOT NULL DEFAULT 0 COMMENT '是否顶置 1: 为顶置贴',
  hot_state INT NOT NULL DEFAULT 0 COMMENT '最热标识',
  new_state INT NOT NULL DEFAULT 0 COMMENT '最新标识',
  top_total_num INT NOT NULL DEFAULT 0 COMMENT '点赞总数',
  content_total_num INT NOT NULL DEFAULT 0 COMMENT '评论总数',
  view_total_num INT NOT NULL DEFAULT 0 COMMENT '浏览人数',
  del INT NOT NULL DEFAULT 0 COMMENT '记录有效状态, 1 .删除',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  img1 VARCHAR(255) NULL COMMENT '图片1',
  img2 VARCHAR(255) NULL COMMENT '图片1',
  img3 VARCHAR(255) NULL COMMENT '图片1',
  img4 VARCHAR(255) NULL COMMENT '图片1',
  img5 VARCHAR(255) NULL COMMENT '图片1',
  img6 VARCHAR(255) NULL COMMENT '图片1',
  img7 VARCHAR(255) NULL COMMENT '图片1',
  img8 VARCHAR(255) NULL COMMENT '图片1',
  img9 VARCHAR(255) NULL COMMENT '图片1',
  content VARCHAR(1024) NULL COMMENT '主题内容',
  PRIMARY KEY (id),
  INDEX (topic_type_id),
  INDEX (user_id)
)  CHARSET=UTF8MB4 , COMMENT '主题';


-- 评论表
CREATE TABLE gfb_topics_comment (
  id BIGINT AUTO_INCREMENT COMMENT '评论唯一标识',
  topic_id INT NOT NULL DEFAULT 0 COMMENT '主题id',
  topic_type_id INT NOT NULL DEFAULT 0 COMMENT '主题类型Id',
  content VARCHAR(255) NOT NULL DEFAULT '无评论' COMMENT '评论内容',
  user_id INT NOT NULL DEFAULT 0 COMMENT '评论用户ID',
  user_name VARCHAR(36) NOT NULL DEFAULT '' COMMENT '用户名-此处冗余',
  user_icon_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '用户头像-此处冗余',
  top_total_num INT NOT NULL DEFAULT 0 COMMENT '点赞总数',
  content_total_num INT NOT NULL DEFAULT 0 COMMENT '回复总数',
  del INT NOT NULL DEFAULT 0 COMMENT '记录有效状态, 1 .删除',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (topic_id),
  INDEX (user_id)
)  CHARSET=UTF8MB4 , COMMENT '评论表';

-- 回复表
CREATE TABLE gfb_topics_reply (
  id BIGINT AUTO_INCREMENT COMMENT '回复唯一标识',
  topic_id BIGINT NOT NULL DEFAULT 0 COMMENT '主题id',
  topic_comment_id BIGINT NOT NULL DEFAULT 0 COMMENT '评论表ID',
  topic_type_id INT NOT NULL DEFAULT 0 COMMENT '主题类型Id',
  topic_reply_id BIGINT NOT NULL DEFAULT 0 COMMENT '回复Id',
  reply_type INT NOT NULL DEFAULT 0 COMMENT '回复类型: 0 评论, 1.回复',
  content VARCHAR(255) NOT NULL DEFAULT '无评论' COMMENT '评论内容',
  top_total_num INT NOT NULL DEFAULT 0 COMMENT '点赞总数',
  user_id INT NOT NULL DEFAULT 0 COMMENT '回复用户ID',
  user_name VARCHAR(36) NOT NULL DEFAULT '' COMMENT '用户名-此处冗余',
  user_icon_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '用户头像-此处冗余',
  for_user_id INT NOT NULL DEFAULT 0 COMMENT '被@的用户Id , 如果回复类型为评论直接为评论用户ID',
  for_user_name VARCHAR(36) NOT NULL DEFAULT '' COMMENT '用户名-此处冗余',
  for_user_icon_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '用户头像-此处冗余',
  del INT NOT NULL DEFAULT 0 COMMENT '记录有效状态, 1 .删除',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (topic_id),
  INDEX (topic_comment_id),
  INDEX (user_id)
)  CHARSET=UTF8MB4 , COMMENT '评论表';

-- 点赞记录表
CREATE TABLE gfb_topics_top_record (
  id BIGINT AUTO_INCREMENT COMMENT '点赞记录唯一标识',
  user_id INT NOT NULL DEFAULT 0 COMMENT '回复用户ID',
  source_id BIGINT NOT NULL DEFAULT 0 COMMENT '点赞来源',
  source_type INT NOT NULL DEFAULT 0 COMMENT '点赞类型 0:主题点赞, 1:评论点赞, 2:回复点赞',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (user_id),
  INDEX (user_id , source_id , source_type)
)  CHARSET=UTF8MB4 , COMMENT '点赞记录标';


-- 论坛举报表
DROP TABLE IF EXISTS gfb_topics_report;
CREATE TABLE gfb_topics_report (
  id BIGINT AUTO_INCREMENT COMMENT '帖子举报唯一标识',
  source_id BIGINT NOT NULL DEFAULT 0 COMMENT '点赞来源',
  source_type INT NOT NULL DEFAULT 0 COMMENT '点赞类型 0:主题点赞, 1:评论点赞, 2:回复点赞',
  report_type INT NOT NULL DEFAULT 0 COMMENT '举报类型 0: 广告, 1: 政治有害类, 2: 暴恐类. 3:淫秽色情类, 4:赌博类, 5:诈骗类,  6:其他有害类',
  user_id INT NOT NULL DEFAULT 0 COMMENT '举报用户ID',
  report_user_id INT NOT NULL DEFAULT 0 COMMENT '被举报用户ID',
  response_state INT NOT NULL DEFAULT 0 COMMENT '举报处理状态 0:待处理, 1:处理成功, 2:处理失败',
  response_content VARCHAR(128) NOT NULL DEFAULT '待处理' COMMENT '举报结果',
  response_date DATETIME NULL COMMENT '举报处理时间',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (user_id),
  INDEX (report_user_id),
  INDEX (user_id , source_id , source_type)
)  CHARSET=UTF8MB4 , COMMENT '帖子举报表';



-- 论坛用户表
DROP TABLE IF EXISTS gfb_topics_users;
CREATE TABLE gfb_topics_users (
  id BIGINT AUTO_INCREMENT COMMENT '点赞记录唯一标识',
  user_id INT NOT NULL DEFAULT 0 COMMENT '用户ID',
  username varchar(32) NOT NULL DEFAULT '' COMMENT '用户昵称',
  avatar varchar(255) NOT NULL DEFAULT '' COMMENT '头像地址',
  force_state INT NOT NULL DEFAULT 0 COMMENT '冻结状态 0:主题点赞, 1:评论点赞, 2:回复点赞',
  level_id INT NOT NULL DEFAULT 0 COMMENT '等级ID',
  use_integral INT NOT NULL DEFAULT 0 COMMENT '可用积分',
  no_use_integral INT NOT NULL DEFAULT 0 COMMENT '不可用积分',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (user_id)
)  CHARSET=UTF8MB4 , COMMENT '论坛用户表';

-- 论坛积分变动记录表
DROP TABLE IF EXISTS gfb_topics_integral_record;
CREATE TABLE gfb_topics_integral_record (
  id BIGINT AUTO_INCREMENT COMMENT '点赞记录唯一标识',
  user_id INT NOT NULL DEFAULT 0 COMMENT '用户ID',
  op_type VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作类型',
  op_name VARCHAR(32) NOT NULL DEFAULT '' COMMENT '操作名称',
  op_flag VARCHAR(2) NOT NULL DEFAULT 'D' COMMENT 'D 标识加, C 标识减',
  op_money INT NOT NULL DEFAULT 0 COMMENT '操作积分',
  use_integral INT NOT NULL DEFAULT 0 COMMENT '可用积分',
  no_use_integral INT NOT NULL DEFAULT 0 COMMENT '不可用积分',
  source_id INT NOT NULL DEFAULT 0 COMMENT '来源ID',
  source_type INT NOT NULL DEFAULT 0 COMMENT '来源类型',
  del INT NOT NULL DEFAULT 0 COMMENT '记录有效状态, 1 .删除',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (user_id),
  INDEX (source_id),
  INDEX (user_id, op_type)
)  CHARSET=UTF8MB4 , COMMENT '论坛积分变动记录表';


-- 论坛回复通知
DROP TABLE IF EXISTS gfb_topics_reply_notices;
CREATE TABLE gfb_topics_reply_notices (
  id BIGINT AUTO_INCREMENT COMMENT '点赞记录唯一标识',
  user_id INT NOT NULL DEFAULT 0 COMMENT '回复用户ID',
  reply_id BIGINT NOT NULL DEFAULT 0 COMMENT '通知信息ID',
  source_type INT NOT NULL DEFAULT 0 COMMENT '点赞类型 0:评论, 1:回复',
  for_user_id INT NOT NULL DEFAULT 0 COMMENT '被@的用户Id , 如果回复类型为评论直接为评论用户ID',
  for_user_name VARCHAR(36) NOT NULL DEFAULT '' COMMENT '用户名-此处冗余',
  for_user_icon_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '用户头像-此处冗余',
  view_state INT NOT NULL DEFAULT 0 COMMENT '阅读状态, 0:未阅读, 1.以阅读',
  create_date DATETIME NULL COMMENT '创建时间',
  update_date DATETIME NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  INDEX (user_id)
)  CHARSET=UTF8MB4 , COMMENT '论坛回复通知表';

-- 商品计划
DROP TABLE IF EXISTS `gfb_product_plan`;
CREATE TABLE `gfb_product_plan` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) DEFAULT NULL COMMENT '商品计划类型 0广富送 1联通送',
  `name` varchar(255) DEFAULT NULL COMMENT '广富送计划名',
  `time_limit` int(2) unsigned NOT NULL DEFAULT '0' COMMENT '期限',
  `lowest` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '单个用户加入金额最小阈值',
  `apr` int(11) DEFAULT NULL COMMENT '利率',
  `fee_ratio` int(11) DEFAULT NULL COMMENT '费率',
  `is_open` int(11) DEFAULT NULL,
  `start_at` datetime DEFAULT NULL COMMENT '开始时间',
  `end_at` datetime DEFAULT NULL COMMENT '结束时间',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- 商品sku
DROP TABLE IF EXISTS `gfb_product_sku`;
CREATE TABLE `gfb_product_sku` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) DEFAULT '0' COMMENT '类型：0普通sku 1.套餐sku',
  `sc_id` int(11) DEFAULT NULL COMMENT 'sku_classify主键id',
  `no` int(11) DEFAULT NULL COMMENT '序号',
  `name` varchar(255) DEFAULT NULL,
  `plan_id` int(11) DEFAULT NULL COMMENT '商品计划id type为1时生效',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

-- 商品分类sku分类关联表
DROP TABLE IF EXISTS `gfb_product_classify_sku_classify_ref`;
CREATE TABLE `gfb_product_classify_sku_classify_ref` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pc_id` int(11) DEFAULT NULL COMMENT 'gfb_product_classify 表主键',
  `sc_id` int(11) DEFAULT NULL COMMENT 'gfb_product_sku_classify主键id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='商品分类表';

-- 商品分类表
DROP TABLE IF EXISTS `gfb_product_classify`;
CREATE TABLE `gfb_product_classify` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `no` int(11) DEFAULT NULL COMMENT '编号',
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='商品分类表';

-- 商品sku分类表
DROP TABLE IF EXISTS `gfb_product_sku_classify`;
CREATE TABLE `gfb_product_sku_classify` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `no` int(11) DEFAULT NULL COMMENT '排序序号',
  `name` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- 子商品表
DROP TABLE IF EXISTS `gfb_product_item`;
CREATE TABLE `gfb_product_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) DEFAULT NULL COMMENT '父级id',
  `price` int(11) DEFAULT NULL COMMENT '价格',
  `discount_price` int(11) DEFAULT NULL COMMENT '折扣价',
  `after_sales_service` text COMMENT '售后服务',
  `img_url` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `details` text COMMENT '商品详情',
  `q_and_a` text COMMENT '问答',
  `inventory` int(11) DEFAULT '0' COMMENT '库存',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `is_enable` int(11) DEFAULT '0' COMMENT '是否上架',
  `enable_at` datetime DEFAULT NULL COMMENT '上架时间',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- 商品子表与sku的关联表
DROP TABLE IF EXISTS `gfb_product_item_sku_ref`;
CREATE TABLE `gfb_product_item_sku_ref` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_item_id` int(11) DEFAULT NULL,
  `sku_id` int(11) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8 COMMENT='商品子表与sku的关联表';

-- 商品配送详情表
DROP TABLE IF EXISTS `gfb_product_logistics`;
CREATE TABLE `gfb_product_logistics` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `order_number` varchar(255) DEFAULT NULL COMMENT '所属订单编号',
  `state` int(11) DEFAULT NULL COMMENT '收寄状态：0收件人 1.寄件人',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `express_name` int(11) DEFAULT NULL COMMENT '快递名称',
  `express_number` varchar(255) DEFAULT NULL COMMENT '快递编号',
  `name` varchar(255) DEFAULT NULL COMMENT '收件人姓名',
  `phone` varchar(25) DEFAULT NULL COMMENT '收货号码',
  `country` varchar(255) DEFAULT NULL COMMENT '国家',
  `province` varchar(255) DEFAULT NULL COMMENT '省份',
  `city` varchar(255) DEFAULT NULL COMMENT '城市',
  `district` varchar(255) DEFAULT NULL COMMENT '地区（市区/县）',
  `detailed_address` varchar(1024) DEFAULT NULL COMMENT '详细地址',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `update_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='商品配送地址';

-- 商品表
DROP TABLE IF EXISTS `gfb_product`;
CREATE TABLE `gfb_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pc_id` int(11) DEFAULT NULL COMMENT 'gfb_product_classify 表主键',
  `name` varchar(255) DEFAULT NULL COMMENT '商品名',
  `img_url` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `after_sales_service` text COMMENT '售后服务',
  `details` text COMMENT '商品详情',
  `q_and_a` text COMMENT '问答',
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- 商品子表与sku的关联表
DROP TABLE IF EXISTS `gfb_product_order`;
CREATE TABLE `gfb_product_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) DEFAULT NULL COMMENT '商品计划类型 0广富送 1联通送',
  `status` int(11) DEFAULT NULL COMMENT '状态：1.未付款 2.已付款 3.待发货 4.待收货 5.已完成 6.已取消',
  `user_id` int(11) DEFAULT NULL COMMENT '会员id',
  `order_number` varchar(255) NOT NULL DEFAULT '' COMMENT '订单编号',
  `pay_number` varchar(255) DEFAULT NULL COMMENT '支付流水号',
  `pay_type` int(11) DEFAULT NULL COMMENT '支付方式：0在线支付 ',
  `product_address_id` int(11) DEFAULT NULL COMMENT '商品配送地址id',
  `pay_money` int(11) DEFAULT NULL COMMENT '实付款',
  `product_money` int(11) DEFAULT NULL COMMENT '商品金额',
  `discounts_money` int(11) DEFAULT NULL COMMENT '折扣金额',
  `fee` int(11) DEFAULT NULL COMMENT '手续费',
  `earnings` int(11) DEFAULT NULL COMMENT '收益',
  `is_del` int(11) DEFAULT '0' COMMENT '是否 0否 1是',
  `pay_at` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='商品子表与sku的关联表';

-- 商品子表与sku的关联表
DROP TABLE IF EXISTS `gfb_product_order_buy_log`;
CREATE TABLE `gfb_product_order_buy_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_item_id` int(11) DEFAULT NULL,
  `product_order_id` int(11) DEFAULT NULL COMMENT '订单id',
  `product_money` int(11) DEFAULT NULL COMMENT '购买时商品的价格',
  `discounts_money` int(11) DEFAULT NULL,
  `is_del` int(11) DEFAULT '0' COMMENT '是否删除 0否 1是',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 COMMENT='商品子表与sku的关联表';

DROP TABLE IF EXISTS `gfb_product_collect`;
CREATE TABLE `gfb_product_collect` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_item_id` int(11) DEFAULT NULL COMMENT '子商品id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8 COMMENT='商品收藏表';


ALTER TABLE `gfb_product_order`
  ADD COLUMN `remark` varchar(1024) NULL DEFAULT '' COMMENT '订单备注' AFTER `pay_at`;

ALTER TABLE `gfb_product_order_buy_log`
  ADD COLUMN `plan_id` INT(11) NOT NULL COMMENT '订单id' AFTER `id`;
ALTER TABLE `gfb_product_order_buy_log`
  ADD COLUMN `plan_money` INT(11) NULL DEFAULT 0  COMMENT '订单购买金额' AFTER `id`;
ALTER TABLE `gfb_product_order_buy_log`
  ADD COLUMN `pay_money` INT(11) NULL DEFAULT 0  COMMENT '订单购买金额' AFTER `plan_money`;

ALTER TABLE `gfb_topics`
  CHANGE COLUMN `titel` `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '标的' AFTER `id`;

ALTER TABLE `gfb_product_order`
  ADD COLUMN `ship_at` datetime NULL COMMENT '发货时间' AFTER `pay_at`;

ALTER TABLE `gfb_finance_plan`
  ADD COLUMN `order_number` varchar(255) DEFAULT NULL COMMENT '订单编号' AFTER `end_lock_at`\


ALTER TABLE `gfb_users`
  ADD COLUMN `join_company` varchar(255) COLLATE utf8_unicode_ci NOT NULL;
/*
Navicat MySQL Data Transfer


CREATE TABLE gfb_product_agent
(
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT(10) UNSIGNED NOT NULL COMMENT '用户ID',
  name       VARCHAR(255)     NOT NULL COMMENT '代理商名称',
  level      TINYINT(2) UNSIGNED NOT NULL  COMMENT '等级；0：省代，1：市代，2县代，3：零售',
  parent_id  INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级代理',
  commission_discount INT(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级代理',
  remark     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
  created_at DATETIME         NULL,
  updated_at DATETIME         NULL,
  CONSTRAINT product_agent_user_id_foreign
  FOREIGN KEY (user_id) REFERENCES gfb_users (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE INDEX product_agent_user_id_foreign
  ON gfb_product_agent (user_id);
Source Server         : root
Source Server Version : 50637
Source Host           : 192.168.1.5:3306
Source Database       : gfb0810

Target Server Type    : MYSQL
Target Server Version : 50637
File Encoding         : 65001

Date: 2017-12-11 09:35:50
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for gfb_borrow_contract
-- ----------------------------
DROP TABLE IF EXISTS `gfb_borrow_contract`;
CREATE TABLE `gfb_borrow_contract` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `borrow_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `type` smallint(2) NOT NULL,
  `created_at` datetime NOT NULL,
  `update_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `status` smallint(1) NOT NULL DEFAULT '0',
  `batch_no` varchar(20) CHARACTER SET utf8 NOT NULL,
  `for_user_id` int(11) DEFAULT NULL,
  `borrow_name` varchar(200) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf-8;


ALTER TABLE gfb_borrow ADD is_contract tinyint(1) DEFAULT '0' COMMENT '是否生成合同';

ALTER TABLE gfb_user_third_account ADD open_account_at datetime DEFAULT NULL COMMENT '合同开户时间';
ALTER TABLE gfb_user_third_account ADD entrust_state smallint(1) DEFAULT '0' COMMENT '是否签署委托授权协议';


ALTER TABLE gfb_user_cache ADD `tender_id` int(10) DEFAULT '0' COMMENT '首投id' AFTER `wait_repay_interest`;

ALTER TABLE gfb_asset ADD `bounty` bigint(20) DEFAULT '0' COMMENT '奖励金';

-- auto-generated definition
create table gfb_count
(
  site_balance varchar(400) null comment '网站余额',
  account_balance varchar(300) null comment '存管账户余额',
  net_wait_collection_principal int null comment '净值标待收本金',
  net_wait_collection_interest int null comment '净值标待收利息',
  net_wait_repayment_principal int null comment '净值标待还本金',
  net_wait_repayment_interest int null comment '净值标待还利息',
  net_advance_principal int null comment '净值标垫付本金',
  car_wait_collection varchar(300) null comment '车贷标待收',
  car_wait_repayment varchar(300) null comment '车贷标待还',
  channel_wait_collection varchar(300) null comment '渠道标待收',
  channel_wait_repayment varchar(300) null comment '渠道标待还',
  net_borrow_principal int null comment '净值标借款本金',
  net_advance_no_principal int null comment '净值标垫付后未收回本金',
  net_advance_no_interest int null comment '净值标垫付未收回利息',
  net_net_advance_principal int null comment '净值标净垫付本金',
  net_net_increase_principal int null comment '净值标净新增本金',
  id int auto_increment
    primary key,
  net_advance_yes_principal int null comment '净值标垫付后收回本金',
  car_borrow_principal varchar(300) null comment '车贷标借款本金',
  car_repayment_principal varchar(300) null comment '车贷标还款本金',
  car_increase_principal varchar(300) null comment '车贷标净新增本金',
  car_wait_collection_principal varchar(300) null comment '车贷标每个月待收本金',
  net_repayment_principal varchar(300) null comment '净值标还款本金',
  channel_borrow_principal varchar(300) null comment '渠道标借款本金',
  channel_repayment_principal varchar(300) null comment '渠道标还款本金',
  channel_increase_principal varchar(300) null comment '渠道标净新增本金',
  channel_wait_collection_principal varchar(300) null comment '渠道标待收本金',
  wait_collection_principal int null comment '净值标每月待收本金',
  create_time datetime null comment '创建时间',
  update_time datetime null comment '修改时间',
  count_date datetime null comment '统计的年月份'
)
  comment '资金统计表'
;
