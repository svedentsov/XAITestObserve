((window, document, Stomp, Chart) => {
    'use strict';

    // --- 1. МОДУЛЬ КОНФИГУРАЦИИ ---
    const config = {
        API_ENDPOINTS: {
            statistics: '/api/v1/statistics',
            paginatedTests: (page, size) => `/api/v1/tests?page=${page}&size=${size}&sort=timestamp,desc`,
            testDetails: (id) => `/api/v1/tests/${id}`,
            feedback: (id) => `/api/v1/analysis/${id}/feedback`,
            deleteAll: '/api/v1/tests/all',
            createDemo: '/demo/create'
        },
        DOM: {
            rightPanelContent: document.getElementById('rightPanelContent'),
            toastContainer: document.getElementById('toastContainer'),
            statsContainer: document.getElementById('stats-container'),
            passRateChartCtx: document.getElementById('passRateChart')?.getContext('2d'),
            slowTestsChartCtx: document.getElementById('slowTestsChart')?.getContext('2d'),
            tabButtons: document.querySelectorAll('.tab-button'),
            tabContents: document.querySelectorAll('.tab-content'),
            testListTableBody: document.getElementById('testListTableBody'),
            listWrapper: document.querySelector('.test-list-wrapper'),
            listSpinner: document.getElementById('list-spinner'),
            deleteAllDataBtn: document.getElementById('deleteAllDataBtn'),
            createDemoBtn: document.getElementById('createDemoBtn'),
            testRowTemplate: document.getElementById('test-row-template'),
        },
        METADATA_LABELS: {
            buildNumber: 'Номер сборки', jenkinsJobUrl: 'Сборка в Jenkins',
            jiraTicket: 'Задача в Jira', gitBranch: 'Ветка Git',
            commitHash: 'Хеш коммита', triggeredBy: 'Кем запущено'
        },
        PAGE_SIZE: 50,
        CSRF_TOKEN: document.querySelector("meta[name='_csrf']")?.content,
        CSRF_HEADER: document.querySelector("meta[name='_csrf_header']")?.content,
    };

    // --- 2. МОДУЛЬ СОСТОЯНИЯ ПРИЛОЖЕНИЯ ---
    const state = {
        stompClient: null,
        passRateChart: null,
        slowTestsChart: null,
        testDetailsCache: new Map(),
        currentPage: 0,
        isLoading: false,
        isLastPage: false,
    };

    // --- 3. СЕРВИСНЫЙ МОДУЛЬ API ---
    const apiService = {
        async fetchJson(url) {
            const response = await fetch(url);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        },
        async fetchWithMethod(method, url) {
            const headers = {};
            if (config.CSRF_HEADER && config.CSRF_TOKEN) {
                headers[config.CSRF_HEADER] = config.CSRF_TOKEN;
            }
            const response = await fetch(url, { method, headers });
            if (!response.ok) throw new Error(`Server error: ${response.statusText}`);
            return response;
        },
        async submitJson(url, data) {
            const headers = { 'Content-Type': 'application/json' };
            if (config.CSRF_HEADER && config.CSRF_TOKEN) {
                headers[config.CSRF_HEADER] = config.CSRF_TOKEN;
            }
            const response = await fetch(url, { method: 'POST', headers, body: JSON.stringify(data) });
            if (!response.ok) throw new Error(`Server responded with status ${response.status}.`);
            return response;
        },
        getPaginatedTests: (page, size) => apiService.fetchJson(config.API_ENDPOINTS.paginatedTests(page, size)),
        getStatistics: () => apiService.fetchJson(`${config.API_ENDPOINTS.statistics}?_=${new Date().getTime()}`),
        getTestDetails: (id) => apiService.fetchJson(config.API_ENDPOINTS.testDetails(id)),
        submitFeedback: (id, data) => apiService.submitJson(config.API_ENDPOINTS.feedback(id), data),
        deleteAllData: () => apiService.fetchWithMethod('DELETE', config.API_ENDPOINTS.deleteAll),
        createDemo: () => apiService.fetchWithMethod('POST', config.API_ENDPOINTS.createDemo),
    };

    // --- 4. МОДУЛЬ УТИЛИТ ---
    const utils = {
        escapeHtml: (unsafe) => typeof unsafe !== 'string' ? unsafe : unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;"),
        formatDateTime: (dateString) => dateString ? new Date(dateString).toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'medium' }) : 'N/A',
    };

    // --- 5. МОДУЛЬ РЕНДЕРИНГА UI (ПОЛНАЯ РЕАЛИЗАЦИЯ) ---
    const uiRenderer = {
        renderPlaceholder(message = 'Выберите тестовый запуск из списка.', isError = false) {
            const iconClass = isError ? "fa-solid fa-circle-exclamation" : "fa-regular fa-hand-pointer";
            config.DOM.rightPanelContent.innerHTML = `
            <div class="placeholder-content ${isError ? 'error-state' : ''}">
                <i class="${iconClass} placeholder-icon" aria-hidden="true"></i>
                <p>${utils.escapeHtml(message)}</p>
            </div>`;
        },

        createTestRow(run) {
            const template = config.DOM.testRowTemplate.content.cloneNode(true);
            const row = template.querySelector('tr');
            row.dataset.id = run.id;
            row.setAttribute('aria-label', `Детали теста: ${run.testMethod}, статус ${run.status}`);
            const statusIcons = { PASSED: 'fa-solid fa-circle-check success-icon', FAILED: 'fa-solid fa-circle-xmark failure-icon', SKIPPED: 'fa-solid fa-circle-minus skipped-icon', BROKEN: 'fa-solid fa-circle-exclamation skipped-icon' };
            const icon = row.querySelector('.status-indicator i');
            icon.className = statusIcons[run.status] || 'fa-solid fa-circle-question';
            icon.title = run.status;
            row.querySelector('.test-method-cell').textContent = run.testMethod || 'N/A';
            row.querySelector('.badge-env').textContent = run.configuration?.environment || 'N/A';
            row.querySelector('.test-version-cell').textContent = run.configuration?.appVersion || 'N/A';
            row.querySelector('.test-run-time-cell').textContent = utils.formatDateTime(run.timestamp);
            return row;
        },

        prependNewTestRow(run) {
            const newRow = this.createTestRow(run);
            config.DOM.testListTableBody.prepend(newRow);
        },

        renderTestDetails(testRun) {
            let contentHtml = this.renderRunInfo(testRun);
            if (testRun.environmentDetails) contentHtml += this.renderEnvironmentDetails(testRun.environmentDetails);
            if (testRun.customMetadata && Object.keys(testRun.customMetadata).length > 0) contentHtml += this.renderCustomMetadata(testRun.customMetadata);
            if (testRun.analysisResults?.length > 0) contentHtml += this.renderAnalysisResults(testRun.analysisResults);
            if (testRun.status === 'FAILED') contentHtml += this.renderFailureDetails(testRun);
            if (testRun.executionPath?.length > 0) contentHtml += this.renderExecutionPath(testRun.executionPath);
            config.DOM.rightPanelContent.innerHTML = contentHtml;
        },

        renderStatistics(stats) {
            const renderTopList = (data) => !data || Object.keys(data).length === 0 ? '<p class="no-data-message">Нет данных о сбоях.</p>' : `<ul>${Object.entries(data).map(([testName, count]) => `<li><span>${utils.escapeHtml(testName)}</span> <strong>${count}</strong></li>`).join('')}</ul>`;
            config.DOM.statsContainer.innerHTML = `<div class="stats-grid"><div class="stats-card"><h4>Прогоны</h4><p>${stats.totalRuns || 0}</p></div><div class="stats-card"><h4>Pass Rate</h4><p>${(stats.passRate || 0).toFixed(1)}%</p></div><div class="stats-card status-passed"><h4>Прошли</h4><p>${stats.passedRuns || 0}</p></div><div class="stats-card status-failed"><h4>Упали</h4><p>${stats.failedRuns || 0}</p></div></div><div class="failing-tests-card"><h4>Топ нестабильных тестов</h4><div class="content">${renderTopList(stats.failureCountByTest)}</div></div>`;
        },

        renderRunInfo(testRun) {
            const durationSeconds = testRun.durationMillis ? (testRun.durationMillis / 1000).toFixed(2) : 'N/A';
            return `<div class="detail-block"><h3><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Информация о запуске</h3><div class="info-grid-3col"><p><strong>ID:</strong> ${utils.escapeHtml(testRun.id)}</p><p><strong>Статус:</strong> <span class="status-badge status-${(testRun.status || 'unknown').toLowerCase()}">${utils.escapeHtml(testRun.status)}</span></p><p><strong>Класс:</strong> ${utils.escapeHtml(testRun.testClass || 'N/A')}</p><p><strong>Метод:</strong> ${utils.escapeHtml(testRun.testMethod || 'N/A')}</p><p><strong>Тестовый набор:</strong> ${utils.escapeHtml(testRun.configuration?.testSuite || 'N/A')}</p><p><strong>Версия приложения:</strong> ${utils.escapeHtml(testRun.configuration?.appVersion || 'N/A')}</p><p><strong>Время запуска:</strong> ${utils.formatDateTime(testRun.startTime)}</p><p><strong>Время завершения:</strong> ${utils.formatDateTime(testRun.endTime)}</p><p><strong>Длительность:</strong> ${utils.escapeHtml(durationSeconds)} сек</p></div></div>`;
        },

        renderEnvironmentDetails(env) {
            return `<div class="detail-block"><h3><i class="fa-solid fa-desktop" aria-hidden="true"></i> Детали окружения</h3><div class="info-grid-3col"><p><strong>Имя:</strong> ${utils.escapeHtml(env.name || 'N/A')}</p><p><strong>ОС:</strong> ${utils.escapeHtml(env.osType || 'N/A')} ${utils.escapeHtml(env.osVersion || '')}</p><p><strong>Браузер:</strong> ${utils.escapeHtml(env.browserType || 'N/A')} ${utils.escapeHtml(env.browserVersion || '')}</p><p><strong>Разрешение:</strong> ${utils.escapeHtml(env.screenResolution || 'N/A')}</p><p><strong>Устройство:</strong> ${utils.escapeHtml(env.deviceType || 'N/A')}</p>${env.deviceName ? `<p><strong>Имя устройства:</strong> ${utils.escapeHtml(env.deviceName)}</p>` : ''}</div></div>`;
        },

        renderCustomMetadata(metadata) {
            const content = Object.entries(metadata).map(([key, value]) => {
                const label = config.METADATA_LABELS[key] || utils.escapeHtml(key);
                const isUrl = value && (value.startsWith('http://') || value.startsWith('https://'));
                return `<p><strong>${label}:</strong> ${isUrl ? `<a href="${utils.escapeHtml(value)}" target="_blank" rel="noopener noreferrer">Ссылка</a>` : utils.escapeHtml(value)}</p>`;
            }).join('');
            return `<div class="detail-block"><h3><i class="fa-solid fa-cogs"></i> Дополнительные данные</h3><div class="info-grid-3col">${content}</div></div>`;
        },

        renderAnalysisResults(results) {
            const resultItems = results.map(result => {
                const confidence = result.aiConfidence !== undefined ? `${(result.aiConfidence * 100).toFixed(0)}%` : 'N/A';
                const explanationHtml = result.explanationData ? `<details><summary>Данные для объяснения (XAI)</summary><pre>${utils.escapeHtml(JSON.stringify(result.explanationData, null, 2))}</pre></details>` : '';
                return `<div class="analysis-result-item"><h4>${utils.escapeHtml(result.analysisType)} <span class="ai-confidence">${confidence}</span></h4><p><strong>Причина:</strong> ${utils.escapeHtml(result.suggestedReason || 'N/A')}</p><p><strong>Решение:</strong> ${utils.escapeHtml(result.solution || 'N/A')}</p>${explanationHtml}<div class="feedback-section" data-analysis-id="${result.id}"><span>Анализ был полезен?</span><div class="feedback-buttons"><button class="feedback-btn" data-correct="true" title="Да"><i class="fa-solid fa-thumbs-up"></i></button><button class="feedback-btn" data-correct="false" title="Нет"><i class="fa-solid fa-thumbs-down"></i></button></div><div class="feedback-submitted-msg" style="display: none;">Спасибо за ваш отзыв!</div></div></div>`;
            }).join('');
            return `<div class="detail-block"><h3><i class="fa-solid fa-brain" aria-hidden="true"></i> Результаты Анализа AI</h3>${resultItems}</div>`;
        },

        renderFailureDetails(testRun) {
            let content = '';
            if (testRun.exceptionMessage) content += `<div class="failure-subsection"><p><strong>Сообщение:</strong> ${utils.escapeHtml(testRun.exceptionMessage)}</p></div>`;
            if (testRun.stackTrace) content += `<div class="failure-subsection"><p><strong>Стек-трейс:</strong></p><pre class="error-pre">${utils.escapeHtml(testRun.stackTrace)}</pre></div>`;
            return `<div class="detail-block"><h3><i class="fa-solid fa-bug" aria-hidden="true"></i> Детали сбоя</h3>${content}</div>`;
        },

        renderExecutionPath(path) {
            const steps = path.map((step, index) => `<li class="step-${(step.result || 'unknown').toLowerCase()}"><div class="step-header"><strong>Шаг ${index + 1}:</strong> ${utils.escapeHtml(step.action || 'N/A')}</div><div class="step-details"><p><strong>Статус:</strong> <span class="status-badge status-${(step.result || 'unknown').toLowerCase()}">${utils.escapeHtml(step.result || 'N/A')}</span></p><p><strong>Локатор:</strong> ${utils.escapeHtml(step.locatorStrategy || 'N/A')} = ${utils.escapeHtml(step.locatorValue || 'N/A')}</p>${step.errorMessage ? `<p class="step-error-message"><strong>Ошибка:</strong> ${utils.escapeHtml(step.errorMessage)}</p>` : ''}</div></li>`).join('');
            return `<div class="detail-block"><h3><i class="fa-solid fa-list-ol" aria-hidden="true"></i> Путь выполнения</h3><ul class="execution-path-visualizer">${steps}</ul></div>`;
        },

        showToast(message, type = 'info') {
            const iconMap = { 'success': 'fa-circle-check', 'error': 'fa-circle-xmark', 'info': 'fa-circle-info' };
            const toast = document.createElement('div');
            toast.className = `toast ${type}`;
            toast.innerHTML = `<i class="fa-solid ${iconMap[type]}"></i> ${utils.escapeHtml(message)}`;
            config.DOM.toastContainer.appendChild(toast);
            requestAnimationFrame(() => toast.classList.add('show'));
            setTimeout(() => {
                toast.classList.remove('show');
                toast.addEventListener('transitionend', () => toast.remove(), { once: true });
            }, 5000);
        }
    };

    // --- 6. МОДУЛЬ УПРАВЛЕНИЯ ГРАФИКАМИ ---
    const chartManager = {
        renderPassRateChart(trendData) {
            if (!config.DOM.passRateChartCtx || !trendData) return;
            const sortedData = [...trendData].sort((a, b) => new Date(a.date) - new Date(b.date));
            const chartData = { labels: sortedData.map(item => item.date), datasets: [{ label: 'Pass Rate (%)', data: sortedData.map(item => item.passRate), borderColor: 'rgb(30, 142, 62)', backgroundColor: 'rgba(30, 142, 62, 0.1)', fill: true, tension: 0.3 }]};
            if (state.passRateChart) { state.passRateChart.data = chartData; state.passRateChart.update(); } else { state.passRateChart = new Chart(config.DOM.passRateChartCtx, { type: 'line', data: chartData, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } } }); }
        },
        renderSlowTestsChart(slowTestsData) {
            if (!config.DOM.slowTestsChartCtx || !slowTestsData) return;
            const sortedData = [...slowTestsData].sort((a, b) => b.averageDurationMillis - a.averageDurationMillis);
            const chartData = { labels: sortedData.map(item => item.testName.split('.').pop()), datasets: [{ label: 'Средняя длительность (сек)', data: sortedData.map(item => (item.averageDurationMillis / 1000).toFixed(2)), backgroundColor: 'rgba(217, 48, 37, 0.7)', borderColor: 'rgb(217, 48, 37)', borderWidth: 1 }]};
            if (state.slowTestsChart) { state.slowTestsChart.data = chartData; state.slowTestsChart.update(); } else { state.slowTestsChart = new Chart(config.DOM.slowTestsChartCtx, { type: 'bar', data: chartData, options: { responsive: true, maintainAspectRatio: false, indexAxis: 'y', plugins: { legend: { display: false } } } }); }
        }
    };

    // --- 7. МОДУЛЬ WEBSOCKET ---
    const webSocketHandler = {
        connect() {
            try {
                const socket = new SockJS('/ws');
                state.stompClient = Stomp.over(socket);
                state.stompClient.debug = null;
                state.stompClient.connect({}, () => {
                    console.log('Connected to WebSocket');
                    uiRenderer.showToast('Подключено к серверу для обновлений.', 'success');
                    state.stompClient.subscribe('/topic/new-test-run', message => {
                        const newTestRun = JSON.parse(message.body);
                        state.testDetailsCache.set(newTestRun.id, newTestRun);
                        uiRenderer.prependNewTestRow(newTestRun);
                        actions.loadStatistics();
                        uiRenderer.showToast(`Получен новый запуск: ${newTestRun.testMethod}`, 'info');
                    });
                }, error => {
                    console.error('WebSocket connection error:', error);
                    uiRenderer.showToast('Соединение потеряно. Переподключение...', 'error');
                    setTimeout(() => this.connect(), 5000);
                });
            } catch (e) {
                console.error("Failed to initialize WebSocket", e);
                uiRenderer.showToast('Не удалось подключиться к WebSocket.', 'error');
            }
        }
    };

    // --- 8. МОДУЛЬ ДЕЙСТВИЙ (CONTROLLER) ---
    const actions = {
        async fetchAndRenderTestRuns(page, initialLoad = false) {
             if (state.isLoading || state.isLastPage) return;
             state.isLoading = true;
             config.DOM.listSpinner.style.display = 'block';
             try {
                const pageData = await apiService.getPaginatedTests(page, config.PAGE_SIZE);
                const fragment = document.createDocumentFragment();
                pageData.content.forEach(run => {
                    state.testDetailsCache.set(run.id, run);
                    fragment.appendChild(uiRenderer.createTestRow(run));
                });
                config.DOM.testListTableBody.appendChild(fragment);
                state.isLastPage = pageData.last;
                state.currentPage = pageData.number;
                if (initialLoad && pageData.content.length > 0) {
                    const firstRow = config.DOM.testListTableBody.querySelector('.test-row');
                    if (firstRow) this.loadTestDetails(firstRow.dataset.id, firstRow);
                } else if (initialLoad) {
                     uiRenderer.renderPlaceholder();
                }
             } catch (error) {
                 console.error('Failed to fetch test runs:', error);
                 uiRenderer.showToast('Не удалось загрузить список тестов.', 'error');
             } finally {
                 state.isLoading = false;
                 config.DOM.listSpinner.style.display = 'none';
             }
        },
        async loadStatistics() {
            try {
                const stats = await apiService.getStatistics();
                uiRenderer.renderStatistics(stats);
                chartManager.renderPassRateChart(stats.dailyPassRateTrend);
                chartManager.renderSlowTestsChart(stats.topSlowTests);
            } catch (error) {
                console.error('Error loading statistics:', error);
                uiRenderer.showToast('Не удалось загрузить статистику.', 'error');
            }
        },
        async loadTestDetails(testRunId, selectedRow = null) {
            document.querySelectorAll('.test-row.selected').forEach(row => row.classList.remove('selected'));
            if (selectedRow) selectedRow.classList.add('selected');
            uiRenderer.renderPlaceholder('Загрузка деталей...');
            if (state.testDetailsCache.has(testRunId)) {
                uiRenderer.renderTestDetails(state.testDetailsCache.get(testRunId));
                return;
            }
            try {
                const testRun = await apiService.getTestDetails(testRunId);
                state.testDetailsCache.set(testRunId, testRun);
                uiRenderer.renderTestDetails(testRun);
            } catch (error) {
                console.error('Error loading test details:', error);
                uiRenderer.renderPlaceholder('Ошибка при загрузке деталей.', true);
                uiRenderer.showToast('Не удалось загрузить детали.', 'error');
            }
        },
        async submitFeedback(feedbackSection, isCorrect) {
            const analysisId = feedbackSection.dataset.analysisId;
            try {
                await apiService.submitFeedback(analysisId, { isAiSuggestionCorrect: isCorrect });
                uiRenderer.showToast('Спасибо за ваш отзыв!', 'success');
                feedbackSection.classList.add('submitted');
                feedbackSection.querySelector('.feedback-buttons').style.display = 'none';
                const msgEl = feedbackSection.querySelector('.feedback-submitted-msg');
                if (msgEl) msgEl.style.display = 'inline';
            } catch (error) {
                console.error('Error submitting feedback:', error);
                uiRenderer.showToast('Не удалось отправить отзыв.', 'error');
            }
        },
        async deleteAllData() {
            if (confirm('Вы уверены, что хотите удалить все данные? Это действие необратимо.')) {
                try {
                    await apiService.deleteAllData();
                    uiRenderer.showToast('Все данные удалены. Страница будет перезагружена.', 'success');
                    setTimeout(() => window.location.reload(), 2000);
                } catch (error) {
                    uiRenderer.showToast('Ошибка при удалении данных.', 'error');
                }
            }
        },
        async createDemo() {
            config.DOM.createDemoBtn.disabled = true;
            config.DOM.createDemoBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Создание...';
            try {
                await apiService.createDemo();
                uiRenderer.showToast('Запрос на создание демо-записи отправлен.', 'info');
            } catch (error) {
                uiRenderer.showToast('Ошибка при создании демо-записи.', 'error');
            } finally {
                config.DOM.createDemoBtn.disabled = false;
                config.DOM.createDemoBtn.innerHTML = '<i class="fa-solid fa-plus"></i> Создать демо';
            }
        }
    };

    // --- 9. МОДУЛЬ ОБРАБОТЧИКОВ СОБЫТИЙ ---
    const eventHandlers = {
        setupEventListeners() {
            config.DOM.listWrapper?.addEventListener('scroll', () => { if (config.DOM.listWrapper.scrollTop + config.DOM.listWrapper.clientHeight >= config.DOM.listWrapper.scrollHeight - 50) { actions.fetchAndRenderTestRuns(state.currentPage + 1); } });
            config.DOM.testListTableBody?.addEventListener('click', (event) => { const row = event.target.closest('.test-row'); if (row) actions.loadTestDetails(row.dataset.id, row); });
            config.DOM.rightPanelContent?.addEventListener('click', e => { const button = e.target.closest('.feedback-btn'); if (button) { const feedbackSection = button.closest('.feedback-section'); if (!feedbackSection.classList.contains('submitted')) { actions.submitFeedback(feedbackSection, button.dataset.correct === 'true'); } } });
            config.DOM.tabButtons.forEach(button => button.addEventListener('click', (e) => this.handleTabSwitch(e.currentTarget)));
            config.DOM.deleteAllDataBtn?.addEventListener('click', actions.deleteAllData);
            config.DOM.createDemoBtn?.addEventListener('click', actions.createDemo);
        },
        handleTabSwitch(clickedButton) {
            const targetTabId = clickedButton.dataset.tab;
            localStorage.setItem('activeTabId', targetTabId);
            config.DOM.tabButtons.forEach(btn => btn.classList.remove('active'));
            config.DOM.tabContents.forEach(content => content.classList.remove('active'));
            clickedButton.classList.add('active');
            const targetContent = document.getElementById(`${targetTabId}TabContent`);
            if (targetContent) targetContent.classList.add('active');
            requestAnimationFrame(() => { if (state.passRateChart) state.passRateChart.resize(); if (state.slowTestsChart) state.slowTestsChart.resize(); });
        },
        restoreActiveTab() {
            const savedTabId = localStorage.getItem('activeTabId') || 'overview';
            const buttonToActivate = document.querySelector(`.tab-button[data-tab="${savedTabId}"]`);
            if (buttonToActivate) this.handleTabSwitch(buttonToActivate);
        }
    };

    // --- 10. ТОЧКА ВХОДА ПРИЛОЖЕНИЯ ---
    const init = () => {
        console.log('XAI Observer Dashboard Initializing...');
        eventHandlers.restoreActiveTab();
        eventHandlers.setupEventListeners();
        webSocketHandler.connect();
        actions.loadStatistics();
        actions.fetchAndRenderTestRuns(0, true);
    };

    document.addEventListener('DOMContentLoaded', init);

})(window, document, window.Stomp, window.Chart);
