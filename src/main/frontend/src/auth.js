// src/main/frontend/src/auth.js
// Это упрощённый способ управления аутентификацией.
// В реальности использовался бы localStorage, Pinia/Vuex, и токены.
let currentCredentials = null;

export const setCredentials = (username, password) => {
  currentCredentials = { username, password };
};

export const getAuthHeaders = () => {
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

export const isAuthenticated = () => !!currentCredentials;

// Добавим функцию для очистки данных при выходе
export const clearCredentials = () => {
  currentCredentials = null;
};