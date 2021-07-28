package com.popdue.locker;

public class LockData {

    // 锁的名称
    private String name;

    // 锁的值
    private String value;

    // 锁的保持时长，可防止发生故障时死锁
    private int duration;

    // 锁的支持并发数，超出并发数直接返回
    private volatile int parallel;

    // 锁的异常类
    private Class<? extends RuntimeException> exception;

    // 锁的KEY
    private String key;

    public LockData(String name, String value, int duration, int parallel,Class<? extends RuntimeException> exception) {
        this.name = name;
        this.value = value;
        this.duration = duration;
        this.parallel = parallel;
        this.exception = exception;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getParallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    public Class<? extends RuntimeException> getException() {
        return exception;
    }

    public void setException(Class<? extends RuntimeException> exception) {
        this.exception = exception;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
