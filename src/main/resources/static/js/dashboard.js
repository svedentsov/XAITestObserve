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
            html: document.documentElement,
            themeToggle: document.getElementById('theme-toggle'),
            searchInput: document.getElementById('search-input'),
            statusFilters: document.getElementById('status-filters'),
            noResultsPlaceholder: document.getElementById('no-results-placeholder'),
            rightPanel: document.querySelector('.right-panel'),
            rightPanelContent: document.getElementById('rightPanelContent'),
            toastContainer: document.getElementById('toastContainer'),
            statsContainer: document.getElementById('stats-container'),
            passRateChartCtx: document.getElementById('passRateChart')?.getContext('2d'),
            failingTestsChartCtx: document.getElementById('failingTestsChart')?.getContext('2d'),
            exceptionTypesChartCtx: document.getElementById('exceptionTypesChart')?.getContext('2d'),
            runsByEnvChartCtx: document.getElementById('runsByEnvChart')?.getContext('2d'),
            runsBySuiteChartCtx: document.getElementById('runsBySuiteChart')?.getContext('2d'),
            tabButtons: document.querySelectorAll('.tab-button'),
            tabContents: document.querySelectorAll('.tab-content'),
            testListTableBody: document.getElementById('testListTableBody'),
            listWrapper: document.querySelector('.test-list-wrapper'),
            listSpinner: document.getElementById('list-spinner'),
            deleteAllDataBtn: document.getElementById('deleteAllDataBtn'),
            createDemoBtn: document.getElementById('createDemoBtn'),
            testRowTemplate: document.getElementById('test-row-template'),
        },
        METADATA_LABELS: { buildNumber: 'Номер сборки', jenkinsJobUrl: 'Сборка в Jenkins', jiraTicket: 'Задача в Jira' },
        PAGE_SIZE: 50,
        CSRF_TOKEN: document.querySelector("meta[name='_csrf']")?.content,
        CSRF_HEADER: document.querySelector("meta[name='_csrf_header']")?.content,
    };

    // --- 2. МОДУЛЬ СОСТОЯНИЯ ПРИЛОЖЕНИЯ ---
    const state = { stompClient: null, charts: {}, testDetailsCache: new Map(), currentPage: 0, isLoading: false, isLastPage: false, activeFilters: { status: 'ALL', searchQuery: '' }, debounceTimer: null };

    // --- 3. СЕРВИСНЫЙ МОДУЛЬ API ---
    const apiService = {
        async fetchJson(url) { const response = await fetch(url); if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`); return response.json(); },
        async fetchWithMethod(method, url, body = null) { const headers = {}; if (config.CSRF_HEADER && config.CSRF_TOKEN) { headers[config.CSRF_HEADER] = config.CSRF_TOKEN; } if (body) { headers['Content-Type'] = 'application/json'; } const response = await fetch(url, { method, headers, body: body ? JSON.stringify(body) : null }); if (!response.ok) throw new Error(`Server error: ${response.statusText}`); return response; },
        getPaginatedTests: (page, size) => apiService.fetchJson(config.API_ENDPOINTS.paginatedTests(page, size)),
        getStatistics: () => apiService.fetchJson(`${config.API_ENDPOINTS.statistics}?_=${new Date().getTime()}`),
        getTestDetails: (id) => apiService.fetchJson(config.API_ENDPOINTS.testDetails(id)),
        submitFeedback: (id, data) => apiService.fetchWithMethod('POST', config.API_ENDPOINTS.feedback(id), data),
        deleteAllData: () => apiService.fetchWithMethod('DELETE', config.API_ENDPOINTS.deleteAll),
        createDemo: () => apiService.fetchWithMethod('POST', config.API_ENDPOINTS.createDemo),
    };

    // --- 4. МОДУЛЬ УТИЛИТ ---
    const utils = {
        escapeHtml: (unsafe) => typeof unsafe !== 'string' ? unsafe : unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;"),
        formatDateTime: (dateString) => dateString ? new Date(dateString).toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'medium' }) : 'N/A',
        debounce(func, delay) { clearTimeout(state.debounceTimer); state.debounceTimer = setTimeout(func, delay); }
    };

    // --- 5. МОДУЛЬ РЕНДЕРИНГА UI ---
    const uiRenderer = {
        renderPlaceholder(message = 'Выберите тестовый запуск из списка.', isError = false) { const iconClass = isError ? "fa-solid fa-circle-exclamation" : "fa-regular fa-hand-pointer"; config.DOM.rightPanelContent.innerHTML = `<div class="placeholder-content ${isError ? 'error-state' : ''}"><i class="${iconClass} placeholder-icon" aria-hidden="true"></i><p>${utils.escapeHtml(message)}</p></div>`; },
        createTestRow(run) { const template = config.DOM.testRowTemplate.content.cloneNode(true); const row = template.querySelector('tr'); row.dataset.id = run.id; row.dataset.status = run.status; row.setAttribute('aria-label', `Детали теста: ${run.testMethod}, статус ${run.status}`); row.querySelector('[data-method-cell]').textContent = run.testMethod || 'N/A'; row.querySelector('.badge-env').textContent = run.configuration?.environment || 'N/A'; row.querySelector('.version-col').textContent = run.configuration?.appVersion || 'N/A'; row.querySelector('.time-col').textContent = utils.formatDateTime(run.timestamp); return row; },
        prependNewTestRow(run) { const newRow = this.createTestRow(run); config.DOM.testListTableBody.prepend(newRow); this.applyFilters(); },
        applyFilters() { const { status, searchQuery } = state.activeFilters; const query = searchQuery.toLowerCase().trim(); let visibleCount = 0; config.DOM.testListTableBody.querySelectorAll('.test-row').forEach(row => { const methodText = row.querySelector('[data-method-cell]').textContent.toLowerCase(); const statusText = row.dataset.status; const statusMatch = status === 'ALL' || statusText === status || (status === 'SKIPPED' && (statusText === 'SKIPPED' || statusText === 'BROKEN')); const searchMatch = methodText.includes(query); if (statusMatch && searchMatch) { row.style.display = ''; visibleCount++; } else { row.style.display = 'none'; } }); config.DOM.noResultsPlaceholder.style.display = visibleCount === 0 ? 'block' : 'none'; },
        showToast(message, type = 'info') { const iconMap = { 'success': 'fa-circle-check', 'error': 'fa-circle-xmark', 'info': 'fa-circle-info' }; const toast = document.createElement('div'); toast.className = `toast ${type}`; toast.innerHTML = `<i class="fa-solid ${iconMap[type]}"></i> <span>${utils.escapeHtml(message)}</span>`; config.DOM.toastContainer.appendChild(toast); requestAnimationFrame(() => toast.classList.add('show')); setTimeout(() => { toast.classList.remove('show'); toast.addEventListener('transitionend', () => toast.remove(), { once: true }); }, 5000); },

        renderStatistics(stats) {
            if (!config.DOM.statsContainer) return;
            const avgDurationSec = (stats.averageTestDuration / 1000).toFixed(1);
            config.DOM.statsContainer.innerHTML = `
                <div class="stats-grid">
                    <div class="stats-card"><h4>Всего запусков</h4><p>${stats.totalRuns || 0}</p></div>
                    <div class="stats-card"><h4>Pass Rate</h4><p>${(stats.passRate || 0).toFixed(1)}%</p></div>
                    <div class="stats-card status-passed"><h4>Прошли</h4><p>${stats.passedRuns || 0}</p></div>
                    <div class="stats-card status-failed"><h4>Упали</h4><p>${stats.failedRuns || 0}</p></div>
                    <div class="stats-card status-skipped"><h4>Пропущены</h4><p>${stats.skippedRuns || 0}</p></div>
                    <div class="stats-card"><h4>Уникальных тестов</h4><p>${stats.uniqueTestCount || 0}</p></div>
                    <div class="stats-card"><h4>Среднее время</h4><p>${avgDurationSec}с</p></div>
                    <div class="stats-card"><h4>Самый нестабильный</h4><p title="${stats.mostUnstableTest || ''}">${stats.mostUnstableTest || 'N/A'}</p></div>
                </div>`;
        },
        renderTestDetails(testRun) {
            let contentHtml = this.renderRunInfo(testRun);
            contentHtml += this.renderEnvironmentDetails(testRun.environmentDetails);
            if (testRun.analysisResults?.length > 0) contentHtml += this.renderAnalysisResults(testRun.analysisResults);
            if (testRun.status === 'FAILED' || testRun.status === 'BROKEN') contentHtml += this.renderFailureDetails(testRun, testRun.failedStep);
            if (testRun.executionPath?.length > 0) contentHtml += this.renderExecutionPath(testRun.executionPath, testRun.failedStep);
            if (testRun.customMetadata && Object.keys(testRun.customMetadata).length > 0) contentHtml += this.renderCustomMetadata(testRun.customMetadata);
            if (testRun.testTags?.length > 0) contentHtml += this.renderTags(testRun.testTags);
            if (testRun.artifacts) contentHtml += this.renderArtifacts(testRun.artifacts);
            config.DOM.rightPanelContent.innerHTML = contentHtml;
        },
        renderRunInfo(testRun) { const durationSeconds = testRun.durationMillis ? (testRun.durationMillis / 1000).toFixed(2) : 'N/A'; const statusClass = (testRun.status || 'unknown').toLowerCase(); return `<div class="detail-block"><h3><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Информация о запуске</h3><div class="info-grid-3col"><p><strong>ID:</strong> ${utils.escapeHtml(testRun.id)}</p><p><strong>Статус:</strong> <span class="status-badge status-${statusClass}">${utils.escapeHtml(testRun.status)}</span></p><p><strong>Класс:</strong> ${utils.escapeHtml(testRun.testClass || 'N/A')}</p><p><strong>Метод:</strong> ${utils.escapeHtml(testRun.testMethod || 'N/A')}</p><p><strong>Набор:</strong> ${utils.escapeHtml(testRun.configuration?.testSuite || 'N/A')}</p><p><strong>Версия:</strong> ${utils.escapeHtml(testRun.configuration?.appVersion || 'N/A')}</p><p><strong>Начало:</strong> ${utils.formatDateTime(testRun.startTime)}</p><p><strong>Конец:</strong> ${utils.formatDateTime(testRun.endTime)}</p><p><strong>Длительность:</strong> ${utils.escapeHtml(durationSeconds)} сек</p></div></div>`; },
        renderTags(tags) { const tagBadges = tags.map(tag => `<span class="tag-badge">${utils.escapeHtml(tag)}</span>`).join(''); return `<div class="detail-block"><h3><i class="fa-solid fa-tags" aria-hidden="true"></i> Теги</h3><div class="tags-container">${tagBadges}</div></div>`; },
        renderArtifacts(artifacts) { let links = ''; if (artifacts.screenshotUrls?.length) { links += artifacts.screenshotUrls.map(url => `<a href="${url}" target="_blank" rel="noopener noreferrer" class="artifact-link"><i class="fa-regular fa-image"></i> Скриншот</a>`).join(''); } if (artifacts.videoUrl) { links += `<a href="${artifacts.videoUrl}" target="_blank" rel="noopener noreferrer" class="artifact-link"><i class="fa-solid fa-film"></i> Видео</a>`; } if (artifacts.appLogUrls?.length) { links += artifacts.appLogUrls.map(url => `<a href="${url}" target="_blank" rel="noopener noreferrer" class="artifact-link"><i class="fa-solid fa-file-lines"></i> Лог</a>`).join(''); } if (artifacts.harFileUrl) { links += `<a href="${artifacts.harFileUrl}" target="_blank" rel="noopener noreferrer" class="artifact-link"><i class="fa-solid fa-network-wired"></i> HAR</a>`; } if (!links) return ''; return `<div class="detail-block"><h3><i class="fa-solid fa-folder-open" aria-hidden="true"></i> Артефакты</h3><div class="artifacts-container">${links}</div></div>`; },
        renderEnvironmentDetails(env) { if (!env) return ''; return `<div class="detail-block"><h3><i class="fa-solid fa-desktop" aria-hidden="true"></i> Детали окружения</h3><div class="info-grid-3col"><p><strong>Имя:</strong> ${utils.escapeHtml(env.name || 'N/A')}</p><p><strong>ОС:</strong> ${utils.escapeHtml(env.osType || '')} ${utils.escapeHtml(env.osVersion || '')}</p><p><strong>Браузер:</strong> ${utils.escapeHtml(env.browserType || '')} ${utils.escapeHtml(env.browserVersion || '')}</p><p><strong>Разрешение:</strong> ${utils.escapeHtml(env.screenResolution || 'N/A')}</p><p><strong>Тип устройства:</strong> ${utils.escapeHtml(env.deviceType || 'N/A')}</p><p><strong>Имя устройства:</strong> ${utils.escapeHtml(env.deviceName || 'N/A')}</p><p><strong>Версия драйвера:</strong> ${utils.escapeHtml(env.driverVersion || 'N/A')}</p><p><strong>URL приложения:</strong> ${utils.escapeHtml(env.appBaseUrl || 'N/A')}</p></div></div>`; },
        renderAnalysisResults(results) { const resultItems = results.map(result => { const confidence = result.aiConfidence !== undefined ? `${(result.aiConfidence * 100).toFixed(0)}%` : 'N/A'; const explanationHtml = result.explanationData ? `<details><summary>Данные для объяснения (XAI)</summary><pre>${utils.escapeHtml(JSON.stringify(result.explanationData, null, 2))}</pre></details>` : ''; return `<div class="analysis-result-item"><div class="analysis-header"><h4>${utils.escapeHtml(result.analysisType)}</h4><span class="ai-confidence">${confidence}</span></div><p><strong><i class="fa-solid fa-lightbulb"></i> Причина:</strong> ${utils.escapeHtml(result.suggestedReason || 'N/A')}</p><p><strong><i class="fa-solid fa-wrench"></i> Решение:</strong> ${utils.escapeHtml(result.solution || 'N/A')}</p>${explanationHtml}<div class="feedback-actions"><span class="feedback-prompt">Анализ корректен?</span><button class="feedback-btn" data-analysis-id="${result.id}" data-is-correct="true"><i class="fa-solid fa-thumbs-up"></i></button><button class="feedback-btn" data-analysis-id="${result.id}" data-is-correct="false"><i class="fa-solid fa-thumbs-down"></i></button></div></div>`; }).join(''); return `<div class="detail-block"><h3><i class="fa-solid fa-brain" aria-hidden="true"></i> Результаты Анализа AI</h3>${resultItems}</div>`; },
        renderFailureDetails(testRun, failedStep) { let content = ''; if (testRun.exceptionMessage) content += `<div class="failure-subsection"><p><strong>Сообщение:</strong> ${utils.escapeHtml(testRun.exceptionMessage)}</p></div>`; if (failedStep) { content += `<div class="failure-subsection"><h4>Ключевой шаг сбоя (№${failedStep.stepNumber})</h4><p><strong>Действие:</strong> ${utils.escapeHtml(failedStep.action)}</p><p><strong>Локатор:</strong> <code>${utils.escapeHtml(failedStep.locatorStrategy)} = ${utils.escapeHtml(failedStep.locatorValue)}</code></p><p class="step-error-message"><strong>Ошибка на шаге:</strong> ${utils.escapeHtml(failedStep.errorMessage)}</p></div>`; } if (testRun.stackTrace) content += `<div class="failure-subsection"><details><summary>Показать стек-трейс</summary><pre class="error-pre">${utils.escapeHtml(testRun.stackTrace)}</pre></details></div>`; return `<div class="detail-block"><h3><i class="fa-solid fa-bug" aria-hidden="true"></i> Детали сбоя</h3>${content}</div>`; },
        renderExecutionPath(path, failedStep) { const steps = path.map(step => { const stepStatus = (step.result || 'unknown').toLowerCase(); const icon = { 'success': 'fa-check', 'failure': 'fa-xmark', 'skipped': 'fa-minus' }[stepStatus] || 'fa-question'; const isFailedStep = failedStep && step.stepNumber === failedStep.stepNumber; return `<li class="step-${stepStatus} ${isFailedStep ? 'failed-step-highlight' : ''}"><div class="step-header"><i class="fa-solid ${icon}"></i><strong>Шаг ${step.stepNumber}:</strong> ${utils.escapeHtml(step.action || 'N/A')}</div><div class="step-details"><p><strong>Локатор:</strong> <code>${utils.escapeHtml(step.locatorStrategy || 'N/A')} = ${utils.escapeHtml(step.locatorValue || 'N/A')}</code></p>${step.errorMessage ? `<p class="step-error-message"><strong>Ошибка:</strong> ${utils.escapeHtml(step.errorMessage)}</p>` : ''}</div></li>`; }).join(''); return `<div class="detail-block"><h3><i class="fa-solid fa-list-ol" aria-hidden="true"></i> Путь выполнения</h3><ul class="execution-path-visualizer">${steps}</ul></div>`; },
        renderCustomMetadata(metadata) { const content = Object.entries(metadata).map(([key, value]) => { const label = config.METADATA_LABELS[key] || utils.escapeHtml(key); const isUrl = value && (value.startsWith('http://') || value.startsWith('https://')); return `<p><strong>${label}:</strong> ${isUrl ? `<a href="${utils.escapeHtml(value)}" target="_blank" rel="noopener noreferrer">Ссылка</a>` : utils.escapeHtml(value)}</p>`; }).join(''); return `<div class="detail-block"><h3><i class="fa-solid fa-cogs"></i> Дополнительные данные</h3><div class="info-grid-3col">${content}</div></div>`; },
    };

    // --- 6. МОДУЛЬ УПРАВЛЕНИЯ ГРАФИКАМИ ---
    const chartManager = {
        chartColors: ['#3b82f6', '#ef4444', '#f97316', '#16a34a', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981'],
        getChartColors(count) { const colors = []; for(let i=0; i<count; i++) { colors.push(this.chartColors[i % this.chartColors.length]); } return colors; },
        renderChart(id, ctx, type, data, options = {}) { if (!ctx) return; if (state.charts[id]) { state.charts[id].data = data; state.charts[id].options = { ...state.charts[id].options, ...options }; state.charts[id].update(); } else { state.charts[id] = new Chart(ctx, { type, data, options: { responsive: true, maintainAspectRatio: false, ...options } }); } },
        renderPassRateChart(trendData) { if(!trendData || trendData.length === 0) return; const sortedData = [...trendData].sort((a, b) => new Date(a.date) - new Date(b.date)); this.renderChart('passRate', config.DOM.passRateChartCtx, 'line', { labels: sortedData.map(d => d.date), datasets: [{ label: 'Pass Rate (%)', data: sortedData.map(d => d.passRate), borderColor: '#16a34a', backgroundColor: 'rgba(22, 163, 74, 0.1)', fill: true, tension: 0.3 }] }, { plugins: { legend: { display: false } } }); },
        renderTopFailingTestsChart(failingTests) { if(!failingTests || Object.keys(failingTests).length === 0) return; const labels = Object.keys(failingTests); const data = Object.values(failingTests); this.renderChart('failingTests', config.DOM.failingTestsChartCtx, 'bar', { labels: labels.map(l => l.split('.').pop()), datasets: [{ label: 'Кол-во падений', data, backgroundColor: this.getChartColors(data.length) }] }, { indexAxis: 'y', plugins: { legend: { display: false } } }); },
        renderExceptionTypesChart(exceptions) { if(!exceptions || Object.keys(exceptions).length === 0) return; const labels = Object.keys(exceptions); const data = Object.values(exceptions); this.renderChart('exceptions', config.DOM.exceptionTypesChartCtx, 'bar', { labels: labels.map(l => l.split('.').pop()), datasets: [{ label: 'Кол-во', data, backgroundColor: this.getChartColors(data.length) }] }, { plugins: { legend: { display: false } } }); },
        renderPieChart(id, ctx, chartData) { if(!chartData || Object.keys(chartData).length === 0) return; const labels = Object.keys(chartData); const data = Object.values(chartData); this.renderChart(id, ctx, 'pie', { labels, datasets: [{ data, backgroundColor: this.getChartColors(data.length) }] }, { plugins: { legend: { position: 'right' } } }); }
    };

    // --- 7. МОДУЛЬ WEBSOCKET ---
    const webSocketHandler = { connect() { try { const socket = new SockJS('/ws'); state.stompClient = Stomp.over(socket); state.stompClient.debug = null; state.stompClient.connect({}, () => { uiRenderer.showToast('Подключено к серверу для обновлений.', 'success'); state.stompClient.subscribe('/topic/new-test-run', message => { const newTestRun = JSON.parse(message.body); state.testDetailsCache.set(newTestRun.id, newTestRun); uiRenderer.prependNewTestRow(newTestRun); actions.loadStatistics(); uiRenderer.showToast(`Получен новый запуск: ${newTestRun.testMethod}`, 'info'); }); }, () => { uiRenderer.showToast('Соединение потеряно. Переподключение...', 'error'); setTimeout(() => this.connect(), 5000); }); } catch (e) { uiRenderer.showToast('Не удалось подключиться к WebSocket.', 'error'); } } };

    // --- 8. МОДУЛЬ ДЕЙСТВИЙ (CONTROLLER) ---
    const actions = {
        async fetchAndRenderTestRuns(page, initialLoad = false) { if (state.isLoading || state.isLastPage) return; state.isLoading = true; config.DOM.listSpinner.style.display = 'block'; try { const pageData = await apiService.getPaginatedTests(page, config.PAGE_SIZE); const fragment = document.createDocumentFragment(); pageData.content.forEach(run => { state.testDetailsCache.set(run.id, run); fragment.appendChild(uiRenderer.createTestRow(run)); }); config.DOM.testListTableBody.appendChild(fragment); state.isLastPage = pageData.last; state.currentPage = pageData.number; if (initialLoad && pageData.content.length > 0) { const firstRow = config.DOM.testListTableBody.querySelector('.test-row'); if (firstRow) this.loadTestDetails(firstRow.dataset.id, firstRow); } uiRenderer.applyFilters(); } catch (error) { uiRenderer.showToast('Не удалось загрузить список тестов.', 'error'); } finally { state.isLoading = false; config.DOM.listSpinner.style.display = 'none'; } },
        async loadStatistics() { try { const stats = await apiService.getStatistics(); uiRenderer.renderStatistics(stats); chartManager.renderPassRateChart(stats.dailyPassRateTrend); chartManager.renderTopFailingTestsChart(stats.topFailingTests); chartManager.renderExceptionTypesChart(stats.topExceptionTypes); chartManager.renderPieChart('runsBySuite', config.DOM.runsBySuiteChartCtx, stats.runsBySuite); chartManager.renderPieChart('runsByEnv', config.DOM.runsByEnvChartCtx, stats.runsByEnvironment); } catch (error) { console.error("Failed to load statistics:", error); uiRenderer.showToast('Не удалось загрузить статистику.', 'error'); } },
        async loadTestDetails(testRunId, selectedRow = null) { document.querySelectorAll('.test-row.selected').forEach(row => row.classList.remove('selected')); if (selectedRow) selectedRow.classList.add('selected'); document.body.classList.add('show-details-on-mobile'); uiRenderer.renderPlaceholder('Загрузка деталей...'); if (state.testDetailsCache.has(testRunId)) { uiRenderer.renderTestDetails(state.testDetailsCache.get(testRunId)); return; } try { const testRun = await apiService.getTestDetails(testRunId); state.testDetailsCache.set(testRunId, testRun); uiRenderer.renderTestDetails(testRun); } catch (error) { uiRenderer.renderPlaceholder('Ошибка при загрузке деталей.', true); uiRenderer.showToast('Не удалось загрузить детали.', 'error'); } },
        async submitAiFeedback(analysisId, isCorrect, button) { const feedbackContainer = button.closest('.feedback-actions'); if (feedbackContainer.classList.contains('voted')) return; try { await apiService.submitFeedback(analysisId, { isAiSuggestionCorrect: isCorrect }); uiRenderer.showToast('Спасибо за ваш отзыв!', 'success'); feedbackContainer.classList.add('voted'); button.classList.add(isCorrect ? 'selected-true' : 'selected-false'); } catch (error) { uiRenderer.showToast('Не удалось отправить отзыв.', 'error'); } },
        async deleteAllData() { if (confirm('Вы уверены, что хотите удалить все данные? Это действие необратимо.')) { try { await apiService.deleteAllData(); uiRenderer.showToast('Все данные удалены. Страница будет перезагружена.', 'success'); setTimeout(() => window.location.reload(), 2000); } catch (error) { uiRenderer.showToast('Ошибка при удалении данных.', 'error'); } } },
        async createDemo() { config.DOM.createDemoBtn.disabled = true; config.DOM.createDemoBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Создание...'; try { await apiService.createDemo(); uiRenderer.showToast('Запрос на создание демо-записи отправлен.', 'info'); } catch (error) { uiRenderer.showToast('Ошибка при создании демо-записи.', 'error'); } finally { setTimeout(() => { config.DOM.createDemoBtn.disabled = false; config.DOM.createDemoBtn.innerHTML = '<i class="fa-solid fa-plus"></i> Создать демо'; }, 1000); } }
    };

    // --- 9. МОДУЛЬ ОБРАБОТЧИКОВ СОБЫТИЙ ---
    const eventHandlers = {
        setupEventListeners() {
            config.DOM.listWrapper?.addEventListener('scroll', () => { if (config.DOM.listWrapper.scrollTop + config.DOM.listWrapper.clientHeight >= config.DOM.listWrapper.scrollHeight - 50) { actions.fetchAndRenderTestRuns(state.currentPage + 1); } });
            config.DOM.testListTableBody?.addEventListener('click', (event) => { const row = event.target.closest('.test-row'); if (row) actions.loadTestDetails(row.dataset.id, row); });
            config.DOM.rightPanel?.addEventListener('click', e => { const feedbackBtn = e.target.closest('.feedback-btn'); if (feedbackBtn) { const { analysisId, isCorrect } = feedbackBtn.dataset; actions.submitAiFeedback(analysisId, isCorrect === 'true', feedbackBtn); } });
            config.DOM.tabButtons.forEach(button => button.addEventListener('click', (e) => this.handleTabSwitch(e.currentTarget)));
            config.DOM.deleteAllDataBtn?.addEventListener('click', actions.deleteAllData);
            config.DOM.createDemoBtn?.addEventListener('click', actions.createDemo);
            config.DOM.themeToggle?.addEventListener('click', this.handleThemeToggle);
            config.DOM.searchInput?.addEventListener('input', e => utils.debounce(() => this.handleSearch(e), 300));
            config.DOM.statusFilters?.addEventListener('click', this.handleStatusFilter);
        },
        handleThemeToggle() { const isDark = config.DOM.html.classList.toggle('dark-theme'); localStorage.setItem('theme', isDark ? 'dark' : 'light'); },
        handleSearch(event) { state.activeFilters.searchQuery = event.target.value; uiRenderer.applyFilters(); },
        handleStatusFilter(event) { const button = event.target.closest('.filter-btn'); if (!button || button.classList.contains('active')) return; config.DOM.statusFilters.querySelector('.active').classList.remove('active'); button.classList.add('active'); state.activeFilters.status = button.dataset.status; uiRenderer.applyFilters(); },
        handleTabSwitch(clickedButton) { const targetTabId = clickedButton.dataset.tab; localStorage.setItem('activeTabId', targetTabId); config.DOM.tabButtons.forEach(btn => btn.classList.remove('active')); config.DOM.tabContents.forEach(content => content.classList.remove('active')); clickedButton.classList.add('active'); const targetContent = document.getElementById(`${targetTabId}TabContent`); if (targetContent) targetContent.classList.add('active'); if (targetTabId === 'widgets') { actions.loadStatistics(); } requestAnimationFrame(() => { Object.values(state.charts).forEach(chart => { if(chart) chart.resize(); }); }); },
        restoreActiveTab() { const savedTabId = localStorage.getItem('activeTabId') || 'overview'; const buttonToActivate = document.querySelector(`.tab-button[data-tab="${savedTabId}"]`); if (buttonToActivate) this.handleTabSwitch(buttonToActivate); }
    };

    // --- 10. ТОЧКА ВХОДА ПРИЛОЖЕНИЯ ---
    const init = () => { const savedTheme = localStorage.getItem('theme'); const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches; if (savedTheme === 'dark' || (!savedTheme && prefersDark)) { config.DOM.html.classList.add('dark-theme'); } eventHandlers.restoreActiveTab(); eventHandlers.setupEventListeners(); webSocketHandler.connect(); actions.fetchAndRenderTestRuns(0, true); };
    document.addEventListener('DOMContentLoaded', init);

})(window, document, window.Stomp, window.Chart);
