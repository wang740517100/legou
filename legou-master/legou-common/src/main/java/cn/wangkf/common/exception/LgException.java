package cn.wangkf.common.exception;

import cn.wangkf.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LgException extends RuntimeException {

    private ExceptionEnum exceptionEnum;


}
