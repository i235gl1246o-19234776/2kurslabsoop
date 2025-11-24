// src/main/frontend/src/api.js
import { getAuthHeaders } from './auth.js';

const API_BASE_URL = 'http://localhost:8080/api';

// Основной метод для выполнения операций
async function performOperation(operation, operand1Id, operand2Id, factoryType) {
    // Используем переданный factoryType или получаем тип фабрики из localStorage как fallback
    const effectiveFactoryType = factoryType || localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
    console.log('Отправка запроса на операцию:', {
        operation,
        operand1Id,
        operand2Id,
        factoryType: effectiveFactoryType
    });
    const requestDto = {
        operand1Id: operand1Id,
        operand2Id: operand2Id,
        factoryType: effectiveFactoryType
    };
    const response = await fetch(`${API_BASE_URL}/operations/${operation}`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(requestDto),
    });
    if (!response.ok) {
        const errorText = await response.text().catch(() => '');
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка операции: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка операции: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function performAddition(operand1Id, operand2Id, factoryType) {
    return performOperation('add', operand1Id, operand2Id, factoryType);
}

export async function performSubtraction(operand1Id, operand2Id, factoryType) {
    return performOperation('subtract', operand1Id, operand2Id, factoryType);
}

export async function performMultiplication(operand1Id, operand2Id, factoryType) {
    return performOperation('multiply', operand1Id, operand2Id, factoryType);
}

export async function performDivision(operand1Id, operand2Id, factoryType) {
    return performOperation('divide', operand1Id, operand2Id, factoryType);
}

export async function createFunction(functionDto) {
    console.log('Отправка запроса на создание функции:', functionDto);
    const response = await fetch(`${API_BASE_URL}/functions`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(functionDto),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка создания функции: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка создания функции: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function getFunctionById(id) {
    console.log('Запрос функции по ID:', id);
    const response = await fetch(`${API_BASE_URL}/functions/${id}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка получения функции: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка получения функции: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function getAllFunctionsByUserId(userId) {
    const response = await fetch(`${API_BASE_URL}/functions/user/${userId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка загрузки функций: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка загрузки функций: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function performIntegration(functionId, fromX, toX, threadCount, factoryType) {
    console.log('Отправка запроса на вычисление интеграла:', {
        functionId,
        fromX,
        toX,
        threadCount,
        factoryType
    });
    const requestDto = {
        functionId: functionId,
        fromX: fromX,
        toX: toX,
        threadCount: threadCount,
        factoryType: factoryType
    };
    const response = await fetch(`${API_BASE_URL}/operations/integrate`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(requestDto),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка интегрирования: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка интегрирования: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function createCompositeFunction(compositeDto) {
    console.log('Отправка запроса на создание сложной функции:', compositeDto);
    const response = await fetch(`${API_BASE_URL}/functions/composite`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(compositeDto),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка создания сложной функции: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка создания сложной функции: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function getAllPointsByFunctionId(functionId) {
    console.log('Запрос точек для функции с ID:', functionId);
    const response = await fetch(`${API_BASE_URL}/tabulated-points/function/${functionId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка получения точек: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка получения точек: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function createFunctionFromMath(creationDto) {
    const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
    const dtoWithFactory = { ...creationDto, factoryType };
    console.log('Отправка запроса на создание функции из Math:', dtoWithFactory);
    const response = await fetch(`${API_BASE_URL}/functions/from-math`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(dtoWithFactory),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка создания функции из Math: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка создания функции из Math: ${response.status} - ${errorText}`);
        }
    }
    return response.json();
}

export async function createTabulatedPoints(functionId, xValues, yValues) {
    console.log('Создание точек для функции с ID:', functionId, 'Количество точек:', xValues.length);
    const promises = xValues.map((x, i) =>
        fetch(`${API_BASE_URL}/tabulated-points`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                functionId: functionId,
                xVal: x,
                yVal: yValues[i]
            }),
        })
    );
    const responses = await Promise.all(promises);
    for (let i = 0; i < responses.length; i++) {
        if (!responses[i].ok) {
            const errorText = await responses[i].text();
            try {
                const errorJson = JSON.parse(errorText);
                throw new Error(`Ошибка создания точки ${i}: ${responses[i].status} - ${errorJson.message || errorText}`);
            } catch (e) {
                throw new Error(`Ошибка создания точки ${i}: ${responses[i].status} - ${errorText}`);
            }
        }
    }
    console.log('Точки успешно созданы для функции с ID:', functionId);
    return functionId;
}

export async function performDifferentiation(functionId, factoryType = 'array') {
    console.log('Отправка запроса на дифференцирование:', { functionId, factoryType });

    if (!functionId || typeof functionId !== 'number' || isNaN(functionId)) {
        throw new Error('Неверный ID функции для дифференцирования. Ожидалось число.');
    }

    // Проверяем, что функция существует
    try {
        await getFunctionById(functionId);
    } catch (error) {
        throw new Error('Функция не существует. Невозможно выполнить дифференцирование.');
    }

    const requestDto = {
        functionId: functionId,
        factoryType: factoryType
    };
    const response = await fetch(`${API_BASE_URL}/operations/differentiate`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(requestDto),
    });
    if (!response.ok) {
        const errorText = await response.text();
        try {
            const errorJson = JSON.parse(errorText);
            throw new Error(`Ошибка дифференцирования: ${response.status} - ${errorJson.message || errorText}`);
        } catch (e) {
            throw new Error(`Ошибка дифференцирования: ${response.status} - ${errorText}`);
        }
    }
    const result = await response.json();
    console.log('Результат дифференцирования:', result);
    return result;
}

export async function getAvailableMathFunctionNames() {
    const headers = getAuthHeaders();
    try {
        console.log('getAvailableMathFunctionNames: вызван');
        console.log('getAvailableMathFunctionNames: заголовки', headers);
        const response = await fetch(`${API_BASE_URL}/functions/math-functions`, {
            method: 'GET',
            headers: headers,
        });
        console.log('getAvailableMathFunctionNames: получен ответ', response);
        if (!response.ok) {
            const errorText = await response.text();
            console.log('getAvailableMathFunctionNames: ошибка ответа', response.status, errorText);
            try {
                const errorJson = JSON.parse(errorText);
                throw new Error(`Ошибка загрузки функций: ${response.status} - ${errorJson.message || errorText}`);
            } catch (e) {
                throw new Error(`Ошибка загрузки функций: ${response.status} - ${errorText}`);
            }
        }
        const data = await response.json();
        console.log('getAvailableMathFunctionNames: полученные данные', data);
        return data; // Ожидаем массив строк
    } catch (err) {
        console.error('getAvailableMathFunctionNames: Ошибка при загрузке имён функций:', err);
        throw new Error(`Ошибка при загрузке имён функций: ${err.message}`);
    }
}