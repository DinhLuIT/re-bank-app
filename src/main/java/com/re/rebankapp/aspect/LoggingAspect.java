package com.re.rebankapp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Lớp Aspect này đóng vai trò như một "camera giám sát" tự động ghi nhận 
 * thời gian chạy (Execution Time) của tất cả các hàm trong tầng Service 
 * mà không cần phải chèn code tính giờ vào từng hàm.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * @Around là loại Aspect mạnh nhất. Nó cho phép chặn luồng chạy TRƯỚC và SAU khi hàm thực thi.
     * Cú pháp "execution(* com.re.rebankapp.service.*.*(..))" có nghĩa là:
     * Quét tất cả các hàm (bất kể kiểu trả về) nằm trong tất cả các class thuộc package service.
     */
    @Around("execution(* com.re.rebankapp.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Lấy tên class và tên hàm đang được gọi
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // 1. CHẠY HÀM THỰC SỰ: Lệnh proceed() sẽ cho phép luồng code đi tiếp vào trong Service
        Object result = joinPoint.proceed();

        // 2. TÍNH TOÁN THỜI GIAN
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 3. GHI LOG (In ra Terminal)
        log.info("[AUDIT] {}.{}() thực thi mất {} ms", className, methodName, executionTime);

        // 4. Trả về kết quả cho luồng chạy ban đầu
        return result;
    }
}
