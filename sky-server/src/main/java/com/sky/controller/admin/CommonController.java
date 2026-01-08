package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    @ApiOperation("文件上传")

    public Result<String> upload(MultipartFile file){
        try {
            String originalFilename = file.getOriginalFilename();
            //获取文件后缀
            originalFilename=originalFilename.substring(originalFilename.lastIndexOf("."));
            //生成新的文件名
           String uuid=UUID.randomUUID().toString()+originalFilename;
            String upload = aliOssUtil.upload(file.getBytes(), uuid);
            return Result.success(upload);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e.getMessage());
        }
        return Result.error(MessageConstant.UPLOAD_FAILED );
    }
}
