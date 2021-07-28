package com.popdue.locker.aspect;

import com.popdue.locker.LockData;
import com.popdue.locker.annotation.GlobalLock;
import com.popdue.locker.core.RedisLock;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
@Order(0)
public class GlobalLockAspect extends LockAspect {

    @Resource
    private RedisLock redisLock;

    /**
     * 注解切入点
     */
    @Pointcut("@annotation(com.popdue.locker.annotation.GlobalLock)")
    public void pointCut() {
    }

    /**
     * 切入点前置，加锁
     */
    // @Before("pointCut()")
    public void before(JoinPoint joinPoint) throws Exception {
    }

    /**
     * 切入点后置，解锁即删除锁
     */
    // @After("pointCut()")
    public void after(JoinPoint joinPoint) throws Exception {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        GlobalLock globalLock = method.getAnnotation(GlobalLock.class);

        // 按类和方法生成锁的名称，不推荐生成锁的名称
        String name = globalLock.name();
        if (name.isEmpty()) {
            name = method.getDeclaringClass().getName() + "." + method.getName();
        }

        LockData data = new LockData(name, globalLock.value(), globalLock.duration(), globalLock.parallel(), globalLock.exception());
        // 尝试加锁
        redisLock.lock(data);
        reentrantLock.unlock();

        // 执行锁的内容
        Object result = proceedingJoinPoint.proceed();

        // 尝试解锁
        redisLock.unlock(data);

        return result;

    }

    public void afterReturning(JoinPoint joinPoint) {
    }

    @AfterThrowing("pointCut()")
    public void afterThrowing(JoinPoint joinPoint) throws Exception {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        GlobalLock globalLock = method.getAnnotation(GlobalLock.class);

        String name = globalLock.name();
        if (name.isEmpty()) {
            name = method.getDeclaringClass().getName() + "." + method.getName();
        }

        // 异常清理
        CountDownLatch latch = redisLock.delete(name, globalLock.exception());
        if(null == latch){
            return;
        }
        latch.countDown();
    }

}


