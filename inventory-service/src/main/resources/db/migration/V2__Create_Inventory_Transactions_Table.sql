-- Migration to create inventory_transactions table
-- This table tracks all inventory movements and changes

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    reference_id VARCHAR(100),
    reference_type VARCHAR(50),
    notes VARCHAR(500),
    performed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_product_id ON inventory_transactions(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_transaction_type ON inventory_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_reference_id ON inventory_transactions(reference_id);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_reference_type ON inventory_transactions(reference_type);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_created_at ON inventory_transactions(created_at);

-- Add comments for documentation
COMMENT ON TABLE inventory_transactions IS 'Inventory transaction history for audit trail';
COMMENT ON COLUMN inventory_transactions.transaction_type IS 'Type of transaction: IN, OUT, ADJUSTMENT, TRANSFER';
COMMENT ON COLUMN inventory_transactions.quantity IS 'Quantity involved in the transaction';
COMMENT ON COLUMN inventory_transactions.previous_quantity IS 'Quantity before the transaction';
COMMENT ON COLUMN inventory_transactions.new_quantity IS 'Quantity after the transaction';
COMMENT ON COLUMN inventory_transactions.reference_id IS 'Reference to external system (order ID, etc.)';
COMMENT ON COLUMN inventory_transactions.reference_type IS 'Type of reference: ORDER, RETURN, ADJUSTMENT, etc.';
COMMENT ON COLUMN inventory_transactions.performed_by IS 'User or system that performed the transaction';
