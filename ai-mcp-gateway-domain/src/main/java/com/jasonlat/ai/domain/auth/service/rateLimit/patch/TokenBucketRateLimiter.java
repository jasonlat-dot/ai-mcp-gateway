package com.jasonlat.ai.domain.auth.service.rateLimit.patch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单机令牌桶限流器（自研实现，替代Guava @Beta标记的RateLimiter）
 * 原理：以固定速率往桶内生成令牌，请求进来需要先获取令牌，拿到令牌才允许放行，无令牌则触发限流
 * 线程安全：使用AtomicLong + CAS自旋保证并发下令牌增减原子性，volatile保证时间戳可见性
 */
public class TokenBucketRateLimiter {
    /**
     * 每秒往令牌桶生成的令牌数量（限流速率）
     * 例：permitsPerSecond=10 → 每秒生成10个令牌，QPS上限10
     */
    private final double permitsPerSecond;

    /**
     * 令牌桶最大容量，最多存放多少个令牌
     * 取值为向上取整后的每秒速率，避免令牌溢出堆积过多
     */
    private final long maxPermits;

    /**
     * 当前桶内剩余可用令牌数量
     * AtomicLong保证多线程并发修改原子性，避免线程安全问题
     */
    private final AtomicLong storedTokens;

    /**
     * 上一次补充令牌的纳秒时间戳
     * volatile修饰：保证多线程之间变量修改立即可见，防止缓存行导致时间戳读取滞后
     */
    private volatile long lastRefillTime;

    /**
     * 构造方法：初始化令牌桶
     * @param permitsPerSecond 每秒生成令牌数量（限流QPS）
     */
    public TokenBucketRateLimiter(double permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond; // 10 -> 表示10个令牌/秒
        // 桶容量向上取整，比如每秒3.2个令牌，桶最大容量为4
        this.maxPermits = (long) Math.ceil(permitsPerSecond);
        // 初始化时令牌桶放满令牌，应对瞬间突发流量
        this.storedTokens = new AtomicLong(maxPermits);
        // 初始化上次填充时间为当前系统纳秒时间
        this.lastRefillTime = System.nanoTime();
    }

    /**
     * 尝试获取 1 个令牌
     * 等价 Guava RateLimiter.tryAcquire() 方法
     * @return true: 获取令牌成功（放行）; false: 令牌不足（触发限流）
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取指定数量令牌
     * @param permits 需要获取的令牌个数
     * @return true: 获取成功放行; false: 令牌不足限流
     */
    public boolean tryAcquire(int permits) {
        // 传入负数/0个令牌，直接默认放行（非法参数容错）
        if (permits <= 0) {
            return true;
        }
        // 先根据时间差，补充这段时间内应该生成的新令牌
        refillTokens();

        long currentTokenNum;
        // CAS自旋循环：并发扣减令牌，保证线程安全
        do {
            // 获取当前剩余令牌
            currentTokenNum = storedTokens.get();
            // 剩余令牌不够本次申请，直接返回限流
            if (currentTokenNum < permits) {
                return false;
            }
            // CAS尝试扣减令牌：当前值等于currentTokenNum，则减去permits并更新
            // 若期间被其他线程修改，CAS失败，进入下一轮循环重试
        } while (!storedTokens.compareAndSet(currentTokenNum, currentTokenNum - permits));

        // 令牌扣减成功，请求放行
        return true;
    }

    /**
     * 核心填充令牌逻辑：根据时间间隔，计算新增令牌并填充到桶内
     */
    private synchronized void refillTokens() {
        // 获取当前系统纳秒时间
        long now = System.nanoTime();
        // 拿到上次填充令牌的时间
        long oldTime = lastRefillTime;
        // 当前时间小于等于上次填充时间，说明已经填充过，直接跳过
        if (now <= oldTime) {
            return;
        }

        // 1、计算两次填充间隔的总秒数
        long nanoDiff = now - oldTime;
        double seconds = TimeUnit.NANOSECONDS.toNanos(nanoDiff) / 1e9;

        // 2、计算这段时间应该新增多少个令牌
        long addPermits = (long) (seconds * permitsPerSecond);
        // 时间太短，新增令牌不足1个，无需填充
        if (addPermits <= 0) {
            return;
        }

        // 3、原有令牌 + 新增令牌，不能超过桶最大容量
        long currentStore = storedTokens.get();
        long newTotalPermits = Math.min(currentStore + addPermits, maxPermits);
        // 更新桶内最新令牌总数
        storedTokens.set(newTotalPermits);
        // 更新本次填充时间戳，作为下一次计算的起点
        lastRefillTime = now;
    }

}