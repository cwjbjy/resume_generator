ALTER TABLE orders
    ADD COLUMN plan_name VARCHAR(50) NULL AFTER plan_id,
    ADD COLUMN duration_days INT NULL AFTER plan_name;

UPDATE orders o
JOIN plans p ON p.id = o.plan_id
SET o.plan_name = p.name, o.duration_days = p.duration_days;

ALTER TABLE orders
    MODIFY COLUMN plan_name VARCHAR(50) NOT NULL,
    MODIFY COLUMN duration_days INT NOT NULL;
