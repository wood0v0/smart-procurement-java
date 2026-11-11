# Smart Procurement Backend（Java / Spring Boot）

采购需求 → 结构化 → 多供应商比价 → PR → 审批 → PO 的后端 MVP。

## 技术栈

- Java 21
- Spring Boot 3.5
- MyBatis（注解 Mapper）
- MySQL 8（生产）/ H2（测试，MySQL 兼容模式）
- Flyway（数据库版本管理）
- Springdoc OpenAPI（Swagger UI）
- JUnit 5 + MockMvc

## 快速开始

### 方式一：本地 MySQL

```bash
mysql -u root -p -e "create database procurement character set utf8mb4"
export DB_URL='jdbc:mysql://localhost:3306/procurement?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8'
export DB_USER=root
export DB_PASSWORD=your_password
mvn spring-boot:run
```

访问：

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- 健康检查：`http://localhost:8080/actuator/health`

### 方式二：Docker Compose

```bash
docker compose up -d --build
```

## API

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/v1/procurement/analyze` | 自然语言需求 → 比价 + PR |
| GET | `/api/v1/procurement/requests` | PR 列表 |
| GET | `/api/v1/procurement/requests/{id}` | PR 详情 |
| POST | `/api/v1/procurement/requests/{id}/approve` | 审批（`X-Role: approver`） |
| GET | `/api/v1/catalog/products?kind=laptop` | 商品目录 |
| GET | `/api/v1/catalog/suppliers` | 供应商 |

示例：

```bash
curl -X POST http://localhost:8080/api/v1/procurement/analyze \
  -H 'Content-Type: application/json' \
  -d '{"text":"采购 2 台联想笔记本，预算 10000"}'
```

```bash
curl -X POST http://localhost:8080/api/v1/procurement/requests/1/approve \
  -H 'Content-Type: application/json' \
  -H 'X-Role: approver' \
  -d '{"approver":"王经理","action":"APPROVE","note":"同意"}'
```

## 设计点

- 需求解析：数量、品类、品牌、预算、交期；常见错字纠正。
- 比价：按品类召回有效报价，价格/预算/交期/评分/库存加权打分。
- 审批：只有 `approver` / `director` 可审批；重复审批返回 409。
- 单据：`PR-yyyyMMdd-####`、`PO-yyyyMMdd-####`。
- 报价有效期：过期报价不参与比价。

## 测试

```bash
mvn test
```

覆盖：

- 需求解析与错字纠正
- 分析 → PR → 审批 → PO 全流程
- 采购员越权审批 403
- OpenAPI 文档

## 局限

- 角色通过 `X-Role` 请求头模拟，未接 SSO/真实用户体系。
- 商品/报价为演示数据，未接真实供应商或 ERP。
- 未接 LLM，需求解析为规则实现；后续可替换为模型抽取。

## Spring Cloud 模块

`cloud/` 下提供一个可运行的 Spring Cloud 演示：

- Eureka 服务注册中心（8761）
- Spring Cloud Gateway（8081），把 `/api/**` 转发到采购后端（8080）

启动方式和验证命令见 [cloud/README.md](cloud/README.md)。

