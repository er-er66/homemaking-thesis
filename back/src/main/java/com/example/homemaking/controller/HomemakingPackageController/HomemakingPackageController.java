package com.example.homemaking.controller.HomemakingPackageController;

import com.example.homemaking.dto.HomemakingPackageDTO;
import com.example.homemaking.entity.HomemakingPackage;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.HomemakingPackageService;
import com.example.homemaking.util.PageUtil;
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
    /**
     * 套餐列表
     * <p>不传 pageNum/pageSize：返回数组（管理端、注册页沿用旧行为）</p>
     * <p>传 pageNum/pageSize：返回 {total,pageNum,pageSize,records} 分页体（用户首页）</p>
     * <p>管理端不传 status → 不过滤，上下架都要看到；首页传 status=0 只看上架。</p>
     */
    @RequestMapping("/list")
    public Result<?> list(@RequestParam(required = false) Integer pageNum,
                          @RequestParam(required = false) Integer pageSize,
                          @RequestParam(required = false) Integer serviceType,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String packageName) {
        if (!PageUtil.enabled(pageNum, pageSize)) {
            List<HomemakingPackage> list = homemakingPackageService.list();
            return Result.success(list);
        }
        return Result.success(homemakingPackageService.page(pageNum, pageSize, serviceType, status, packageName));
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

    /**
     * 套餐上架/下架
     * <p>不传 status：0(上架) ↔ 1(下架) 切换 —— 管理端列表的「上架/下架」按钮走这条</p>
     * <p>传 status=0 或 1：显式设置，幂等，适合后续做批量上下架</p>
     *
     * @param id     套餐ID
     * @param status 可选，0上架 1下架；不传表示切换
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id,
                                       @RequestParam(required = false) Integer status) {
        log.info("套餐上架/下架，id={}, status={}", id, status);
        if (status != null && status != 0 && status != 1) {
            return Result.error("status 只能是 0(上架) 或 1(下架)");
        }
        int count = (status == null)
                ? homemakingPackageService.toggleStatus(id)
                : homemakingPackageService.updateStatus(id, status);
        if (count > 0) {
            return Result.success(status == null ? "状态切换成功" : "状态更新成功");
        }
        return Result.error("操作失败，套餐不存在或已删除");
    }

}
