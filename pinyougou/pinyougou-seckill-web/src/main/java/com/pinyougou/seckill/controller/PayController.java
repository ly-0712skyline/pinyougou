package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference(timeout = 10000)
    private SeckillOrderService orderService;

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
            //根据在redis中的秒杀订单
            TbSeckillOrder seckillOrder = orderService.findSeckillOrderInRedisByOrderId(outTradeNo);

            if (seckillOrder != null) {
                //调用支付系统 统一下单 方法
                //总金额
                String totalFee = (long)(seckillOrder.getMoney()*100)+"";
                return payService.createNative(outTradeNo, totalFee);
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
            //1分钟内未支付则返回支付超时
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
                    orderService.saveSeckillOrderInRedisToDb(outTradeNo, map.get("transaction_id"));
                    result = Result.ok("支付成功");
                    break;
                }
                count++;
                if (count > 20) {
                    //已经超时
                    result = Result.fail("支付超时");

                    //关闭订单
                    map = payService.closeOrder(outTradeNo);
                    if (map != null && "ORDERPAID".equals(map.get("err_code"))) {
                        //在关闭订单的过程中被用户支付了
                        orderService.saveSeckillOrderInRedisToDb(outTradeNo, map.get("transaction_id"));
                        result = Result.ok("支付成功");
                        break;
                    }

                    //将redis中的订单删除并更新库存
                    orderService.deleteSeckillOrderInRedis(outTradeNo);

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
