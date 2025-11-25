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
let storedUserId = null;

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
    storedUserId = userData.id;

    return userData;
  },

  // --- Выход (очистка credentials и userId) ---
  logout: () => {
    storedCredentials = null;
    storedUserId = null;
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
  createFunction: async (functionData) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    if (!storedUserId) {
      throw new Error('User ID not available. Please log in again.');
    }

    const bodyData = {
        functionName: functionData.functionName,
        functionExpression: functionData.functionExpression,
        typeFunction: functionData.typeFunction,
        userId: storedUserId,
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
  createTabulatedPoints: async (functionId, xval, yval) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const pointData = {
        functionId: functionId,
        xval: xval,
        yval: yval
    };

    const response = await fetch('/api/tabulated-points', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(pointData),
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

  // --- Вычисление и сохранение табулированных точек из MathFunction ---
  calculateAndSaveTabulatedPoints: async (functionId, mathFunctionName, start, end, count, factoryType) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const calculateData = {
        functionId: functionId,
        mathFunctionName: mathFunctionName,
        start: start,
        end: end,
        count: count,
        factoryType: factoryType
    };

    const response = await fetch('/api/tabulated-points/calculate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(calculateData),
    });

    return handleResponse(response);
  },

  // --- Получение функций пользователя: GET /api/functions?userId={userId} ---
  getFunctionsByUserId: async (userId) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch(`/api/functions?userId=${userId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- ПОЛУЧЕНИЕ ВСЕХ ФУНКЦИЙ (для администраторов) ---
  getAllFunctions: async () => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/functions/all', {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // --- Получение табулированных точек по functionId ---
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

  // --- Выполнение операции над двумя табулированными функциями ---
  executeOperation: async (functionIdA, functionIdB, operation, factoryType = 'array') => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionIdA,
      functionIdB,
      operation,
      factoryType
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

  // --- Интерполяция табулированной функции ---
  interpolateFunction: async (functionId, method, start, end, step, pointCount) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      method,
      start,
      end,
      step,
      pointCount
    };

    const response = await fetch(`/api/functions/${functionId}/interpolate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(payload),
    });

    return handleResponse(response);
  },

  // --- Выполнение операции над двумя функциями ---
  performOperation: async (functionIdA, functionIdB, operationTypeId) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionIdA,
      functionIdB,
      operationTypeId
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

  // --- ВЫЧИСЛЕНИЕ ОПРЕДЕЛЕННОГО ИНТЕГРАЛА (ИСПРАВЛЕННЫЙ МЕТОД) ---
  calculateIntegral: async function(functionId, a, b, steps, threadCount) {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionId: functionId,
      a: parseFloat(a),
      b: parseFloat(b),
      steps: parseInt(steps),
      threadCount: parseInt(threadCount)
    };

    const response = await fetch('/api/integration', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}` // Исправлено: Basic вместо Bearer
      },
      body: JSON.stringify(payload)
    });

    return handleResponse(response); // Используем общий обработчик ответов
  },

  // --- Создание составной функции ---
  // В api.js добавьте метод для создания составной функции
  createCompositeFunction: async function(functionData) {
      try {
          const response = await fetch('/api/composite-functions', {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json',
              },
              body: JSON.stringify(functionData)
          });

          if (!response.ok) {
              const errorText = await response.text();
              throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
          }

          return await response.json();
      } catch (error) {
          console.error('Error creating composite function:', error);
          throw error;
      }
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