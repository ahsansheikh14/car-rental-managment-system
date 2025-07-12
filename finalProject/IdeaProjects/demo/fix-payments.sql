-- Make sure payments table exists
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT,
    amount DOUBLE,
    method VARCHAR(50)
);

-- Try to add payment_date column (if not exists)
ALTER TABLE payments ADD COLUMN payment_date DATE;

-- Try to add status column (if not exists)
ALTER TABLE payments ADD COLUMN status VARCHAR(20);

-- Update the payment_date column with values if it was just created
UPDATE payments SET payment_date = CURRENT_DATE WHERE payment_date IS NULL;

-- Update the status column with default values if they're NULL
UPDATE payments SET status = 'Pending' WHERE status IS NULL; 