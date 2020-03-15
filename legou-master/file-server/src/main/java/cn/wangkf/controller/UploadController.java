package cn.wangkf.controller;

import cn.wangkf.common.enums.ExceptionEnum;
import cn.wangkf.common.exception.LgException;
import cn.wangkf.service.impl.UploadServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private UploadServiceImpl uploadServiceImpl;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file")MultipartFile file){
        String url = this.uploadServiceImpl.upload(file);
        if(StringUtils.isBlank(url)){
            //url为空，证明上传失败
            throw new LgException(ExceptionEnum.FILE_UPLOAD_FAIL);
        }
        return ResponseEntity.ok(url);
    }
}
