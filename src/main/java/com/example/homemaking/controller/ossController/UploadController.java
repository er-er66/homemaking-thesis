package com.example.homemaking.controller.ossController;

import com.example.homemaking.result.Result;
import com.example.homemaking.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class UploadController {

    @Autowired
    private OssUtil ossUtil;

    @PostMapping("/admin/upload-avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        try {
            String url = ossUtil.upload(file);
            log.info("头像上传成功：{}", url);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return Result.success(data);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("上传失败");
        }
    }
}