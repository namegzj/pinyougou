package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final String COOKIE_CART_LIST = "PYG_CART_LIST";
    private static final int COOKIE_CART_LIST_MAX_AGE = 24 * 60 * 60;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference
    private CartService cartService;


    @GetMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pyg.com", allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num) {

        try {
         /*   //设置允许跨域请求
            response.setHeader("Access-Control-Allow-Origin", "http://item.pyg.com");
           // 设置允许接收客户端cookie和响应cookie
            response.setHeader("Access-Control-Allow-Credentials", "true");
            */
            //1、获取当前购物车列表
            List<Cart> cartList = findCartList();

            //2、将购买商品sku 的购买数量更新到购物车列表并返回最新的购物车列表
            cartList = cartService.addItemToCartList(cartList, itemId, num);
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //判断是否登录
            if ("anonymousUser".equals(username)) {
                //3、将最新的购物车列表写回cookie
                CookieUtils.setCookie(request, response, COOKIE_CART_LIST, JSON.toJSONString(cartList), COOKIE_CART_LIST_MAX_AGE, true);

            } else {
                //已登录
                cartService.saveCartLisToRedis(username, cartList);
            }
            return Result.ok("购物车添加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("加入购物车失败");
    }

    @GetMapping("/findCartList")
    public List<Cart> findCartList() {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            //查询cookie中购物车列表；
            List<Cart> cookie_cartList = new ArrayList<>();
            //没有登录,保存到cookie
            String cartListJsonStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);

            if (!StringUtils.isEmpty(cartListJsonStr)) {
                cookie_cartList = JSONArray.parseArray(cartListJsonStr, Cart.class);
            }
            //判断是否登录
            if ("anonymousUser".equals(username)) {
                return cookie_cartList;
            } else {
                //已登录
                List<Cart> redis_cartList = cartService.findCartInRedis(username);
                if (cookie_cartList.size() > 0) {
                    redis_cartList = cartService.mergeCartList(cookie_cartList, redis_cartList);
                    CookieUtils.deleteCookie(request, response, COOKIE_CART_LIST );

                    cartService.saveCartLisToRedis(username, redis_cartList);
                }
                return redis_cartList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @GetMapping("/getUsername")
    public Map<String, Object> getUsername() {
        Map<String, Object> map = new HashMap<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);
        return map;
    }


}
