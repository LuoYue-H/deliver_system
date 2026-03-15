package org.example.deliver_system.controller;

import org.example.deliver_system.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "/uploads/";

    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传失败，请选择文件");
        }

        try {
            // 获取项目根路径
            String projectPath = new File("").getAbsolutePath();
            File uploadDir = new File(projectPath + UPLOAD_DIR);

            // 使用 mkdirs() 确保所有父目录都存在
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            File dest = new File(uploadDir.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);

            // 返回可访问的URL
            return Result.success(UPLOAD_DIR + newFilename);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}
