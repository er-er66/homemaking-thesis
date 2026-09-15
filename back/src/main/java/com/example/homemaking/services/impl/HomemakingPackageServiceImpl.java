package com.example.homemaking.services.impl;

import com.alibaba.fastjson.JSON;
import com.example.homemaking.dto.HomemakingPackageDTO;
import com.example.homemaking.dto.PackageCacheDTO;
import com.example.homemaking.entity.HomemakingPackage;
import com.example.homemaking.mapper.HomemakingPackageMapper;
import com.example.homemaking.services.HomemakingPackageService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class HomemakingPackageServiceImpl implements HomemakingPackageService {
    @Resource//@Resource 默认先按名称匹配，再按类型匹配
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private HomemakingPackageMapper homemakingPackageMapper;

    private static final String KEY_PREFIX = "homemaking:package:";

    //缓存的过期时间（秒）这里2个小时
    private static final long BASE_TTL_SECONDS = 2 * 60 * 60;//单位为秒
    //缓存的随机时间（秒）30 分钟防止缓存雪崩
    /**
     * 随机范围30分钟
     * 用来防止缓存雪崩，避免所有缓存同时过期，造成同一时间访问MYSQL，这样会造成MYSQL宕机
     * 当我加上随机时间，那么在2个小时过期的缓存会在这30分钟内随机平摊的访问MYSQL数据库，避免了缓存雪崩
     */
    private static final long RANDOM_RANGE = 60 * 30;//随机范围30分钟

    /**
     * 缓存空值的过期时间（秒）5 分钟
     * 用来防止缓存穿透
     * 如果有大量的请求去请求不存在的数据，Redis没有就会去查询MYSQL，这样缓存持续不命中，导致数据库持续承受压力，这就是缓存穿透
     * 根据key查询数据，Redis没有，就去查数据库，但数据也没有就，就往Redis中写入一个空标识（null,空字符串），
     * 设置时间为5分钟，如果5分钟内在来相同请求就会，直接返回空值，不会去MYSQL查询数据，从而避免了缓存穿透
     */
    private static final long NULL_TTL = 5 * 60;


    public HomemakingPackage getPackageById(Long id) {
        // 拼接缓存key前缀 + 业务id，生成完整Redis键
        String cacheKey = KEY_PREFIX + id;
        //从Redis中根据key来查询缓存的字符串
        //opsForValue() 代表操作 Redis String 字符串类型
        String cacheJson = redisTemplate.opsForValue().get(cacheKey);

        PackageCacheDTO cacheDTO = null;

        //判断：缓存字符串不为空、不是空白字符
        if (StringUtils.isNotBlank(cacheJson)) {
            //如果存的是NULL则为空标识符，直接返回空值
            if ("NULL".equals(cacheJson)) {
                return null;
            }
            // JSON字符串反序列化为Java对象
            cacheDTO = JSON.parseObject(cacheJson, PackageCacheDTO.class);
        }
        if (cacheDTO != null && cacheDTO.getPackageInfo() != null) {
            HomemakingPackage pak = cacheDTO.getPackageInfo();//获取到套餐原始数据赋值给pak
            if (checkPackageEffective(pak)) {
                return pak;
            } else {
                //套餐以失效主动清理缓存，防止占用无效缓存
                redisTemplate.delete(cacheKey);
                return null;
            }
        }
        //缓存未命中，去数据库查询数据
        HomemakingPackage pak = homemakingPackageMapper.selectById(id);
        if (pak == null || pak.getStatus() == 1 || pak.getIsDeleted() == 1) {//pak.getStatus()==1 代表下架状态

            //第 1 个：cacheKey → Redis 的 key
            //第 2 个："NULL" → 存入 Redis 的 value（固定占位字符串）
            //第 3 个：过期时长数值 NULL_TTL = 5 * 60 = 300 秒（5 分钟）
            //第 4 个：时间单位 TimeUnit.SECONDS → 秒
            redisTemplate.opsForValue().set(cacheKey, "NULL", NULL_TTL, TimeUnit.SECONDS);//手动设置缓存空标识
            return null;
        }
        PackageCacheDTO dto = new PackageCacheDTO();
        dto.setPackageInfo(pak);//给dto赋值
        long nowTs = System.currentTimeMillis();
        dto.setCacheExpireAt(nowTs + (BASE_TTL_SECONDS + new Random().nextLong(RANDOM_RANGE)) * 1000);//设置缓存过期时间

        //将dto序列化为JSON字符串
        long realTtl = BASE_TTL_SECONDS + new Random().nextLong(RANDOM_RANGE);//随机缓存过期
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(dto), realTtl, TimeUnit.SECONDS);
        //写入缓存再次进行一次校验，防止套餐在此时过期
        return checkPackageEffective(pak) ? pak : null;//true就为pak 否则为null
    }

    private boolean checkPackageEffective(HomemakingPackage pak) {
        //判断套餐是否在有效期
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = pak.getExpireStartTime();//套餐开始时间
        LocalDateTime endTime = pak.getExpireEndTime();//套餐到期结束时间
        //当前时间 > 开始时间 并且 当前时间 < 结束时间 则返回true，否则返回false
        return now.isAfter(startTime) && now.isBefore(endTime);
    }



    @Override
    public String create(com.example.homemaking.dto.HomemakingPackageDTO homemakingPackageDTO) {
        HomemakingPackage homemakingPackage = new HomemakingPackage();
        BeanUtils.copyProperties(homemakingPackageDTO,homemakingPackage);
        homemakingPackage.setCreateTime(LocalDateTime.now());
        homemakingPackage.setUpdateTime(LocalDateTime.now());
        homemakingPackage.setIsDeleted(0);
        int count = homemakingPackageMapper.insert(homemakingPackage);
      if(count>0){
        return "create success";
      }else{
        return "create failed";
      }
    }

    @Override
    public List<HomemakingPackage> list() {
        List<HomemakingPackage> list = homemakingPackageMapper.selectList();
        return list;
    }

    @Override
    public int updatePackage(HomemakingPackageDTO homemakingPackageDTO) {
        HomemakingPackage homemakingPackage = new HomemakingPackage();
        BeanUtils.copyProperties(homemakingPackageDTO,homemakingPackage);
        homemakingPackage.setUpdateTime(LocalDateTime.now());
        return homemakingPackageMapper.updateById(homemakingPackage);
    }

    @Override
    public List<HomemakingPackage> getOrderList(int account, int orderStatus) {
      return homemakingPackageMapper.getOrderList(account,orderStatus);
    }
}