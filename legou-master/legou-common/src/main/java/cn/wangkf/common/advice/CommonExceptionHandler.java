package cn.wangkf.common.advice;

import cn.wangkf.common.enums.ExceptionEnum;
import cn.wangkf.common.exception.LgException;
import cn.wangkf.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class CommonExceptionHandler {

    /*@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }*/

    @ExceptionHandler(LgException.class)
    public ResponseEntity<String> handleException(LgException e){
        ExceptionEnum em  = e.getExceptionEnum();
        return ResponseEntity.status(em.getCode()).body(em.getMsg());
    }

}
