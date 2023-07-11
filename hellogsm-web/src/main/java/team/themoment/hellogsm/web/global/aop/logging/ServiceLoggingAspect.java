package team.themoment.hellogsm.web.global.aop.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void onRequest() {
    }


    @Around("onRequest()")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.debug("At {}#{} [Pre] Parameters: {}", className, methodName, params(proceedingJoinPoint));

        Object result = proceedingJoinPoint.proceed();

        log.debug("At {}#{} [Post] Retrun: {}", className, methodName, result);
        return result;
    }

    private ArrayList<Object> params(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        return new ArrayList<>(Arrays.asList(arguments));
    }
}
