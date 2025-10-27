INSERT INTO users (username, password_hash)
VALUES (?, ?)
RETURNING id, username, created_at;