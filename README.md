# springboot+redis实现分布式锁locker 

redis实现分布式全局锁，支持注解和并发，以及全局共同使用。

使用时在pom.xml引入:
```
<dependency>
    <groupId>com.popdue.locker</groupId>
    <artifactId>spring-boot-starter-popdue-locker</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 在项目的application.yml中配制redis连接：
```
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```
    
#### 使用方式示例，在方式上注释：
```
@GlobalLock(parallel = 3)
public String get(String name) throws Exception {
   String data = "Get获得名称：" + name;
   return data;
}
```

#### 注解参数说明，默认可以不传入参数：
* name：锁的名称，实现多个地方持有相同锁，必须设置相同名称；
* value：锁的值，建议默认生成，避免误操作其它的锁，注意实现多地方持相同锁，锁的值也必须设置相同；
* duration：锁的保持时间，操作未完成，锁过期，单位是秒，默认10秒；
* parallel：锁的并发，即锁在同一时间允许排队的数量，按先进先出原则排队，默认不支持并发锁；
* exception：锁的异常类，必须继承RuntimeException，定义自己的异常；


注意默认使用的Spring Boot版本是2.4.6，修改版本的在项目的pom.xml中的spring-boot.version指定就可以：
```
<spring-boot.version>2.4.6</spring-boot.version>
```
修改后重新打包就可以了。


