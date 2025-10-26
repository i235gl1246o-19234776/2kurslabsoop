INSERT INTO function_points (function_id, x_val, y_val)
VALUES ($1, $2, $3)
ON CONFLICT (function_id, x_val) DO UPDATE
SET y_val = EXCLUDED.y_val, computed_at = CURRENT_TIMESTAMP
RETURNING id, x_val, y_val, computed_at;