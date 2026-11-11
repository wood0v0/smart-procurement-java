CREATE TABLE audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_id BIGINT,
  action VARCHAR(30) NOT NULL,
  actor VARCHAR(100),
  detail VARCHAR(2000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_request ON audit_logs (request_id);

CREATE TABLE idempotency_keys (
  key_value VARCHAR(100) PRIMARY KEY,
  request_id BIGINT NOT NULL,
  action VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
