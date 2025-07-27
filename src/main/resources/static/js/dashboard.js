document.addEventListener('DOMContentLoaded', () => {

    const DOM_ELEMENTS = {
        rightPanelContent: document.getElementById('rightPanelContent'),
        toastContainer: document.getElementById('toastContainer'),
        statsContainer: document.getElementById('stats-container'),
        passRateChartCtx: document.getElementById('passRateChart'),
        slowTestsChartCtx: document.getElementById('slowTestsChart'),
        tabButtons: document.querySelectorAll('.tab-button'),
        tabContents: document.querySelectorAll('.tab-content'),
        testListTableBody: document.getElementById('testListTableBody'),
    };

    const API_ENDPOINTS = {
        statistics: '/api/v1/statistics',
        testDetails: (id) => `/api/v1/tests/${id}`,
        feedback: (id) => `/api/v1/analysis/${id}/feedback`,
        deleteAll: '/api/v1/tests/all',
        createDemo: '/demo/create',
        testListFragment: '/test-list-fragment'
    };

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

    let passRateChart = null;
    let slowTestsChart = null;
    const testDetailsCache = new Map();

    function initDashboard() {
        console.log('Dashboard initialization started.');

        restoreActiveTab();
        loadStatistics();
        initializeTestRowClickListeners();
        formatTestRunTimes();
        loadInitialTestDetails();
        initializeTabSwitching();
        initializeHeaderActions();

        console.log('Dashboard initialization complete.');
    }

    function restoreActiveTab() {
        const savedTabId = localStorage.getItem('activeTabId');
        if (savedTabId) {
            DOM_ELEMENTS.tabButtons.forEach(btn => btn.classList.remove('active'));
            DOM_ELEMENTS.tabContents.forEach(content => content.classList.remove('active'));

            const savedButton = document.querySelector(`.tab-button[data-tab="${savedTabId}"]`);
            const savedContent = document.getElementById(`${savedTabId}TabContent`);

            if (savedButton && savedContent) {
                savedButton.classList.add('active');
                savedContent.classList.add('active');
                console.log(`Restored active tab to: ${savedTabId}`);
            }
        }
    }

    function formatTestRunTimes() {
        document.querySelectorAll('.test-run-time-cell').forEach(cell => {
            const timestamp = cell.dataset.timestamp;
            if (timestamp) {
                cell.textContent = formatDateTime(timestamp);
            }
        });
        console.log('Test run times formatted.');
    }

    async function loadStatistics() {
        DOM_ELEMENTS.statsContainer.innerHTML = `<p class="loading-message">${UI_MESSAGES.loadingStats}</p>`;
        try {
            const response = await fetch(`${API_ENDPOINTS.statistics}?_=${new Date().getTime()}`);
            if (!response.ok) throw new Error('Failed to fetch statistics');
            const stats = await response.json();
            renderStatistics(stats);
            renderPassRateChart(stats.dailyPassRateTrend);
            renderSlowTestsChart(stats.topSlowTests);
            console.log('Statistics loaded and rendered successfully.');
        } catch (error) {
            console.error('Error loading statistics:', error);
            DOM_ELEMENTS.statsContainer.innerHTML = `<p class="loading-message error">Не удалось загрузить статистику.</p>`;
            showToast('Не удалось загрузить статистику.', 'error');
        }
    }

    async function refreshDashboardData() {
        console.log('Refreshing all dashboard data...');
        await Promise.all([
            loadStatistics(),
            reloadTestRunList()
        ]);
        console.log('Dashboard data refreshed.');
    }

    async function reloadTestRunList() {
        try {
            const response = await fetch(API_ENDPOINTS.testListFragment);
            if (!response.ok) throw new Error('Could not fetch test list fragment.');

            const newTbodyHtml = await response.text();
            DOM_ELEMENTS.testListTableBody.innerHTML = newTbodyHtml;

            formatTestRunTimes();

            const selectedId = localStorage.getItem('selectedTestId');
            if (selectedId) {
                const selectedRow = document.querySelector(`.test-row[data-id="${selectedId}"]`);
                if (selectedRow) {
                    selectedRow.classList.add('selected');
                }
            }

            console.log('Test run list reloaded.');
        } catch (error) {
            console.error('Error reloading test run list:', error);
            showToast('Не удалось обновить список тестов.', 'error');
        }
    }

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

    function renderPassRateChart(trendData) {
        if (!DOM_ELEMENTS.passRateChartCtx || !trendData) {
            console.warn('Pass Rate Chart canvas element or trend data not found.');
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
                            drawOnChartArea: false,
                        },
                        title: {
                            display: true,
                            text: 'Всего запусков'
                        },
                        ticks: {
                            callback: function(value) {
                                return Number.isInteger(value) ? value : null;
                            }
                        }
                    }
                }
            }
        });
    }

    function renderSlowTestsChart(slowTestsData) {
        if (!DOM_ELEMENTS.slowTestsChartCtx || !slowTestsData) {
            console.warn('Slow Tests Chart canvas element or data not found.');
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
                indexAxis: 'y',
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
                        ticks: {
                           autoSkip: false,
                           callback: function(value, index, values) {
                               const label = this.getLabelForValue(value);
                               return label.length > 30 ? label.substring(0, 30) + '...' : label;
                           }
                        }
                    }
                }
            }
        });
    }

    async function loadTestDetails(testRunId, selectedRow = null) {
        document.querySelectorAll('.test-row').forEach(row => row.classList.remove('selected'));
        if (selectedRow) {
            selectedRow.classList.add('selected');
        }
        localStorage.setItem('selectedTestId', testRunId);

        if (testDetailsCache.has(testRunId)) {
            console.log(`Cache HIT for test run ID: ${testRunId}`);
            renderTestDetails(testDetailsCache.get(testRunId));
            return;
        }

        console.log(`Cache MISS for test run ID: ${testRunId}. Fetching from server...`);
        DOM_ELEMENTS.rightPanelContent.innerHTML = `<div class="placeholder-content"><p>${UI_MESSAGES.loadingDetails}</p></div>`;

        try {
            const response = await fetch(API_ENDPOINTS.testDetails(testRunId));
            if (!response.ok) throw new Error('Failed to fetch test details');

            const testRun = await response.json();

            testDetailsCache.set(testRunId, testRun);
            console.log(`Saved test run ID ${testRunId} to cache.`);

            renderTestDetails(testRun);

        } catch (error) {
            console.error('Error loading test details:', error);
            DOM_ELEMENTS.rightPanelContent.innerHTML = `<div class="placeholder-content error-state"><p>${UI_MESSAGES.errorState}</p></div>`;
            showToast('Не удалось загрузить детали.', 'error');
        }
    }

    function renderTestDetails(testRun) {
        const durationSeconds = testRun.durationMillis ? (testRun.durationMillis / 1000).toFixed(2) : 'N/A';

        let contentHtml = `
            <div class="detail-block">
                <h3><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Информация о запуске</h3>
                <div class="info-grid run-info-grid">
                    <p><strong>ID:</strong> ${escapeHtml(testRun.id)}</p>
                    <p><strong>Статус:</strong> <span class="status-badge status-${testRun.status.toLowerCase()}">${escapeHtml(testRun.status)}</span></p>
                    <p><strong>Класс:</strong> ${escapeHtml(testRun.testClass || 'N/A')}</p>
                    <p><strong>Метод:</strong> ${escapeHtml(testRun.testMethod || 'N/A')}</p>
                    <p><strong>Время запуска:</strong> <span class="run-info-time">${testRun.startTime ? formatDateTime(testRun.startTime) : 'N/A'}</span></p>
                    <p><strong>Время завершения:</strong> <span class="run-info-time">${testRun.endTime ? formatDateTime(testRun.endTime) : 'N/A'}</span></p>
                    <p><strong>Длительность:</strong> ${escapeHtml(durationSeconds)} сек</p>
                    <p><strong>Версия приложения:</strong> ${escapeHtml(testRun.configuration?.appVersion || 'N/A')}</p>
                    <p><strong>Тестовый набор:</strong> ${escapeHtml(testRun.configuration?.testSuite || 'N/A')}</p>
                </div>
            </div>
        `;

        if (testRun.environmentDetails) {
            const env = testRun.environmentDetails;
            contentHtml += `
                <div class="detail-block">
                    <h3><i class="fa-solid fa-desktop" aria-hidden="true"></i> Детали окружения</h3>
                    <div class="info-grid">
                        <p><strong>Имя:</strong> ${escapeHtml(env.name || 'N/A')}</p>
                        <p><strong>ОС:</strong> ${escapeHtml(env.osType || 'N/A')} ${escapeHtml(env.osVersion || '')}</p>
                        <p><strong>Браузер:</strong> ${escapeHtml(env.browserType || 'N/A')} ${escapeHtml(env.browserVersion || '')}</p>
                        <p><strong>Разрешение:</strong> ${escapeHtml(env.screenResolution || 'N/A')}</p>
                        <p><strong>Устройство:</strong> ${escapeHtml(env.deviceType || 'N/A')}</p>
                        ${env.deviceName ? `<p><strong>Имя устройства:</strong> ${escapeHtml(env.deviceName)}</p>` : ''}
                    </div>
                </div>
            `;
        }

        if (testRun.testTags && testRun.testTags.length > 0) {
            contentHtml += `
                <div class="detail-block">
                    <h3><i class="fa-solid fa-tags" aria-hidden="true"></i> Теги</h3>
                    <div class="tags-container">
                        ${testRun.testTags.map(tag => `<span class="test-tag">${escapeHtml(tag)}</span>`).join('')}
                    </div>
                </div>
            `;
        }

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
                        <p><strong>Причина:</strong> ${escapeHtml(result.suggestedReason || 'N/A')}</p>
                        <p><strong>Решение:</strong> ${escapeHtml(result.solution || 'N/A')}</p>
                        ${result.rawData ? `<details><summary>Сопутствующие данные</summary><pre class="raw-data-block">${escapeHtml(result.rawData)}</pre></details>` : ''}
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
                        ${testRun.failedStep.confidenceScore !== undefined ? `<p><strong>Уверенность AI:</strong> ${(testRun.failedStep.confidenceScore * 100).toFixed(0)}%</p>` : ''}
                        ${testRun.failedStep.errorMessage ? `<p><strong>Сообщение об ошибке:</strong> ${escapeHtml(testRun.failedStep.errorMessage)}</p>` : ''}
                    </div>`;
            }
            if (testRun.exceptionType || testRun.exceptionMessage || testRun.stackTrace) {
                contentHtml += `<div class="failure-subsection">`;
                if (testRun.exceptionType) contentHtml += `<p><strong>Тип исключения:</strong> ${escapeHtml(testRun.exceptionType)}</p>`;
                if (testRun.exceptionMessage) contentHtml += `<p><strong>Сообщение:</strong> ${escapeHtml(testRun.exceptionMessage)}</p>`;
                if (testRun.stackTrace) contentHtml += `<p><strong>Стек-трейс:</strong></p><pre class="error-pre">${escapeHtml(testRun.stackTrace)}</pre>`;
                contentHtml += `</div>`;
            }
            contentHtml += `</div>`;
        }

        if (testRun.executionPath && testRun.executionPath.length > 0) {
            contentHtml += `
                <div class="detail-block">
                    <h3><i class="fa-solid fa-list-ol" aria-hidden="true"></i> Путь выполнения</h3>
                    <ul class="execution-path-visualizer">`;

            testRun.executionPath.forEach((step, index) => {
                const status = (step.result || 'unknown').toLowerCase();
                const statusClass = `step-${status}`;

                const confidence = step.confidenceScore !== undefined ? `Уверенность: ${(step.confidenceScore * 100).toFixed(0)}%` : '';
                const stepDuration = step.stepDurationMillis ? `Длительность: ${(step.stepDurationMillis / 1000).toFixed(2)} сек` : '';

                contentHtml += `
                    <li class="${statusClass}">
                        <div class="step-header">
                            <strong>Шаг ${index + 1}:</strong> ${escapeHtml(step.action || 'Неизвестное действие')}
                            <span class="step-status status-badge status-${status}">${escapeHtml(step.result || 'N/A')}</span>
                        </div>
                        <div class="step-details">
                            <p><strong>Локатор:</strong> ${escapeHtml(step.locatorStrategy || 'N/A')} = ${escapeHtml(step.locatorValue || 'N/A')}</p>
                            ${step.interactedText ? `<p><strong>Взаимодействовали с текстом:</strong> ${escapeHtml(step.interactedText)}</p>` : ''}
                            ${confidence ? `<p>${confidence}</p>` : ''}
                            ${step.errorMessage ? `<p class="step-error-message"><strong>Ошибка:</strong> ${escapeHtml(step.errorMessage)}</p>` : ''}
                            ${stepDuration ? `<p>${stepDuration}</p>` : ''}
                        </div>
                    </li>`;
            });

            contentHtml += `</ul></div>`;
        }

        DOM_ELEMENTS.rightPanelContent.innerHTML = contentHtml;
    }

    async function submitFeedback(analysisId, isCorrect, buttonContainer) {
        try {
            const response = await fetch(API_ENDPOINTS.feedback(analysisId), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ isAiSuggestionCorrect: isCorrect })
            });

            if (!response.ok) throw new Error(`Server responded with status ${response.status}.`);

            showToast(UI_MESSAGES.feedbackSuccess, 'success');
            if (buttonContainer) {
                buttonContainer.style.display = 'none';
                const submittedMsg = buttonContainer.nextElementSibling;
                if (submittedMsg) submittedMsg.style.display = 'block';
            }
        } catch (error) {
            console.error('Error submitting feedback:', error);
            showToast(UI_MESSAGES.feedbackError, 'error');
        }
    }

    function initializeTestRowClickListeners() {
        DOM_ELEMENTS.testListTableBody.addEventListener('click', (event) => {
            const row = event.target.closest('.test-row');
            if (row) {
                loadTestDetails(row.dataset.id, row);
            }
        });

        DOM_ELEMENTS.testListTableBody.addEventListener('keydown', (event) => {
            const row = event.target.closest('.test-row');
            if (row && (event.key === 'Enter' || event.key === ' ')) {
                event.preventDefault();
                loadTestDetails(row.dataset.id, row);
            }
        });
    }

    function loadInitialTestDetails() {
        const savedTestId = localStorage.getItem('selectedTestId');
        let rowToSelect = document.querySelector(`.test-row[data-id="${savedTestId}"]`) || document.querySelector('.test-row');

        if (rowToSelect) {
            loadTestDetails(rowToSelect.dataset.id, rowToSelect);
        } else {
            DOM_ELEMENTS.rightPanelContent.innerHTML = `<div class="placeholder-content"><i class="fa-regular fa-hand-pointer placeholder-icon"></i><p>${UI_MESSAGES.selectTestRun}</p></div>`;
            console.log('No test runs found to load initial details.');
        }
    }

    function initializeTabSwitching() {
        DOM_ELEMENTS.tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetTabId = button.dataset.tab;
                localStorage.setItem('activeTabId', targetTabId);

                DOM_ELEMENTS.tabButtons.forEach(btn => btn.classList.remove('active'));
                DOM_ELEMENTS.tabContents.forEach(content => content.classList.remove('active'));

                button.classList.add('active');
                document.getElementById(`${targetTabId}TabContent`).classList.add('active');

                if (targetTabId === 'widgets') {
                    requestAnimationFrame(() => {
                        if (passRateChart) passRateChart.resize();
                        if (slowTestsChart) slowTestsChart.resize();
                    });
                }
            });
        });
    }

    function prependNewTestRow(dto) {
        const statusIcons = {
            'PASSED': 'fa-solid fa-circle-check status-icon success-icon',
            'FAILED': 'fa-solid fa-circle-xmark status-icon failure-icon',
            'SKIPPED': 'fa-solid fa-circle-minus status-icon skipped-icon',
            'BROKEN': 'fa-solid fa-circle-exclamation status-icon skipped-icon'
        };

        const newRow = document.createElement('tr');
        newRow.className = 'test-row';
        newRow.dataset.id = dto.id;
        newRow.tabIndex = 0;
        newRow.setAttribute('aria-label', `Просмотреть детали запуска теста: ${dto.testMethod} со статусом ${dto.status}`);

        newRow.innerHTML = `
            <td>
                <span class="status-indicator">
                    <i class="${statusIcons[dto.status] || 'fa-solid fa-circle-question'}" title="${dto.status}"></i>
                </span>
            </td>
            <td class="test-method-cell">${escapeHtml(dto.testMethod)}</td>
            <td>
                <span class="badge badge-env">${escapeHtml(dto.configuration?.environment || 'N/A')}</span>
            </td>
            <td>${escapeHtml(dto.configuration?.appVersion || 'N/A')}</td>
            <td class="test-run-time-cell" data-timestamp="${dto.timestamp}"></td>
        `;

        DOM_ELEMENTS.testListTableBody.prepend(newRow);
        formatTestRunTimes();
    }

    function initializeHeaderActions() {
        const deleteAllBtn = document.getElementById('deleteAllDataBtn');
        if (deleteAllBtn) {
            deleteAllBtn.addEventListener('click', async () => {
                if (confirm('Вы уверены, что хотите удалить все данные о запусках? Это действие необратимо.')) {
                    try {
                        const response = await fetch(API_ENDPOINTS.deleteAll, { method: 'DELETE' });
                        if (response.ok) {
                            testDetailsCache.clear();
                            showToast('Все данные успешно удалены. Страница будет перезагружена.', 'success');
                            setTimeout(() => {
                                localStorage.removeItem('selectedTestId');
                                window.location.reload();
                            }, 2000);
                        } else {
                           throw new Error('Server error on delete.');
                        }
                    } catch (error) {
                        console.error('Error deleting all data:', error);
                        showToast('Ошибка при удалении данных.', 'error');
                    }
                }
            });
        }

        const createDemoBtn = document.getElementById('createDemoBtn');
        if(createDemoBtn) {
            createDemoBtn.addEventListener('click', async () => {
                createDemoBtn.disabled = true;

                try {
                    const response = await fetch(API_ENDPOINTS.createDemo, { method: 'POST' });
                    if (!response.ok) throw new Error('Server error during demo creation.');

                    const newTestRunDetails = await response.json();

                    testDetailsCache.set(newTestRunDetails.id, newTestRunDetails);
                    console.log(`Proactively cached new demo test run: ${newTestRunDetails.id}`);

                    prependNewTestRow(newTestRunDetails);

                    const newRow = document.querySelector(`.test-row[data-id="${newTestRunDetails.id}"]`);
                    if(newRow) {
                        loadTestDetails(newTestRunDetails.id, newRow);
                    }

                    loadStatistics();

                    showToast('Демо-запись успешно создана', 'success');

                } catch (error) {
                    console.error("Failed to create demo record:", error);
                    showToast('Ошибка при создании демо-записи.', 'error');
                } finally {
                    createDemoBtn.disabled = false;
                }
            });
        }
    }

    function showToast(message, type = 'success') {
        if (!DOM_ELEMENTS.toastContainer) return;
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        if (type === 'info') toast.style.backgroundColor = '#2196F3';

        const iconClass = type === 'success' ? 'fa-circle-check' : (type === 'error' ? 'fa-circle-xmark' : 'fa-circle-info');
        toast.innerHTML = `<i class="fa-solid ${iconClass}"></i> ${escapeHtml(message)}`;
        DOM_ELEMENTS.toastContainer.appendChild(toast);

        setTimeout(() => toast.classList.add('show'), 10);
        setTimeout(() => {
            toast.classList.remove('show');
            toast.addEventListener('transitionend', () => toast.remove(), { once: true });
        }, 5000);
    }

    function escapeHtml(unsafe) {
        if (typeof unsafe !== 'string') return unsafe;
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(unsafe));
        return div.innerHTML;
    }

    function formatDateTime(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return dateString;
        return date.toLocaleString('ru-RU', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        }).replace(',', '');
    }

    initDashboard();
});