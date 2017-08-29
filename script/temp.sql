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
);
ALTER TABLE gfb_eve COMMENT = '交易明细全流水' DEFAULT CHARSET=utf8;