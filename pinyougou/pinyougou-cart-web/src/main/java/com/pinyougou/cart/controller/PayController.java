package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private OrderService orderService;

    @Reference
    private PayService payService;

    /**
     * 根据支付日志id 统一下单获取支付链接二维码地址
     *
     * @param outTradeNo 支付日志id
     * @return 统一下单的结果（交易编号，操作结果，总金额，支付二维码链接地址）
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo) {
        try {
            //根据支付日志id 查询支付日志
            TbPayLog payLog = orderService.findPayLogByOutTradeNo(outTradeNo);

            if (payLog != null) {
                //调用支付系统 统一下单 方法
                return payService.createNative(outTradeNo, payLog.getTotalFee() + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * 根据交易编号查询支付状态
     * @param outTradeNo 交易编号
     * @return 查询结果
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = Result.fail("查询支付状态失败");
        try {
            //3分钟内未支付则返回支付超时
            int count = 0;
            while(true) {
                //1. 每隔3秒调用支付系统业务方法查询支付状态；
                Map<String, String> map = payService.queryPayStatus(outTradeNo);
                if (map == null) {
                    break;
                }
                //2. 如果支付成功则更新支付日志状态为1，该支付对应的所有订单的支付状态为2
                if ("SUCCESS".equals(map.get("trade_state"))) {
                    //支付成功；更新状态
                    orderService.updateOrderStatus(outTradeNo, map.get("transaction_id"));
                    result = Result.ok("支付成功");
                    break;
                }
                count++;
                if (count > 60) {
                    //已经超时
                    result = Result.fail("支付超时");
                    break;
                }
                //睡眠3秒
                Thread.sleep(3000);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
