package com.example.homemaking.mapper;

import com.example.homemaking.entity.UserImg;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserImgMapper {
    /**
     * 保存用户订单封面
     * @param userImg
     * @return
     */
    int addUserImg(UserImg userImg);

    /**
     * 根据订单号查询封面图片
     * @param orderNo
     * @return
     */
    UserImg selectByOrderNo(String orderNo);
}