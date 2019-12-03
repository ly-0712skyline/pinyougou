package com.pinyougou.shop.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    /**
     * 根据用户输入的用户名进行登录认证
     * @param username 用户在页面中输入的用户名
     * @return 用户信息
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //角色权限集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //根据商家主键查询
        TbSeller seller = sellerService.findOne(username);

        if (seller != null && "1".equals(seller.getStatus())) {
            //已经审核通过
            return new User(username, seller.getPassword(), authorities);
        }

        //将与用户输入的用户名与密码进行对比校验；密码一致则认证成功，否则失败
        //return new User(username, "123456", authorities);
        return null;
    }
}
