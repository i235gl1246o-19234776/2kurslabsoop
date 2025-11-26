// src/api.js
const encodeBase64 = (str) => {
  return btoa(unescape(encodeURIComponent(str)));
};

const handleResponse = async (response) => {
  const contentType = response.headers.get('content-type');
  const responseText = await response.text();

  // Проверяем, является ли ответ HTML
  if (contentType && contentType.includes('text/html')) {
    if (responseText.includes('<!DOCTYPE html>') || responseText.includes('<html')) {
      throw new Error('Сервер вернул HTML страницу вместо JSON данных. Проверьте правильность эндпоинтов и настройки сервера.');
    }
  }

  if (response.ok) {
    try {
      return JSON.parse(responseText);
    } catch (e) {
      return responseText;
    }
  } else {
    let errorData;
    try {
      errorData = JSON.parse(responseText);
    } catch (e) {
      errorData = {
        error: responseText || response.statusText || 'Unknown error'
      };
    }
    const errorMessage = errorData.error || errorData.message || `Request failed with status ${response.status}`;
    const error = new Error(errorMessage);
    error.status = response.status;
    error.response = { data: errorData };
    throw error;
  }
};

let storedCredentials = null;
let storedUserId = null;

export const api = {
  login: async (name, password) => {
    try {
      const response = await fetch('/api/users/authenticate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: name,
          password: password
        }),
      });
      const userData = await handleResponse(response);
      // Сохраняем учетные данные для базовой аутентификации
      storedCredentials = encodeBase64(`${name}:${password}`);
      storedUserId = userData.id;
      return userData;
    } catch (error) {
      console.error('Ошибка входа:', error);
      throw error;
    }
  },

  getStoredUserId: () => {
    return storedUserId;
  },

  getStoredCredentials: () => {
    return storedCredentials;
  },

  isAuthenticated: () => {
    return !!storedCredentials;
  },

  logout: () => {
    storedCredentials = null;
    storedUserId = null;
  },

  createFunction: async (functionData) => {
    if (!storedCredentials || !storedUserId) {
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

  getFunctionsByUserId: async (userId) => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
    }

    const response = await fetch(`/api/functions?userId=${userId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  getTabulatedPointsByFunctionId: async (functionId) => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
    }

    const response = await fetch(`/api/tabulated-points/function/${functionId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  getAvailableFactories: async () => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
    }

    const response = await fetch('/api/factories', {
      method: 'GET',
      headers: {
        'Authorization': `Basic ${storedCredentials}`,
      },
    });

    return handleResponse(response);
  },

  calculateIntegral: async (functionId, a, b, steps, threadCount) => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
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
        'Authorization': `Basic ${storedCredentials}`
      },
      body: JSON.stringify(payload)
    });

    return handleResponse(response);
  },

  performOperation: async (functionIdA, functionIdB, operationTypeId) => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
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

  createTabulatedPoints: async (functionId, xval, yval) => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
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

  createCompositeFunction: async (functionData) => {
    try {
      if (!storedCredentials || !storedUserId) {
        throw new Error('User ID not available. Please log in again.');
      }

      const response = await fetch('/api/functions/composite', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${storedCredentials}`
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

  differentiateFunction: async (functionId, factoryType = 'array') => {
    if (!storedCredentials || !storedUserId) {
      throw new Error('User ID not available. Please log in again.');
    }

    const payload = {
      functionId,
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
  }
};