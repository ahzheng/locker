package com.popdue.locker.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GlobalLock {

    /**
     * The name of the lock, it is the identifier of the lock,
     * it is not recommended to use the name of the default lock
     */
    String name() default "";

    /**
     * Lock value
     */
    String value() default "";

    /**
     * Lock hold time
     */
    int duration() default 10;

    /**
     * The number of concurrent locks supported.
     * Concurrency is not supported by default
     */
    int parallel() default -1;

    Class<? extends RuntimeException> exception() default RuntimeException.class;

}
