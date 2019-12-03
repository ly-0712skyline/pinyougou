package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //品优购在redis中的购物车key键名
    private static final String REDIS_CART_LIST = "CART_LIST";

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public PageInfo<TbOrder> search(Integer pageNum, Integer pageSize, TbOrder order) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbOrder.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(order.getProperty())) {
            criteria.andLike("property", "%" + order.getProperty() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public String addOrder(TbOrder order) {
        //支付日志id
        String outTradeNo = "";

        //1、获取到当前用户的购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(order.getUserId());
        if (cartList != null && cartList.size() > 0) {
            //2、遍历每个购物车对象cart，生成一笔订单order
            TbOrder tbOrder = null;
            String orderId = "";
            //本次交易的总金额
            Double totalFee = 0.0;
            //本次交易对应的所有订单id
            String orderIds = "";
            for (Cart cart : cartList) {
                tbOrder = new TbOrder();
                orderId = idWorker.nextId()+"";
                tbOrder.setOrderId(orderId);

                if (orderIds.length() > 0) {
                    orderIds += "," + orderId;
                } else {
                    orderIds = orderId;
                }

                tbOrder.setUserId(order.getUserId());
                tbOrder.setSourceType(order.getSourceType());
                tbOrder.setReceiver(order.getReceiver());
                tbOrder.setReceiverAreaName(order.getReceiverAreaName());
                tbOrder.setReceiverMobile(order.getReceiverMobile());
                //未付款
                tbOrder.setStatus("1");
                tbOrder.setCreateTime(new Date());
                tbOrder.setUpdateTime(tbOrder.getCreateTime());

                tbOrder.setSellerId(cart.getSellerId());

                //每笔订单的总金额
                Double orderPayment = 0.0;

                //2.1、遍历每个购物车对象中的商品，保存对应的订单商品orderItem
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    orderItem.setOrderId(orderId);
                    orderItem.setId(idWorker.nextId());
                    orderItemMapper.insertSelective(orderItem);

                    //累计本笔订单的所有订单商品的总金额
                    orderPayment += orderItem.getTotalFee();
                }
                //订单总金额
                tbOrder.setPayment(orderPayment);

                //累计每笔订单的总金额
                totalFee += orderPayment;

                add(tbOrder);
            }
            //3、创建支付日志信息；如果为微信付款则状态为0，如果是货到付款则默认为1已支付
            TbPayLog tbPayLog = new TbPayLog();
            outTradeNo = idWorker.nextId() + "";
            tbPayLog.setOutTradeNo(outTradeNo);
            tbPayLog.setCreateTime(new Date());
            tbPayLog.setPayType(order.getPaymentType());
            tbPayLog.setUserId(order.getUserId());
            //每次要支付的总金额= 所有订单的总金额之和；微信、支付宝对应总金额的单位都为分，所以要乘于100取整
            tbPayLog.setTotalFee((long)(totalFee*100));
            //所有订单id字符串；订单id之间使用,分隔
            tbPayLog.setOrderList(orderIds);
            if ("1".equals(order.getPaymentType())) {
                //微信付款，状态为0未支付
                tbPayLog.setTradeState("0");
            } else {
                //货到付款，状态为1已支付
                tbPayLog.setTradeState("1");
            }
            payLogMapper.insertSelective(tbPayLog);

            //4、要清空用户的购物车数据，删除redis中的购物车数据
            redisTemplate.boundHashOps(REDIS_CART_LIST).delete(order.getUserId());
        }
        //5、返回支付日志id
        return outTradeNo;
    }

    @Override
    public TbPayLog findPayLogByOutTradeNo(String outTradeNo) {
        return payLogMapper.selectByPrimaryKey(outTradeNo);
    }

    @Override
    public void updateOrderStatus(String outTradeNo, String transaction_id) {
        //- 更新支付日志信息，支付状态为1
        TbPayLog payLog = findPayLogByOutTradeNo(outTradeNo);
        payLog.setPayTime(new Date());
        //已支付
        payLog.setTradeState("1");
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKeySelective(payLog);

        //- 该支付日志对应的所有订单都需要更新为已支付2
        //update tb_order set status=?,paytime=? where order_id in(?,?)
        String[] orderIds = payLog.getOrderList().split(",");

        //要更新的数据
        TbOrder tbOrder = new TbOrder();
        tbOrder.setPaymentTime(new Date());
        //已支付
        tbOrder.setStatus("2");

        //创建更新条件
        Example example = new Example(TbOrder.class);
        example.createCriteria().andIn("orderId", Arrays.asList(orderIds));
        orderMapper.updateByExampleSelective(tbOrder, example);
    }

}
