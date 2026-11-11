INSERT INTO suppliers (id, name, rating, shipping_fee, delivery_days) VALUES
  (1, '京东企业购', 4.8, 0, 2),
  (2, '苏宁企业购', 4.6, 20, 3),
  (3, '得力办公', 4.4, 15, 5),
  (4, '工品汇', 4.5, 10, 4);

INSERT INTO products (id, kind, brand, model, specs, active) VALUES
  (1, 'laptop', '联想', 'ThinkPad E14', '14英寸/i5/16G/512G', TRUE),
  (2, 'laptop', '戴尔', 'Latitude 3440', '14英寸/i5/16G/512G', TRUE),
  (3, 'monitor', '戴尔', 'U2422H', '23.8英寸/1920x1080', TRUE),
  (4, 'tablet', '苹果', 'iPad 10 (2022)', '10.9英寸/64G/WiFi', TRUE),
  (5, 'tool', '世达', '内六角扳手 9件', '1.5-10mm/9件套', TRUE),
  (6, 'safety', '霍尼韦尔', '防护手套', '丁腈/均码', TRUE);

INSERT INTO quotes (product_id, supplier_id, unit_price, stock, delivery_days, source, valid_until) VALUES
  (1, 1, 4599, 30, 2, '京东报价单', NULL),
  (1, 2, 4699, 20, 3, '苏宁报价单', NULL),
  (1, 3, 4799, 10, 5, '得力报价单', NULL),
  (2, 1, 4899, 15, 2, '京东报价单', NULL),
  (2, 2, 4799, 25, 3, '苏宁报价单', NULL),
  (3, 1, 1099, 40, 2, '京东报价单', NULL),
  (3, 3, 1049, 30, 5, '得力报价单', NULL),
  (4, 1, 2999, 20, 2, '京东报价单', NULL),
  (4, 2, 2899, 10, 3, '苏宁报价单', NULL),
  (5, 3, 89, 100, 5, '得力报价单', NULL),
  (5, 4, 79, 80, 4, '工品汇报价单', NULL),
  (6, 4, 49, 200, 4, '工品汇报价单', NULL);
