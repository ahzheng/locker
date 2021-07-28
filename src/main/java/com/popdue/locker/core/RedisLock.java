package com.popdue.locker.core;

import com.popdue.locker.LatchData;
import com.popdue.locker.LockData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RedisLock extends Lock {

    @Resource
    protected RedisTemplate redisTemplate;

    /**
     * 加锁
     */
    @Override
    public void lock(LockData data) throws Exception {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        // 锁的值
        if (null == data.getName() || data.getName().isEmpty()) {
            throwException("锁的命名不存在");
        }

        // 锁的key加密
        if (null == data.getKey() || data.getKey().isEmpty()) {
            data.setKey(prefix(data.getName()));
        }

        // 锁的值
        if (null == data.getValue() || data.getValue().isEmpty()) {
            String value = null;
            // 同步锁的值
            LatchData latchData = mapParallel.get(data.getKey());
            if (null != latchData && null != latchData.getValue()) {
                value = latchData.getValue();
            }

            if (null == latchData || null == latchData.getValue()) {
                value = uuid();
            }

            data.setValue(value);
        }

        // 尝试抢锁
        boolean result = false;
        // 设置过期时间锁，推荐使用
        if (-1 != data.getDuration() && 0 < data.getDuration()) {
            result = redisTemplate.opsForValue().setIfAbsent(data.getKey(), data.getValue(), data.getDuration(), TimeUnit.SECONDS);
        }

        // 设置永久时间锁，不推荐使用，可能造成死锁
        if (-1 == data.getDuration() || 0 >= data.getDuration()) {
            result = redisTemplate.opsForValue().setIfAbsent(data.getKey(), data.getValue());
        }

        // 暂时保证一个应用并发[增加MQ后改成全局重新抢锁]
        if (result && !mapParallel.containsKey(data.getKey())) {
            LatchData latchData = new LatchData(data.getName(), data.getValue(), 1, data.getDuration());
            mapParallel.put(data.getKey(), latchData);
        }

        // 并发锁的记录
        if (!result) {
            parallel(data);
        }
        reentrantLock.unlock();

        // 等待重新抢锁
        if (!result) {
            await(data);
            return;
        }

        // 防止可能的集群出现故障
        Object value = redisTemplate.opsForValue().get(data.getKey());
        if (!(value instanceof String)) {
            lock(data);
            return;
        }

        // 防止可能集群故障后锁被其它人使用
        if (!data.getValue().equals(value.toString())) {
            lock(data);
            return;
        }

        keep(data);
    }

    @Override
    public void lock() throws Exception {
    }

    /**
     * 解锁
     */
    @Override
    public void unlock(LockData data) throws Exception {
        CountDownLatch latch = delete(data);
        if (null == latch) {
            return;
        }

        latch.countDown();
    }


    @Override
    public void unlock() throws Exception {
    }


    /**
     * 删除
     */
    @Override
    public synchronized CountDownLatch delete(String name, Class<? extends RuntimeException> exception) throws Exception {
        String key = prefix(name);
        // 解锁即删除
        boolean result = redisTemplate.delete(key);
        if (!result) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof String) {
                throwException(exception, "锁的缓存清理出错了");
            }
        }

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
     * 删除
     */
    @Override
    public synchronized CountDownLatch delete(LockData data) throws Exception {
        return delete(data.getName(), data.getException());
    }

    /**
     * 保持锁，直到释放
     */
    protected void keep(LockData data) {
        Thread thread = new Thread(() -> {
            long temp = data.getDuration() / 3;
            if (1 > data.getDuration()) {
                return;
            }

            Object value = redisTemplate.opsForValue().get(data.getKey());
            if (null != data || !(value instanceof String) || !data.getValue().equals(value.toString())) {
                return;
            }

            // 设置睡眠时间
            try {
                Thread.sleep(temp * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 再次保持
            if (redisTemplate.expire(data.getKey(), data.getDuration(), TimeUnit.SECONDS)) {
                keep(data);
            }
        });

        thread.start();
    }

}
