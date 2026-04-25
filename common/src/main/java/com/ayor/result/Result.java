package com.ayor.result;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class Result<T> {

    //返回码
    private Integer code;

    //返回消息
    private String message;

    //返回数据
    private T data;

    /**
     * 默认构造方法。
     */
    public Result() {
    }

    /**
     * 创建一个基础响应对象，仅在数据非空时写入返回数据。
     *
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 基础响应对象
     */
    private static <T> Result<T> build(T data) {
        Result<T> result = new Result<>();
        if (data != null)
            result.setData(data);
        return result;
    }

    /**
     * 根据业务返回码构建响应对象。
     *
     * @param body 返回数据
     * @param resultCodeEnum 返回码枚举
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = build(body);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }


    /**
     * 创建成功响应，并携带返回数据。
     *
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    /**
     * 创建不携带数据的成功响应。
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    /**
     * 创建默认失败响应。
     *
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail() {
        return build(null, ResultCodeEnum.FAIL);
    }

    /**
     * 按指定返回码和消息创建失败响应。
     *
     * @param code 返回码
     * @param message 返回消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = build(null);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 将响应对象序列化为 JSON 字符串。
     *
     * @return JSON 字符串
     */
    public String toJSONString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

    /**
     * 执行消息处理逻辑，并根据返回消息是否为空决定返回成功或失败结果。
     *
     * @param action 返回消息的计算逻辑
     * @return 响应对象
     */
    public static Result<Void> messageHandler(Supplier<String> action) {
        String message = action.get();
        return message == null ? Result.ok() : Result.fail(ResultCodeEnum.FAIL.getCode(), message);
    }

    /**
     * 执行数据处理逻辑，并根据返回数据是否为空决定返回成功或失败结果。
     *
     * @param action 返回数据的计算逻辑
     * @param message 数据为空时返回的消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Result<T> dataMessageHandler(Supplier<T> action, String message) {
        T data = action.get();
        return data == null ? Result.fail(ResultCodeEnum.FAIL.getCode(), message) : Result.ok(data);
    }

}
