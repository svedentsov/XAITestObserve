document.addEventListener('DOMContentLoaded', () => {

    // --- DOM Elements Cache ---
    const DOM_ELEMENTS = {
        rightPanelContent: document.getElementById('rightPanelContent'),
        testRows: document.querySelectorAll('.test-row'),
        toastContainer: document.getElementById('toastContainer'),
        statsContainer: document.getElementById('stats-container'),
        passRateChartCtx: document.getElementById('passRateChart'),
        slowTestsChartCtx: document.getElementById('slowTestsChart'),
        tabButtons: document.querySelectorAll('.tab-button'),
        tabContents: document.querySelectorAll('.tab-content'),
        initialSuccessAlert: document.querySelector('.initial-alert-message.success'),
        initialErrorAlert: document.querySelector('.initial-alert-message.error'),
        // Добавляем ссылки на заголовки блоков деталей
        detailBlockHeaders: null // Будет заполнено при загрузке деталей
    };

    // --- API Endpoints ---
    const API_ENDPOINTS = {
        statistics: '/api/v1/statistics',
        testDetails: (id) => `/api/v1/tests/${id}`,
        feedback: (id) => `/api/v1/analysis/${id}/feedback`
    };

    // --- User-facing Messages ---
    const UI_MESSAGES = {
        feedbackSuccess: 'Спасибо за ваш отзыв!',
        feedbackError: 'Не удалось отправить отзыв. Попробуйте снова.',
        statsError: 'Не удалось загрузить статистику. Пожалуйста, попробуйте обновить страницу.',
        testDetailsNotFound: 'Детали тестового запуска не найдены.',
        testDetailsServerError: 'Ошибка сервера при загрузке деталей:',
        loadingStats: 'Загрузка статистики...',
        loadingDetails: 'Загрузка деталей...',
        noDataFailure: 'Нет данных о сбоях.',
        selectTestRun: 'Выберите тестовый запуск из списка, чтобы увидеть детали.',
        errorState: 'Произошла ошибка.'
    };

    // --- Chart Instances ---
    let passRateChart = null;
    let slowTestsChart = null;

    // IntersectionObserver для sticky заголовков
    let stickyHeaderObserver = null;

    /**
     * Initializes the dashboard by loading data and setting up event listeners.
     */
    function initDashboard() {
        console.log('Dashboard initialization started.');
        loadInitialMessages();
        loadStatistics();
        initializeTestRowClickListeners();
        loadInitialTestDetails();
        initializeTabSwitching();
        console.log('Dashboard initialization complete.');
    }

    /**
     * Displays initial success or error messages from the Thymeleaf model as toasts.
     */
    function loadInitialMessages() {
        if (DOM_ELEMENTS.initialSuccessAlert && DOM_ELEMENTS.initialSuccessAlert.textContent.trim()) {
            showToast(DOM_ELEMENTS.initialSuccessAlert.textContent.trim(), 'success');
        }
        if (DOM_ELEMENTS.initialErrorAlert && DOM_ELEMENTS.initialErrorAlert.textContent.trim()) {
            showToast(DOM_ELEMENTS.initialErrorAlert.textContent.trim(), 'error');
        }
    }

    /**
     * Fetches and renders statistics and charts.
     */
    async function loadStatistics() {
        DOM_ELEMENTS.statsContainer.innerHTML = `<p class="loading-message">${UI_MESSAGES.loadingStats}</p>`;
        try {
            const response = await fetch(API_ENDPOINTS.statistics);
            if (!response.ok) {
                // Log full response for debugging on server side
                const errorText = await response.text();
                console.error(`HTTP error fetching statistics: ${response.status} - ${errorText}`);
                throw new Error(UI_MESSAGES.statsError);
            }
            const stats = await response.json();
            renderStatistics(stats);
            renderPassRateChart(stats.dailyPassRateTrend);
            renderSlowTestsChart(stats.topSlowTests);
            console.log('Statistics loaded and rendered successfully.');
        } catch (error) {
            console.error('Error loading statistics:', error);
            DOM_ELEMENTS.statsContainer.innerHTML = `<p class="loading-message error">${escapeHtml(error.message)}</p>`;
            showToast(UI_MESSAGES.statsError, 'error');
        }
    }

    /**
     * Renders the statistics cards and top failing tests.
     * @param {object} stats - Statistics data.
     */
    function renderStatistics(stats) {
        const totalRuns = stats.totalRuns || 0;
        const passRate = stats.passRate ? stats.passRate.toFixed(1) : 0;
        const passedRuns = stats.passedRuns || 0;
        const failedRuns = stats.failedRuns || 0;

        let topFailingTestsContentHtml = `<p class="no-data-message">${UI_MESSAGES.noDataFailure}</p>`;
        if (stats.failureCountByTest && Object.keys(stats.failureCountByTest).length > 0) {
            topFailingTestsContentHtml = '<ul>';
            for (const [testName, count] of Object.entries(stats.failureCountByTest)) {
                topFailingTestsContentHtml += `<li><span>${escapeHtml(testName)}</span> <strong>${count}</strong></li>`;
            }
            topFailingTestsContentHtml += '</ul>';
        }

        DOM_ELEMENTS.statsContainer.innerHTML = `
            <div class="stats-grid">
                <div class="stats-card">
                    <h4>Прогоны</h4>
                    <p>${totalRuns}</p>
                </div>
                <div class="stats-card">
                    <h4>Pass Rate</h4>
                    <p>${passRate}%</p>
                </div>
                <div class="stats-card status-passed">
                    <h4>Прошли</h4>
                    <p>${passedRuns}</p>
                </div>
                <div class="stats-card status-failed">
                    <h4>Упали</h4>
                    <p>${failedRuns}</p>
                </div>
            </div>
            <div class="failing-tests-card">
                <h4>Топ нестабильных тестов</h4>
                <div class="content">
                    ${topFailingTestsContentHtml}
                </div>
            </div>
        `;
    }

    /**
     * Renders the Pass Rate Trend Chart.
     * @param {Array<object>} trendData - Data for the pass rate trend.
     */
    function renderPassRateChart(trendData) {
        if (!DOM_ELEMENTS.passRateChartCtx) {
            console.warn('Pass Rate Chart canvas element not found.');
            return;
        }

        const sortedData = [...trendData].sort((a, b) => new Date(a.date) - new Date(b.date));

        const labels = sortedData.map(item => item.date);
        const passRates = sortedData.map(item => item.passRate);
        const totalRuns = sortedData.map(item => item.totalRuns);

        if (passRateChart) {
            passRateChart.destroy();
        }

        passRateChart = new Chart(DOM_ELEMENTS.passRateChartCtx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Pass Rate (%)',
                    data: passRates,
                    borderColor: 'rgb(30, 142, 62)',
                    backgroundColor: 'rgba(30, 142, 62, 0.1)',
                    fill: true,
                    tension: 0.3,
                    yAxisID: 'y'
                },
                {
                    label: 'Всего запусков',
                    data: totalRuns,
                    borderColor: 'rgb(26, 115, 232)',
                    backgroundColor: 'rgba(26, 115, 232, 0.1)',
                    fill: false,
                    tension: 0.3,
                    yAxisID: 'y1',
                    borderDash: [5, 5]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                    }
                },
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: 'Дата'
                        }
                    },
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: {
                            display: true,
                            text: 'Процент прохождения (%)'
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        grid: {
                            drawOnChartArea: false, // Only draw grid lines for the first Y-axis
                        },
                        title: {
                            display: true,
                            text: 'Всего запусков'
                        },
                        ticks: {
                            callback: function(value) {
                                return Number.isInteger(value) ? value : null; // Show only integers
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Renders the Top 5 Slowest Tests Chart.
     * @param {Array<object>} slowTestsData - Data for the slowest tests.
     */
    function renderSlowTestsChart(slowTestsData) {
        if (!DOM_ELEMENTS.slowTestsChartCtx) {
            console.warn('Slow Tests Chart canvas element not found.');
            return;
        }

        const sortedData = [...slowTestsData].sort((a, b) => b.averageDurationMillis - a.averageDurationMillis);

        const labels = sortedData.map(item => item.testName);
        const durations = sortedData.map(item => (item.averageDurationMillis / 1000).toFixed(2));

        if (slowTestsChart) {
            slowTestsChart.destroy();
        }

        slowTestsChart = new Chart(DOM_ELEMENTS.slowTestsChartCtx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Средняя длительность (сек)',
                    data: durations,
                    backgroundColor: 'rgba(217, 48, 37, 0.7)',
                    borderColor: 'rgb(217, 48, 37)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y', // Horizontal bar chart
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.parsed.x !== null) {
                                    label += `${context.parsed.x} сек`;
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
                            text: 'Средняя длительность (секунды)'
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: 'Тест Метод'
                        }
                    }
                }
            }
        });
    }

    /**
     * Loads and displays details for a specific test run.
     * @param {string} testRunId - The ID of the test run.
     * @param {HTMLElement} [selectedRow=null] - The table row element that was clicked.
     */
    async function loadTestDetails(testRunId, selectedRow = null) {
        DOM_ELEMENTS.testRows.forEach(row => row.classList.remove('selected'));
        if (selectedRow) {
            selectedRow.classList.add('selected');
        }

        DOM_ELEMENTS.rightPanelContent.innerHTML = `<div class="placeholder-content"><p>${UI_MESSAGES.loadingDetails}</p></div>`;

        // Отключаем предыдущий IntersectionObserver, если он был
        if (stickyHeaderObserver) {
            stickyHeaderObserver.disconnect();
            stickyHeaderObserver = null;
        }

        try {
            const response = await fetch(API_ENDPOINTS.testDetails(testRunId));
            if (!response.ok) {
                const errorMessage = response.status === 404
                    ? UI_MESSAGES.testDetailsNotFound
                    : `${UI_MESSAGES.testDetailsServerError} ${response.status}`;
                throw new Error(errorMessage);
            }
            const testRun = await response.json();
            renderTestDetails(testRun);
            // После рендера деталей, инициализируем sticky заголовки
            initializeStickyHeaders();
            console.log(`Details for test run ${testRunId} loaded.`);
        } catch (error) {
            console.error('Error loading test details:', error);
            DOM_ELEMENTS.rightPanelContent.innerHTML = `
                <div class="placeholder-content error-state">
                    <p>${UI_MESSAGES.errorState} ${escapeHtml(error.message)}</p>
                </div>`;
            showToast(error.message, 'error');
        }
    }

    /**
     * Renders the detailed information for a test run.
     * @param {object} testRun - The test run data.
     */
    function renderTestDetails(testRun) {
        const formattedTimestamp = new Date(testRun.timestamp).toLocaleString('ru-RU');

        let contentHtml = `
            <div class="detail-block">
                <h3><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Информация о запуске</h3>
                <div class="info-grid">
                    <p><strong>ID:</strong> ${escapeHtml(testRun.id)}</p>
                    <p><strong>Статус:</strong> <span class="status-badge status-${testRun.status.toLowerCase()}">${escapeHtml(testRun.status)}</span></p>
                    <p><strong>Класс:</strong> ${escapeHtml(testRun.testClass || 'N/A')}</p>
                    <p><strong>Метод:</strong> ${escapeHtml(testRun.testMethod || 'N/A')}</p>
                    <p><strong>Время:</strong> ${escapeHtml(formattedTimestamp)}</p>
                    <p><strong>Окружение:</strong> ${escapeHtml(testRun.configuration?.environment || 'N/A')}</p>
                    <p><strong>Версия:</strong> ${escapeHtml(testRun.configuration?.appVersion || 'N/A')}</p>
                    <p><strong>Набор:</strong> ${escapeHtml(testRun.configuration?.testSuite || 'N/A')}</p>
                </div>
            </div>
        `;

        if (testRun.analysisResults && testRun.analysisResults.length > 0) {
            contentHtml += `<div class="detail-block"><h3><i class="fa-solid fa-brain" aria-hidden="true"></i> Результаты Анализа AI</h3>`;
            testRun.analysisResults.forEach(result => {
                const confidence = result.aiConfidence !== undefined ? `${(result.aiConfidence * 100).toFixed(0)}%` : 'N/A';
                const feedbackHtml = `
                    <div class="feedback-section" data-analysis-id="${result.id}">
                        <span>Анализ был полезен?</span>
                        <div class="feedback-buttons">
                            <button class="feedback-btn" data-correct="true" title="Да, анализ верный" aria-label="Поставить лайк анализу"><i class="fa-solid fa-thumbs-up" aria-hidden="true"></i></button>
                            <button class="feedback-btn" data-correct="false" title="Нет, анализ неверный" aria-label="Поставить дизлайк анализу"><i class="fa-solid fa-thumbs-down" aria-hidden="true"></i></button>
                        </div>
                        <div class="feedback-submitted-msg" style="display: none;">${UI_MESSAGES.feedbackSuccess}</div>
                    </div>`;

                contentHtml += `
                    <div class="analysis-result-item">
                        <h4>${escapeHtml(result.analysisType)} <span class="ai-confidence">${confidence}</span></h4>
                        <p><strong><i class="fa-solid fa-lightbulb" aria-hidden="true"></i> Причина:</strong> ${escapeHtml(result.suggestedReason || 'N/A')}</p>
                        <p><strong><i class="fa-solid fa-wrench" aria-hidden="true"></i> Решение:</strong> ${escapeHtml(result.solution || 'N/A')}</p>
                        ${result.rawData ? `<details><summary>Сопутствующие данные</summary><pre>${escapeHtml(result.rawData)}</pre></details>` : ''}
                        ${feedbackHtml}
                    </div>`;
            });
            contentHtml += `</div>`;
        }

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
            if (testRun.exceptionType || testRun.stackTrace) {
                contentHtml += `<div class="failure-subsection"><h4>Исключение</h4><pre class="error-pre">${escapeHtml(testRun.exceptionType || 'N/A')}\n${escapeHtml(testRun.stackTrace || 'Нет стектрейса')}</pre></div>`;
            }
            contentHtml += `</div>`;
        }

        if (testRun.executionPath && testRun.executionPath.length > 0) {
            contentHtml += `<div class="detail-block"><h3><i class="fa-solid fa-list-ol" aria-hidden="true"></i> Путь выполнения</h3><ul class="execution-path-visualizer">`;
            testRun.executionPath.forEach((step, index) => {
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

        DOM_ELEMENTS.rightPanelContent.innerHTML = contentHtml;
    }

    /**
     * Инициализирует IntersectionObserver для sticky заголовков в правой панели.
     */
    function initializeStickyHeaders() {
        DOM_ELEMENTS.detailBlockHeaders = DOM_ELEMENTS.rightPanelContent.querySelectorAll('.detail-block h3');

        // Отключаем предыдущий observer, если он есть
        if (stickyHeaderObserver) {
            stickyHeaderObserver.disconnect();
        }

        // Если заголовков нет или это мобильный вид, не инициализируем observer
        if (!DOM_ELEMENTS.detailBlockHeaders.length || window.innerWidth < 992) {
            return;
        }

        const options = {
            root: DOM_ELEMENTS.rightPanelContent, // Контейнер прокрутки
            rootMargin: '0px 0px 0px 0px', // Наблюдаем за пересечением с 0px от верхнего края
            threshold: [0, 1] // Наблюдаем, когда элемент входит/выходит из видимости
        };

        stickyHeaderObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                const header = entry.target;
                // Проверяем, прилип ли заголовок.
                // intersectionRatio < 1 означает, что элемент частично невидим (скорее всего, прилип).
                // boundingClientRect.top <= 0 означает, что элемент достиг или пересек верхний край root.
                if (entry.intersectionRatio < 1 && entry.boundingClientRect.top <= 0) {
                    header.classList.add('is-sticky');
                } else {
                    header.classList.remove('is-sticky');
                }
            });
        }, options);

        DOM_ELEMENTS.detailBlockHeaders.forEach(header => {
            stickyHeaderObserver.observe(header);
        });
        console.log('Sticky headers initialized with IntersectionObserver.');
    }


    /**
     * Submits feedback for an AI analysis result.
     * Uses event delegation for feedback buttons.
     * @param {string} analysisId - The ID of the analysis result.
     * @param {boolean} isCorrect - True if feedback is positive, false otherwise.
     * @param {HTMLElement} buttonContainer - The parent element of the feedback buttons to hide.
     */
    async function submitFeedback(analysisId, isCorrect, buttonContainer) {
        try {
            const response = await fetch(API_ENDpoints.feedback(analysisId), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    correct: isCorrect,
                    userComment: "Feedback from UI",
                    username: "demo-user" // Consider making this dynamic if user auth exists
                })
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error(`Server responded with status ${response.status}: ${errorText}`);
                throw new Error(`Сервер ответил со статусом ${response.status}.`);
            }

            showToast(UI_MESSAGES.feedbackSuccess, 'success');
            if (buttonContainer) {
                buttonContainer.style.display = 'none';
                const submittedMsg = buttonContainer.nextElementSibling;
                if (submittedMsg) submittedMsg.style.display = 'block';
            }
            console.log(`Feedback for analysis ${analysisId} submitted.`);

        } catch (error) {
            console.error('Error submitting feedback:', error);
            showToast(`${UI_MESSAGES.feedbackError} ${escapeHtml(error.message)}`, 'error');
        }
    }

    /**
     * Attaches event listeners to test row clicks and keydowns.
     */
    function initializeTestRowClickListeners() {
        DOM_ELEMENTS.testRows.forEach(row => {
            row.addEventListener('click', () => {
                loadTestDetails(row.dataset.id, row);
            });
            row.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault(); // Prevent default scroll for space
                    loadTestDetails(row.dataset.id, row);
                }
            });
        });

        // Event delegation for feedback buttons inside rightPanelContent
        DOM_ELEMENTS.rightPanelContent.addEventListener('click', event => {
            const button = event.target.closest('.feedback-btn');
            if (button) {
                const feedbackSection = button.closest('.feedback-section');
                if (feedbackSection) {
                    const analysisId = feedbackSection.dataset.analysisId;
                    const isCorrect = button.dataset.correct === 'true';
                    submitFeedback(analysisId, isCorrect, button.parentElement);
                }
            }
        });
    }

    /**
     * Loads details for the first test run when the page loads.
     */
    function loadInitialTestDetails() {
        const firstTestRow = DOM_ELEMENTS.testRows[0];
        if (firstTestRow) {
            loadTestDetails(firstTestRow.dataset.id, firstTestRow);
        } else {
            DOM_ELEMENTS.rightPanelContent.innerHTML = `<div class="placeholder-content">
                <i class="fa-regular fa-hand-pointer placeholder-icon" aria-hidden="true"></i>
                <p>${UI_MESSAGES.selectTestRun}</p>
            </div>`;
            console.log('No test runs found to load initial details.');
        }
    }

    /**
     * Sets up tab switching functionality.
     */
    function initializeTabSwitching() {
        DOM_ELEMENTS.tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetTabId = button.dataset.tab;

                DOM_ELEMENTS.tabButtons.forEach(btn => {
                    btn.classList.remove('active');
                    btn.setAttribute('aria-selected', 'false');
                });
                DOM_ELEMENTS.tabContents.forEach(content => content.classList.remove('active'));

                button.classList.add('active');
                button.setAttribute('aria-selected', 'true');
                document.getElementById(`${targetTabId}TabContent`).classList.add('active');

                // Resize charts when the widgets tab becomes active
                if (targetTabId === 'widgets') {
                    // Use requestAnimationFrame to ensure canvas is fully visible before resizing
                    requestAnimationFrame(() => {
                        if (passRateChart) passRateChart.resize();
                        if (slowTestsChart) slowTestsChart.resize();
                        console.log('Charts resized.');
                    });
                }
            });
        });
    }

    /**
     * Displays a toast notification.
     * @param {string} message - The message to display.
     * @param {'success' | 'error'} type - The type of toast (success or error).
     */
    function showToast(message, type = 'success') {
        if (!DOM_ELEMENTS.toastContainer) {
            console.warn('Toast container not found. Cannot show toast.');
            return;
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.setAttribute('role', type === 'error' ? 'alert' : 'status'); // Accessibility role
        const iconClass = type === 'success' ? 'fa-circle-check' : 'fa-circle-xmark';
        toast.innerHTML = `<i class="fa-solid ${iconClass}" aria-hidden="true"></i> ${escapeHtml(message)}`;
        DOM_ELEMENTS.toastContainer.appendChild(toast);

        // Animate in
        setTimeout(() => toast.classList.add('show'), 10);

        // Animate out and remove
        setTimeout(() => {
            toast.classList.remove('show');
            toast.addEventListener('transitionend', () => toast.remove(), { once: true });
        }, 5000); // Display for 5 seconds
    }

    /**
     * Escapes HTML entities in a string to prevent XSS.
     * @param {string} unsafe - The string to escape.
     * @returns {string} The escaped string.
     */
    function escapeHtml(unsafe) {
        if (typeof unsafe !== 'string') {
            return unsafe; // Return as is if not a string
        }
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(unsafe));
        return div.innerHTML;
    }

    // --- Start the dashboard ---
    initDashboard();
});
