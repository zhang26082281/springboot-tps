package com.sunfield.springboot.tps.aspect;

import com.sunfield.springboot.tps.annotation.RateLimit;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Scope
@Aspect
public class RateLimitAspect {
    private ConcurrentHashMap<String, RateLimiter> map = new ConcurrentHashMap<>();
    private RateLimiter rateLimiter;
    @Resource
    private HttpServletResponse response;

    @Pointcut("@annotation(com.sunfield.springboot.tps.annotation.RateLimit)")
    public void serviceLimit() {
    }

    @Around("serviceLimit()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        //获取拦截的方法名
        Signature sig = joinPoint.getSignature();
        //获取拦截的方法名
        MethodSignature msig = (MethodSignature) sig;
        //返回被织入增加处理目标对象
        Object target = joinPoint.getTarget();
        //为了获取注解信息
        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        //获取注解信息
        RateLimit annotation = currentMethod.getAnnotation(RateLimit.class);
        //获取注解每秒加入桶中的token
        double limit = annotation.limit();
        // 注解所在方法名区分不同的限流策略
        String functionName = msig.getName();

        //获取rateLimiter
        if (map.containsKey(functionName)) {
            rateLimiter = map.get(functionName);
        } else {
            map.put(functionName, RateLimiter.create(limit));
            rateLimiter = map.get(functionName);
        }

        if (rateLimiter.tryAcquire()) {
            //执行方法
            result = joinPoint.proceed();
        } else {
            System.err.println("请过过于频繁，请稍后再试");
        }
        return result;
    }
}