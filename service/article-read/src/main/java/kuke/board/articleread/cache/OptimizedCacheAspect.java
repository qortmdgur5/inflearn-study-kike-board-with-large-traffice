package kuke.board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OptimizedCacheAspect {
    private final OptimizedCacheManager optimizedCacheManager;

    @Around("@annotation(OptimizedCachable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OptimizedCachable cachable = findAnnotation(joinPoint);
        return optimizedCacheManager.process(
                cachable.type(),
                cachable.ttlSeconds(),
                joinPoint.getArgs(),
                findReturnType(joinPoint),
                joinPoint::proceed
        );
    }

    private OptimizedCachable findAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod().getAnnotation(OptimizedCachable.class);
    }

    private Class<?> findReturnType(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }
}
