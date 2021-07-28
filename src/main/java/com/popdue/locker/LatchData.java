package com.popdue.locker;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class LatchData {

    private String name;

    private String value;

    private int quantity;

    private List<CountDownLatch>  latch = new Vector<>();

    private long startTime;

    private long endTime;

    public LatchData(String name, String value, int quantity) {
        this.name = name;
        this.value = value;
        this.quantity = quantity;
        this.startTime = System.currentTimeMillis();
    }

    public LatchData(String name, String value, int quantity, int duration) {
        this.name = name;
        this.value = value;
        this.quantity = quantity;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + duration;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<CountDownLatch> getLatch() {
        return latch;
    }

    public CountDownLatch getLatchRemove() {
        if(0 == latch.size()){
            return null;
        }
        return latch.remove(0);
    }

    public void setLatch(List<CountDownLatch> latch) {
        this.latch = latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch.add(latch);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

}
