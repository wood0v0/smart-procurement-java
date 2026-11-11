CREATE TABLE suppliers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  rating DECIMAL(3, 1) NOT NULL DEFAULT 4.5,
  shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
  delivery_days INT NOT NULL DEFAULT 3
);

CREATE TABLE products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  kind VARCHAR(20) NOT NULL,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(120) NOT NULL,
  specs VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uk_products_kind_brand_model UNIQUE (kind, brand, model)
);

CREATE TABLE quotes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  unit_price DECIMAL(12, 2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  delivery_days INT NOT NULL DEFAULT 3,
  source VARCHAR(100),
  valid_until DATE,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_quotes_product_supplier UNIQUE (product_id, supplier_id)
);

CREATE TABLE purchase_requests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pr_no VARCHAR(32),
  raw_text TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  total DECIMAL(14, 2) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE request_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id BIGINT NOT NULL,
  product_id BIGINT,
  kind VARCHAR(20) NOT NULL,
  brand VARCHAR(50),
  model VARCHAR(120),
  quantity INT NOT NULL,
  unit_price DECIMAL(12, 2) NOT NULL,
  supplier VARCHAR(100) NOT NULL,
  total DECIMAL(14, 2) NOT NULL
);

CREATE INDEX idx_request_items_request ON request_items (request_id);

CREATE TABLE purchase_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  po_no VARCHAR(32) NOT NULL UNIQUE,
  request_id BIGINT NOT NULL,
  supplier VARCHAR(100) NOT NULL,
  total DECIMAL(14, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE approvals (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id BIGINT NOT NULL,
  approver VARCHAR(100) NOT NULL,
  action VARCHAR(20) NOT NULL,
  note VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_approvals_request ON approvals (request_id);
