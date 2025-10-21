-- Migration to create order_items table
-- This table stores individual items within an order

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_sku ON order_items(product_sku);

-- Add comments for documentation
COMMENT ON TABLE order_items IS 'Individual items within an order';
COMMENT ON COLUMN order_items.order_id IS 'Reference to the parent order';
COMMENT ON COLUMN order_items.product_id IS 'ID of the product from inventory service';
COMMENT ON COLUMN order_items.product_name IS 'Name of the product at time of order';
COMMENT ON COLUMN order_items.product_sku IS 'SKU of the product at time of order';
COMMENT ON COLUMN order_items.quantity IS 'Quantity of this item in the order';
COMMENT ON COLUMN order_items.unit_price IS 'Price per unit at time of order';
COMMENT ON COLUMN order_items.total_price IS 'Total price for this item (unit_price * quantity - discount + tax)';
COMMENT ON COLUMN order_items.discount_amount IS 'Discount applied to this item';
COMMENT ON COLUMN order_items.tax_amount IS 'Tax applied to this item';
