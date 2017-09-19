package com.gofobao.framework.award.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelRequest;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.RedPackageRes;
import com.gofobao.framework.award.vo.response.VoViewOpenRedPackageWarpRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gofobao.framework.listener.providers.NoticesMessageProvider.GSON;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class RedPackageBizImpl implements RedPackageBiz {
    @Autowired
    MqHelper mqHelper;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    UserService userService;

    @Autowired
    AssetService assetService;

    @Autowired
    BorrowService borrowService;

    @Autowired
    TenderService tenderService;

    @Autowired
    NewAssetLogService newAssetLogService;

    public static final String DATA = "[{\"userId\":294,\"forUserId\":22,\"money\":169,\"principal\":169,\"interest\":0,\"remark\":\"接收理财师提成奖励 1.69元\",\"seqNo\":\"20170912233501295376\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230500992\"},\n" +
            "{\"userId\":582,\"forUserId\":22,\"money\":7,\"principal\":7,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.07元\",\"seqNo\":\"20170912233538922664\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230538184\"},\n" +
            "{\"userId\":901,\"forUserId\":22,\"money\":449,\"principal\":449,\"interest\":0,\"remark\":\"接收理财师提成奖励 4.49元\",\"seqNo\":\"20170912233539513090\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230539026\"},\n" +
            "{\"userId\":1067,\"forUserId\":22,\"money\":11,\"principal\":11,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.11元\",\"seqNo\":\"20170912233539504784\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230539675\"},\n" +
            "{\"userId\":1621,\"forUserId\":22,\"money\":505,\"principal\":505,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.05元\",\"seqNo\":\"20170912233540717800\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230540321\"},\n" +
            "{\"userId\":1699,\"forUserId\":22,\"money\":730,\"principal\":730,\"interest\":0,\"remark\":\"接收理财师提成奖励 7.30元\",\"seqNo\":\"20170912233541055939\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230541090\"},\n" +
            "{\"userId\":2388,\"forUserId\":22,\"money\":46,\"principal\":46,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.46元\",\"seqNo\":\"20170912233541862809\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230541800\"},\n" +
            "{\"userId\":2470,\"forUserId\":22,\"money\":483,\"principal\":483,\"interest\":0,\"remark\":\"接收理财师提成奖励 4.83元\",\"seqNo\":\"20170912233542167237\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230542518\"},\n" +
            "{\"userId\":2524,\"forUserId\":22,\"money\":388,\"principal\":388,\"interest\":0,\"remark\":\"接收理财师提成奖励 3.88元\",\"seqNo\":\"20170912233543318696\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230543090\"},\n" +
            "{\"userId\":2534,\"forUserId\":22,\"money\":41,\"principal\":41,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.41元\",\"seqNo\":\"20170912233543815707\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230543719\"},\n" +
            "{\"userId\":2601,\"forUserId\":22,\"money\":249,\"principal\":249,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.49元\",\"seqNo\":\"20170912233544675367\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230544311\"},\n" +
            "{\"userId\":2839,\"forUserId\":22,\"money\":28,\"principal\":28,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.28元\",\"seqNo\":\"20170912233545039933\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230544956\"},\n" +
            "{\"userId\":2860,\"forUserId\":22,\"money\":514,\"principal\":514,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.14元\",\"seqNo\":\"20170912233545289436\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230545525\"},\n" +
            "{\"userId\":3031,\"forUserId\":22,\"money\":26,\"principal\":26,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.26元\",\"seqNo\":\"20170912233546526628\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230546129\"},\n" +
            "{\"userId\":3232,\"forUserId\":22,\"money\":194,\"principal\":194,\"interest\":0,\"remark\":\"接收理财师提成奖励 1.94元\",\"seqNo\":\"20170912233546433656\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230546740\"},\n" +
            "{\"userId\":3478,\"forUserId\":22,\"money\":207,\"principal\":207,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.07元\",\"seqNo\":\"20170912233547396025\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230547325\"},\n" +
            "{\"userId\":3661,\"forUserId\":22,\"money\":284,\"principal\":284,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.84元\",\"seqNo\":\"20170912233548790154\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230548000\"},\n" +
            "{\"userId\":4442,\"forUserId\":22,\"money\":3,\"principal\":3,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.03元\",\"seqNo\":\"20170912233548951439\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230548758\"},\n" +
            "{\"userId\":4632,\"forUserId\":22,\"money\":608,\"principal\":608,\"interest\":0,\"remark\":\"接收理财师提成奖励 6.08元\",\"seqNo\":\"20170912233549037917\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230549338\"},\n" +
            "{\"userId\":5307,\"forUserId\":22,\"money\":17,\"principal\":17,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.17元\",\"seqNo\":\"20170912233550632007\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230549980\"},\n" +
            "{\"userId\":5685,\"forUserId\":22,\"money\":17,\"principal\":17,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.17元\",\"seqNo\":\"20170912233550811246\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230550534\"},\n" +
            "{\"userId\":5866,\"forUserId\":22,\"money\":80,\"principal\":80,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.80元\",\"seqNo\":\"20170912233551818137\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230551158\"},\n" +
            "{\"userId\":5957,\"forUserId\":22,\"money\":250,\"principal\":250,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.50元\",\"seqNo\":\"20170912233551791506\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230551746\"},\n" +
            "{\"userId\":6168,\"forUserId\":22,\"money\":25,\"principal\":25,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.25元\",\"seqNo\":\"20170912233552360765\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230552349\"},\n" +
            "{\"userId\":6276,\"forUserId\":22,\"money\":30,\"principal\":30,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.30元\",\"seqNo\":\"20170912233553560770\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230552961\"},\n" +
            "{\"userId\":8156,\"forUserId\":22,\"money\":36,\"principal\":36,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.36元\",\"seqNo\":\"20170912233553411330\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230553569\"},\n" +
            "{\"userId\":8605,\"forUserId\":22,\"money\":365,\"principal\":365,\"interest\":0,\"remark\":\"接收理财师提成奖励 3.65元\",\"seqNo\":\"20170912233554751589\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230554141\"},\n" +
            "{\"userId\":8667,\"forUserId\":22,\"money\":1971,\"principal\":1971,\"interest\":0,\"remark\":\"接收理财师提成奖励 19.71元\",\"seqNo\":\"20170912233554855674\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230554746\"},\n" +
            "{\"userId\":8678,\"forUserId\":22,\"money\":2,\"principal\":2,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.02元\",\"seqNo\":\"20170912233555709362\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230555349\"},\n" +
            "{\"userId\":8693,\"forUserId\":22,\"money\":30,\"principal\":30,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.30元\",\"seqNo\":\"20170912233556059846\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230555970\"},\n" +
            "{\"userId\":8712,\"forUserId\":22,\"money\":83,\"principal\":83,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.83元\",\"seqNo\":\"20170912233556697752\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230556561\"},\n" +
            "{\"userId\":8742,\"forUserId\":22,\"money\":554,\"principal\":554,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.54元\",\"seqNo\":\"20170912233557269289\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230557184\"},\n" +
            "{\"userId\":8744,\"forUserId\":22,\"money\":2236,\"principal\":2236,\"interest\":0,\"remark\":\"接收理财师提成奖励 22.36元\",\"seqNo\":\"20170912233557117240\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230557737\"},\n" +
            "{\"userId\":9554,\"forUserId\":22,\"money\":514,\"principal\":514,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.14元\",\"seqNo\":\"20170912233558988938\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230558392\"},\n" +
            "{\"userId\":9587,\"forUserId\":22,\"money\":12,\"principal\":12,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.12元\",\"seqNo\":\"20170912233559001834\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230558953\"},\n" +
            "{\"userId\":9632,\"forUserId\":22,\"money\":3,\"principal\":3,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.03元\",\"seqNo\":\"20170912233559962136\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230559552\"},\n" +
            "{\"userId\":9648,\"forUserId\":22,\"money\":35,\"principal\":35,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.35元\",\"seqNo\":\"20170912233600259936\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230560223\"},\n" +
            "{\"userId\":9695,\"forUserId\":22,\"money\":29,\"principal\":29,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.29元\",\"seqNo\":\"20170912233600286022\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230560767\"},\n" +
            "{\"userId\":9727,\"forUserId\":22,\"money\":46,\"principal\":46,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.46元\",\"seqNo\":\"20170912233601040155\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230561354\"},\n" +
            "{\"userId\":9784,\"forUserId\":22,\"money\":4,\"principal\":4,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.04元\",\"seqNo\":\"20170912233602983026\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230561969\"},\n" +
            "{\"userId\":10156,\"forUserId\":22,\"money\":96,\"principal\":96,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.96元\",\"seqNo\":\"20170912233602366380\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230562555\"},\n" +
            "{\"userId\":10281,\"forUserId\":22,\"money\":16,\"principal\":16,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.16元\",\"seqNo\":\"20170912233603723693\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230563164\"},\n" +
            "{\"userId\":12495,\"forUserId\":22,\"money\":17,\"principal\":17,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.17元\",\"seqNo\":\"20170912233603868343\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230563804\"},\n" +
            "{\"userId\":13378,\"forUserId\":22,\"money\":2,\"principal\":2,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.02元\",\"seqNo\":\"20170912233604169939\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230564373\"},\n" +
            "{\"userId\":13723,\"forUserId\":22,\"money\":6,\"principal\":6,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.06元\",\"seqNo\":\"20170912233605276291\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230564987\"},\n" +
            "{\"userId\":13789,\"forUserId\":22,\"money\":4,\"principal\":4,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.04元\",\"seqNo\":\"20170912233605228446\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230565611\"},\n" +
            "{\"userId\":16556,\"forUserId\":22,\"money\":32,\"principal\":32,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.32元\",\"seqNo\":\"20170912233606213410\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230566176\"},\n" +
            "{\"userId\":16823,\"forUserId\":22,\"money\":5,\"principal\":5,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.05元\",\"seqNo\":\"20170912233606183527\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230566769\"},\n" +
            "{\"userId\":19286,\"forUserId\":22,\"money\":267,\"principal\":267,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.67元\",\"seqNo\":\"20170912233607185187\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230567625\"},\n" +
            "{\"userId\":19426,\"forUserId\":22,\"money\":182,\"principal\":182,\"interest\":0,\"remark\":\"接收理财师提成奖励 1.82元\",\"seqNo\":\"20170912233608031748\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230568192\"},\n" +
            "{\"userId\":19645,\"forUserId\":22,\"money\":8,\"principal\":8,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.08元\",\"seqNo\":\"20170912233608637840\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230568845\"},\n" +
            "{\"userId\":23904,\"forUserId\":22,\"money\":36,\"principal\":36,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.36元\",\"seqNo\":\"20170912233609733970\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230569406\"},\n" +
            "{\"userId\":26613,\"forUserId\":22,\"money\":24,\"principal\":24,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.24元\",\"seqNo\":\"20170912233610801365\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230570050\"},\n" +
            "{\"userId\":32439,\"forUserId\":22,\"money\":32,\"principal\":32,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.32元\",\"seqNo\":\"20170912233610723687\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230570615\"},\n" +
            "{\"userId\":33017,\"forUserId\":22,\"money\":11,\"principal\":11,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.11元\",\"seqNo\":\"20170912233611395120\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230571223\"},\n" +
            "{\"userId\":34095,\"forUserId\":22,\"money\":14,\"principal\":14,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.14元\",\"seqNo\":\"20170912233611622643\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230571821\"},\n" +
            "{\"userId\":55609,\"forUserId\":22,\"money\":38,\"principal\":38,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.38元\",\"seqNo\":\"20170912233612521694\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230572412\"},\n" +
            "{\"userId\":62311,\"forUserId\":22,\"money\":2,\"principal\":2,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.02元\",\"seqNo\":\"20170912233613235902\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230573028\"},\n" +
            "{\"userId\":63149,\"forUserId\":22,\"money\":27,\"principal\":27,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.27元\",\"seqNo\":\"20170912233613252101\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230573852\"},\n" +
            "{\"userId\":294,\"forUserId\":22,\"money\":169,\"principal\":169,\"interest\":0,\"remark\":\"接收理财师提成奖励 1.69元\",\"seqNo\":\"20170912233501825135\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230500981\"},\n" +
            "{\"userId\":582,\"forUserId\":22,\"money\":7,\"principal\":7,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.07元\",\"seqNo\":\"20170912233501223156\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230501859\"},\n" +
            "{\"userId\":901,\"forUserId\":22,\"money\":449,\"principal\":449,\"interest\":0,\"remark\":\"接收理财师提成奖励 4.49元\",\"seqNo\":\"20170912233502634140\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230502462\"},\n" +
            "{\"userId\":1067,\"forUserId\":22,\"money\":11,\"principal\":11,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.11元\",\"seqNo\":\"20170912233503290601\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230503153\"},\n" +
            "{\"userId\":1621,\"forUserId\":22,\"money\":505,\"principal\":505,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.05元\",\"seqNo\":\"20170912233503515064\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230503852\"},\n" +
            "{\"userId\":1699,\"forUserId\":22,\"money\":730,\"principal\":730,\"interest\":0,\"remark\":\"接收理财师提成奖励 7.30元\",\"seqNo\":\"20170912233504413164\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230504467\"},\n" +
            "{\"userId\":2388,\"forUserId\":22,\"money\":46,\"principal\":46,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.46元\",\"seqNo\":\"20170912233505695149\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230505061\"},\n" +
            "{\"userId\":2470,\"forUserId\":22,\"money\":483,\"principal\":483,\"interest\":0,\"remark\":\"接收理财师提成奖励 4.83元\",\"seqNo\":\"20170912233505754116\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230505716\"},\n" +
            "{\"userId\":2524,\"forUserId\":22,\"money\":388,\"principal\":388,\"interest\":0,\"remark\":\"接收理财师提成奖励 3.88元\",\"seqNo\":\"20170912233506209023\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230506304\"},\n" +
            "{\"userId\":2534,\"forUserId\":22,\"money\":41,\"principal\":41,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.41元\",\"seqNo\":\"20170912233506928336\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230506886\"},\n" +
            "{\"userId\":2601,\"forUserId\":22,\"money\":249,\"principal\":249,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.49元\",\"seqNo\":\"20170912233507046659\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230507500\"},\n" +
            "{\"userId\":2839,\"forUserId\":22,\"money\":28,\"principal\":28,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.28元\",\"seqNo\":\"20170912233508783055\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230508108\"},\n" +
            "{\"userId\":2860,\"forUserId\":22,\"money\":514,\"principal\":514,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.14元\",\"seqNo\":\"20170912233508095587\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230508782\"},\n" +
            "{\"userId\":3031,\"forUserId\":22,\"money\":26,\"principal\":26,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.26元\",\"seqNo\":\"20170912233509921293\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230509349\"},\n" +
            "{\"userId\":3232,\"forUserId\":22,\"money\":194,\"principal\":194,\"interest\":0,\"remark\":\"接收理财师提成奖励 1.94元\",\"seqNo\":\"20170912233509775721\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230509923\"},\n" +
            "{\"userId\":3478,\"forUserId\":22,\"money\":207,\"principal\":207,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.07元\",\"seqNo\":\"20170912233510555738\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230510540\"},\n" +
            "{\"userId\":3661,\"forUserId\":22,\"money\":284,\"principal\":284,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.84元\",\"seqNo\":\"20170912233511752232\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230511155\"},\n" +
            "{\"userId\":4442,\"forUserId\":22,\"money\":3,\"principal\":3,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.03元\",\"seqNo\":\"20170912233511041974\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230511715\"},\n" +
            "{\"userId\":4632,\"forUserId\":22,\"money\":608,\"principal\":608,\"interest\":0,\"remark\":\"接收理财师提成奖励 6.08元\",\"seqNo\":\"20170912233512419249\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230512370\"},\n" +
            "{\"userId\":5307,\"forUserId\":22,\"money\":17,\"principal\":17,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.17元\",\"seqNo\":\"20170912233513469127\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230512953\"},\n" +
            "{\"userId\":5685,\"forUserId\":22,\"money\":17,\"principal\":17,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.17元\",\"seqNo\":\"20170912233513441156\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230513571\"},\n" +
            "{\"userId\":5866,\"forUserId\":22,\"money\":80,\"principal\":80,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.80元\",\"seqNo\":\"20170912233514265480\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230514208\"},\n" +
            "{\"userId\":5957,\"forUserId\":22,\"money\":250,\"principal\":250,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.50元\",\"seqNo\":\"20170912233514919682\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230514773\"},\n" +
            "{\"userId\":6168,\"forUserId\":22,\"money\":25,\"principal\":25,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.25元\",\"seqNo\":\"20170912233515278644\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230515358\"},\n" +
            "{\"userId\":6276,\"forUserId\":22,\"money\":30,\"principal\":30,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.30元\",\"seqNo\":\"20170912233516846512\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230516002\"},\n" +
            "{\"userId\":8156,\"forUserId\":22,\"money\":36,\"principal\":36,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.36元\",\"seqNo\":\"20170912233516280638\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230516608\"},\n" +
            "{\"userId\":8605,\"forUserId\":22,\"money\":365,\"principal\":365,\"interest\":0,\"remark\":\"接收理财师提成奖励 3.65元\",\"seqNo\":\"20170912233517501032\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230517131\"},\n" +
            "{\"userId\":8667,\"forUserId\":22,\"money\":1971,\"principal\":1971,\"interest\":0,\"remark\":\"接收理财师提成奖励 19.71元\",\"seqNo\":\"20170912233517999154\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230517740\"},\n" +
            "{\"userId\":8678,\"forUserId\":22,\"money\":2,\"principal\":2,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.02元\",\"seqNo\":\"20170912233518836421\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230518474\"},\n" +
            "{\"userId\":8693,\"forUserId\":22,\"money\":30,\"principal\":30,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.30元\",\"seqNo\":\"20170912233519120802\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230519143\"},\n" +
            "{\"userId\":8712,\"forUserId\":22,\"money\":83,\"principal\":83,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.83元\",\"seqNo\":\"20170912233519986960\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230519747\"},\n" +
            "{\"userId\":8742,\"forUserId\":22,\"money\":554,\"principal\":554,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.54元\",\"seqNo\":\"20170912233520454476\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230520354\"},\n" +
            "{\"userId\":8744,\"forUserId\":22,\"money\":2236,\"principal\":2236,\"interest\":0,\"remark\":\"接收理财师提成奖励 22.36元\",\"seqNo\":\"20170912233521041648\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230520980\"},\n" +
            "{\"userId\":9554,\"forUserId\":22,\"money\":514,\"principal\":514,\"interest\":0,\"remark\":\"接收理财师提成奖励 5.14元\",\"seqNo\":\"20170912233521189562\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230521585\"},\n" +
            "{\"userId\":9587,\"forUserId\":22,\"money\":12,\"principal\":12,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.12元\",\"seqNo\":\"20170912233522798059\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230522238\"},\n" +
            "{\"userId\":9632,\"forUserId\":22,\"money\":3,\"principal\":3,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.03元\",\"seqNo\":\"20170912233522711892\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230522836\"},\n" +
            "{\"userId\":9648,\"forUserId\":22,\"money\":35,\"principal\":35,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.35元\",\"seqNo\":\"20170912233523912144\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230523545\"},\n" +
            "{\"userId\":9695,\"forUserId\":22,\"money\":29,\"principal\":29,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.29元\",\"seqNo\":\"20170912233524052309\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230524117\"},\n" +
            "{\"userId\":9727,\"forUserId\":22,\"money\":46,\"principal\":46,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.46元\",\"seqNo\":\"20170912233524684915\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230524734\"},\n" +
            "{\"userId\":9784,\"forUserId\":22,\"money\":4,\"principal\":4,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.04元\",\"seqNo\":\"20170912233525572180\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230525366\"},\n" +
            "{\"userId\":10156,\"forUserId\":22,\"money\":96,\"principal\":96,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.96元\",\"seqNo\":\"20170912233526934091\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230526021\"},\n" +
            "{\"userId\":10281,\"forUserId\":22,\"money\":16,\"principal\":16,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.16元\",\"seqNo\":\"20170912233526898605\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230526606\"},\n" +
            "{\"userId\":12495,\"forUserId\":22,\"money\":17,\"principal\":17,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.17元\",\"seqNo\":\"20170912233527964515\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230527222\"},\n" +
            "{\"userId\":13378,\"forUserId\":22,\"money\":2,\"principal\":2,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.02元\",\"seqNo\":\"20170912233527437605\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230527855\"},\n" +
            "{\"userId\":13723,\"forUserId\":22,\"money\":6,\"principal\":6,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.06元\",\"seqNo\":\"20170912233528532439\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230528504\"},\n" +
            "{\"userId\":13789,\"forUserId\":22,\"money\":4,\"principal\":4,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.04元\",\"seqNo\":\"20170912233529981128\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230529102\"},\n" +
            "{\"userId\":16556,\"forUserId\":22,\"money\":32,\"principal\":32,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.32元\",\"seqNo\":\"20170912233529008692\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230529706\"},\n" +
            "{\"userId\":16823,\"forUserId\":22,\"money\":5,\"principal\":5,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.05元\",\"seqNo\":\"20170912233530632476\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230530337\"},\n" +
            "{\"userId\":19286,\"forUserId\":22,\"money\":267,\"principal\":267,\"interest\":0,\"remark\":\"接收理财师提成奖励 2.67元\",\"seqNo\":\"20170912233531982172\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230531190\"},\n" +
            "{\"userId\":19426,\"forUserId\":22,\"money\":182,\"principal\":182,\"interest\":0,\"remark\":\"接收理财师提成奖励 1.82元\",\"seqNo\":\"20170912233531194626\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230531859\"},\n" +
            "{\"userId\":19645,\"forUserId\":22,\"money\":8,\"principal\":8,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.08元\",\"seqNo\":\"20170912233532726411\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230532427\"},\n" +
            "{\"userId\":23904,\"forUserId\":22,\"money\":36,\"principal\":36,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.36元\",\"seqNo\":\"20170912233533541648\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230533060\"},\n" +
            "{\"userId\":26613,\"forUserId\":22,\"money\":24,\"principal\":24,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.24元\",\"seqNo\":\"20170912233533845064\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230533765\"},\n" +
            "{\"userId\":32439,\"forUserId\":22,\"money\":32,\"principal\":32,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.32元\",\"seqNo\":\"20170912233534767962\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230534305\"},\n" +
            "{\"userId\":33017,\"forUserId\":22,\"money\":11,\"principal\":11,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.11元\",\"seqNo\":\"20170912233535573149\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230534971\"},\n" +
            "{\"userId\":34095,\"forUserId\":22,\"money\":14,\"principal\":14,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.14元\",\"seqNo\":\"20170912233535522454\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230535591\"},\n" +
            "{\"userId\":55609,\"forUserId\":22,\"money\":38,\"principal\":38,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.38元\",\"seqNo\":\"20170912233536627228\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230536230\"},\n" +
            "{\"userId\":62311,\"forUserId\":22,\"money\":2,\"principal\":2,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.02元\",\"seqNo\":\"20170912233536815010\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230536799\"},\n" +
            "{\"userId\":63149,\"forUserId\":22,\"money\":27,\"principal\":27,\"interest\":0,\"remark\":\"接收理财师提成奖励 0.27元\",\"seqNo\":\"20170912233537341503\",\"type\":\"receiveCommissions\",\"sourceId\":0,\"groupSeqNo\":\"1505230537476\"}]\n";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commonPublishRedpack(Long userId, long money, AssetChangeTypeEnum assetChangeTypeEnum, String onlyNo, String remark, long sourceId) throws Exception {
        Preconditions.checkArgument(sourceId > 0, "sourceId 不能为空");
        Preconditions.checkArgument(!StringUtils.isEmpty(onlyNo), "onleyNo 不能为空");
        Users user = userService.findByIdLock(userId);
        if (user.getIsLock()) {
            log.error("通用打开红包, 当前用户处于冻结状态!");
            throw new Exception("通用打开红包, 当前用户处于冻结状态!");
        }

        Preconditions.checkNotNull(user, "publishRedpack user record is null");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userThirdAccount is null");
        Long redpackAccountId = assetChangeProvider.getRedpackAccountId();
        Asset redpackAsset = assetService.findByUserId(redpackAccountId);
        Preconditions.checkNotNull(redpackAsset, "publishRedpack redpackAsset is null");

        if (redpackAsset.getUseMoney() - money < 0) {
            throw new Exception("非常抱歉, 红包账户余额不足, 请致电平台客服! (红包还可以继续使用)");
        }

        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
        // 判断用户是否派发过
        Specification<NewAssetLog> specification = Specifications
                .<NewAssetLog>and()
                .eq("userId", userId)
                .eq("localType", assetChangeTypeEnum.getLocalType())
                .eq("groupOpSeqNo", onlyNo)
                .build();
        long count = newAssetLogService.count(specification);
        if (count > 0) {
            throw new Exception("红包已经派发");
        }

        // 派发红包
        Gson gson = new Gson();
        double doubleMoney = MoneyHelper.divide(money, 100, 2);
        VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
        voucherPayRequest.setAccountId(redpackAccount.getAccountId()); // 红包账户
        voucherPayRequest.setTxAmount(doubleMoney + "");
        voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
        voucherPayRequest.setDesLine(sourceId + "");
        voucherPayRequest.setChannel(ChannelContant.HTML);
        VoucherPayResponse voucherPayResponse = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
        log.info(String.format("开始派发红包:%s", gson.toJson(voucherPayRequest)));
        log.info(String.format("结束派发红包:%s", gson.toJson(voucherPayResponse)));
        if ((ObjectUtils.isEmpty(voucherPayResponse)) || (!JixinResultContants.SUCCESS.equals(voucherPayResponse.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(voucherPayResponse) ? "当前网络不稳定，请稍候重试" : voucherPayResponse.getRetMsg();
            log.error(String.format("派发红包异常, 主动撤回: %s", msg));
            VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
            voucherPayCancelRequest.setAccountId(redpackAccount.getAccountId());
            voucherPayCancelRequest.setTxAmount(doubleMoney + "");
            voucherPayCancelRequest.setOrgTxDate(voucherPayRequest.getTxDate());
            voucherPayCancelRequest.setOrgTxTime(voucherPayCancelRequest.getTxTime());
            voucherPayCancelRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayCancelRequest.setOrgSeqNo(voucherPayCancelRequest.getSeqNo());
            voucherPayCancelRequest.setAcqRes(onlyNo);
            voucherPayCancelRequest.setChannel(ChannelContant.HTML);
            VoucherPayCancelResponse voucherPayCancelResponse = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET, voucherPayCancelRequest, VoucherPayCancelResponse.class);
            if ((ObjectUtils.isEmpty(voucherPayCancelResponse)) || (!JixinResultContants.SUCCESS.equals(voucherPayCancelResponse.getRetCode()))) {
                msg = ObjectUtils.isEmpty(voucherPayCancelResponse) ? "当前网络出现异常, 请稍后尝试！" : voucherPayCancelResponse.getRetMsg();
                log.error(String.format("撤销红包异常 %s", msg));
            }
            return false;
        }

        // 执行资金变动
        try {
            // 红包账户发送红包
            AssetChange redpackPublish = new AssetChange();
            redpackPublish.setMoney(money);
            redpackPublish.setType(AssetChangeTypeEnum.publishRedpack);  //  扣除红包
            redpackPublish.setUserId(redpackAccountId);
            redpackPublish.setRemark(String.format("平台派发奖励红包 %s元", StringHelper.formatDouble(money / 100D, true)));
            redpackPublish.setGroupSeqNo(onlyNo);
            redpackPublish.setSeqNo(String.format("%s%s%s", voucherPayRequest.getTxDate(), voucherPayRequest.getTxTime(), voucherPayRequest.getSeqNo()));
            redpackPublish.setForUserId(userId);
            redpackPublish.setSourceId(sourceId);
            assetChangeProvider.commonAssetChange(redpackPublish);

            if (StringUtils.isEmpty(remark)) {
                remark = String.format("领取奖励红包 %s元", StringHelper.formatDouble(money / 100D, true));
            }
            // 用户接收红包
            AssetChange redpackR = new AssetChange();
            redpackR.setMoney(money);
            redpackR.setType(assetChangeTypeEnum);
            redpackR.setUserId(userId);
            redpackR.setRemark(remark);
            redpackR.setGroupSeqNo(onlyNo);
            redpackR.setSeqNo(String.format("%s%s%s", voucherPayRequest.getTxDate(), voucherPayRequest.getTxTime(), voucherPayRequest.getSeqNo()));
            redpackR.setForUserId(redpackAccountId);
            redpackR.setSourceId(sourceId);
            assetChangeProvider.commonAssetChange(redpackR);
            return true;
        } catch (Exception e) {
            log.error("红包开启本地资金变动异常", e);
            String msg = ObjectUtils.isEmpty(voucherPayResponse) ? "当前网络不稳定，请稍候重试" : voucherPayResponse.getRetMsg();
            log.error(String.format("派发红包异常, 主动撤回: %s", msg));
            VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
            voucherPayCancelRequest.setAccountId(redpackAccount.getAccountId());
            voucherPayCancelRequest.setTxAmount(doubleMoney + "");
            voucherPayCancelRequest.setOrgTxDate(voucherPayRequest.getTxDate());
            voucherPayCancelRequest.setOrgTxTime(voucherPayCancelRequest.getTxTime());
            voucherPayCancelRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayCancelRequest.setOrgSeqNo(voucherPayCancelRequest.getSeqNo());
            voucherPayCancelRequest.setAcqRes(onlyNo);
            voucherPayCancelRequest.setChannel(ChannelContant.HTML);
            VoucherPayCancelResponse voucherPayCancelResponse = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET, voucherPayCancelRequest, VoucherPayCancelResponse.class);
            log.info("由资金变动异常,进行红包撤回: %s", gson.toJson(voucherPayCancelRequest));
            if ((ObjectUtils.isEmpty(voucherPayCancelResponse)) || (!JixinResultContants.SUCCESS.equals(voucherPayCancelResponse.getRetCode()))) {
                msg = ObjectUtils.isEmpty(voucherPayCancelResponse) ? "当前网络出现异常, 请稍后尝试！" : voucherPayCancelResponse.getRetMsg();
                log.error(String.format("由资金变动异常, 撤销红包异常 %s", msg));
            }
            throw new Exception("派发红包失败");
        }
    }

    @Override
    public ResponseEntity<VoViewRedPackageWarpRes> list(VoRedPackageReq voRedPackageReq) {
        Pageable pageable = new PageRequest(voRedPackageReq.getPageIndex(), voRedPackageReq.getPageSize(), new Sort(Sort.Direction.DESC, "id"));
        VoViewRedPackageWarpRes voViewRedPackageWarpRes = VoBaseResp.ok("查询成功", VoViewRedPackageWarpRes.class);
        List<MarketingRedpackRecord> marketingRedpackRecords = marketingRedpackRecordService.findByUserIdAndState(voRedPackageReq.getUserId(), voRedPackageReq.getStatus(), pageable);

        RedPackageRes redPackageRes = null;
        // 遍历
        for (MarketingRedpackRecord item : marketingRedpackRecords) {
            if (item.getMarketingId() == 1) {
                item.setMarketingId(2L);
            } else if (item.getMarketingId() == 2) {
                item.setMarketingId(3L);
            } else if (item.getMarketingId() == 3) {
                item.setMarketingId(1L);
            } else {
                item.setMarketingId(4L);
            }
            redPackageRes = new RedPackageRes();
            redPackageRes.setExpiryDate(DateHelper.dateToString(item.getPublishTime(), DateHelper.DATE_FORMAT_YMDHM)
                    + "~" + DateHelper.dateToString(item.getCancelTime(), DateHelper.DATE_FORMAT_YMDHM));  // 有效时间
            redPackageRes.setMoney(StringHelper.formatMon(item.getMoney() / 100D));
            redPackageRes.setRedPackageId(item.getId());
            redPackageRes.setTitle(item.getMarkeingTitel());
            redPackageRes.setType(item.getMarketingId().intValue());
            voViewRedPackageWarpRes.getResList().add(redPackageRes);
        }
        return ResponseEntity.ok(voViewRedPackageWarpRes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoViewOpenRedPackageWarpRes> openRedPackage(VoOpenRedPackageReq packageReq) throws Exception {
        MarketingRedpackRecord marketingRedpackRecord = marketingRedpackRecordService.findTopByIdAndUserIdAndDel(packageReq.getRedPackageId(), packageReq.getUserId(), 0);
        if (ObjectUtils.isEmpty(marketingRedpackRecord)) {
            log.error("打开红包失败,该红包id不存在 或者已过期: {redPackageId:" + packageReq.getRedPackageId() + "," +
                    "userId:" + packageReq.getUserId() + "," +
                    "nowTime:" + DateHelper.dateToString(new Date()) + "}");
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "系统开小差了, 又有人要扣奖金了", VoViewOpenRedPackageWarpRes.class));
        }

        if (marketingRedpackRecord.getState() == 2) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "对不起, 当前红包已过期!", VoViewOpenRedPackageWarpRes.class));
        } else if (marketingRedpackRecord.getState() == 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "不要调皮了, 当前红包已经被领取了!", VoViewOpenRedPackageWarpRes.class));
        }

        Date nowDate = new Date();
        // 判断时间
        if (DateHelper.diffInDays(nowDate, marketingRedpackRecord.getCancelTime(), false) > 0) {
            // 更新红包
            marketingRedpackRecord.setState(2);
            marketingRedpackRecordService.save(marketingRedpackRecord);
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "对不起, 当前红包已过期!", VoViewOpenRedPackageWarpRes.class));
        }

        Users users = userService.findById(packageReq.getUserId());
        if (ObjectUtils.isEmpty(users)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "当前用户不存在!", VoViewOpenRedPackageWarpRes.class));
        }

        if (users.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoViewOpenRedPackageWarpRes.error(VoViewOpenRedPackageWarpRes.ERROR, "当前用户锁定, 取消你领取额红包的资格!", VoViewOpenRedPackageWarpRes.class));
        }

        String onlySeql = String.format("%s%s%s", users.getId(), AssetChangeTypeEnum.receiveRedpack.getLocalType(), marketingRedpackRecord.getId());
        boolean result = commonPublishRedpack(users.getId(),
                marketingRedpackRecord.getMoney(),
                AssetChangeTypeEnum.receiveRedpack,
                onlySeql,
                null,
                marketingRedpackRecord.getId());
        if (result) {
            // 更新红包
            marketingRedpackRecord.setState(1);
            marketingRedpackRecordService.save(marketingRedpackRecord);
            //站内信数据装配
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(marketingRedpackRecord.getUserId());
            notices.setRead(false);
            notices.setName("打开红包");
            notices.setContent("你在" + DateHelper.dateToString(new Date()) + "开启红包(" + marketingRedpackRecord.getMarkeingTitel() + ")获得奖励" + StringHelper.formatDouble(marketingRedpackRecord.getMoney() / 100d, true) + "元");
            notices.setType("system");
            notices.setCreatedAt(new Date());
            notices.setUpdatedAt(new Date());
            //发送站内信
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("RedPackageServiceImpl openRedPackage send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("RedPackageServiceImpl openRedPackage send mq exception", e);
            }

            VoViewOpenRedPackageWarpRes voViewOpenRedPackageWarpRes = VoBaseResp.ok("打开红包成功", VoViewOpenRedPackageWarpRes.class);
            voViewOpenRedPackageWarpRes.setMoney(marketingRedpackRecord.getMoney() / 100D);
            return ResponseEntity.ok().body(voViewOpenRedPackageWarpRes);
        } else {
            VoViewOpenRedPackageWarpRes voViewOpenRedPackageWarpRes = VoBaseResp.error(VoBaseResp.ERROR, "打开红包失败", VoViewOpenRedPackageWarpRes.class);
            voViewOpenRedPackageWarpRes.setMoney(0D);
            return ResponseEntity.ok().body(voViewOpenRedPackageWarpRes);
        }
    }

    @Override
    public ResponseEntity<VoBaseResp> publishActivity(VoPublishRedReq voPublishRedReq) throws Exception {
        Date nowDate = new Date();
        String paramStr = voPublishRedReq.getParamStr();

        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(DATA);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int i = 0;
        for (JsonElement item : jsonArray) {
            AssetChange assetChange = gson.fromJson(item, AssetChange.class);
            assetChangeProvider.commonAssetChange(assetChange);

            log.info("补发理财计划" + ++i);
        }


        /*String beginTime = paramMap.get("beginTime");
        if (StringUtils.isEmpty(beginTime)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "派发红包, 签名验证不通过!"));
        }

        Date beginDate = DateHelper.stringToDate(beginTime);
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .between("createdAt", new Range<>(DateHelper.beginOfDate(beginDate), DateHelper.endOfDate(beginDate))).build();

        Long count = tenderService.count(specification);
        if (count == 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "派发红包, 派发对象为空!"));
        }

        int pageSize = 100, pageindex = 0, totalPageIndex = 0;
        totalPageIndex = count.intValue() / pageSize;
        totalPageIndex = count.intValue() % pageSize == 0 ? totalPageIndex : totalPageIndex + 1;
        // ==================================
        // 投资派发红包
        // ==================================
        for (; pageindex < totalPageIndex; pageindex++) {
            Pageable pageable = new PageRequest(pageindex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<Tender> tenderList = tenderService.findList(specification, pageable);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            for (Tender tender : tenderList) {
                log.info(String.format("触发活动: %s", gson.toJson(tender)));
                MarketingData marketingData = new MarketingData();
                marketingData.setTransTime(DateHelper.dateToString(tender.getCreatedAt()));
                marketingData.setUserId(tender.getUserId().toString());
                marketingData.setSourceId(tender.getId().toString());
                marketingData.setMarketingType(MarketingTypeContants.TENDER);
                try {
                    String json = gson.toJson(marketingData);
                    Map<String, String> data = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setMsg(data);
                    mqConfig.setTag(MqTagEnum.MARKETING_TENDER);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("投资营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("投资营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        }*/

        /*
        // ===============================
        // 用户派发红包
        // ===============================
        Specification<Users> usersSpecification = Specifications
                .<Users>and()
                .gt("parentId", 0)
                .between("createdAt", new Range<>(DateHelper.beginOfDate(beginDate), DateHelper.endOfDate(nowDate)))
                .build();


        Long userCount = userService.count(usersSpecification);
        pageindex = 0;
        totalPageIndex = 0;
        totalPageIndex = userCount.intValue() / pageSize;
        totalPageIndex = userCount.intValue() % pageSize == 0 ? totalPageIndex : totalPageIndex + 1;

        for (; pageindex < totalPageIndex; pageindex++) {
            Pageable pageable = new PageRequest(pageindex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
            List<Users> userList = userService.findList(usersSpecification, pageable);
            for (Users users : userList) {
                log.info(String.format("触发活动: %s", gson.toJson(users)));
                MarketingData marketingData = new MarketingData();
                marketingData.setTransTime(DateHelper.dateToString(users.getCreatedAt()));
                marketingData.setUserId(users.getId().toString());
                marketingData.setSourceId(users.getId().toString());
                marketingData.setMarketingType(MarketingTypeContants.OPEN_ACCOUNT);
                try {
                    String json = gson.toJson(marketingData);
                    Map<String, String> data = gson.fromJson(json, TypeTokenContants.MAP_ALL_STRING_TOKEN);
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setMsg(data);
                    mqConfig.setTag(MqTagEnum.MARKETING_OPEN_ACCOUNT);
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_MARKETING);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("开户营销节点触发: %s", new Gson().toJson(marketingData)));
                } catch (Throwable e) {
                    log.error(String.format("开户营销节点触发异常：%s", new Gson().toJson(marketingData)), e);
                }
            }
        }*/
        return null;
    }


}
