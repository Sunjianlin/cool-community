package com.cool.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一响应体（兼容原有异常处理器的code/message字段）
 */
@Data
public class Result<T> {
    // 与现有异常处理器的Map字段完全对齐
    private Integer code;
    private String message;
    private T data;

    // ========== 静态构造方法（简化封装） ==========
    // 成功：带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200); // 新增成功码，与异常码区分
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功：无数据
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败：自定义code+message
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // 失败：复用现有常量（如果有）
    public static <T> Result<T> fail(String message) {
        return fail(400, message); // 默认400
    }
}
