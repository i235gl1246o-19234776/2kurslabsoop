// src/api.js

// - Вспомогательная функция для кодирования в Base64 -
const encodeBase64 = (str) => {
  return btoa(unescape(encodeURIComponent(str)));
};

// - Улучшенная обработка ответов с логированием -
const handleResponse = async (response, url) => {
  console.log(`📊 Ответ от ${url}: Статус ${response.status}`);

  // Читаем текст один раз
  const text = await response.text();
  console.log(`📄 Тело ответа: ${text.substring(0, 500)}`);

  let data;
  try {
    data = JSON.parse(text);
  } catch (e) {
    data = text;
  }

  if (response.ok) {
    return data;
  }

  // Формируем детальное сообщение об ошибке
  let errorMessage;
  if (typeof data === 'string') {
    errorMessage = data;
  } else if (data && typeof data === 'object') {
    errorMessage = data.error || data.message || `Request failed with status ${response.status}`;
  } else {
    errorMessage = `Request failed with status ${response.status}`;
  }

  const error = new Error(errorMessage);
  error.status = response.status;
  error.responseData = data;
  throw error;
};

// - Хранилище данных аутентификации -
let storedCredentials = null;
let storedUserId = null;

// - Экспортируем объект api -
export const api = {
  login: async (name, password) => {
    const credentials = encodeBase64(`${name}:${password}`);
    const url = `/api/users/name/${encodeURIComponent(name)}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    const userData = await handleResponse(response, url);
    storedCredentials = credentials;
    storedUserId = userData.id;
    return userData;
  },

  logout: () => {
    storedCredentials = null;
    storedUserId = null;
  },

  isAuthenticated: () => !!storedCredentials,

  getStoredUserId: () => storedUserId,

  getStoredCredentials: () => storedCredentials,

  createFunction: async (functionData) => {
    if (!storedCredentials) throw new Error('Not authenticated');

    const url = '/api/functions';
    const bodyData = {
      ...functionData,
      userId: storedUserId
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(bodyData),
    });

    return handleResponse(response, url);
  },

  createTabulatedPoints: async (functionId, xval, yval) => {
    if (!storedCredentials) throw new Error('Not authenticated');

    const url = '/api/tabulated-points';
    const pointData = {
      functionId: functionId,
      xval: xval,
      yval: yval
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(pointData),
    });

    return handleResponse(response, url);
  },

  getFunctionsByUserId: async (userId) => {
    if (!storedCredentials) throw new Error('Not authenticated');

    const url = `/api/functions?userId=${userId}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    let functions = await handleResponse(response, url);

    // Нормализуем данные: добавляем functionId, если его нет
    if (Array.isArray(functions)) {
      functions = functions.map(func => ({
        ...func,
        functionId: func.id || func.functionId,
        tabulatedValueIds: func.tabulatedValueIds || [],
        insertable: func.insertable || false,
        removable: func.removable || false
      }));
    }

    console.log('🎯 Полученные функции:', functions);
    return functions;
  },

  getTabulatedPointsByFunctionId: async (functionId) => {
    if (!storedCredentials) throw new Error('Not authenticated');

    const url = `/api/tabulated-points/function/${functionId}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    const points = await handleResponse(response, url);

    if (Array.isArray(points)) {
      return points.map(point => ({
        id: point.id || point.tabulatedValueId,
        functionId: point.functionId,
        xval: point.xval !== undefined ? point.xval : point.x,
        yval: point.yval !== undefined ? point.yval : point.y
      }));
    } else if (points && points.data && Array.isArray(points.data)) {
      // Если сервер вернул { data: [...] }
      return points.data.map(point => ({
        id: point.id || point.tabulatedValueId,
        functionId: point.functionId,
        xval: point.xval !== undefined ? point.xval : point.x,
        yval: point.yval !== undefined ? point.yval : point.y
      }));
    } else if (points && points.points && Array.isArray(points.points)) {
      // Если сервер вернул { points: [...] }
      return points.points.map(point => ({
        id: point.id || point.tabulatedValueId,
        functionId: point.functionId,
        xval: point.xval !== undefined ? point.xval : point.x,
        yval: point.yval !== undefined ? point.yval : point.y
      }));
    } else {
      // Если структура неизвестна, пробуем извлечь точки
      console.warn('Неизвестная структура ответа с точками:', points);
      return [];
    }
  },

  calculateAndSaveTabulatedPoints: async (functionId, mathFunctionName, start, end, count, factoryType) => {
    if (!storedCredentials) throw new Error('Not authenticated');

    const url = '/api/tabulated-points/calculate';
    const calculateData = {
      functionId: functionId,
      mathFunctionName: mathFunctionName,
      start: start,
      end: end,
      count: count,
      factoryType: factoryType || localStorage.getItem('tabulatedFunctionFactory') || 'array'
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Basic ${storedCredentials}`,
      },
      body: JSON.stringify(calculateData),
    });

    return handleResponse(response, url);
  },

  // - Выполнение операции над функциями -
  executeOperation: async (functionIdA, functionIdB, operationTypeId) => {
    if (!storedCredentials) throw new Error('Not authenticated');

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

  // - Вычисление определенного интеграла -
  calculateIntegral: async function(functionId, a, b, steps, threadCount) {
    if (!storedCredentials) throw new Error('Not authenticated');

    const payload = {
      functionId: functionId,
      a: parseFloat(a),
      b: parseFloat(b),
      steps: parseInt(steps),
      threads: parseInt(threadCount),
      factoryType: localStorage.getItem('tabulatedFunctionFactory') || 'array'
    };

    try {
      console.log('Отправка запроса на вычисление интеграла:', payload);

      const response = await fetch('/api/integrate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${storedCredentials}`,
        },
        body: JSON.stringify(payload),
      });

      const result = await handleResponse(response, '/api/integrate');
      console.log('Успешный ответ от сервера при вычислении интеграла:', result);
      return result;
    } catch (error) {
      console.error('Ошибка при запросе к интегралу:', error);
      throw error;
    }
  },

  // - Дифференцирование функции -
  differentiateFunction: async function(functionId, step = 0.001, factoryType = 'array') {
    if (!storedCredentials) throw new Error('Not authenticated');

    const payload = {
      functionId: functionId,
      step: step,
      factoryType: factoryType
    };

    try {
      console.log('Отправка запроса на дифференцирование:', payload);

      const response = await fetch('/api/differentiate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${storedCredentials}`,
        },
        body: JSON.stringify(payload),
      });

      const result = await handleResponse(response, '/api/differentiate');
      console.log('Успешный ответ от сервера при дифференцировании:', result);
      return result;
    } catch (error) {
      console.error('Ошибка при запросе к дифференцированию:', error);
      throw error;
    }
  },

  // - Получение доступных фабрик -
  getAvailableFactories: async function() {
    if (!storedCredentials) throw new Error('Not authenticated');

    const response = await fetch('/api/factories', {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  // - Создание составной функции -
  createCompositeFunction: async function(functionData) {
    try {
      const url = '/api/composite-functions';
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${storedCredentials}`,
        },
        body: JSON.stringify(functionData),
      });

      if (!response.ok) {
        // Читаем текст ошибки для детального сообщения
        const errorText = await response.text();
        try {
          const errorJson = JSON.parse(errorText);
          throw new Error(`HTTP error! status: ${response.status}, message: ${errorJson.message || errorJson.error}`);
        } catch (e) {
          throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
        }
      }

      return await response.json();
    } catch (error) {
      console.error('Error creating composite function:', error);
      throw error;
    }
  },

  // - Получение всех точек для функции (для составных) -
  getAllPointsForFunction: async (functionId) => {
    if (!storedCredentials) throw new Error('Not authenticated');

    // Сначала получаем информацию о функции
    const userId = storedUserId;
    const functions = await api.getFunctionsByUserId(userId);
    const functionInfo = functions.find(f =>
      parseInt(f.functionId || f.id) === parseInt(functionId)
    );

    if (!functionInfo) {
      throw new Error('Function not found');
    }

    // Затем получаем точки
    const points = await api.getTabulatedPointsByFunctionId(functionId);

    return {
      functionInfo,
      points
    };
  },
};