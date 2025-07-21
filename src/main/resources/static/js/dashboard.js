/**
 * Этот скрипт инициализирует основной функционал дашборда приложения XAI Observer.
 * Он отвечает за:
 * - Загрузку и отображение общей статистики.
 * - Рендеринг графиков Pass Rate Trend и Top Slow Tests с использованием Chart.js.
 * - Загрузку и отображение детальной информации о выбранном тестовом запуске.
 * - Отправку пользовательского фидбека по результатам AI-анализа.
 * - Управление переключением вкладок "Тестовые запуски" и "Виджеты".
 * - Отображение всплывающих уведомлений (тостов).
 *
 * @file Основной скрипт дашборда
 * @requires Chart.js Библиотека для построения графиков
 * @requires Font Awesome Для иконок
 */
document.addEventListener('DOMContentLoaded', () => {
    /**
     * Коллекция ссылок на ключевые DOM-элементы приложения.
     * @type {object}
     * @property {HTMLElement} rightPanelContent - Контейнер для детальной информации о тесте.
     * @property {NodeListOf<Element>} testRows - Все строки в таблице списка тестовых запусков.
     * @property {HTMLElement} toastContainer - Контейнер для всплывающих уведомлений (тостов).
     * @property {HTMLElement} statsContainer - Контейнер для отображения общей статистики.
     * @property {HTMLCanvasElement} passRateChartCtx - Элемент canvas для графика процента прохождения.
     * @property {HTMLCanvasElement} slowTestsChartCtx - Элемент canvas для графика медленных тестов.
     * @property {NodeListOf<Element>} tabButtons - Кнопки для переключения вкладок.
     * @property {NodeListOf<Element>} tabContents - Контейнеры содержимого для каждой вкладки.
     */
    const DOM_ELEMENTS = {
        rightPanelContent: document.getElementById('rightPanelContent'),
        testRows: document.querySelectorAll('.test-row'),
        toastContainer: document.getElementById('toastContainer'),
        statsContainer: document.getElementById('stats-container'),
        passRateChartCtx: document.getElementById('passRateChart'),
        slowTestsChartCtx: document.getElementById('slowTestsChart'),
        tabButtons: document.querySelectorAll('.tab-button'),
        tabContents: document.querySelectorAll('.tab-content')
    };

    /**
     * Объект, содержащий URL-адреса API-запросов.
     * @type {object}
     * @property {string} statistics - URL для получения общей статистики.
     * @property {Function} testDetails - Функция, возвращающая URL для получения деталей конкретного тестового запуска.
     * @property {Function} feedback - Функция, возвращающая URL для отправки фидбека по анализу.
     */
    const API_ENDPOINTS = {
        statistics: '/api/v1/statistics',
        testDetails: (id) => `/api/v1/tests/${id}`,
        feedback: (id) => `/api/v1/analysis/${id}/feedback`
    };

    /**
     * Объект, содержащий предопределенные сообщения для всплывающих уведомлений.
     * @type {object}
     */
    const TOAST_MESSAGES = {
        feedbackSuccess: 'Спасибо за ваш отзыв!',
        feedbackError: 'Не удалось отправить отзыв.',
        statsError: 'Не удалось загрузить статистику.',
        testDetailsNotFound: 'Тестовый запуск не найден.',
        testDetailsServerError: 'Ошибка сервера при загрузке деталей: '
    };

    let passRateChart; // Объект для графика процента прохождения (Chart.js instance)
    let slowTestsChart; // Объект для графика медленных тестов (Chart.js instance)

    // Инициализация дашборда при загрузке DOM
    initDashboard();

    /**
     * Инициализирует все компоненты дашборда:
     * - Загружает статистику.
     * - Инициализирует систему уведомлений.
     * - Добавляет слушателей событий клика для строк таблицы тестов.
     * - Загружает детали первого тестового запуска.
     * - Инициализирует логику переключения вкладок.
     */
    function initDashboard() {
        loadStatistics();
        initializeToastNotifications();
        initializeTestRowClickListeners();
        loadInitialTestDetails();
        initializeTabSwitching();
    }

    /**
     * Асинхронно загружает статистические данные с сервера и отображает их.
     * Включает рендеринг общей статистики, графика процента прохождения и графика медленных тестов.
     * В случае ошибки загрузки, выводит сообщение об ошибке.
     * @async
     */
    async function loadStatistics() {
        try {
            const response = await fetch(API_ENDPOINTS.statistics);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const stats = await response.json();
            renderStatistics(stats);
            renderPassRateChart(stats.dailyPassRateTrend);
            renderSlowTestsChart(stats.topSlowTests);
        } catch (error) {
            console.error('Ошибка загрузки статистики:', error);
            DOM_ELEMENTS.statsContainer.innerHTML = `<p class="loading-message error">${TOAST_MESSAGES.statsError}</p>`;
        }
    }

    /**
     * Рендерит общие статистические показатели и список топ-нестабильных тестов в DOM.
     * @param {object} stats - Объект, содержащий статистические данные (например, общее количество запусков, процент прохождения, топ падающих тестов).
     */
    function renderStatistics(stats) {
        let topFailingTestsContentHtml = '<p class="no-data-message">Нет данных о сбоях.</p>';
        // Если есть данные о падающих тестах, формируем HTML-список
        if (stats.failureCountByTest && Object.keys(stats.failureCountByTest).length > 0) {
            topFailingTestsContentHtml = '<ul>';
            for (const [testName, count] of Object.entries(stats.failureCountByTest)) {
                topFailingTestsContentHtml += `<li><span>${escapeHtml(testName)}</span> <strong>${count}</strong></li>`;
            }
            topFailingTestsContentHtml += '</ul>';
        }

        // Вставляем сгенерированный HTML в контейнер статистики
        DOM_ELEMENTS.statsContainer.innerHTML = `
            <div class="stats-grid">
                <div class="stats-card">
                    <h4>Всего прогонов</h4>
                    <p>${stats.totalRuns || 0}</p>
                </div>
                <div class="stats-card">
                    <h4>Pass Rate</h4>
                    <p>${stats.passRate ? stats.passRate.toFixed(1) : 0}%</p>
                </div>
                <div class="stats-card status-passed">
                    <h4>Прошли</h4>
                    <p>${stats.passedRuns || 0}</p>
                </div>
                <div class="stats-card status-failed">
                    <h4>Упали</h4>
                    <p>${stats.failedRuns || 0}</p>
                </div>
            </div>
            <details class="failing-tests-card">
                <summary>Топ нестабильных тестов</summary>
                <div class="content">
                    ${topFailingTestsContentHtml}
                </div>
            </details>
        `;
    }

    /**
     * Рендерит линейный график "Pass Rate Trend" с использованием Chart.js.
     * Включает отображение процента прохождения и общего количества запусков по дням.
     * @param {Array<object>} trendData - Массив объектов с данными для тренда (date, passRate, totalRuns).
     */
    function renderPassRateChart(trendData) {
        if (!DOM_ELEMENTS.passRateChartCtx) return; // Выходим, если canvas элемент не найден

        // Сортируем данные по дате для корректного отображения на графике
        trendData.sort((a, b) => new Date(a.date) - new Date(b.date));

        const labels = trendData.map(item => item.date); // Даты для оси X
        const passRates = trendData.map(item => item.passRate); // Процент прохождения
        const totalRuns = trendData.map(item => item.totalRuns); // Общее количество запусков

        // Уничтожаем предыдущий экземпляр графика, если он существует, чтобы избежать наложения
        if (passRateChart) {
            passRateChart.destroy();
        }

        // Создаем новый график
        passRateChart = new Chart(DOM_ELEMENTS.passRateChartCtx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Pass Rate (%)',
                    data: passRates,
                    borderColor: 'rgb(30, 142, 62)', // Цвет линии (зеленый)
                    backgroundColor: 'rgba(30, 142, 62, 0.1)', // Цвет области под линией
                    fill: true, // Заливка области
                    tension: 0.3 // Сглаживание линии
                },
                {
                    label: 'Всего запусков',
                    data: totalRuns,
                    borderColor: 'rgb(26, 115, 232)', // Цвет линии (синий)
                    backgroundColor: 'rgba(26, 115, 232, 0.1)',
                    fill: false, // Без заливки
                    tension: 0.3,
                    yAxisID: 'y1', // Привязка ко второй оси Y
                    borderDash: [5, 5] // Пунктирная линия
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false, // Позволяет изменять размер графика независимо от соотношения сторон
                plugins: {
                    legend: {
                        position: 'top', // Расположение легенды
                    },
                    tooltip: {
                        mode: 'index', // Всплывающая подсказка для всех линий на одной точке X
                        intersect: false, // Не требует прямого наведения на точку данных
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Дата' // Заголовок оси X
                        }
                    },
                    y: {
                        beginAtZero: true,
                        max: 100, // Максимальное значение для процента прохождения
                        title: {
                            display: true,
                            text: 'Процент прохождения (%)' // Заголовок первой оси Y
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right', // Вторая ось Y справа
                        grid: {
                            drawOnChartArea: false, // Не рисовать сетку для этой оси на области графика
                        },
                        title: {
                            display: true,
                            text: 'Всего запусков' // Заголовок второй оси Y
                        }
                    }
                }
            }
        });
    }

    /**
     * Рендерит горизонтальный столбчатый график "Top Slow Tests" с использованием Chart.js.
     * Отображает среднюю длительность выполнения самых медленных тестов.
     * @param {Array<object>} slowTestsData - Массив объектов с данными для медленных тестов (testName, averageDurationMillis).
     */
    function renderSlowTestsChart(slowTestsData) {
        if (!DOM_ELEMENTS.slowTestsChartCtx) return; // Выходим, если canvas элемент не найден

        // Сортируем данные по убыванию длительности, чтобы самые медленные были сверху
        slowTestsData.sort((a, b) => b.averageDurationMillis - a.averageDurationMillis);

        const labels = slowTestsData.map(item => item.testName); // Названия тестов для оси Y
        const durations = slowTestsData.map(item => (item.averageDurationMillis / 1000).toFixed(2)); // Длительности в секундах

        // Уничтожаем предыдущий экземпляр графика, если он существует
        if (slowTestsChart) {
            slowTestsChart.destroy();
        }

        // Создаем новый график
        slowTestsChart = new Chart(DOM_ELEMENTS.slowTestsChartCtx, {
            type: 'bar', // Тип графика - столбчатый
            data: {
                labels: labels,
                datasets: [{
                    label: 'Средняя длительность (сек)',
                    data: durations,
                    backgroundColor: 'rgba(217, 48, 37, 0.7)', // Цвет столбцов (красный)
                    borderColor: 'rgb(217, 48, 37)', // Цвет границы столбцов
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y', // Горизонтальные столбцы
                plugins: {
                    legend: {
                        display: false // Не отображать легенду
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.parsed.x !== null) {
                                    label += context.parsed.x + ' сек'; // Добавляем "сек" к значению в подсказке
                                }
                                return label;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Средняя длительность (секунды)' // Заголовок оси X
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: 'Тест Метод' // Заголовок оси Y
                        }
                    }
                }
            }
        });
    }

    /**
     * Асинхронно загружает детальную информацию о конкретном тестовом запуске
     * и отображает ее в правой панели.
     * @async
     * @param {string} testRunId - Идентификатор тестового запуска.
     * @param {HTMLElement} [selectedRow=null] - Опциональная ссылка на HTML-элемент строки таблицы,
     * который был выбран пользователем. Используется для добавления класса 'selected'.
     */
    async function loadTestDetails(testRunId, selectedRow = null) {
        // Удаляем класс 'selected' со всех строк таблицы и добавляем его к выбранной строке
        DOM_ELEMENTS.testRows.forEach(row => row.classList.remove('selected'));
        if (selectedRow) {
            selectedRow.classList.add('selected');
        }

        // Отображаем сообщение о загрузке в правой панели
        DOM_ELEMENTS.rightPanelContent.innerHTML = '<div class="placeholder-content"><p>Загрузка деталей...</p></div>';

        try {
            const response = await fetch(API_ENDPOINTS.testDetails(testRunId));
            if (!response.ok) {
                const errorMessage = response.status === 404
                    ? TOAST_MESSAGES.testDetailsNotFound
                    : `${TOAST_MESSAGES.testDetailsServerError}${response.status}`;
                throw new Error(errorMessage);
            }
            const testRun = await response.json();
            renderTestDetails(testRun);
        } catch (error) {
            console.error('Ошибка загрузки деталей теста:', error);
            // Отображаем сообщение об ошибке в правой панели
            DOM_ELEMENTS.rightPanelContent.innerHTML = `
                <div class="placeholder-content error-state">
                    <p>${escapeHtml(error.message)}</p>
                </div>`;
        }
    }

    /**
     * Рендерит детальную информацию о тестовом запуске в правой панели.
     * Формирует HTML-структуру, включая общую информацию, результаты AI-анализа,
     * детали сбоя и визуализацию пути выполнения.
     * @param {object} testRun - Объект, содержащий детальную информацию о тестовом запуске.
     */
    function renderTestDetails(testRun) {
        // Форматируем временную метку для отображения
        const formattedTimestamp = new Date(testRun.timestamp).toLocaleString('ru-RU');

        let contentHtml = `
            <div class="detail-block">
                <h3><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Информация о запуске</h3>
                <div class="info-grid">
                    <p><strong>ID:</strong> ${escapeHtml(testRun.id)}</p>
                    <p><strong>Статус:</strong> <span class="status-badge status-${testRun.status.toLowerCase()}">${escapeHtml(testRun.status)}</span></p>
                    <p><strong>Класс:</strong> ${escapeHtml(testRun.testClass)}</p>
                    <p><strong>Метод:</strong> ${escapeHtml(testRun.testMethod)}</p>
                    <p><strong>Время:</strong> ${escapeHtml(formattedTimestamp)}</p>
                    <p><strong>Окружение:</strong> ${escapeHtml(testRun.configuration?.environment || 'N/A')}</p>
                    <p><strong>Версия:</strong> ${escapeHtml(testRun.configuration?.appVersion || 'N/A')}</p>
                    <p><strong>Набор:</strong> ${escapeHtml(testRun.configuration?.testSuite || 'N/A')}</p>
                </div>
            </div>
        `;

        // Добавляем секцию с результатами AI-анализа, если они есть
        if (testRun.analysisResults && testRun.analysisResults.length > 0) {
            contentHtml += `<div class="detail-block"><h3><i class="fa-solid fa-brain" aria-hidden="true"></i> Результаты Анализа AI</h3>`;
            testRun.analysisResults.forEach(result => {
                // Вычисляем процент уверенности AI
                const confidence = result.aiConfidence !== undefined ? `${(result.aiConfidence * 100).toFixed(0)}%` : 'N/A';
                // Формируем HTML для кнопок обратной связи
                const feedbackHtml = `
                    <div class="feedback-section" data-analysis-id="${result.id}">
                        <span>Анализ был полезен?</span>
                        <div class="feedback-buttons">
                            <button class="feedback-btn" data-correct="true" title="Да, анализ верный" aria-label="Поставить лайк анализу"><i class="fa-solid fa-thumbs-up"></i></button>
                            <button class="feedback-btn" data-correct="false" title="Нет, анализ неверный" aria-label="Поставить дизлайк анализу"><i class="fa-solid fa-thumbs-down"></i></button>
                        </div>
                        <div class="feedback-submitted-msg" style="display: none;">Спасибо за отзыв!</div>
                    </div>`;

                // Формируем HTML для каждого результата анализа
                contentHtml += `
                    <div class="analysis-result-item">
                        <h4>${escapeHtml(result.analysisType)} <span class="ai-confidence">${confidence}</span></h4>
                        <p><strong><i class="fa-solid fa-lightbulb" aria-hidden="true"></i> Причина:</strong> ${escapeHtml(result.suggestedReason)}</p>
                        <p><strong><i class="fa-solid fa-wrench" aria-hidden="true"></i> Решение:</strong> ${escapeHtml(result.solution)}</p>
                        ${result.rawData ? `<details><summary>Сопутствующие данные</summary><pre>${escapeHtml(result.rawData)}</pre></details>` : ''}
                        ${feedbackHtml}
                    </div>`;
            });
            contentHtml += `</div>`;
        }

        // Добавляем секцию с деталями сбоя, если тест провален
        if (testRun.status === 'FAILED') {
            contentHtml += `<div class="detail-block"><h3><i class="fa-solid fa-bug" aria-hidden="true"></i> Детали сбоя</h3>`;
            if (testRun.failedStep) {
                contentHtml += `
                    <div class="failure-subsection">
                        <h4>Проваленный шаг</h4>
                        <p><strong>Действие:</strong> ${escapeHtml(testRun.failedStep.action || 'N/A')}</p>
                        <p><strong>Локатор:</strong> ${escapeHtml(testRun.failedStep.locatorStrategy || 'N/A')} = ${escapeHtml(testRun.failedStep.locatorValue || 'N/A')}</p>
                    </div>`;
            }
            if (testRun.exceptionType) {
                contentHtml += `<div class="failure-subsection"><h4>Исключение</h4><pre class="error-pre">${escapeHtml(testRun.exceptionType || 'N/A')}\n${escapeHtml(testRun.stackTrace || 'Нет стектрейса')}</pre></div>`;
            }
            contentHtml += `</div>`;
        }

        // Добавляем секцию с визуализацией пути выполнения, если данные доступны
        if (testRun.executionPath && testRun.executionPath.length > 0) {
            contentHtml += `<div class="detail-block"><h3><i class="fa-solid fa-list-ol" aria-hidden="true"></i> Путь выполнения</h3><ul class="execution-path-visualizer">`;
            testRun.executionPath.forEach((step, index) => {
                // Выбираем иконку и класс статуса для каждого шага
                const iconClass = step.result === 'SUCCESS' ? 'fa-circle-check' : (step.result === 'FAILURE' ? 'fa-circle-xmark' : 'fa-circle-minus');
                const statusClass = `step-${step.result ? step.result.toLowerCase() : 'unknown'}`;
                const confidence = step.confidenceScore !== undefined ? `Уверенность: ${(step.confidenceScore * 100).toFixed(0)}%` : 'Уверенность: N/A';

                contentHtml += `
                    <li class="${statusClass}">
                        <i class="fa-solid ${iconClass} step-icon" aria-hidden="true"></i>
                        <div class="step-content">
                            <strong>Шаг ${index + 1}:</strong> ${escapeHtml(step.action || 'Неизвестное действие')}
                            <small>${confidence}</small>
                        </div>
                    </li>`;
            });
            contentHtml += `</ul></div>`;
        }

        // Вставляем сгенерированный HTML в правую панель
        DOM_ELEMENTS.rightPanelContent.innerHTML = contentHtml;
    }

    /**
     * Отправляет пользовательский фидбек (обратную связь) по результатам AI-анализа на сервер.
     * После успешной отправки отображает всплывающее уведомление и скрывает кнопки фидбека.
     * @async
     * @param {string} analysisId - Идентификатор результата AI-анализа, к которому относится фидбек.
     * @param {boolean} isCorrect - True, если анализ был признан корректным; false в противном случае.
     * @param {HTMLElement} buttonContainer - Контейнер HTML-элементов кнопок фидбека.
     */
    async function submitFeedback(analysisId, isCorrect, buttonContainer) {
        try {
            const response = await fetch(API_ENDPOINTS.feedback(analysisId), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    correct: isCorrect,
                    userComment: "Feedback from UI", // Заглушка для комментария пользователя
                    username: "demo-user" // Заглушка для имени пользователя
                })
            });

            if (!response.ok) {
                throw new Error(`Сервер ответил со статусом ${response.status}`);
            }

            showToast(TOAST_MESSAGES.feedbackSuccess, 'success');
            // Скрываем кнопки фидбека и показываем сообщение "Спасибо за отзыв!"
            if (buttonContainer) {
                buttonContainer.style.display = 'none';
                const submittedMsg = buttonContainer.nextElementSibling;
                if (submittedMsg) submittedMsg.style.display = 'block';
            }

        } catch (error) {
            console.error('Ошибка отправки отзыва:', error);
            showToast(TOAST_MESSAGES.feedbackError, 'error');
        }
    }

    /**
     * Инициализирует отображение всплывающих уведомлений (тостов) на основе
     * сообщений, которые могли быть переданы из бэкенда (например, при редиректе).
     */
    function initializeToastNotifications() {
        const successMessage = document.querySelector('.alert-message.success p');
        const errorMessage = document.querySelector('.alert-message.error p');
        if (successMessage && successMessage.textContent.trim()) {
            showToast(successMessage.textContent.trim(), 'success');
        }
        if (errorMessage && errorMessage.textContent.trim()) {
            showToast(errorMessage.textContent.trim(), 'error');
        }
    }

    /**
     * Инициализирует слушателей событий для строк таблицы тестовых запусков
     * и для кнопок обратной связи в правой панели.
     * При клике/нажатии Enter на строке теста загружаются ее детали.
     * При клике на кнопки фидбека отправляется отзыв.
     */
    function initializeTestRowClickListeners() {
        // Добавляем слушателей для каждой строки теста
        DOM_ELEMENTS.testRows.forEach(row => {
            // Обработчик клика мышью
            row.addEventListener('click', () => loadTestDetails(row.dataset.id, row));
            // Обработчик нажатий клавиш (Enter или Пробел) для доступности
            row.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault(); // Предотвращаем дефолтное поведение (например, прокрутку)
                    loadTestDetails(row.dataset.id, row);
                }
            });
        });

        // Добавляем делегированный слушатель кликов для кнопок фидбека в правой панели
        DOM_ELEMENTS.rightPanelContent.addEventListener('click', event => {
            const button = event.target.closest('.feedback-btn'); // Ищем ближайшую кнопку фидбека
            if (button) {
                const feedbackSection = button.closest('.feedback-section'); // Ищем родительскую секцию фидбека
                if (feedbackSection) {
                    const analysisId = feedbackSection.dataset.analysisId; // Получаем ID анализа из data-атрибута
                    const isCorrect = button.dataset.correct === 'true'; // Определяем, был ли фидбек положительным
                    submitFeedback(analysisId, isCorrect, button.parentElement); // Отправляем фидбек
                }
            }
        });
    }

    /**
     * Загружает детали первого тестового запуска в таблице при инициализации.
     * Это обеспечивает, что правая панель не будет пустой при первом открытии дашборда.
     */
    function loadInitialTestDetails() {
        const firstTestRow = DOM_ELEMENTS.testRows[0];
        if (firstTestRow) {
            loadTestDetails(firstTestRow.dataset.id, firstTestRow);
        }
    }

    /**
     * Отображает всплывающее уведомление (тост) в нижней правой части экрана.
     * @param {string} message - Текстовое сообщение для отображения в тосте.
     * @param {string} [type='success'] - Тип тоста ('success' или 'error'), влияет на цвет и иконку.
     */
    function showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`; // Добавляем классы для стилизации
        // Выбираем иконку в зависимости от типа тоста
        const iconClass = type === 'success' ? 'fa-check-circle' : 'fa-times-circle';
        toast.innerHTML = `<i class="fa-solid ${iconClass}" aria-hidden="true"></i> ${escapeHtml(message)}`; // Вставляем иконку и сообщение
        DOM_ELEMENTS.toastContainer.appendChild(toast); // Добавляем тост в контейнер

        // Небольшая задержка для запуска CSS-перехода
        setTimeout(() => toast.classList.add('show'), 10);

        // Скрываем и удаляем тост через 5 секунд
        setTimeout(() => {
            toast.classList.remove('show');
            // Удаляем тост из DOM после завершения анимации скрытия
            toast.addEventListener('transitionend', () => toast.remove(), { once: true });
        }, 5000);
    }

    /**
     * Экранирует HTML-сущности в строке, чтобы предотвратить XSS-атаки
     * при вставке пользовательских данных в DOM.
     * @param {string} unsafe - Строка, содержащая потенциально опасный HTML.
     * @returns {string} Экранированная строка.
     */
    function escapeHtml(unsafe) {
        if (typeof unsafe !== 'string') {
            return unsafe; // Возвращаем как есть, если это не строка
        }
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(unsafe));
        return div.innerHTML;
    }

    /**
     * Инициализирует логику переключения вкладок.
     * Добавляет слушателей событий к кнопкам вкладок, переключает активные классы
     * и вызывает изменение размера графиков, когда вкладка "Виджеты" становится видимой.
     */
    function initializeTabSwitching() {
        DOM_ELEMENTS.tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetTab = button.dataset.tab; // Получаем идентификатор целевой вкладки из data-атрибута

                // Удаляем класс 'active' со всех кнопок и содержимого вкладок
                DOM_ELEMENTS.tabButtons.forEach(btn => btn.classList.remove('active'));
                DOM_ELEMENTS.tabContents.forEach(content => content.classList.remove('active'));

                // Добавляем класс 'active' к выбранной кнопке и соответствующему содержимому
                button.classList.add('active');
                document.getElementById(`${targetTab}TabContent`).classList.add('active');

                // Это важно для Chart.js: если контейнер графика был скрыт (display: none),
                // Chart.js может некорректно отрисоваться. Принудительный вызов resize()
                // заставляет графики перерисоваться, когда их контейнер становится видимым.
                if (targetTab === 'widgets') {
                    if (passRateChart) passRateChart.resize();
                    if (slowTestsChart) slowTestsChart.resize();
                }
            });
        });
        // Убедимся, что при загрузке страницы выбрана первая вкладка (Test Runs)
        // Можно добавить здесь вызов DOM_ELEMENTS.tabButtons[0].click()
        // если нет дефолтного активного состояния в HTML.
    }
});
