# Procurement Cloud（Spring Cloud 模块）

给采购后端加的最小 Spring Cloud 演示：

- `eureka-server`：服务注册中心，端口 `8761`
- `api-gateway`：Spring Cloud Gateway，端口 `8081`，通过 `lb://procurement-backend` 负载均衡到采购后端
- 采购后端设置 `EUREKA_ENABLED=true` 后注册为 `procurement-backend`

## 启动顺序

```bash
# 1. 启动采购后端（8080，注册到 Eureka）
cd ..
EUREKA_ENABLED=true mvn spring-boot:run

# 2. 启动 Eureka（8761）
cd cloud/eureka-server
mvn spring-boot:run

# 3. 启动网关（8081）
cd cloud/api-gateway
mvn spring-boot:run
```

## 验证

```bash
# Eureka 控制台
open http://localhost:8761

# 通过网关访问采购后端
curl -X POST http://localhost:8081/api/v1/procurement/analyze \
  -H 'Content-Type: application/json' \
  -d '{"text":"采购 2 台联想笔记本，预算 10000"}'
```

## 说明

- 网关通过 Eureka 的 `lb://procurement-backend` 路由，不再写死 `localhost:8080`。
- 采购后端默认不注册；启动时加 `EUREKA_ENABLED=true`。
- 这是作品集用的最小模块，不是生产级网关（未做限流、鉴权、灰度、熔断、配置中心）。
