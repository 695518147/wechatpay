package com.it.wechatpay.model;

import lombok.Data;

@Data
public class OrderReturnInfo {

    private String return_code;
    private String return_msg;
    private String result_code;
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String prepay_id;
    private String trade_type;

}
