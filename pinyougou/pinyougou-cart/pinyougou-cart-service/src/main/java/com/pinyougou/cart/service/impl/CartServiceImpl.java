package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //品优购在redis中的购物车key键名
    private static final String REDIS_CART_LIST = "CART_LIST";
    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //判断当前加入购物车的商品是否存在、已启用
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在！");
        }
        //已启用
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品非法！");
        }
        Cart cart = findCartInCartListBySellerId(cartList, item.getSellerId());
        if(cart == null) {
            //1、商品对应的商家（cart)不存在(cartList)
            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());
            //创建购物车对象cart，设置商家信息和创建订单商品列表往其添加一个订单商品；再将购物车对象Cart添加到cartList
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            cartList.add(cart);
        } else {
            //2、商品对应的商家（cart)存在(cartList)
            TbOrderItem orderItem = findOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem != null) {
                //2.1、商品在该购物车的订单商品列表orderItemList
                //2.1.1、将购买数量叠加（减）；重新计算总价
                orderItem.setNum(orderItem.getNum()+num);
                //总价 = 单价*总数
                orderItem.setTotalFee(orderItem.getPrice()*orderItem.getNum());

                //2.1.2、如果购买的数量为小于1的时候，将该商品从订单商品列表中移除
                if (orderItem.getNum() < 1) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //2.1.3、如果订单列表的大小为0的时候；需要将该购物车对象cart从购物车列表（cartList）移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            } else {
                //2.2、商品不在该购物车的订单商品列表orderItemList
                // 直接创建一个订单商品，将该商品加入到订单商品列表orderItemList
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListInRedisByUsername(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListInRedisByUsername(List<Cart> cartList, String username) {
        redisTemplate.boundHashOps(REDIS_CART_LIST).put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                addItemToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据商品id 从商品列表中查询
     * @param orderItemList 订单商品列表
     * @param itemId 商品sku id
     * @return 订单商品
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        if (orderItemList != null && orderItemList.size() > 0) {
            for (TbOrderItem orderItem : orderItemList) {
                if (itemId.equals(orderItem.getItemId())) {
                    return orderItem;
                }
            }
        }
        return null;
    }

    /**
     * 创建购物车订单商品对象
     * @param item 商品sku
     * @param num 购买数量
     * @return 订单商品orderItem
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setSellerId(item.getSellerId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        //总价 = 单价*总数
        orderItem.setTotalFee(orderItem.getPrice()*orderItem.getNum());
        return orderItem;
    }

    /**
     * 根据商家id从购物车列表中查询cart购物车对象
     *
     * @param cartList 购物车列表
     * @param sellerId 商家id
     * @return 购物车对象cart
     */
    private Cart findCartInCartListBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }
}
