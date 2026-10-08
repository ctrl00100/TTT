package com.region.lis.handler;

import com.region.lis.common.BusinessException;
import com.region.lis.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 分层捕获 → 统一返回 Result → 自动打日志
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常（Service 层主动抛出） */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusiness(BusinessException e) {
        log.warn("业务异常 code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 缺少请求参数（@RequestParam 没传） */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("参数缺失：{}", e.getParameterName());
        return Result.fail(400, "参数缺失：" + e.getParameterName());
    }

    /** @Valid @RequestBody 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", msg);
        return Result.fail(400, msg);
    }

    /** @Validated @RequestParam 校验失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraint(ConstraintViolationException e) {
        log.warn("参数约束失败：{}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /** 数据库唯一约束/外键约束 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<String> handleSQL(DataIntegrityViolationException e) {
        log.error("数据库约束违反：", e);
        return Result.fail(500, "数据操作失败：可能存在重复或关联数据");
    }

    /** 无权限 */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDenied(AccessDeniedException e) {
        log.warn("访问被拒绝：{}", e.getMessage());
        return Result.fail(403, "无权限访问");
    }

    /** 兜底异常（不暴露内部信息） */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}