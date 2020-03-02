package com.it.wechatpay.controller;

import com.alibaba.fastjson.JSONObject;
import com.it.wechatpay.common.*;
import com.it.wechatpay.model.OrderInfo;
import com.it.wechatpay.model.OrderReturnInfo;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/pay")
@RestController
@Slf4j
public class WeChatController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        HttpGet httpGet = new HttpGet("https://api.weixin.qq.com/sns/jscode2session?appid=" + Configure.appID
                + "&secret=" + Configure.secret + "&js_code=" + code + "&grant_type=authorization_code");
        //设置请求器的配置
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse res = httpClient.execute(httpGet);
        HttpEntity entity = res.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        return result;
    }

    /**
     * 微信小程序支付报签名错误：按照微信支付文档开发最后签名错误的话，一般是商户平台所属公司与小程序注册时提交的公司不是同一主体，
     * 但是微信服务器报的错误是签名错误，需要在商户平台---->产品中心----->APPID授权管理中进行授权，授权通过后才能签名正确
     * @param request
     * @return
     * @throws IOException
     */
    @PostMapping("/toPay")
    public OrderReturnInfo toPay(HttpServletRequest request) throws IOException {
        try {
            String openid = request.getParameter("openid");
            OrderInfo order = new OrderInfo();
            // appId
            order.setAppid(Configure.appID);
            // mch_id
            order.setMch_id(Configure.mch_id);
            order.setNonce_str(RandomStringGenerator.getRandomStringByLength(32));
            // 商品描述
            order.setBody("dfdfdf");

            // 商户订单号
            order.setOut_trade_no(RandomStringGenerator.getRandomStringByLength(32));

            // 标价金额 (订单总金额，单位为分)
            order.setTotal_fee(10);

            // 终端IP
            order.setSpbill_create_ip("123.57.218.54");

            // 通知地址
            order.setNotify_url("https://www.see-source.com/weixinpay/PayResult");

            // 交易类型
            order.setTrade_type("JSAPI");

            // 用户标识, trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。
            order.setOpenid(openid);

            // 签名类型，默认为MD5，支持HMAC-SHA256和MD5。
            order.setSign_type("MD5");

            //生成签名
            String sign = Signature.getSign(order);
            order.setSign(sign);

            String result = HttpRequest.sendPost(Configure.pay_url, order);
            log.info("---------下单返回:"+result);
            XStream xStream = new XStream();
            xStream.alias("xml", OrderReturnInfo.class);

            OrderReturnInfo returnInfo = (OrderReturnInfo)xStream.fromXML(result);
            return returnInfo;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("-------", e);
        }
        return null;
    }

    @GetMapping("/notice")
    public String notice(HttpServletRequest request) throws IOException {
        String reqParams = StreamUtil.read(request.getInputStream());
        log.info("-------支付结果:"+reqParams);
        StringBuffer sb = new StringBuffer("<xml><return_code>SUCCESS</return_code><return_msg>OK</return_msg></xml>");
        return sb.toString();
    }


}
