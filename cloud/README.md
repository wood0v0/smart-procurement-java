# Procurement Cloud（Spring Cloud 模块）

给采购后端加的最小 Spring Cloud 演示：

- `eureka-server`：服务注册中心，端口 `8761`
- `api-gateway`：Spring Cloud Gateway，端口 `8081`，把 `/api/**` 转发到 `http://localhost:8080`

## 启动顺序

```bash
# 1. 启动采购后端（端口 8080）
cd ..
mvn spring-boot:run

# 2. 启动 Eureka（端口 8761）
cd cloud/eureka-server
mvn spring-boot:run

# 3. 启动网关（端口 8081）
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

- 网关目前用静态 URI 指向本地后端，便于单机演示。
- 如果要把采购后端注册到 Eureka，可在后端加入 `spring-cloud-starter-netflix-eureka-client`，并把网关路由改成 `lb://procurement-backend`。
- 这是作品集用的最小模块，不是生产级网关（未做限流、鉴权、灰度）。
