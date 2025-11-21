<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Проверяем аутентификацию через HTTP Basic
    String authHeader = request.getHeader("Authorization");
    boolean isAuthenticated = authHeader != null && authHeader.startsWith("Basic ");
%>
<html>
<head>
    <title>Создание табулированных функций</title>
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
            max-width: 1200px;
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

        .tabs {
            display: flex;
            background: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
        }

        .tab-button {
            flex: 1;
            padding: 20px;
            border: none;
            background: none;
            font-size: 1.1em;
            cursor: pointer;
            transition: all 0.3s ease;
            border-bottom: 3px solid transparent;
        }

        .tab-button:hover {
            background: #e9ecef;
        }

        .tab-button.active {
            background: white;
            border-bottom: 3px solid #3498db;
            color: #3498db;
            font-weight: 600;
        }

        .tab-content {
            padding: 40px;
            min-height: 500px;
        }

        .form-group {
            margin-bottom: 25px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #2c3e50;
            font-size: 1.1em;
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
            padding: 12px 30px;
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

        .btn-primary:disabled {
            background: #bdc3c7;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        .btn-secondary {
            background: #95a5a6;
            color: white;
            margin-right: 10px;
        }

        .btn-danger {
            background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
            color: white;
        }

        .points-table-container {
            margin-top: 30px;
            border: 1px solid #e9ecef;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 5px 15px rgba(0,0,0,0.08);
        }

        .points-table {
            width: 100%;
            border-collapse: collapse;
        }

        .points-table th {
            background: #34495e;
            color: white;
            padding: 15px;
            text-align: center;
            font-weight: 600;
        }

        .points-table td {
            padding: 0;
            border-bottom: 1px solid #e9ecef;
        }

        .points-table input {
            width: 100%;
            padding: 12px 15px;
            border: none;
            text-align: center;
            font-size: 1em;
            transition: background 0.3s ease;
        }

        .points-table input:focus {
            outline: none;
            background: #f8f9fa;
        }

        .points-table tr:hover input {
            background: #f8f9fa;
        }

        .control-panel {
            display: flex;
            gap: 15px;
            align-items: end;
            flex-wrap: wrap;
            margin-bottom: 25px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
        }

        .control-panel .form-group {
            margin-bottom: 0;
            flex: 1;
            min-width: 200px;
        }

        .options-panel {
            display: flex;
            gap: 20px;
            margin-bottom: 25px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
        }

        .function-selector {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 25px;
        }

        .function-option {
            padding: 20px;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
            text-align: center;
        }

        .function-option:hover {
            border-color: #3498db;
            transform: translateY(-2px);
        }

        .function-option.selected {
            border-color: #3498db;
            background: #ebf5fb;
        }

        .function-option h4 {
            color: #2c3e50;
            margin-bottom: 5px;
        }

        .function-option p {
            color: #7f8c8d;
            font-size: 0.9em;
            font-family: monospace;
        }

        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
            padding: 20px;
        }

        .modal-content {
            background: white;
            padding: 40px;
            border-radius: 15px;
            max-width: 500px;
            width: 100%;
            text-align: center;
            box-shadow: 0 20px 40px rgba(0,0,0,0.2);
        }

        .modal-content h3 {
            color: #e74c3c;
            margin-bottom: 20px;
            font-size: 1.5em;
        }

        .modal-content p {
            margin-bottom: 25px;
            font-size: 1.1em;
            line-height: 1.6;
            color: #2c3e50;
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

        .range-inputs {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
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

        .user-info {
            background: #e3f2fd;
            padding: 10px 15px;
            border-radius: 8px;
            margin-bottom: 20px;
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
            margin-bottom: 20px;
            text-align: center;
            font-weight: 500;
        }

        @media (max-width: 768px) {
            .control-panel {
                flex-direction: column;
            }

            .control-panel .form-group {
                min-width: 100%;
            }

            .range-inputs {
                grid-template-columns: 1fr;
            }

            .options-panel {
                flex-direction: column;
            }

            .tab-content {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
    <div id="app" class="container">
        <!-- Заголовок -->
        <div class="header">
            <h1>🎯 Система табулирования функций</h1>
            <p>Создавайте табулированные функции различными способами</p>
        </div>

        <!-- Информация о текущем пользователе -->
        <% if (isAuthenticated) { %>
            <div class="user-info">
                Вы авторизованы через HTTP Basic Authentication (данные пользователя определяются автоматически)
            </div>
        <% } else { %>
            <div class="auth-required">
                ⚠️ Вы не авторизованы! Пожалуйста, войдите в систему для создания функций.
                <br><a href="<%= request.getContextPath() %>/login" style="color: #1976d2; font-weight: bold;">Войти в систему</a>
            </div>
        <% } %>

        <!-- Вкладки -->
        <div class="tabs">
            <button
                v-on:click="activeTab = 'fromArrays'"
                v-bind:class="['tab-button', { active: activeTab === 'fromArrays' }]">
                📊 Из массивов точек
            </button>
            <button
                v-on:click="activeTab = 'fromFunction'"
                v-bind:class="['tab-button', { active: activeTab === 'fromFunction' }]">
                📈 Из математической функции
            </button>
        </div>

        <!-- Содержимое вкладок -->
        <div class="tab-content">
            <!-- Вкладка создания из массивов -->
            <div v-if="activeTab === 'fromArrays'">
                <h2 style="margin-bottom: 30px; color: #2c3e50;">Создание функции из массива точек</h2>

                <div class="control-panel">
                    <div class="form-group">
                        <label>🔢 Количество точек (от 2 до 1000):</label>
                        <input
                            type="number"
                            v-model.number="arraysData.pointsCount"
                            min="2"
                            max="1000"
                            class="form-control"
                            placeholder="Введите количество точек">
                    </div>
                    <button
                        v-on:click="generateTable"
                        v-bind:disabled="arraysData.pointsCount < 2 || arraysData.pointsCount > 1000"
                        class="btn btn-primary">
                        🎲 Создать таблицу
                    </button>
                </div>

                <div v-if="arraysData.pointsCount > 100" class="error-message">
                    ⚠️ Предупреждение: Большое количество точек может привести к медленной работе системы. Рекомендуется использовать не более 100 точек.
                </div>

                <!-- Опции табулированной функции -->
                <div class="options-panel">
                    <div class="form-group">
                        <label>Тип реализации:</label>
                        <select v-model="arraysData.factoryType" class="form-control">
                            <option value="array">Массивная реализация</option>
                            <option value="linked_list">Связный список</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Опции функции:</label>
                        <div style="display: flex; gap: 15px; margin-top: 8px;">
                            <label style="display: flex; align-items: center; gap: 5px;">
                                <input type="checkbox" v-model="arraysData.strict">
                                Строгая
                            </label>
                            <label style="display: flex; align-items: center; gap: 5px;">
                                <input type="checkbox" v-model="arraysData.unmodifiable">
                                Неизменяемая
                            </label>
                        </div>
                    </div>
                </div>

                <!-- Таблица для ввода точек -->
                <div v-if="arraysData.points.length > 0" class="points-table-container">
                    <table class="points-table">
                        <thead>
                            <tr>
                                <th style="width: 10%;">№</th>
                                <th style="width: 45%;">Значение X</th>
                                <th style="width: 45%;">Значение Y</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(point, index) in arraysData.points" v-bind:key="index">
                                <td style="background: #f8f9fa; font-weight: 500; padding: 12px; text-align: center;">
                                    {{ index + 1 }}
                                </td>
                                <td>
                                    <input
                                        type="number"
                                        v-model.number="point.x"
                                        v-on:input="validateArraysInput"
                                        step="any"
                                        class="form-control"
                                        :placeholder="'x' + (index + 1)">
                                </td>
                                <td>
                                    <input
                                        type="number"
                                        v-model.number="point.y"
                                        v-on:input="validateArraysInput"
                                        step="any"
                                        class="form-control"
                                        :placeholder="'y' + (index + 1)">
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div v-if="arraysData.points.length > 0" style="margin-top: 30px; text-align: center;">
                    <button
                        v-on:click="createFromArrays"
                        v-bind:disabled="!isArraysValid || loading || !<%= isAuthenticated %>"
                        class="btn btn-primary">
                        <span v-if="loading" class="loading"></span>
                        <span v-else>✅ Создать функцию</span>
                    </button>
                    <button v-on:click="resetArraysForm" v-bind:disabled="loading" class="btn btn-secondary">
                        🗑️ Очистить
                    </button>
                </div>

                <div v-if="arraysSuccess" class="success-message">
                    ✅ {{ arraysSuccess }}
                </div>

                <div v-if="arraysError" class="error-message">
                    ❌ {{ arraysError }}
                </div>
            </div>

            <!-- Вкладка создания из математической функции -->
            <div v-else>
                <h2 style="margin-bottom: 30px; color: #2c3e50;">Создание функции из математической функции</h2>

                <!-- Выбор функции -->
                <div class="form-group">
                    <label>📐 Выберите математическую функцию:</label>
                    <div class="function-selector">
                        <div
                            v-for="func in availableFunctions"
                            v-bind:key="func.id"
                            v-on:click="functionData.selectedFunction = func.id"
                            v-bind:class="['function-option', { selected: functionData.selectedFunction === func.id }]">
                            <h4>{{ func.name }}</h4>
                            <p>{{ func.formula }}</p>
                        </div>
                    </div>
                </div>

                <!-- Параметры табуляции -->
                <div class="range-inputs">
                    <div class="form-group">
                        <label>🔽 Левый предел (от):</label>
                        <input
                            type="number"
                            v-model.number="functionData.leftX"
                            step="any"
                            class="form-control"
                            placeholder="Начало интервала">
                    </div>
                    <div class="form-group">
                        <label>🔼 Правый предел (до):</label>
                        <input
                            type="number"
                            v-model.number="functionData.rightX"
                            step="any"
                            class="form-control"
                            placeholder="Конец интервала">
                    </div>
                </div>

                <div class="control-panel">
                    <div class="form-group">
                        <label>🔢 Количество точек разбиения (от 2 до 1000):</label>
                        <input
                            type="number"
                            v-model.number="functionData.pointsCount"
                            min="2"
                            max="1000"
                            class="form-control"
                            placeholder="Введите количество точек">
                    </div>
                    <div style="display: flex; align-items: end; gap: 10px;">
                        <span>Шаг: {{ stepSize.toFixed(4) }}</span>
                    </div>
                </div>

                <div v-if="functionData.pointsCount > 100" class="error-message">
                    ⚠️ Предупреждение: Большое количество точек может привести к медленной работе системы. Рекомендуется использовать не более 100 точек.
                </div>

                <!-- Опции табулированной функции -->
                <div class="options-panel">
                    <div class="form-group">
                        <label>Тип реализации:</label>
                        <select v-model="functionData.factoryType" class="form-control">
                            <option value="array">Массивная реализация</option>
                            <option value="linked_list">Связный список</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Опции функции:</label>
                        <div style="display: flex; gap: 15px; margin-top: 8px;">
                            <label style="display: flex; align-items: center; gap: 5px;">
                                <input type="checkbox" v-model="functionData.strict">
                                Строгая
                            </label>
                            <label style="display: flex; align-items: center; gap: 5px;">
                                <input type="checkbox" v-model="functionData.unmodifiable">
                                Неизменяемая
                            </label>
                        </div>
                    </div>
                </div>

                <div style="text-align: center; margin-top: 30px;">
                    <button
                        v-on:click="createFromFunction"
                        v-bind:disabled="!isFunctionValid || loading || !<%= isAuthenticated %>"
                        class="btn btn-primary">
                        <span v-if="loading" class="loading"></span>
                        <span v-else>✅ Создать функцию</span>
                    </button>
                    <button v-on:click="resetFunctionForm" v-bind:disabled="loading" class="btn btn-secondary">
                        🗑️ Очистить
                    </button>
                </div>

                <div v-if="functionSuccess" class="success-message">
                    ✅ {{ functionSuccess }}
                </div>

                <div v-if="functionError" class="error-message">
                    ❌ {{ functionError }}
                </div>
            </div>
        </div>

        <!-- Модальное окно ошибки -->
        <div v-if="showErrorModal" class="modal-overlay" v-on:click.self="closeErrorModal">
            <div class="modal-content">
                <h3>❌ Ошибка</h3>
                <p>{{ errorMessage }}</p>
                <button v-on:click="closeErrorModal" class="btn btn-danger">
                    Закрыть
                </button>
            </div>
        </div>
    </div>

    <script>
        const { createApp, ref } = Vue;

        createApp({
            data() {
                return {
                    activeTab: 'fromArrays',
                    loading: false,
                    showErrorModal: false,
                    errorMessage: '',

                    // Данные для создания из массивов
                    arraysData: {
                        pointsCount: 5,
                        points: [],
                        factoryType: 'array',
                        strict: true,
                        unmodifiable: false
                    },
                    arraysSuccess: '',
                    arraysError: '',

                    // Данные для создания из функции
                    functionData: {
                        selectedFunction: '',
                        leftX: 0,
                        rightX: 10,
                        pointsCount: 10,
                        factoryType: 'array',
                        strict: true,
                        unmodifiable: false
                    },
                    functionSuccess: '',
                    functionError: '',

                    // Доступные математические функции
                    availableFunctions: [
                        { id: 'square', name: 'Квадратичная функция', formula: 'f(x) = x²', class: 'SqrFunction' },
                        { id: 'identity', name: 'Тождественная функция', formula: 'f(x) = x', class: 'IdentityFunction' },
                        { id: 'sin', name: 'Синус', formula: 'f(x) = sin(x)', class: 'SinFunction' },
                        { id: 'cos', name: 'Косинус', formula: 'f(x) = cos(x)', class: 'CosFunction' }
                    ]
                }
            },

            computed: {
                // Вычисление шага для табуляции из математической функции
                stepSize() {
                    if (this.functionData.rightX <= this.functionData.leftX || this.functionData.pointsCount <= 1) {
                        return 0;
                    }
                    return (this.functionData.rightX - this.functionData.leftX) / (this.functionData.pointsCount - 1);
                },

                // Валидация данных для массивов
                isArraysValid() {
                    // Проверка, что есть точки
                    if (this.arraysData.points.length === 0) return false;

                    // Проверка, что все поля заполнены
                    const hasEmptyFields = this.arraysData.points.some(point =>
                        point.x === null || point.x === undefined || isNaN(point.x) ||
                        point.y === null || point.y === undefined || isNaN(point.y)
                    );

                    if (hasEmptyFields) return false;

                    // Проверка, что x строго возрастают
                    for (let i = 1; i < this.arraysData.points.length; i++) {
                        if (this.arraysData.points[i].x <= this.arraysData.points[i-1].x) {
                            return false;
                        }
                    }

                    return true;
                },

                // Валидация данных для математической функции
                isFunctionValid() {
                    return this.functionData.selectedFunction &&
                           this.functionData.leftX < this.functionData.rightX &&
                           this.functionData.pointsCount >= 2 &&
                           this.functionData.pointsCount <= 1000;
                }
            },

            methods: {
                // ===== Методы для создания из массивов =====
                generateTable() {
                    this.arraysError = '';
                    this.arraysSuccess = '';

                    if (this.arraysData.points.length > 0) {
                        if (!confirm('Текущие данные в таблице будут удалены. Продолжить?')) {
                            return;
                        }
                    }

                    // Генерация таблицы с начальными значениями x
                    this.arraysData.points = Array.from({ length: this.arraysData.pointsCount }, (_, i) => ({
                        x: i * 1.0, // Начальные значения x с шагом 1
                        y: null
                    }));
                },

                validateArraysInput() {
                    this.arraysError = '';
                    this.arraysSuccess = '';
                },

                async createFromArrays() {
                    <% if (!isAuthenticated) { %>
                        alert('Вы не авторизованы! Пожалуйста, войдите в систему.');
                        return;
                    <% } %>

                    this.loading = true;
                    this.arraysError = '';
                    this.arraysSuccess = '';

                    try {
                        // Получаем учетные данные из HTTP Basic Authentication
                        const authHeader = document.querySelector('meta[name="auth-header"]');
                        let credentials = '';

                        // Попробуем получить учетные данные из localStorage (если пользователь уже вошел)
                        const savedCredentials = localStorage.getItem('basicAuthCredentials');
                        if (savedCredentials) {
                            credentials = savedCredentials;
                        } else {
                            // Используем заголовок Authorization из текущего запроса
                            credentials = '<%= request.getHeader("Authorization") %>';
                        }

                        const functionResponse = await axios.post(
                            '<%= request.getContextPath() %>/api/functions',
                            {
                                typeFunction: 'tabular',
                                functionName: `Функция из точек (${this.arraysData.points.length} точек)`,
                                functionExpression: JSON.stringify({
                                    factoryType: this.arraysData.factoryType,
                                    strict: this.arraysData.strict,
                                    unmodifiable: this.arraysData.unmodifiable
                                })
                            },
                            {
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Authorization': '<%= request.getHeader("Authorization") %>'
                                }
                            }
                        );

                        const functionId = functionResponse.data.id;

                        // Создаем точки для функции
                        for (let i = 0; i < this.arraysData.points.length; i++) {
                            await axios.post(
                                '<%= request.getContextPath() %>/api/tabulated-points',
                                {
                                    functionId: functionId,
                                    xValue: this.arraysData.points[i].x,
                                    yValue: this.arraysData.points[i].y
                                },
                                {
                                    headers: {
                                        'Content-Type': 'application/json',
                                        'Authorization': '<%= request.getHeader("Authorization") %>'
                                    }
                                }
                            );
                        }

                        this.arraysSuccess = `Функция успешно создана! ID: ${functionId}. Количество точек: ${this.arraysData.points.length}.`;

                        // Через 3 секунды очищаем сообщение об успехе
                        setTimeout(() => {
                            this.arraysSuccess = '';
                        }, 3000);

                        // Сбрасываем форму для создания новой функции
                        this.resetArraysForm();

                    } catch (error) {
                        this.handleError(error, 'arrays');
                    } finally {
                        this.loading = false;
                    }
                },

                resetArraysForm() {
                    this.arraysData.pointsCount = 5;
                    this.arraysData.points = [];
                    this.arraysData.factoryType = 'array';
                    this.arraysData.strict = true;
                    this.arraysData.unmodifiable = false;
                    this.arraysError = '';
                    this.arraysSuccess = '';
                },

                // ===== Методы для создания из математической функции =====
                async createFromFunction() {
                    <% if (!isAuthenticated) { %>
                        alert('Вы не авторизованы! Пожалуйста, войдите в систему.');
                        return;
                    <% } %>

                    this.loading = true;
                    this.functionError = '';
                    this.functionSuccess = '';

                    try {
                        const selectedFunc = this.availableFunctions.find(f => f.id === this.functionData.selectedFunction);
                        const functionName = selectedFunc ?
                            `${selectedFunc.name} на [${this.functionData.leftX}, ${this.functionData.rightX}]` :
                            `Табулированная функция (${this.functionData.pointsCount} точек)`;

                        const functionResponse = await axios.post(
                            '<%= request.getContextPath() %>/api/functions',
                            {
                                typeFunction: 'tabular',
                                functionName: functionName,
                                functionExpression: JSON.stringify({
                                    baseFunction: this.functionData.selectedFunction,
                                    from: this.functionData.leftX,
                                    to: this.functionData.rightX,
                                    pointsCount: this.functionData.pointsCount,
                                    factoryType: this.functionData.factoryType,
                                    strict: this.functionData.strict,
                                    unmodifiable: this.functionData.unmodifiable
                                })
                            },
                            {
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Authorization': '<%= request.getHeader("Authorization") %>'
                                }
                            }
                        );

                        const functionId = functionResponse.data.id;

                        // Рассчитываем точки табуляции
                        const points = this.calculateTabulatedPoints();

                        // Создаем точки для функции
                        for (let i = 0; i < points.length; i++) {
                            await axios.post(
                                '<%= request.getContextPath() %>/api/tabulated-points',
                                {
                                    functionId: functionId,
                                    xValue: points[i].x,
                                    yValue: points[i].y
                                },
                                {
                                    headers: {
                                        'Content-Type': 'application/json',
                                        'Authorization': '<%= request.getHeader("Authorization") %>'
                                    }
                                }
                            );
                        }

                        this.functionSuccess = `Функция успешно создана! ID: ${functionId}. Количество точек: ${points.length}.`;

                        // Через 3 секунды очищаем сообщение об успехе
                        setTimeout(() => {
                            this.functionSuccess = '';
                        }, 3000);

                        // Сбрасываем форму для создания новой функции
                        this.resetFunctionForm();

                    } catch (error) {
                        this.handleError(error, 'function');
                    } finally {
                        this.loading = false;
                    }
                },

                calculateTabulatedPoints() {
                    const points = [];
                    const step = this.stepSize;
                    const func = this.getMathFunction();

                    for (let i = 0; i < this.functionData.pointsCount; i++) {
                        const x = this.functionData.leftX + i * step;
                        const y = func(x);
                        points.push({ x, y });
                    }

                    return points;
                },

                getMathFunction() {
                    const selectedFunc = this.availableFunctions.find(f => f.id === this.functionData.selectedFunction);

                    switch (selectedFunc?.id) {
                        case 'square':
                            return (x) => x * x;
                        case 'identity':
                            return (x) => x;
                        case 'sin':
                            return (x) => Math.sin(x);
                        case 'cos':
                            return (x) => Math.cos(x);
                        default:
                            return (x) => 0;
                    }
                },

                resetFunctionForm() {
                    this.functionData.selectedFunction = '';
                    this.functionData.leftX = 0;
                    this.functionData.rightX = 10;
                    this.functionData.pointsCount = 10;
                    this.functionData.factoryType = 'array';
                    this.functionData.strict = true;
                    this.functionData.unmodifiable = false;
                    this.functionError = '';
                    this.functionSuccess = '';
                },

                // ===== Общие методы =====
                handleError(error, target) {
                    console.error('Ошибка:', error);

                    let errorMessage = 'Произошла ошибка при создании функции.';

                    if (error.response) {
                        // Ошибки от сервера
                        if (error.response.data?.error) {
                            errorMessage = error.response.data.error;
                        } else if (error.response.data?.message) {
                            errorMessage = error.response.data.message;
                        } else {
                            switch (error.response.status) {
                                case 400:
                                    errorMessage = 'Некорректные данные. Проверьте правильность введенных значений.';
                                    break;
                                case 401:
                                    errorMessage = 'Необходимо авторизоваться для создания функции.';
                                    break;
                                case 403:
                                    errorMessage = 'У вас недостаточно прав для создания функции.';
                                    break;
                                case 404:
                                    errorMessage = 'Ресурс не найден. Проверьте правильность запроса.';
                                    break;
                                case 422:
                                    errorMessage = 'Ошибка валидации данных. Проверьте правильность введенных значений.';
                                    break;
                                case 500:
                                    errorMessage = 'Внутренняя ошибка сервера. Попробуйте позже.';
                                    break;
                            }
                        }
                    } else if (error.request) {
                        // Ошибки сети
                        errorMessage = 'Не удалось подключиться к серверу. Проверьте соединение с интернетом.';
                    } else {
                        // Ошибки на клиенте
                        errorMessage = error.message || 'Произошла неизвестная ошибка.';
                    }

                    // Отображаем ошибку в зависимости от контекста
                    if (target === 'arrays') {
                        this.arraysError = errorMessage;
                    } else if (target === 'function') {
                        this.functionError = errorMessage;
                    } else {
                        this.showErrorModal = true;
                        this.errorMessage = errorMessage;
                    }
                },

                closeErrorModal() {
                    this.showErrorModal = false;
                    this.errorMessage = '';
                }
            },

            mounted() {
                // Генерируем начальную таблицу
                this.generateTable();
            }
        }).mount('#app');
    </script>
</body>
</html>