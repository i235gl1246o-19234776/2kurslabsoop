// src/api.js

// --- Вспомогательная функция для кодирования в Base64 ---
const encodeBase64 = (str) => {
  return btoa(unescape(encodeURIComponent(str)));
};

// --- Вспомогательная функция для обработки ответов сервера ---
const handleResponse = async (response) => {
  if (response.ok) {
    try {
      return await response.json();
    } catch (e) {
      // Если ответ не JSON, возвращаем текст
      return await response.text();
    }
  }

  // Попытка получить детальное сообщение об ошибке из ответа
  let errorData;
  try {
    errorData = await response.json();
  } catch (e) {
    // Если не удалось распарсить JSON, создаем базовый объект ошибки
    errorData = response.statusText ? { error: response.statusText } : { error: 'Unknown error' };
  }

  // Формируем сообщение об ошибке
  const errorMessage = errorData.error || errorData.message || `Request failed with status ${response.status}`;
  const error = new Error(errorMessage);
  error.status = response.status;
  error.response = { data: errorData };

  throw error;
};

// --- Хранилище данных аутентификации ---
let storedCredentials = null;
let storedUserId = null; // <-- НОВОЕ: храним userId

// --- Экспортируем объект api ---
export const api = {
  // --- Регистрация: POST /api/users ---
  register: async (name, password) => {
    const response = await fetch('/api/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name, password }),
    });

    return handleResponse(response);
  },

  // --- Аутентификация: GET /api/users/name/{name} с Basic Auth ---
  login: async (name, password) => {
    const credentials = encodeBase64(`${name}:${password}`);

    const response = await fetch(`/api/users/name/${encodeURIComponent(name)}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    const userData = await handleResponse(response);

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

  // --- Получение сохранённых Base64-credentials (для использования вручную) ---
  getStoredCredentials: () => {
    return storedCredentials;
  },

  // --- Получение информации о текущем пользователе ---
  getCurrentUser: async () => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/users/me', {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
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

    const response = await fetch('/api/functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(bodyData),
    });

    return handleResponse(response);
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

    return handleResponse(response);
  },

  // --- Обновление табулированной точки: PUT /api/tabulated-points/function/{functionId}/x/{oldX} ---
  updateTabulatedPoint: async (functionId, oldX, newX, newY) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/tabulated-points/function/${functionId}/x/${oldX}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify({ newX, newY }),
    });

    return handleResponse(response);
  },

  // --- Удаление табулированной точки: DELETE /api/tabulated-points/function/{functionId}/x/{xValue} ---
  deleteTabulatedPoint: async (functionId, xValue) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/tabulated-points/function/${functionId}/x/${xValue}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- Вычисление и сохранение табулированных точек из MathFunction: POST /api/tabulated-points/calculate (требует аутентификации) ---
  // Тело запроса: { "functionId": 123, "mathFunctionName": "SqrFunction", "start": 0.0, "end": 10.0, "count": 100 }
  calculateAndSaveTabulatedPoints: async (functionId, mathFunctionName, start, end, count, factoryType) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    // Формируем тело запроса
    const calculateData = {
        functionId: functionId,
        mathFunctionName: mathFunctionName, // Имя функции, как оно зарегистрировано на сервере
        start: start,
        end: end,
        count: count,
        factoryType: factoryType
    };

    const response = await fetch('/api/tabulated-points/calculate', { // <-- НОВЫЙ URL
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(calculateData), // <-- Отправляем сформированный объект
    });

    return handleResponse(response);
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

    return handleResponse(response);
  },

  // --- ПОЛУЧЕНИЕ ВСЕХ ФУНКЦИЙ (для администраторов) ---
  getAllFunctions: async () => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/functions/all', { // <-- URL для получения всех функций
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- НОВОЕ: Получение табулированных точек по functionId ---
  getTabulatedPointsByFunctionId: async (functionId) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/tabulated-points/function/${functionId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- НОВОЕ: Выполнение операции над двумя табулированными функциями ---
  executeOperation: async (functionIdA, functionIdB, operation, factoryType = 'array') => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionIdA,
      functionIdB,
      operation,        // "add", "subtract", "multiply", "divide"
      factoryType       // "array" или "linked-list"
    };

    const response = await fetch('/api/operations/execute', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(payload),
    });

    return handleResponse(response);
  },

  // --- Выполнение дифференцирования функции ---
  differentiateFunction: async (functionId, step = 0.001, factoryType = 'array') => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionId,
      step,
      factoryType
    };

    const response = await fetch('/api/differentiation', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(payload),
    });

    return handleResponse(response);
  },

  // --- Вычисление значения функции в точке с использованием apply() ---
  applyFunction: async (functionId, xValue) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/functions/${functionId}/apply?x=${xValue}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- Проверка поддержки интерфейсов Insertable и Removable ---
  checkFunctionInterfaces: async (functionId) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/functions/${functionId}/interfaces`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- Получение информации о типах поддерживаемых математических функций ---
  getAvailableMathFunctions: async () => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/math-functions', {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- Получение информации о типах фабрик ---
  getAvailableFactories: async () => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/factories', {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- Вычисление определенного интеграла ---
  calculateIntegral: async (functionId, a, b, n, threadCount) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionId,
      a,
      b,
      n,
      threadCount
    };

    const response = await fetch('/api/integration', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(payload),
    });

    return handleResponse(response);
  },

  // --- Создание составной функции ---
  createCompositeFunction: async (functionData) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/composite-functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(functionData),
    });

    return handleResponse(response);
  },

  // --- Удаление функции ---
  deleteFunction: async (functionId) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/functions/${functionId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  }
};