package com.popdue.locker;

import com.popdue.locker.LockData;

public interface Locker {

    void lock(LockData entity) throws Exception;

    void lock() throws Exception;

    void unlock(LockData entity) throws Exception;

    void unlock() throws Exception;
}
