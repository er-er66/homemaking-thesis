package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoomMember {
    private Integer id;
    private String roomId;//所属房间id
    private String userId;//成员id
    private Integer userType;//用户类型 1普通用户 2家政人员 3管理员
    private LocalDateTime joinTime;//加入时间
}