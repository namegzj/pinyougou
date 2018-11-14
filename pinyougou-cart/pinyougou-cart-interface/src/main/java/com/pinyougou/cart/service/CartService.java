package com.pinyougou.cart.service;

import com.pinyougou.vo.Cart;

import java.util.List;

public interface CartService {

    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    void saveCartLisToRedis(String username, List<Cart> cartList);

    List<Cart> findCartInRedis(String username);

    List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
