INSERT INTO operation_types (id, name, result_type) VALUES
    (1, 'AndThen', 'function'),
    (2, 'NewtonMethod', 'num'),
    (3, 'RungeMethod', 'num'),
    (4, 'Derivative', 'function'),
    (5, 'DefiniteIntegral', 'num'),
    (6, 'Evaluate', 'num')
ON CONFLICT (id) DO NOTHING;