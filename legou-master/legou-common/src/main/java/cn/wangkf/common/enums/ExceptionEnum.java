package cn.wangkf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnum {

    PARENT_ID_CANNOT_BE_NULL(400, "分类父ID不能为空！"),
    BRAND_NOT_FOUND(400, "查询不到任何品牌！"),
    BRAND_ADD_FAIL(500, "新增品牌失败！"),
    FILE_UPLOAD_FAIL(500, "文件上传失败！"),
    ;

    private int code;
    private String msg;


}
