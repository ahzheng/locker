package com.popdue.locker;

public enum LockEnum {
    PREFIX("GL-");
    private String name;
    private String value;

    LockEnum(String value){
        this.value = value;
    }

    public String value(){
        return value;
    }
}
