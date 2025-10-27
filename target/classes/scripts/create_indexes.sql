CREATE INDEX IF NOT EXISTS idx_functions_user_id ON functions(user_id);
CREATE INDEX IF NOT EXISTS idx_function_points_function_id ON function_points(function_id);
CREATE INDEX IF NOT EXISTS idx_function_operations_function_id ON function_operations(function_id);
CREATE INDEX IF NOT EXISTS idx_function_operations_result_func ON function_operations(result_function_id);
CREATE INDEX IF NOT EXISTS idx_function_operations_params ON function_operations USING GIN (parameters);