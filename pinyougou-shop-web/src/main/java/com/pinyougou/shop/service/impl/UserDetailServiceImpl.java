package com.pinyougou.shop.service.impl;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //根据商家id到数据库中查询商家信息;如果存在并审核通过则返回用户信息，否则说明用户名不对登录失败
        TbSeller seller = sellerService.findOne(username);
        if (seller != null && "1".equals(seller.getStatus())) {
            //角色权限集合（本来应该根据用户名到数据库中查询的，但是我们的系统没有角色表）
            List<GrantedAuthority> authorities = new ArrayList<>();
            //指定一个角色叫ROLE_SELLER
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

            //将用户在表单中输入的密码会与User中的密码进行对比，如果一致则登录成功，否则失败
            return new User(username, seller.getPassword(), authorities);
        }

        return null;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
}
