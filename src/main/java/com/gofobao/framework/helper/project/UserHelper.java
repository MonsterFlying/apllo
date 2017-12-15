package com.gofobao.framework.helper.project;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.tender.service.TenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户模块 工具类
 * created by max on 2017/2/24.
 */
@Component
public class UserHelper {
    public static final int EMAIL_NUM = 1;//邮箱标识
    public static final int REALNAME_NUM = 2;//真实姓名标识
    public static final int PHONE_NUM = 3;//手机标识
    public static final int CARD_ID_NUM = 4;//身份证标识
    public static final int BANK_ACCOUNT_NUM = 5;//银行账户标识
    public static final int USERNAME_NUM = 6;//用户名标识

    @Autowired
    private AssetService assetService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowService borrowService;

    /**
     * 计算信用额度
     *
     * @param userId
     * @return
     */
    public long getNetWorthQuota(long userId) {
        Asset asset = assetService.findByUserId(userId);
        UserCache userCache = userCacheService.findById(userId);
        /* 投标复审中的金额 */
        long tenderAgainVerifyMoney = tenderService.findTenderAgainVerifyMoney(userId);
        /* 总资产 = 可用金额 + 待回款本金 + 在即信复审本金*/
        long assets = asset.getUseMoney() + userCache.getWaitCollectionPrincipal() + tenderAgainVerifyMoney;
        /* 信用标借款金额 */
        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .eq("userId", userId)
                .eq("status", 1)
                .build();
        List<Borrow> borrowList = borrowService.findList(bs);
        long sumRepayTotal = 0;
        //筛选已复审借款集合
        borrowList = borrowList.stream().filter(borrow -> (borrow.getMoney().longValue() == borrow.getMoneyYes().longValue())
                && (!ObjectUtils.isEmpty(borrow.getSuccessAt()))).collect(Collectors.toList());
        for (Borrow borrow : borrowList) {
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(NumberHelper.toDouble(StringHelper.toString(borrow.getMoney())),
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getRecheckAt());
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            /*总偿还*/
            long repayTotal = NumberHelper.toLong(rsMap.get("repayTotal"));
            //累加总偿还
            sumRepayTotal = sumRepayTotal + repayTotal;
        }

            /* 信用额度 总资产*0.8 - 待还  - 复审中待还*/
        long netWorthQuota = new Double(MoneyHelper.multiply(assets, 0.8)).longValue() - asset.getPayment() - sumRepayTotal;
        return netWorthQuota > 0 ? netWorthQuota : 0;
    }

    /**
     * 讲字符串根据规则进行隐藏字符
     *
     * @param str  待处理字符串
     * @param type 字符串类型
     * @return 空字符串  处理后的字符串
     */

    public static String hideChar(String str, Integer type) {
        StringBuffer rs = new StringBuffer();
        do {
            str = StringUtils.trimAllWhitespace(str);
            if ((ObjectUtils.isEmpty(str)) || ObjectUtils.isEmpty(type)) {
                break;
            }

            switch (type) {
                case EMAIL_NUM:
                    rs.append(str.substring(0, 3));
                    rs.append("***");
                    rs.append(str.substring(str.indexOf("@")));
                    break;
                case REALNAME_NUM:
                    rs.append(str.substring(0, 1));
                    for (int i = 0; i < (str.length() - 1); i++) {
                        rs.append("*");
                    }
                    break;
                case PHONE_NUM:
                    rs.append(str);
                    rs.replace(3, 7, "****");
                    break;
                case CARD_ID_NUM:
                    rs.append(str.substring(0, 4));
                    for (int i = 0; i < (str.length() - 8); i++) {
                        rs.append("*");
                    }
                    rs.append(str.substring(str.length() - 4));
                    break;
                case BANK_ACCOUNT_NUM:
                    rs.append(str.substring(0, 3));

                    if (str.length() < 4) {
                        break;
                    }

                    for (int i = 0; i < (str.length() - 7); i++) {
                        rs.append("*");
                    }
                    rs.append(str.substring(str.length() - 4));
                    break;
                case USERNAME_NUM:
                    rs.append(str.substring(0, 2));

                    for (int i = 0; i < 3; i++) {
                        rs.append("*");
                    }
                    break;
                default:

            }
        } while (false);
        return rs.toString();
    }

