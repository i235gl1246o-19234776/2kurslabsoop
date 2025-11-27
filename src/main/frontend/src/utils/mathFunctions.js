/**
 * Утилиты для работы с математическими функциями
 */

// Базовые математические функции
export const mathFunctions = {
  identity: (x) => x,
  sqr: (x) => x * x,
  cube: (x) => x * x * x,
  sqrt: (x) => x >= 0 ? Math.sqrt(x) : NaN,
  sin: (x) => Math.sin(x),
  cos: (x) => Math.cos(x),
  tan: (x) => Math.tan(x),
  exp: (x) => Math.exp(x),
  ln: (x) => x > 0 ? Math.log(x) : NaN,
  log10: (x) => x > 0 ? Math.log10(x) : NaN,
  abs: (x) => Math.abs(x),
  constant: (c) => (x) => c
};

// Создание составной функции
export const createCompositeFunction = (outerFn, innerFn) => {
  return (x) => outerFn(innerFn(x));
};

// Создание линейной комбинации функций
export const createLinearCombination = (...fns) => {
  return (x) => fns.reduce((sum, fn) => sum + fn(x), 0);
};

// Интерполяция по методу линейной интерполяции
export const linearInterpolation = (points, x) => {
  if (points.length < 2) return null;

  // Сортируем точки по x
  const sortedPoints = [...points].sort((a, b) => a.x - b.x);

  // Если x меньше минимального значения
  if (x <= sortedPoints[0].x) {
    return sortedPoints[0].y;
  }

  // Если x больше максимального значения
  if (x >= sortedPoints[sortedPoints.length - 1].x) {
    return sortedPoints[sortedPoints.length - 1].y;
  }

  // Находим интервал, содержащий x
  for (let i = 0; i < sortedPoints.length - 1; i++) {
    if (x >= sortedPoints[i].x && x <= sortedPoints[i + 1].x) {
      const x0 = sortedPoints[i].x;
      const y0 = sortedPoints[i].y;
      const x1 = sortedPoints[i + 1].x;
      const y1 = sortedPoints[i + 1].y;

      // Линейная интерполяция
      return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }
  }

  return null;
};

// Численное дифференцирование
export const numericalDerivative = (fn, x, h = 1e-5) => {
  return (fn(x + h) - fn(x - h)) / (2 * h);
};

// Численное интегрирование методом трапеций
export const trapezoidalIntegration = (fn, a, b, n = 1000) => {
  const h = (b - a) / n;
  let sum = 0.5 * (fn(a) + fn(b));

  for (let i = 1; i < n; i++) {
    sum += fn(a + i * h);
  }

  return sum * h;
};

// Численное интегрирование методом Симпсона
export const simpsonIntegration = (fn, a, b, n = 1000) => {
  if (n % 2 !== 0) n++;
  const h = (b - a) / n;
  let sum = fn(a) + fn(b);

  for (let i = 1; i < n; i++) {
    const x = a + i * h;
    sum += (i % 2 === 0) ? 2 * fn(x) : 4 * fn(x);
  }

  return sum * h / 3;
};

// Парсинг математического выражения в функцию
export const parseMathExpression = (expression) => {
  // Упрощенная версия - в реальном приложении стоит использовать библиотеку math.js
  try {
    // Замена ^ на ** для возведения в степень
    const normalized = expression.replace(/\^/g, '**');

    return new Function('x', `return ${normalized};`);
  } catch (e) {
    console.error('Ошибка парсинга математического выражения:', e);
    return () => NaN;
  }
};

// Форматирование числа для отображения
export const formatNumber = (num, precision = 6) => {
  if (typeof num !== 'number' || isNaN(num)) return 'N/A';

  // Для очень маленьких или очень больших чисел используем научную нотацию
  if (Math.abs(num) < 1e-4 || Math.abs(num) > 1e4) {
    return num.toExponential(precision);
  }

  return num.toFixed(precision);
};

// Генерация точек для табуляции функции
export const tabulateFunction = (fn, start, end, count) => {
  if (count <= 0) throw new Error('Количество точек должно быть положительным');
  if (start >= end) throw new Error('Начало интервала должно быть меньше конца');

  const points = [];
  const step = (end - start) / (count - 1);

  for (let i = 0; i < count; i++) {
    const x = start + i * step;
    const y = fn(x);
    points.push({ x, y });
  }

  return points;
};

// Проверка на уникальность x-значений
export const hasUniqueXValues = (points) => {
  const xValues = points.map(p => p.x);
  return new Set(xValues).size === xValues.length;
};

// Проверка на упорядоченность по x
export const isSortedByX = (points) => {
  for (let i = 1; i < points.length; i++) {
    if (points[i].x < points[i - 1].x) {
      return false;
    }
  }
  return true;
};

// Объединение двух наборов точек с одинаковыми x-значениями
export const mergePointsWithSameX = (pointsA, pointsB, operation = (a, b) => a + b) => {
  if (pointsA.length !== pointsB.length) {
    throw new Error('Наборы точек должны иметь одинаковое количество элементов');
  }

  const result = [];

  for (let i = 0; i < pointsA.length; i++) {
    const xA = pointsA[i].x;
    const xB = pointsB[i].x;

    if (Math.abs(xA - xB) > 1e-9) {
      throw new Error(`Несовпадение x-значений в точке ${i}: ${xA} и ${xB}`);
    }

    const yA = pointsA[i].y;
    const yB = pointsB[i].y;

    result.push({
      x: xA,
      y: operation(yA, yB)
    });
  }

  return result;
};