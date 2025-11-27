/**
 * Конфигурации для Chart.js
 */

export const createLineChartConfig = (points, options = {}) => {
  const defaultOptions = {
    title: 'График функции',
    xLabel: 'X',
    yLabel: 'Y',
    borderColor: '#4CAF50',
    backgroundColor: 'rgba(76, 175, 80, 0.1)',
    pointBackgroundColor: '#E91E63',
    pointBorderColor: '#FFFFFF',
    pointRadius: 5,
    pointHoverRadius: 8,
    tension: 0.1,
    responsive: true,
    maintainAspectRatio: false
  };

  const mergedOptions = { ...defaultOptions, ...options };

  return {
    type: 'line',
    data: {
      labels: points.map(p => p.x.toFixed(3)),
      datasets: [{
        label: mergedOptions.title,
        data: points.map(p => ({ x: p.x, y: p.y })),
        borderColor: mergedOptions.borderColor,
        backgroundColor: mergedOptions.backgroundColor,
        pointBackgroundColor: mergedOptions.pointBackgroundColor,
        pointBorderColor: mergedOptions.pointBorderColor,
        pointRadius: mergedOptions.pointRadius,
        pointHoverRadius: mergedOptions.pointHoverRadius,
        tension: mergedOptions.tension,
        fill: false
      }]
    },
    options: {
      responsive: mergedOptions.responsive,
      maintainAspectRatio: mergedOptions.maintainAspectRatio,
      plugins: {
        title: {
          display: true,
          text: mergedOptions.title
        },
        tooltip: {
          callbacks: {
            title: function(context) {
              return `x = ${context[0].parsed.x.toFixed(3)}`;
            },
            label: function(context) {
              return `y = ${context.parsed.y.toFixed(3)}`;
            }
          }
        },
        legend: {
          position: 'top'
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: mergedOptions.xLabel
          },
          type: 'linear',
          position: 'bottom'
        },
        y: {
          title: {
            display: true,
            text: mergedOptions.yLabel
          },
          beginAtZero: false
        }
      }
    }
  };
};

export const createBarChartConfig = (labels, data, options = {}) => {
  const defaultOptions = {
    title: 'Результаты сравнения',
    xLabel: 'Категории',
    yLabel: 'Значения',
    backgroundColor: 'rgba(54, 162, 235, 0.5)',
    borderColor: 'rgba(54, 162, 235, 1)',
    borderWidth: 1,
    responsive: true,
    maintainAspectRatio: false
  };

  const mergedOptions = { ...defaultOptions, ...options };

  return {
    type: 'bar',
     {
      labels: labels,
      datasets: [{
        label: mergedOptions.title,
        data: data,
        backgroundColor: mergedOptions.backgroundColor,
        borderColor: mergedOptions.borderColor,
        borderWidth: mergedOptions.borderWidth
      }]
    },
    options: {
      responsive: mergedOptions.responsive,
      maintainAspectRatio: mergedOptions.maintainAspectRatio,
      plugins: {
        title: {
          display: true,
          text: mergedOptions.title
        },
        legend: {
          position: 'top'
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: mergedOptions.xLabel
          }
        },
        y: {
          title: {
            display: true,
            text: mergedOptions.yLabel
          },
          beginAtZero: true
        }
      }
    }
  };
};

export const createScatterChartConfig = (points, options = {}) => {
  const defaultOptions = {
    title: 'Точки функции',
    xLabel: 'X',
    yLabel: 'Y',
    backgroundColor: 'rgba(255, 99, 132, 0.5)',
    borderColor: 'rgba(255, 99, 132, 1)',
    pointRadius: 6,
    pointHoverRadius: 8,
    responsive: true,
    maintainAspectRatio: false
  };

  const mergedOptions = { ...defaultOptions, ...options };

  return {
    type: 'scatter',
     {
      datasets: [{
        label: mergedOptions.title,
         points.map(p => ({ x: p.x, y: p.y })),
        backgroundColor: mergedOptions.backgroundColor,
        borderColor: mergedOptions.borderColor,
        pointRadius: mergedOptions.pointRadius,
        pointHoverRadius: mergedOptions.pointHoverRadius
      }]
    },
    options: {
      responsive: mergedOptions.responsive,
      maintainAspectRatio: mergedOptions.maintainAspectRatio,
      plugins: {
        title: {
          display: true,
          text: mergedOptions.title
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              return `(${context.parsed.x.toFixed(3)}, ${context.parsed.y.toFixed(3)})`;
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: mergedOptions.xLabel
          },
          type: 'linear'
        },
        y: {
          title: {
            display: true,
            text: mergedOptions.yLabel
          },
          type: 'linear'
        }
      }
    }
  };
};

