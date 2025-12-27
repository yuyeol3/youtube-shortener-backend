package io.github.yuyeol.youtube_shortener;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    // 모든 컨트롤러 메소드 실행 시 감지
    @Around("restControllerMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
// RequestScope가 아닐 때(예: 테스트 코드, 내부 호출 등)를 대비한 방어 코드
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            // 웹 요청이 아니면 그냥 원래 로직 수행하고 끝냄 (로그 안 남김)
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();

        log.info("Request: [{} {}] - executed in {}ms",
                request.getMethod(),
                request.getRequestURI(),
                (end - start));

        return result;
    }
}