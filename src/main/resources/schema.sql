-- schema.sql
CREATE TABLE IF NOT EXISTS payments (
  payment_id SERIAL PRIMARY KEY,
  order_id INT NOT NULL,
  amount NUMERIC(12,2) NOT NULL,
  method VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
  reference VARCHAR(64) UNIQUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
  id SERIAL PRIMARY KEY,
  idempotency_key VARCHAR(255) UNIQUE NOT NULL,
  request_fingerprint TEXT NOT NULL,
  response_code INT,
  response_body JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS refunds (
  id SERIAL PRIMARY KEY,
  payment_id INT REFERENCES payments(payment_id) ON DELETE CASCADE,
  amount NUMERIC(12,2) NOT NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING','SUCCESS','FAILED')),
  provider_ref VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