export const createDoughnutChartConfig = (labels, data, options = {}) => {
  const defaultOptions = {
    title: 'Доли',
    backgroundColor: [
      'rgba(255, 99, 132, 0.8)',
      'rgba(54, 162, 235, 0.8)',
      'rgba(255, 206, 86, 0.8)',
      'rgba(75, 192, 192, 0.8)',
      'rgba(153, 102, 255, 0.8)',
      'rgba(255, 159, 64, 0.8)'
    ],
    borderColor: [
      'rgba(255, 99, 132, 1)',
      'rgba(54, 162, 235, 1)',
      'rgba(255, 206, 86, 1)',
      'rgba(75, 192, 192, 1)',
      'rgba(153, 102, 255, 1)',
      'rgba(255, 159, 64, 1)'
    ],
    borderWidth: 1,
    responsive: true,
    maintainAspectRatio: false
  };

  const mergedOptions = { ...defaultOptions, ...options };

  return {
    type: 'doughnut',
     {
      labels: labels,
      datasets: [{
        label: mergedOptions.title,
         data,
        backgroundColor: mergedOptions.backgroundColor,
        borderColor: mergedOptions.borderColor,
        borderWidth: mergedOptions.borderWidth
      }]
    },
    options: {
      responsive: mergedOptions.responsive,
      maintainAspectRatio: mergedOptions.maintainAspectRatio,
      plugins: {
        title: {
          display: true,
          text: mergedOptions.title
        },
        legend: {
          position: 'right'
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              const label = context.label || '';
              const value = context.raw || 0;
              const total = context.dataset.data.reduce((a, b) => a + b, 0);
              const percentage = total > 0 ? Math.round((value / total) * 100) : 0;
              return `${label}: ${value} (${percentage}%)`;
            }
          }
        }
      }
    }
  };
};

// Конфигурация для графика производительности
export const createPerformanceChartConfig = (threadCounts, executionTimes) => {
  return {
    type: 'bar',
     {
      labels: threadCounts.map(t => `${t} потоков`),
      datasets: [{
        label: 'Время выполнения (мс)',
         executionTimes,
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: 'Время (мс)'
          }
        },
        x: {
          title: {
            display: true,
            text: 'Количество потоков'
          }
        }
      },
      plugins: {
        title: {
          display: true,
          text: 'Зависимость времени выполнения от количества потоков'
        }
      }
    }
  };
};

// Конфигурация для графика интегрирования
export const createIntegrationChartConfig = (points, a, b) => {
  const config = createLineChartConfig(points, {
    title: 'Область интегрирования',
    borderColor: '#3498db'
  });

  // Добавляем вертикальные линии для границ интегрирования
  config.options.plugins.annotation = {
    annotations: {
      line1: {
        type: 'line',
        xMin: a,
        xMax: a,
        borderColor: '#e74c3c',
        borderWidth: 2,
        label: {
          display: true,
          content: 'a',
          position: 'top'
        }
      },
      line2: {
        type: 'line',
        xMin: b,
        xMax: b,
        borderColor: '#e74c3c',
        borderWidth: 2,
        label: {
          display: true,
          content: 'b',
          position: 'top'
        }
      }
    }
  };

  return config;
};

// Конфигурация для операций над функциями
export const createOperationsChartConfig = (pointsA, pointsB, resultPoints, operation) => {
  const colors = {
    add: ['#2ecc71', '#27ae60', '#2ecc71'],
    subtract: ['#e74c3c', '#c0392b', '#e74c3c'],
    multiply: ['#f39c12', '#d35400', '#f39c12'],
    divide: ['#3498db', '#2980b9', '#3498db']
  };

  const [colorA, colorB, colorResult] = colors[operation] || ['#3498db', '#2980b9', '#e74c3c'];

  return {
    type: 'line',
     {
      labels: resultPoints.map(p => p.x.toFixed(2)),
      datasets: [
        {
          label: 'Функция A',
           pointsA.map(p => ({ x: p.x, y: p.y })),
          borderColor: colorA,
          backgroundColor: `${colorA}80`,
          tension: 0.1,
          fill: false
        },
        {
          label: 'Функция B',
           pointsB.map(p => ({ x: p.x, y: p.y })),
          borderColor: colorB,
          backgroundColor: `${colorB}80`,
          tension: 0.1,
          fill: false
        },
        {
          label: `Результат: A ${operation} B`,
           resultPoints.map(p => ({ x: p.x, y: p.y })),
          borderColor: colorResult,
          backgroundColor: `${colorResult}80`,
          tension: 0.1,
          fill: false,
          borderWidth: 3
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        title: {
          display: true,
          text: `Результат операции: ${operation.toUpperCase()}`
        },
        tooltip: {
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'X'
          }
        },
        y: {
          title: {
            display: true,
            text: 'Y'
          }
        }
      }
    }
  };
};