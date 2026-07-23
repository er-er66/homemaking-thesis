package com.example.homemaking.controller.HomemakingPackageController;

import com.example.homemaking.dto.HomemakingPackageDTO;
import com.example.homemaking.entity.HomemakingPackage;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.HomemakingPackageService;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController//接收前端请求 并自动返回JSON响应
@RequestMapping("admin/package")
@Slf4j
public class HomemakingPackageController {

    @Autowired
    private HomemakingPackageService homemakingPackageService;
    @RequestMapping("/list")
    public Result<List<HomemakingPackage>> list(){

      List<HomemakingPackage> list = homemakingPackageService.list();
        return Result.success(list);
    }
    /**
     * 创建套餐
     * @param homemakingPackageDTO
     * @return
     */
    @PostMapping("create")
    public Result< String> create(@RequestBody HomemakingPackageDTO homemakingPackageDTO){
        log.info("create package:{}", homemakingPackageDTO);
        homemakingPackageService.create(homemakingPackageDTO);
        return Result.success("create success");
    }
    @PutMapping("/update")
    public Result<String> updatePackage(@RequestBody HomemakingPackageDTO homemakingPackageDTO){
        log.info("update package:{}", homemakingPackageDTO);
      int count = homemakingPackageService.updatePackage(homemakingPackageDTO);
      if (count>0){
          return Result.success("update success");
      }
        return Result.error("update failed");
    }
}
