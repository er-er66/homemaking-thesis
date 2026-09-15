package com.example.homemaking.mapper;

import com.example.homemaking.entity.HomemakingOrderImg;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomemakingOrderImgMapper {
    /**
     * 完成订单封面图片
     * @param homemakingOrderImg 订单封面图片
     */
    Boolean CompletedOrderImg(HomemakingOrderImg homemakingOrderImg);
}
