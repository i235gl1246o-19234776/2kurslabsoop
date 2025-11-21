<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String authHeader = request.getHeader("Authorization");
    boolean isAuthenticated = authHeader != null && authHeader.startsWith("Basic ");
%>
<html>
<head>
    <title>Поиск пользователя по имени</title>
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
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
            padding: 20px;
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }

        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
            font-weight: 300;
        }

        .header p {
            font-size: 1.2em;
            opacity: 0.9;
        }

        .content {
            padding: 40px;
        }

        .user-info {
            background: #e3f2fd;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 30px;
            text-align: center;
            font-weight: 500;
            color: #1976d2;
        }

        .auth-required {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 30px;
            text-align: center;
        }

        .search-container {
            display: flex;
            gap: 15px;
            margin-bottom: 30px;
            align-items: end;
        }

        .form-group {
            flex: 1;
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
            padding: 12px 25px;
            border: none;
            border-radius: 8px;
            font-size: 1.1em;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: 600;
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
        }

        .result-container {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 10px;
            margin-top: 20px;
            border-left: 5px solid #3498db;
        }

        .result-container h3 {
            color: #2c3e50;
            margin-bottom: 15px;
        }

        .result-data {
            background: white;
            padding: 15px;
            border-radius: 8px;
            font-family: monospace;
            word-break: break-all;
            margin-top: 10px;
        }

        .success-message {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
            font-weight: 500;
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

        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid #f3f3f3;
            border-top: 3px solid #3498db;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-right: 10px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        @media (max-width: 768px) {
            .search-container {
                flex-direction: column;
            }

            .form-group {
                width: 100%;
            }

            .content {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <div id="app" class="container">
        <!-- Заголовок -->
        <div class="header">
            <h1>🔍 Поиск пользователя по имени</h1>
            <p>Получение информации о пользователе по его имени</p>
        </div>

        <!-- Информация о текущем пользователе -->
        <% if (isAuthenticated) { %>
            <div class="user-info">
                Вы авторизованы через HTTP Basic Authentication
            </div>
        <% } else { %>
            <div class="auth-required">
                ⚠️ Вы не авторизованы! Пожалуйста, войдите в систему для поиска пользователей.
                <br><a href="<%= request.getContextPath() %>/login.jsp" style="color: #1976d2; font-weight: bold;">Войти в систему</a>
            </div>
        <% } %>

        <!-- Поиск пользователя -->
        <div class="content">
            <div class="search-container">
                <div class="form-group">
                    <label>Введите имя пользователя:</label>
                    <input
                        type="text"
                        v-model="searchName"
                        class="form-control"
                        placeholder="admina"
                        @keyup.enter="searchUser">
                </div>
                <button
                    @click="searchUser"
                    :disabled="loading || !<%= isAuthenticated %>"
                    class="btn btn-primary">
                    🔍 Найти пользователя
                </button>
                <button
                    @click="resetSearch"
                    class="btn btn-secondary">
                    🗑️ Сбросить
                </button>
            </div>

            <!-- Результаты поиска -->
            <div v-if="result" class="result-container">
                <h3>Результат поиска:</h3>
                <div class="result-data">
                    <pre>{{ JSON.stringify(result, null, 2) }}</pre>
                </div>
                <div style="margin-top: 15px;">
                    <strong>ID пользователя:</strong> {{ result.id }}
                </div>
                <div v-if="result.id" style="margin-top: 10px;">
                    <button @click="useUserId(result.id)" class="btn btn-primary">
                        Использовать этот ID
                    </button>
                </div>
            </div>

            <div v-if="loading" class="success-message">
                <span class="loading"></span> Поиск пользователя "{{ searchName }}"...
            </div>

            <div v-if="errorMessage" class="error-message">
                ❌ {{ errorMessage }}
            </div>

            <div v-if="infoMessage" class="success-message">
                ✅ {{ infoMessage }}
            </div>
        </div>
    </div>

    <script>
        const { createApp, ref } = Vue;

        createApp({
            data() {
                return {
                    loading: false,
                    errorMessage: '',
                    infoMessage: '',
                    searchName: '',
                    result: null
                }
            },

            methods: {
                async searchUser() {
                    <% if (!isAuthenticated) { %>
                        alert('Вы не авторизованы! Пожалуйста, войдите в систему.');
                        return;
                    <% } %>

                    if (!this.searchName.trim()) {
                        this.errorMessage = 'Пожалуйста, введите имя пользователя для поиска.';
                        return;
                    }

                    this.loading = true;
                    this.errorMessage = '';
                    this.infoMessage = '';
                    this.result = null;

                    try {
                        // Используем URL с параметром name
                        const response = await axios.get(
                            `<%= request.getContextPath() %>/api/users/name/${encodeURIComponent(this.searchName.trim())}`,
                            {
                                headers: {
                                    'Authorization': '<%= request.getHeader("Authorization") %>',
                                    'Content-Type': 'application/json'
                                }
                            }
                        );

                        if (response.data) {
                            this.result = response.data;
                            this.infoMessage = `Пользователь "${this.searchName}" найден!`;
                        } else {
                            this.errorMessage = 'Пользователь не найден.';
                        }

                    } catch (error) {
                        console.error('Ошибка при поиске пользователя:', error);

                        if (error.response) {
                            if (error.response.status === 404) {
                                this.errorMessage = `Пользователь "${this.searchName}" не найден.`;
                            } else if (error.response.status === 401) {
                                this.errorMessage = 'Необходимо авторизоваться для доступа к данным.';
                            } else if (error.response.status === 403) {
                                this.errorMessage = 'Недостаточно прав для доступа к данным.';
                            } else {
                                this.errorMessage = 'Ошибка сервера: ' + (error.response.data?.error || 'Неизвестная ошибка');
                            }
                        } else {
                            this.errorMessage = 'Ошибка сети: ' + error.message;
                        }
                    } finally {
                        this.loading = false;
                    }
                },

                resetSearch() {
                    this.searchName = '';
                    this.result = null;
                    this.errorMessage = '';
                    this.infoMessage = '';
                },

                useUserId(userId) {
                    // Пример использования найденного user_id
                    alert(`Получен user_id: ${userId}\nТеперь вы можете использовать его в других запросах.`);

                    // Вы можете сохранить его в localStorage или использовать для других операций
                    localStorage.setItem('currentUserId', userId);
                    this.infoMessage = `User ID ${userId} сохранен в локальное хранилище.`;
                }
            },

            mounted() {
                // Автоматически подставляем имя текущего пользователя, если доступно
                const currentUsername = '<%= session.getAttribute("username") != null ? session.getAttribute("username") : "" %>';
                if (currentUsername) {
                    this.searchName = currentUsername;
                }
            }
        }).mount('#app');
    </script>
</body>
</html>