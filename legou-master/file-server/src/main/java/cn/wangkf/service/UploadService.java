package cn.wangkf.service;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public interface UploadService {
    /**
     * 文件上传
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}
