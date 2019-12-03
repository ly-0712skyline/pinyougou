package com.pinyougou.cart.service;

import com.pinyougou.vo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 将购买的商品和数量加入到购物车列表并返回最新的购物车列表
     * @param itemId 商品sku id
     * @param num 购买数量
     * @return 购物车列表
     */
    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 根据用户名从redis中查询购物车列表
     * @param username 用户名
     * @return 购物车列表
     */
    List<Cart> findCartListInRedisByUsername(String username);

    /**
     * 将用户的购物车列表保存到redis
     * @param cartList 购物车列表
     * @param username 用户id
     */
    void saveCartListInRedisByUsername(List<Cart> cartList, String username);

    /**
     * 合并两个购物车数据到一个新购物车
     * @param cartList1 购物车列表1
     * @param cartList2 购物车列表2
     * @return 新购物车列表
     */
    List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
