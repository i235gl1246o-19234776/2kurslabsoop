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
      throw new Error(errorData.error || 'Registration failed');
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
      throw new Error(errorData.error || 'Login failed');
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
      throw new Error(errorData.error || 'Failed to create function');
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
      throw new Error(errorData.error || 'Failed to save tabulated points');
    }
    return response.json();
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
      throw new Error(errorData.error || 'Failed to fetch functions');
    }
    return response.json(); // Возвращает массив функций
  }

  // Другие методы можно добавить здесь
  // Пример: получение точек функции
  // getTabulatedPointsByFunctionId: async (functionId) => {
  //   if (!storedCredentials) {
  //     throw new Error('Not authenticated. Please log in first.');
  //   }
  //   const response = await fetch(`/api/tabulated-points/function/${functionId}`, {
  //     method: 'GET',
  //     headers: {
  //       'Authorization': `Basic ${storedCredentials}`,
  //     },
  //   });
  //   if (!response.ok) {
  //     const errorData = await response.json();
  //     throw new Error(errorData.error || 'Failed to fetch tabulated points');
  //   }
  //   return response.json();
  // },
}; // <-- Закрывающая скобка и точка с запятой добавлены