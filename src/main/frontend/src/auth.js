// src/main/frontend/src/auth.js
let currentCredentials = null;

export const setCredentials = (username, password) => {
  currentCredentials = { username, password };
  // Сохраняем в localStorage для сохранения состояния при перезагрузке
  localStorage.setItem('auth', JSON.stringify({ username }));
};

export const getAuthHeaders = () => {
  if (!currentCredentials) {
    const authData = localStorage.getItem('auth');
    if (authData) {
      try {
        const { username } = JSON.parse(authData);
        // Восстанавливаем только имя пользователя
        currentCredentials = { username, password: 'admin123' }; // Используем пароль по умолчанию
      } catch (e) {
        console.warn('Ошибка восстановления данных аутентификации:', e);
      }
    }
  }

  if (!currentCredentials) {
    console.warn("Пользователь не аутентифицирован при попытке вызова API.");
    return { 'Content-Type': 'application/json' };
  }

  const credentials = btoa(`${currentCredentials.username}:${currentCredentials.password}`);
  return {
    'Authorization': `Basic ${credentials}`,
    'Content-Type': 'application/json',
  };
};

export const isAuthenticated = () => {
  if (!currentCredentials) {
    const authData = localStorage.getItem('auth');
    if (authData) {
      try {
        const { username } = JSON.parse(authData);
        currentCredentials = { username, password: 'admin123' };
      } catch (e) {
        console.warn('Ошибка восстановления данных аутентификации:', e);
      }
    }
  }
  return !!currentCredentials;
};

export const clearCredentials = () => {
  currentCredentials = null;
  localStorage.removeItem('auth');
};