package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.PayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;
    @Override
    public Map<String, String> createNative(String outTradeNo, String totalFee) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            //1. 组装微信统一下单需要的数据；
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串；可以由微信工具类获取
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；在转换为xml的时候可以指定自动生成
            //paramMap.put("sign", "");
            //商品描述
            paramMap.put("body", "品优购-110");
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);
            //标价金额
            paramMap.put("total_fee", totalFee);
            //终端IP
            paramMap.put("spbill_create_ip", "127.0.0.1");
            //通知地址
            paramMap.put("notify_url", notifyurl);
            //交易类型
            paramMap.put("trade_type", "NATIVE");

            //将上述的map转换为xml
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送到 统一下单 的内容为：" + signedXml);

            //2. 创建HttpClient发送请求https://api.mch.weixin.qq.com/pay/unifiedorder
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //3. 处理返回结果
            String content = httpClient.getContent();
            System.out.println("发送到 统一下单 的返回结果为：" + content);

            //将返回结果转化为map
            Map<String, String> map = WXPayUtil.xmlToMap(content);

            resultMap.put("outTradeNo", outTradeNo);
            resultMap.put("totalFee", totalFee);
            resultMap.put("result_code", map.get("result_code"));
            resultMap.put("code_url", map.get("code_url"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try {
            //1. 组装微信统一下单需要的数据；
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串；可以由微信工具类获取
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；在转换为xml的时候可以指定自动生成
            //paramMap.put("sign", "");
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //将上述的map转换为xml
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送到 查询订单 的内容为：" + signedXml);

            //2. 创建HttpClient发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //3. 处理返回结果
            String content = httpClient.getContent();
            System.out.println("发送到 查询订单 的返回结果为：" + content);

            //将返回结果转化为map
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, String> closeOrder(String outTradeNo) {
        try {
            //1. 组装微信统一下单需要的数据；
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串；可以由微信工具类获取
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；在转换为xml的时候可以指定自动生成
            //paramMap.put("sign", "");
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //将上述的map转换为xml
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送到 关闭订单 的内容为：" + signedXml);

            //2. 创建HttpClient发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //3. 处理返回结果
            String content = httpClient.getContent();
            System.out.println("发送到 关闭订单 的返回结果为：" + content);

            //将返回结果转化为map
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
