// src/api.js

// --- Вспомогательная функция для кодирования в Base64 ---
const encodeBase64 = (str) => {
  return btoa(unescape(encodeURIComponent(str)));
};

// --- Хранилище данных аутентификации ---
let storedCredentials = null;
let storedUserId = null; // <-- НОВОЕ: храним userId

// --- Экспортируем объект api ---
export const api = {
  // --- Регистрация: POST /api/users ---
  register: async (username, password) => {
    const response = await fetch('/api/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      // Пытаемся извлечь сообщение об ошибке из разных возможных полей
      const errorMessage = errorData.error || errorData.message || 'Registration failed';
      throw new Error(errorMessage);
    }
    return response.json();
  },

  // --- Аутентификация: GET /api/users/name/{name} с Basic Auth ---
  login: async (username, password) => {
    const credentials = encodeBase64(`${username}:${password}`);

    const response = await fetch(`/api/users/name/${encodeURIComponent(username)}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    if (!response.ok) {
      const errorData = await response.json();
      const errorMessage = errorData.error || errorData.message || 'Login failed';
      throw new Error(errorMessage);
    }

    const userData = await response.json(); // Ожидаем { id: 123, username: "user", role: "USER" }

    // Сохраняем credentials и userId для последующих запросов
    storedCredentials = credentials;
    storedUserId = userData.id; // <-- СОХРАНЯЕМ userId из ответа

    return userData;
  },

  // --- Выход (очистка credentials и userId) ---
  logout: () => {
    storedCredentials = null;
    storedUserId = null; // <-- ОЧИЩАЕМ userId
  },

  // --- Проверка аутентификации ---
  isAuthenticated: () => {
    return !!storedCredentials;
  },

  // --- Получение сохранённого userId ---
  getStoredUserId: () => {
    return storedUserId;
  },

  // --- Создание функции: POST /api/functions (требует аутентификации) ---
  // Тело запроса: { "functionName": "...", "functionExpression": "...", "typeFunction": "...", "userId": ... }
  createFunction: async (functionData) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    if (!storedUserId) {
      throw new Error('User ID not available. Please log in again.');
    }

    // --- ДОБАВЛЯЕМ userId в тело запроса ---
    const bodyData = {
        functionName: functionData.functionName,
        functionExpression: functionData.functionExpression,
        typeFunction: functionData.typeFunction,
        userId: storedUserId, // <-- ДОБАВЛЕНО
    };
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---

    // --- ЛОГИРОВАНИЕ ДЛЯ createFunction ---
    console.log("api.createFunction: Подготовленные данные для JSON.stringify:", bodyData);
    console.log("api.createFunction: JSON.stringify(bodyData):", JSON.stringify(bodyData));
    // --- КОНЕЦ ЛОГИРОВАНИЯ ---

    const response = await fetch('/api/functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(bodyData),
    });

    if (!response.ok) {
      const errorData = await response.json();
      const errorMessage = errorData.error || errorData.message || 'Failed to create function';
      throw new Error(errorMessage);
    }
    return response.json();
  },

  // --- Создание табулированных точек: POST /api/tabulated-points (требует аутентификации) ---
  // Тело запроса: { "functionId": ..., "xval": ..., "yval": ... }
  createTabulatedPoints: async (functionId, xval, yval) => { // <-- Изменим сигнатуру функции
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    // Формируем тело запроса в нужном формате
    const pointData = {
        functionId: functionId, // <-- Обратите внимание на регистр: functionId
        xval: xval,           // <-- Обратите внимание на регистр: xval
        yval: yval            // <-- Обратите внимание на регистр: yval
    };

    // --- ЛОГИРОВАНИЕ ДЛЯ createTabulatedPoints ---
    console.log("api.createTabulatedPoints: Подготовленные данные для JSON.stringify:", pointData);
    console.log("api.createTabulatedPoints: JSON.stringify(pointData):", JSON.stringify(pointData));
    // --- КОНЕЦ ЛОГИРОВАНИЯ ---

    const response = await fetch('/api/tabulated-points', { // <-- Правильный URL с дефисом
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(pointData), // <-- Отправляем сформированный объект
    });

    if (!response.ok) {
      const errorData = await response.json();
      const errorMessage = errorData.error || errorData.message || 'Failed to save tabulated points';
      throw new Error(errorMessage);
    }
    return response.json();
  },

  // --- Вычисление и сохранение табулированных точек из MathFunction: POST /api/tabulated-points/calculate (требует аутентификации) ---
  // Тело запроса: { "functionId": 123, "mathFunctionName": "SqrFunction", "start": 0.0, "end": 10.0, "count": 100 }
  calculateAndSaveTabulatedPoints: async (functionId, mathFunctionName, start, end, count) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    // Формируем тело запроса
    const calculateData = {
        functionId: functionId,
        mathFunctionName: mathFunctionName, // Имя функции, как оно зарегистрировано на сервере
        start: start,
        end: end,
        count: count
    };

    // --- ЛОГИРОВАНИЕ ДЛЯ calculateAndSaveTabulatedPoints ---
    console.log("api.calculateAndSaveTabulatedPoints: Подготовленные данные для JSON.stringify:", calculateData);
    console.log("api.calculateAndSaveTabulatedPoints: JSON.stringify(calculateData):", JSON.stringify(calculateData));
    // --- КОНЕЦ ЛОГИРОВАНИЯ ---

    const response = await fetch('/api/tabulated-points/calculate', { // <-- НОВЫЙ URL
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(calculateData), // <-- Отправляем сформированный объект
    });

    if (!response.ok) {
      const errorData = await response.json();
      const errorMessage = errorData.error || errorData.message || 'Failed to calculate and save tabulated points';
      throw new Error(errorMessage);
    }
    // Сервер возвращает JSON с сообщением, но обычно для POST на создание возвращают 201 Created
    // и иногда объект созданного ресурса. В данном случае, сервер возвращает 200 OK с сообщением.
    // Можно вернуть response.json() или просто true, если сервер возвращает сообщение.
    // const result = await response.json();
    // return result;
    // Или просто проверить статус:
    return response.json(); // Возвращает JSON {"message": "..."}
  },

  // --- Получение функций пользователя: GET /api/functions?userId={userId} (требует аутентификации) ---
  getFunctionsByUserId: async (userId) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/functions?userId=${userId}`, { // <-- URL с параметром userId
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`, // Требуется аутентификация
      },
    });

    if (!response.ok) {
      const errorData = await response.json();
      const errorMessage = errorData.error || errorData.message || 'Failed to fetch functions';
      throw new Error(errorMessage);
    }
    return response.json(); // Возвращает массив функций
  }
};