<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Главная - Математическое приложение</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 40px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
        }
        .status {
            margin-top: 20px;
            padding: 15px;
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            border-radius: 4px;
            color: #155724;
        }
        .api-link {
            display: block;
            margin: 10px 0;
            padding: 10px;
            background-color: #e9ecef;
            text-decoration: none;
            color: #007bff;
            border-radius: 4px;
            transition: background-color 0.3s;
        }
        .api-link:hover {
            background-color: #dee2e6;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Добро пожаловать в приложение "Математические функции"</h1>

    <div class="status">
        <p><strong>Статус:</strong> Сервлеты успешно развернуты!</p>
        <p><strong>Время запуска:</strong> <%= new java.util.Date() %></p>
    </div>

    <h2>API Эндпоинты (для тестирования):</h2>
    <a href="api/users" class="api-link">GET /api/users - Получить всех пользователей</a>
    <a href="api/functions?userId=1" class="api-link">GET /api/functions?userId=1 - Получить функции пользователя 1</a>
    <!-- Добавьте другие ссылки по мере необходимости -->

    <p><em>Примечание: Это стартовая страница. Основная работа происходит через API.</em></p>
</div>
</body>
</html>