    /**
     * 根据身份证号码获取生日  年份  月日
     *
     * @param type   0.年份  1.月日
     * @param cardId
     * @return
     */
    public static String getBirthDayByCardId(String cardId, int type) {
        Date birthDay = null;
        if (cardId.length() == 18) {
            birthDay = DateHelper.stringToDate(cardId.substring(6, 10));
            birthDay = DateHelper.stringToDate(cardId.substring(10, 14));
        } else if (cardId.length() == 15) {
            birthDay = DateHelper.stringToDate(cardId.substring(6, 10));
            birthDay = DateHelper.stringToDate(cardId.substring(10, 14));

        }
        return null;
    }

    /**
     * 隐藏会员对象变量内字符串的字符
     *
     * @param user 会员对象
     * @return
     */
    public static Users hideCharByUser(Users user) {
        user.setEmail(hideChar(user.getEmail(), UserHelper.EMAIL_NUM));
        user.setRealname(hideChar(user.getRealname(), UserHelper.REALNAME_NUM));
        user.setPhone(hideChar(user.getPhone(), UserHelper.PHONE_NUM));
        user.setCardId(hideChar(user.getCardId(), UserHelper.CARD_ID_NUM));
        user.setUsername(hideChar(user.getUsername(), UserHelper.USERNAME_NUM));
        return user;
    }

    /**
     * 获取资产类型字符串
     *
     * @return
     */
    public static String getAssetTypeStr(String assetType) {

        if (StringUtils.isEmpty(assetType)) {
            return "";
        }

        String[] strArr = assetType.split("_");
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer tempBuff = null;
        for (String str : strArr) {
            tempBuff = new StringBuffer(str);
            tempBuff.setCharAt(0, Character.toUpperCase(str.charAt(0)));
            stringBuffer.append(tempBuff);
        }

        assetType = stringBuffer.toString();
        String rs = "";
        switch (CapitalChangeEnum.valueOf(assetType)) {
            case Award:
                rs = "奖励";
                break;
            case Bonus:
                rs = "提成";
                break;
            case AwardVirtualMoney:
                rs = "赠送体验金";
                break;
            case Borrow:
                rs = "借款";
                break;
            case Cash:
                rs = "提现";
                break;
            case CollectionAdd:
                rs = "添加代收";
                break;
            case CollectionLower:
                rs = "扣除待收";
                break;
            case Correct:
                rs = "数据修正";
                break;
            case ExpenditureOther:
                rs = "其他支出";
                break;
            case Fee:
                rs = "费用";
                break;
            case Frozen:
                rs = "冻结资金";
                break;
            case IncomeOther:
                rs = "其他收入";
                break;
            case IncomeOverdue:
                rs = "收到逾期费";
                break;
            case IncomeRepayment:
                rs = "回款";
                break;
            case IntegralCash:
                rs = "积分折现";
                break;
            case InterestManager:
                rs = "利息管理费";
                break;
            case Manager:
                rs = "账户管理费";
                break;
            case Overdue:
                rs = "逾期费";
                break;
            case PaymentAdd:
                rs = "添加待还";
                break;
            case PaymentLower:
                rs = "扣除待还";
                break;
            case Recharge:
                rs = "充值";
                break;
            case Repayment:
                rs = "还款";
                break;
            case Tender:
                rs = "投标";
                break;
            case Unfrozen:
                rs = "解除冻结";
                break;
            case VirtualTender:
                rs = "投资体验标";
                break;
            default:
        }

        return rs;
    }
}
