package com.cool.server.handler;

import com.cool.common.constant.MessageConstant;
import com.cool.common.exception.BusinessException;
import com.cool.pojo.Result;
import com.cool.server.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理系统异常（兜底）
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        String msg = e.getMessage() != null ? e.getMessage() : MessageConstant.FAIL;
        return Result.fail(500, msg); // 替换Map为Result
    }

    // 处理业务运行时异常
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        String msg = e.getMessage() != null ? e.getMessage() : "请求失败";
        return Result.fail(400, msg); // 替换Map为Result
    }

    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数校验异常: ", e);
        String msg = e.getBindingResult().getFieldError() != null ?
                e.getBindingResult().getFieldError().getDefaultMessage() : MessageConstant.PARAM_ERROR;
        return Result.fail(400, msg); // 替换Map为Result
    }


}
