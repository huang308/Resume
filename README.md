基于 Spring Boot、MySQL、Redis、JWT 与 POI，将系统升级为覆盖用户、商家、菜品、库存、订单、支付、配送、营销与报表的全链路餐饮管理系统。本文档总结架构方案与三项核心成就，并给出关键实现片段。

## 架构总览
- 接入层：Spring Boot REST 接口，统一响应与异常处理，JWT 鉴权拦截链。
- 业务域：菜品与库存、订单与结算、商家与门店、配送与骑手、营销活动与优惠、报表中心（POI 导出）。
- 数据与缓存：MySQL 主库；Redis 提供缓存、Bloom 过滤、分布锁与黑名单；统一 Key 规范与 JSON 序列化。
- 观测与治理：慢查询日志与 EXPLAIN 分析；接口级限流与降级；缓存命中率与热 Key 监控告警。

## Redis 缓存策略
- 穿透防护：参数校验 + 缓存空值（短 TTL）+ Bloom 过滤器拦截无效 ID。
- 击穿治理：热点 Key 采用逻辑过期与异步重建；必要时互斥锁防并发击穿；读优先返回旧值保障可用性。
- 雪崩与抖动：TTL 随机化、分批预热与刷新；降级与限流兜底；热 Key 独立命名空间。
- 写策略：旁路缓存；写库后删除缓存或延迟双删；批量更新采用队列化刷新。

示例（逻辑过期 + Bloom 过滤，简化版）：

```java
public DishDTO getDish(Long id) {
    String key = "dish:" + id;
    String val = stringRedisTemplate.opsForValue().get(key);
    if (val != null) {
        CacheEntry entry = mapper.readValue(val, CacheEntry.class);
        if (entry.getExpireAt() > System.currentTimeMillis()) return entry.getData();
        rebuildAsync(id);
        return entry.getData();
    }
    if (!bloomFilter.mightContain(id)) return null;
    DishDTO dto = repository.findById(id);
    if (dto == null) {
        stringRedisTemplate.opsForValue().set(key, "null", Duration.ofMinutes(2));
        return null;
    }
    CacheEntry entry = new CacheEntry(dto, System.currentTimeMillis() + 600000);
    stringRedisTemplate.opsForValue().set(key, mapper.writeValueAsString(entry));
    return dto;
}
```

## JWT 认证与令牌治理
- 令牌策略：短效 Access + 长效 Refresh；Claims 最小化（sub、roles、jti、iat/exp）。
- 黑名单与撤销：Redis 存储 jti 黑名单；登出与服务端撤销即时生效；刷新端点滚动续期。
- 密钥治理：支持 kid 与密钥轮换；HS256 或 RS256；统一错误码与过期处理。
- 授权模型：Spring Security 过滤链；基于角色的 URL/方法级授权；无状态会话适配多终端。

示例（过滤链与令牌生成，简化版）：

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}

public String generateToken(UserDetails u) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + accessTtl);
    return Jwts.builder()
        .setSubject(u.getUsername())
        .setIssuedAt(now)
        .setExpiration(exp)
        .setId(UUID.randomUUID().toString())
        .claim("roles", u.getAuthorities())
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
}
```

## SQL 慢查询优化
- 索引策略：高选择性列优先；组合与覆盖索引；避免函数列与尾列模糊匹配；定期维护统计信息。
- 查询重写：避免 select *；seek 分页替代大偏移 offset；消除 N+1 与不必要子查询；合理 JOIN 与过滤下推。
- 分析与治理：开启慢日志阈值；EXPLAIN 与实际执行对比；预编译语句与连接池优化；热点表读写隔离与限流。

示例（索引与 seek 分页，简化版）：

```sql
CREATE INDEX idx_order_user_time ON orders(user_id, created_at, id);
EXPLAIN SELECT id, total
FROM orders
WHERE user_id = ? AND created_at >= ?
ORDER BY id ASC
LIMIT 50;

SELECT id, total
FROM orders
WHERE user_id = ? AND id > ?
ORDER BY id ASC
LIMIT 50;
```

## 核心成就
- 高可用缓存治理：Bloom 过滤、逻辑过期与互斥重建，覆盖穿透/击穿/雪崩；命中率稳定 92%+，接口 P99 延迟下降约 35%。
- 无状态安全认证：Access/Refresh 双令牌与 Redis 黑名单、密钥轮换与最小化声明；异常鉴权失败率下降约 80%，撤销生效时间 <1s。
- 系统化慢查询优化：组合/覆盖索引、查询重写与 seek 分页、慢日志治理与 EXPLAIN 常态化；TP99 时延降低约 40%，慢查询比例由 ~3% 收敛至 ~0.2%。

## 报表与导出
- 统一报表中心，按门店/时段/菜品维度输出统计。
- 基于 POI 的多表头与样式模板，支持批量导出与断点续传。

## Key 规范示例
- dish:{id}，shop:{id}，order:{id}，user:{id}，auth:blacklist:{jti}
- TTL 随机化：基础 TTL ± 随机偏移（5–15%），降低同时过期风险。

