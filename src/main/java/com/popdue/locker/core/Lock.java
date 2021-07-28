package com.popdue.locker.core;

import com.popdue.locker.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Lock implements Locker {
    // 锁并发使用数量
    protected static Map<String, LatchData> mapParallel = new ConcurrentHashMap<>();

    /**
     * 并发锁
     */
    protected synchronized void parallel(LockData data) throws Exception {
        // 暂时保证一个应用并发[增加MQ后改成全局重新抢锁]
        if (1 == data.getParallel() || -1 == data.getParallel() || !mapParallel.containsKey(data.getKey())) {
            throwException(data.getException(), "锁已被使用");
        }

        // 并发记录
        LatchData latchData;
        if (!mapParallel.containsKey(data.getKey())) {
            latchData = new LatchData(data.getName(), data.getValue(), 1, data.getDuration());
            mapParallel.put(data.getKey(), latchData);
        }

        latchData = mapParallel.get(data.getKey());
        int quantity = latchData.getQuantity() + 1;
        if (quantity > data.getParallel()) {
            throwException(data.getException(), "锁的并发超出范围" );
        }

        latchData.setQuantity(quantity);
    }

    /**
     * 并发等待重新抢
     */
    protected void await(LockData data) throws Exception {
        if (!mapParallel.containsKey(data.getKey())) {
            throwException(data.getException(), "锁的等待不存在");
        }

        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        LatchData latchData = mapParallel.get(data.getKey());
        CountDownLatch latch = new CountDownLatch(1);
        latchData.setLatch(latch);
        reentrantLock.unlock();

        // 开始线程阻塞
        latch.await();

        lock(data);
    }

    /**
     * 删除
     */
    protected synchronized CountDownLatch delete(LockData data) throws Exception{
        // 并发锁不存在
        LatchData latchData = mapParallel.get(data.getKey());
        if (null == latchData) {
            return null;
        }

        // 更新信息|取消等待，继续抢锁
        int quantity = latchData.getQuantity() - 1;

        if (1 < quantity) {
            latchData.setQuantity(quantity);
        }

        if (2 > quantity) {
            mapParallel.remove(data.getKey());
        }

        return latchData.getLatchRemove();
    }

    public synchronized CountDownLatch delete(String name,Class<? extends RuntimeException> exception) throws Exception {
        String key = prefix(name);
        // 并发锁不存在
        LatchData latchData = mapParallel.get(key);
        if (null == latchData) {
            return null;
        }

        // 更新信息|取消等待，继续抢锁
        int quantity = latchData.getQuantity() - 1;

        if (1 < quantity) {
            latchData.setQuantity(quantity);
        }

        if (2 > quantity) {
            mapParallel.remove(key);
        }

        return latchData.getLatchRemove();
    }

    /**
     * 保持前缀
     */
    public String prefix(String name) {
        return LockEnum.PREFIX.value() + md5(name);
    }

    /**
     * UUID生成锁的值
     */
    protected String uuid() {
        return UUID.randomUUID().toString();
    }

    protected void throwException(Class<?> clazz, Object... data) throws Exception {
        if (null == data || 0 == data.length) {
            data = new Object[]{"分布式锁异常"};
        }

        if (null == clazz) {
            throw new LockException(data[0].toString());
        }

        // 生成构造参数的类型
        Class<?>[] paramType = new Class<?>[data.length];
        int i = 0;
        for (Object item : data) {
            paramType[i] = item.getClass();
            i++;
        }
        Object exception = clazz.getDeclaredConstructor(paramType).newInstance(data);

        if (!(exception instanceof RuntimeException)) {
            throw new LockException(data[0].toString());
        }

        throw (RuntimeException) exception;
    }

    protected void throwException(Object... data) throws Exception {
        throwException(null, data);
    }

    public String md5(String data) {
        if (data.isEmpty()) {
            return "";
        }

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                sb.append(temp);
            }
            data = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return data;
    }
}
