// src/main/frontend/src/api.js
import { getAuthHeaders } from './auth.js';

const API_BASE_URL = 'http://localhost:8080/api';

export const createFunction = async (functionName, typeFunction, functionExpression, userId) => {
  const response = await fetch(`${API_BASE_URL}/functions`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({
      functionName: functionName,
      typeFunction: typeFunction,
      functionExpression: functionExpression,
      userId: userId
    }),
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
};

export const createFunctionFromMath = async (creationDto) => {
  // Получаем сохранённый тип фабрики из localStorage
  const factoryType = localStorage.getItem('selectedTabulatedFunctionFactory') || 'array';
  // Добавляем его к DTO
  const dtoWithFactory = { ...creationDto, factoryType };

  const response = await fetch(`${API_BASE_URL}/functions/from-math`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(dtoWithFactory), // Передаём обновлённый DTO
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const errorMessage = errorData.message || `HTTP error! status: ${response.status}`;
    throw new Error(errorMessage);
  }

  return response.json();
};

export const createTabulatedPoints = async (functionId, xValues, yValues) => {
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

  return functionId;
};

// --- НОВАЯ функция вынесена отдельно ---
export const getAvailableMathFunctionNames = async () => {
  console.log('getAvailableMathFunctionNames: вызван'); // <-- Отладка
  const headers = getAuthHeaders();
  console.log('getAvailableMathFunctionNames: заголовки', headers); // <-- Отладка

  const response = await fetch(`${API_BASE_URL}/functions/math-functions`, {
    method: 'GET',
    headers: headers,
  });

  console.log('getAvailableMathFunctionNames: получен ответ', response); // <-- Отладка

  if (!response.ok) {
    const errorText = await response.text();
    console.log('getAvailableMathFunctionNames: ошибка ответа', response.status, errorText); // <-- Отладка
    try {
      const errorJson = JSON.parse(errorText);
      throw new Error(`Ошибка загрузки функций: ${response.status} - ${errorJson.message || errorText}`);
    } catch (e) {
      throw new Error(`Ошибка загрузки функций: ${response.status} - ${errorText}`);
    }
  }

  const data = await response.json();
  console.log('getAvailableMathFunctionNames: полученные данные', data); // <-- Отладка
  return data; // Ожидаем массив строк
};