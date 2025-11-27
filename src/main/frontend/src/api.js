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

  // --- Аутентификация: POST /api/users/authenticate ---
  login: async (name, password) => {
    const credentials = encodeBase64(`${name}:${password}`);
    const response = await fetch('/api/users/authenticate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${credentials}`,
      },
      body: JSON.stringify({ name, password })
    });
    const userData = await handleResponse(response);

    // Сохраняем credentials и userId для последующих запросов
    storedCredentials = credentials;
    storedUserId = userData.id;
    localStorage.setItem('authCredentials', credentials);
    localStorage.setItem('userId', userData.id);

    return userData;
  },

  // --- Выход (очистка credentials и userId) ---
  logout: () => {
    storedCredentials = null;
    storedUserId = null;
    localStorage.removeItem('authCredentials');
    localStorage.removeItem('userId');
  },

  // --- Проверка аутентификации ---
  isAuthenticated: () => {
    if (storedCredentials) return true;

    const creds = localStorage.getItem('authCredentials');
    if (creds) {
      storedCredentials = creds;
      storedUserId = localStorage.getItem('userId');
      return true;
    }

    return false;
  },

  // --- Получение сохранённого userId ---
  getStoredUserId: () => {
    if (!storedUserId) {
      storedUserId = localStorage.getItem('userId');
    }
    return storedUserId;
  },

  // --- Получение сохранённых Base64-credentials (для использования вручную) ---
  getStoredCredentials: () => {
    if (!storedCredentials) {
      storedCredentials = localStorage.getItem('authCredentials');
    }
    return storedCredentials;
  },

  // --- Создание функции: POST /api/functions ---
  createFunction: async (functionData) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const response = await fetch('/api/functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(functionData),
    });
    return handleResponse(response);
  },

  // --- Создание табулированных точек: POST /api/tabulated-points ---
  createTabulatedPoint: async (functionId, xVal, yVal) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const pointData = {
      functionId: functionId,
      xVal: xVal,
      yVal: yVal
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

  // --- Дифференцирование функции ---
  differentiate: async (functionId, factoryType = 'array') => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionId,
      factoryType
    };

    const response = await fetch('/api/operations/differentiate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(payload),
    });
    return handleResponse(response);
  },

  // --- Вычисление определенного интеграла ---
  calculateIntegral: async (functionId, a, b, steps, threadCount) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      functionId,
      a,
      b,
      steps,
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
  createCompositeFunction: async (params) => {
    if (!storedCredentials) {
      throw new Error('Not authenticated. Please log in first.');
    }

    const payload = {
      userId: params.userId,
      baseFunctionName: params.baseFunctionName,
      outerFunctionName: params.outerFunctionName,
      customName: params.customName
    };

    const response = await fetch('/api/composite-functions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(payload),
    });
    return handleResponse(response);
  }
};