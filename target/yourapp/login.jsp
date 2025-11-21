<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Вход в систему</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .container {
            max-width: 400px;
            width: 100%;
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            padding: 30px;
        }

        .header {
            text-align: center;
            margin-bottom: 30px;
        }

        .header h1 {
            color: #2c3e50;
            font-size: 2em;
            margin-bottom: 10px;
        }

        .header p {
            color: #7f8c8d;
            font-size: 1.1em;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #2c3e50;
        }

        .form-control {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            font-size: 1em;
            transition: border-color 0.3s ease;
        }

        .form-control:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
        }

        .btn {
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            font-size: 1.1em;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: 600;
            margin-top: 10px;
        }

        .btn-primary {
            background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(52, 152, 219, 0.3);
        }

        .btn-secondary {
            background: #95a5a6;
            color: white;
            margin-top: 15px;
        }

        .register-link {
            text-align: center;
            margin-top: 20px;
            color: #7f8c8d;
        }

        .register-link a {
            color: #3498db;
            text-decoration: none;
            font-weight: 600;
        }

        .register-link a:hover {
            text-decoration: underline;
        }

        .error-message {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }

        .success-message {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }

        .info-message {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔐 Вход в систему</h1>
            <p>Введите свои учетные данные для доступа к системе</p>
        </div>

        <div class="info-message">
            <strong>Инструкция:</strong> Используйте HTTP Basic Authentication. В браузере будет запрошено имя пользователя и пароль.
        </div>

        <%
            String error = (String) request.getAttribute("error");
            String success = (String) request.getAttribute("success");
        %>

        <% if (error != null) { %>
            <div class="error-message">
                <%= error %>
            </div>
        <% } %>

        <% if (success != null) { %>
            <div class="success-message">
                <%= success %>
            </div>
        <% } %>

        <form id="loginForm" method="POST" action="<%= request.getContextPath() %>/login">
            <div class="form-group">
                <label for="username">Имя пользователя:</label>
                <input type="text" id="username" name="username" class="form-control" required placeholder="Введите имя пользователя">
            </div>

            <div class="form-group">
                <label for="password">Пароль:</label>
                <input type="password" id="password" name="password" class="form-control" required placeholder="Введите пароль">
            </div>

            <button type="submit" class="btn btn-primary">Войти</button>
        </form>

        <div class="register-link">
            Нет аккаунта? <a href="<%= request.getContextPath() %>/register">Зарегистрироваться</a>
        </div>
    </div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // Кодируем учетные данные в Base64 для HTTP Basic Authentication
            const credentials = btoa(unescape(encodeURIComponent(username + ':' + password)));

            try {
                // Проверяем аутентификацию, отправив запрос к защищенному эндпоинту
                const response = await fetch('<%= request.getContextPath() %>/api/functions', {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Basic ' + credentials,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    alert('Вход успешен! Перенаправление...');
                    // Перенаправляем на страницу создания функций
                    window.location.href = '<%= request.getContextPath() %>/create-functions.jsp';
                } else {
                    const errorData = await response.json();
                    alert('Ошибка входа: ' + (errorData.error || 'Неверные учетные данные'));
                }
            } catch (error) {
                alert('Ошибка подключения к серверу: ' + error.message);
            }
        });
    </script>
</body>
</html>