// src/main/frontend/src/services/FunctionFileService.js
export class FunctionFileService {
  // Сохранение функции в файл (браузер)
  static saveFunctionToFile(points, fileName = 'function.txt') {
    const serializedData = JSON.stringify(points, null, 2);
    const blob = new Blob([serializedData], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    // Очистка
    setTimeout(() => {
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    }, 0);
  }

  // Загрузка функции из файла (браузер)
  static loadFunctionFromFile(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const functionString = event.target.result;
          const parsedData = JSON.parse(functionString);

          // Проверяем формат данных
          if (Array.isArray(parsedData) && parsedData.length > 0 &&
              parsedData[0].x !== undefined && parsedData[0].y !== undefined) {
            resolve(parsedData);
          } else {
            reject(new Error('Неверный формат данных в файле'));
          }
        } catch (error) {
          reject(new Error(`Ошибка при загрузке функции: ${error.message}`));
        }
      };
      reader.onerror = () => {
        reject(new Error('Ошибка чтения файла'));
      };
      reader.readAsText(file);
    });
  }
}