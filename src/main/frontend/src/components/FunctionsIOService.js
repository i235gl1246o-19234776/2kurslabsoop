// src/main/frontend/src/services/FunctionsIOService.js
export class FunctionsIOService {
  // Сохранение функции в текстовом формате
  static saveAsText(points, fileName = 'function.txt') {
    let content = `${points.length}\n`;
    points.forEach(point => {
      content += `${point.x} ${point.y}\n`;
    });

    const blob = new Blob([content], { type: 'text/plain' });
    this._downloadBlob(blob, fileName);
  }

  // Загрузка функции из текстового формата
  static loadFromText(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const lines = event.target.result.split('\n').filter(line => line.trim() !== '');
          if (lines.length === 0) {
            reject(new Error('Файл пустой'));
            return;
          }

          const count = parseInt(lines[0].trim());
          if (isNaN(count) || count <= 0) {
            reject(new Error('Некорректное количество точек'));
            return;
          }

          const points = [];
          for (let i = 1; i <= count && i < lines.length; i++) {
            const parts = lines[i].trim().split(/\s+/);
            if (parts.length < 2) {
              throw new Error(`Некорректный формат данных в строке ${i}: ${lines[i]}`);
            }

            const x = parseFloat(parts[0]);
            const y = parseFloat(parts[1]);

            if (isNaN(x) || isNaN(y)) {
              throw new Error(`Ошибка парсинга чисел в строке ${i}: ${lines[i]}`);
            }

            points.push({ x, y });
          }

          if (points.length !== count) {
            throw new Error(`Ожидалось ${count} точек, получено ${points.length}`);
          }

          resolve(points);
        } catch (error) {
          reject(new Error(`Ошибка при загрузке функции из текстового файла: ${error.message}`));
        }
      };

      reader.onerror = () => {
        reject(new Error('Ошибка чтения файла'));
      };

      reader.readAsText(file);
    });
  }

  // Сохранение функции в JSON формате
  static saveAsJson(points, fileName = 'function.json') {
    const content = JSON.stringify(points, null, 2);
    const blob = new Blob([content], { type: 'application/json' });
    this._downloadBlob(blob, fileName);
  }

  // Загрузка функции из JSON формата
  static loadFromJson(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const parsedData = JSON.parse(event.target.result);

          // Если данные в формате { points: [...] }, извлекаем массив
          let points = parsedData;
          if (parsedData.points && Array.isArray(parsedData.points)) {
            points = parsedData.points;
          }

          // Проверяем и преобразуем точки к единому формату
          const normalizedPoints = points.map(p => ({
            x: p.x !== undefined ? p.x : (p.xVal !== undefined ? p.xVal : 0),
            y: p.y !== undefined ? p.y : (p.yVal !== undefined ? p.yVal : 0)
          }));

          if (normalizedPoints.length === 0) {
            throw new Error('Файл не содержит точек');
          }

          resolve(normalizedPoints);
        } catch (error) {
          reject(new Error(`Ошибка при загрузке функции из JSON файла: ${error.message}`));
        }
      };

      reader.onerror = () => {
        reject(new Error('Ошибка чтения файла'));
      };

      reader.readAsText(file);
    });
  }

  // Сохранение функции в бинарном формате
  static saveAsBinary(points, fileName = 'function.bin') {
    const arrayBuffer = new ArrayBuffer(4 + points.length * 16);
    const dataView = new DataView(arrayBuffer);

    dataView.setInt32(0, points.length, true);

    for (let i = 0; i < points.length; i++) {
      const offset = 4 + i * 16;
      dataView.setFloat64(offset, points[i].x, true);
      dataView.setFloat64(offset + 8, points[i].y, true);
    }

    const blob = new Blob([arrayBuffer], { type: 'application/octet-stream' });
    this._downloadBlob(blob, fileName);
  }

  // Загрузка функции из бинарного формата
  static loadFromBinary(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const arrayBuffer = event.target.result;
          const dataView = new DataView(arrayBuffer);

          const count = dataView.getInt32(0, true);
          if (count <= 0 || count > 10000) {
            throw new Error('Некорректное количество точек');
          }

          const expectedSize = 4 + count * 16;
          if (arrayBuffer.byteLength < expectedSize) {
            throw new Error('Файл поврежден или имеет неправильный размер');
          }

          const points = [];
          for (let i = 0; i < count; i++) {
            const offset = 4 + i * 16;
            const x = dataView.getFloat64(offset, true);
            const y = dataView.getFloat64(offset + 8, true);
            points.push({ x, y });
          }

          resolve(points);
        } catch (error) {
          reject(new Error(`Ошибка при загрузке функции из бинарного файла: ${error.message}`));
        }
      };

      reader.onerror = () => {
        reject(new Error('Ошибка чтения файла'));
      };

      reader.readAsArrayBuffer(file);
    });
  }

  // Вспомогательный метод для скачивания blob
  static _downloadBlob(blob, fileName) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();

    setTimeout(() => {
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    }, 0);
  }
}