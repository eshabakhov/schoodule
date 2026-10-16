<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"
                encoding="UTF-8"
                omit-xml-declaration="yes"
                doctype-system="about:legacy-compat"/>

    <xsl:param name="view"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$view = 'list-fragment'">
                <xsl:call-template name="curriculums-results"/>
            </xsl:when>
            <xsl:when test="$view = 'requirements-fragment'">
                <xsl:call-template name="requirements-results"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="page"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="page-title">
        <xsl:choose>
            <xsl:when test="$view = 'create'">
                <xsl:text>Новый федеральный учебный план</xsl:text>
            </xsl:when>
            <xsl:when test="$view = 'edit'">
                <xsl:text>Редактировать федеральный учебный план</xsl:text>
            </xsl:when>
            <xsl:when test="$view = 'details'">
                <xsl:value-of select="/schoodule/curriculums/curriculum/title"/>
            </xsl:when>
            <xsl:when test="$view = 'requirements'">
                <xsl:text>Требования: </xsl:text>
                <xsl:value-of select="/schoodule/curriculums/curriculum/title"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>Федеральные учебные планы</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="page">
        <html lang="ru">
            <head>
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0"/>
                <xsl:if test="/schoodule/csrf/token">
                    <meta name="_csrf">
                        <xsl:attribute name="content">
                            <xsl:value-of select="/schoodule/csrf/token"/>
                        </xsl:attribute>
                    </meta>
                    <meta name="_csrf_header">
                        <xsl:attribute name="content">
                            <xsl:value-of select="/schoodule/csrf/header"/>
                        </xsl:attribute>
                    </meta>
                </xsl:if>
                <meta charset="UTF-8"/>
                <meta name="description"
                      content="Schoodule - система управления школьными расписаниями"/>
                <title><xsl:call-template name="page-title"/></title>
                <link type="image/png" rel="icon" href="/favicon.svg"/>
                <link rel="stylesheet" href="/css/main.css"/>
                <xsl:if test="$view = 'details' or $view = 'requirements'">
                    <link rel="stylesheet"
                          href="/css/federal-curriculum-details.css"/>
                </xsl:if>
            </head>
            <body>
                <xsl:call-template name="header"/>
                <main>
                    <xsl:call-template name="sidebar"/>
                    <div class="app-content" id="app-content">
                        <div class="page-body">
                            <xsl:choose>
                                <xsl:when test="$view = 'create'">
                                    <xsl:call-template name="create"/>
                                </xsl:when>
                                <xsl:when test="$view = 'edit'">
                                    <xsl:call-template name="edit"/>
                                </xsl:when>
                                <xsl:when test="$view = 'details'">
                                    <xsl:call-template name="details"/>
                                </xsl:when>
                                <xsl:when test="$view = 'requirements'">
                                    <xsl:call-template name="requirements"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="curriculums"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </div>
                </main>
                <footer class="app-footer">
                    <div class="app-footer-inner">
                        <span class="app-footer-logo">Schoodule</span>
                        <span class="app-footer-copy">© 2026</span>
                    </div>
                </footer>
                <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
                <script type="module" src="/js/security.js"></script>
                <xsl:if test="$view = 'list' or $view = 'details' or $view = 'requirements'">
                    <script type="module"
                            src="/js/federal-curriculums.js"></script>
                </xsl:if>
                <xsl:if test="$view = 'requirements'">
                    <script type="module"
                            src="/js/federal-curriculum-details.js"></script>
                </xsl:if>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="header">
        <header class="app-header" id="app-header">
            <div class="app-header-inner">
                <a class="app-header-logo"
                   href="/"
                   title="Перейти на главную страницу">
                    <xsl:call-template name="calendar-icon"/>
                    <xsl:text>Schoodule</xsl:text>
                    <span class="app-header-logo-version">v0.0.1</span>
                </a>
                <h1 class="app-header-title">
                    <xsl:call-template name="page-title"/>
                </h1>
                <xsl:if test="/schoodule/user/subscription = 'true'">
                    <a class="logout-btn"
                       href="/users/subscription"
                       title="Подписка">
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
                             viewBox="0 0 24 24">
                            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
                        </svg>
                        <xsl:text>Подписка</xsl:text>
                    </a>
                </xsl:if>
                <a class="logout-btn" href="/users/profile" title="Профиль">
                    <svg xmlns="http://www.w3.org/2000/svg"
                         fill="none"
                         stroke="currentColor"
                         stroke-width="2"
                         viewBox="0 0 24 24">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                    </svg>
                    <span><xsl:value-of select="/schoodule/user/username"/></span>
                </a>
            </div>
        </header>
    </xsl:template>

    <xsl:template name="sidebar">
        <aside class="sidebar" id="sidebar">
            <nav class="sidebar-nav">
                <xsl:if test="/schoodule/user/admin = 'true'">
                    <div class="sidebar-section">Общее</div>
                    <a class="sidebar-link"
                       title="Школы">
                        <xsl:attribute name="href">
                            <xsl:value-of select="/schoodule/schools/href"/>
                        </xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
                             viewBox="0 0 24 24">
                            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                            <polyline points="9 22 9 12 15 12 15 22"/>
                        </svg>
                        <span class="sidebar-link-label">Школы</span>
                    </a>
                    <a class="sidebar-link"
                       href="/federal/curriculums"
                       title="Федеральные планы">
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
                             viewBox="0 0 24 24">
                            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                            <path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5z"/>
                            <path d="M8 7h8"/>
                            <path d="M8 11h8"/>
                            <path d="M8 15h5"/>
                        </svg>
                        <span class="sidebar-link-label">Федеральные планы</span>
                    </a>
                </xsl:if>
            </nav>
        </aside>
    </xsl:template>

    <xsl:template name="curriculums">
        <div class="page-title-row">
            <div>
                <h1>Федеральные учебные планы</h1>
                <p class="page-subtitle">Справочник нормативных учебных планов для школ</p>
            </div>
            <a class="btn-primary"
               href="/federal/curriculums/create"
               title="Добавить федеральный учебный план в систему"
               style="display:inline-flex;align-items:center;gap:.5rem;padding:.5rem 1rem;border-radius:var(--radius-sm);font-size:.875rem;font-weight:500;text-decoration:none;">
                <svg xmlns="http://www.w3.org/2000/svg"
                     fill="none"
                     stroke="currentColor"
                     stroke-width="2"
                     style="width:15px;height:15px;"
                     viewBox="0 0 24 24">
                    <line x1="12" x2="12" y1="5" y2="19"/>
                    <line x1="5" x2="19" y1="12" y2="12"/>
                </svg>
                <xsl:text>Добавить план</xsl:text>
            </a>
        </div>
        <div class="search-wrap"
             style="width:100%;max-width:360px;margin-bottom:1.25rem;">
            <svg xmlns="http://www.w3.org/2000/svg"
                 fill="none"
                 stroke="currentColor"
                 stroke-width="2"
                 viewBox="0 0 24 24">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" x2="16.65" y1="21" y2="16.65"/>
            </svg>
            <label for="federal-curriculum-search">
                <input id="federal-curriculum-search"
                       type="text"
                       placeholder="Поиск плана...">
                    <xsl:attribute name="value">
                        <xsl:value-of select="/schoodule/curriculums/filters/item[name = 'title']/value"/>
                    </xsl:attribute>
                </input>
            </label>
        </div>
        <xsl:call-template name="curriculums-results"/>
    </xsl:template>

    <xsl:template name="curriculums-results">
        <div id="federal-curriculums-results">
            <div class="card-grid" id="federal-curriculums-container">
                <xsl:if test="not(/schoodule/curriculums/curriculum)">
                    <div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--muted-fg);">
                        <xsl:text>Федеральные учебные планы не найдены.</xsl:text>
                    </div>
                </xsl:if>
                <xsl:for-each select="/schoodule/curriculums/curriculum">
                    <div class="card-item federal-curriculum-row"
                         style="cursor:pointer;">
                        <xsl:attribute name="data-id">
                            <xsl:value-of select="id"/>
                        </xsl:attribute>
                        <div class="card-item-header">
                            <div class="card-item-title-row">
                                <div class="card-item-icon">
                                    <svg xmlns="http://www.w3.org/2000/svg"
                                         fill="none"
                                         stroke="currentColor"
                                         stroke-width="2"
                                         viewBox="0 0 24 24">
                                        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                                        <path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5z"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="card-item-period">
                            <span class="card-item-name">
                                <xsl:value-of select="title"/>
                            </span>
                            <div class="card-item-period-label">
                                <span><xsl:value-of select="level"/></span>
                                <xsl:text> · </xsl:text>
                                <span><xsl:value-of select="week"/></span>
                                <xsl:text> · </xsl:text>
                                <span><xsl:value-of select="year"/></span>
                            </div>
                        </div>
                    </div>
                </xsl:for-each>
            </div>
            <xsl:call-template name="pagination">
                <xsl:with-param name="page"
                                select="/schoodule/curriculums/page"/>
                <xsl:with-param name="prefix"
                                select="'federal-curriculums'"/>
                <xsl:with-param name="ellipsis" select="'…'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template name="details">
        <xsl:variable name="curriculum"
                      select="/schoodule/curriculums/curriculum"/>
        <nav class="breadcrumbs" aria-label="Навигационная цепочка">
            <a class="breadcrumb-link"
               href="/federal/curriculums"
               title="Перейти к федеральным учебным планам">
                <xsl:text>Федеральные учебные планы</xsl:text>
            </a>
            <span class="breadcrumb-separator">-&gt;</span>
            <span class="breadcrumb-current">
                <xsl:value-of select="$curriculum/title"/>
            </span>
        </nav>
        <div class="page-title-row" style="margin-bottom:1.5rem;">
            <div>
                <h1 class="title-with-action">
                    <span><xsl:value-of select="$curriculum/title"/></span>
                    <a class="btn-icon-edit"
                       title="Редактировать"
                       aria-label="Редактировать">
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id, '/edit')"/>
                        </xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             height="15"
                             stroke="currentColor"
                             stroke-width="2"
                             viewBox="0 0 24 24"
                             width="15">
                            <path d="M12 20h9"/>
                            <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>
                        </svg>
                    </a>
                </h1>
                <p class="page-subtitle">Информация о федеральном учебном плане</p>
            </div>
        </div>
        <div class="school-info">
            <xsl:call-template name="info-row">
                <xsl:with-param name="label" select="'ID:'"/>
                <xsl:with-param name="value" select="$curriculum/id"/>
            </xsl:call-template>
            <xsl:call-template name="info-row">
                <xsl:with-param name="label" select="'Уровень:'"/>
                <xsl:with-param name="value" select="$curriculum/level"/>
            </xsl:call-template>
            <xsl:call-template name="info-row">
                <xsl:with-param name="label" select="'Неделя:'"/>
                <xsl:with-param name="value" select="$curriculum/week"/>
            </xsl:call-template>
            <xsl:call-template name="info-row">
                <xsl:with-param name="label" select="'Версия:'"/>
                <xsl:with-param name="value" select="$curriculum/version"/>
            </xsl:call-template>
            <xsl:call-template name="info-row">
                <xsl:with-param name="label" select="'Учебный год:'"/>
                <xsl:with-param name="value" select="$curriculum/year"/>
            </xsl:call-template>
            <div class="info-row">
                <span class="info-label">Описание:</span>
                <span class="info-value">
                    <xsl:choose>
                        <xsl:when test="normalize-space($curriculum/description)">
                            <xsl:value-of select="$curriculum/description"/>
                        </xsl:when>
                        <xsl:otherwise>—</xsl:otherwise>
                    </xsl:choose>
                </span>
            </div>
            <div class="info-row">
                <span class="info-label">Требования:</span>
                <span class="info-value">
                    <a class="req-info-link"
                       title="Перейти к требованиям учебного плана">
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id, '/requirements')"/>
                        </xsl:attribute>
                        <xsl:text>Перейти к требованиям учебного плана</xsl:text>
                    </a>
                </span>
            </div>
        </div>
        <div class="details-footer-actions">
            <button class="btn-outline-action btn-delete-curriculum"
                    type="submit"
                    style="color:var(--destructive);border-color:#fecaca;background:#fef2f2;">
                <xsl:attribute name="data-id">
                    <xsl:value-of select="$curriculum/id"/>
                </xsl:attribute>
                <xsl:attribute name="data-name">
                    <xsl:value-of select="$curriculum/title"/>
                </xsl:attribute>
                <xsl:text>Удалить план</xsl:text>
            </button>
        </div>
    </xsl:template>

    <xsl:template name="info-row">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <div class="info-row">
            <span class="info-label"><xsl:value-of select="$label"/></span>
            <span class="info-value"><xsl:value-of select="$value"/></span>
        </div>
    </xsl:template>

    <xsl:template name="create">
        <xsl:call-template name="curriculum-form">
            <xsl:with-param name="editing" select="false()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="edit">
        <xsl:call-template name="curriculum-form">
            <xsl:with-param name="editing" select="true()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="curriculum-form">
        <xsl:param name="editing"/>
        <xsl:variable name="curriculum"
                      select="/schoodule/curriculums/curriculum"/>
        <div class="page-title-row" style="justify-content:center;">
            <div>
                <h1 style="margin:0;">
                    <xsl:choose>
                        <xsl:when test="$editing">
                            <xsl:text>Редактировать федеральный учебный план</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>Новый федеральный учебный план</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </h1>
                <xsl:if test="not($editing)">
                    <p class="page-subtitle">Заполните метаданные плана</p>
                </xsl:if>
            </div>
        </div>
        <div style="max-width:640px;margin:0 auto;">
            <form method="post"
                  style="display:flex;flex-direction:column;gap:1rem;">
                <xsl:attribute name="action">
                    <xsl:choose>
                        <xsl:when test="$editing">
                            <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id, '/edit')"/>
                        </xsl:when>
                        <xsl:otherwise>/federal/curriculums/create</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:call-template name="csrf-input"/>
                <label for="title-name"
                       style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    <xsl:text>Название</xsl:text>
                    <input id="title-name"
                           name="title"
                           type="text"
                           autofocus="autofocus"
                           required="required">
                        <xsl:if test="$editing">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$curriculum/title"/>
                            </xsl:attribute>
                        </xsl:if>
                    </input>
                </label>
                <label style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    <xsl:text>Уровень образования</xsl:text>
                    <select name="level" required="required">
                        <xsl:call-template name="option">
                            <xsl:with-param name="value" select="'PRIMARY'"/>
                            <xsl:with-param name="selected"
                                            select="$editing and $curriculum/level = 'PRIMARY'"/>
                        </xsl:call-template>
                        <xsl:call-template name="option">
                            <xsl:with-param name="value" select="'BASIC'"/>
                            <xsl:with-param name="selected"
                                            select="$editing and $curriculum/level = 'BASIC'"/>
                        </xsl:call-template>
                        <xsl:call-template name="option">
                            <xsl:with-param name="value" select="'SECONDARY'"/>
                            <xsl:with-param name="selected"
                                            select="$editing and $curriculum/level = 'SECONDARY'"/>
                        </xsl:call-template>
                    </select>
                </label>
                <label style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    <xsl:text>Учебная неделя</xsl:text>
                    <select name="week" required="required">
                        <xsl:call-template name="option">
                            <xsl:with-param name="value" select="'FIVE_DAYS'"/>
                            <xsl:with-param name="selected"
                                            select="$editing and $curriculum/week = 'FIVE_DAYS'"/>
                        </xsl:call-template>
                        <xsl:call-template name="option">
                            <xsl:with-param name="value" select="'SIX_DAYS'"/>
                            <xsl:with-param name="selected"
                                            select="$editing and $curriculum/week = 'SIX_DAYS'"/>
                        </xsl:call-template>
                    </select>
                </label>
                <label for="version-name"
                       style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    <xsl:text>Версия</xsl:text>
                    <input id="version-name"
                           name="version"
                           type="text"
                           required="required">
                        <xsl:if test="$editing">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$curriculum/version"/>
                            </xsl:attribute>
                        </xsl:if>
                    </input>
                </label>
                <label for="year-name"
                       style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    <xsl:text>Учебный год</xsl:text>
                    <input id="year-name"
                           name="year"
                           type="text"
                           required="required">
                        <xsl:choose>
                            <xsl:when test="$editing">
                                <xsl:attribute name="value">
                                    <xsl:value-of select="$curriculum/year"/>
                                </xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="placeholder">2026/2027</xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </input>
                </label>
                <label style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    <xsl:text>Описание</xsl:text>
                    <textarea name="description" rows="4"><xsl:if test="$editing"><xsl:value-of select="$curriculum/description"/></xsl:if></textarea>
                </label>
                <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.25rem;">
                    <a class="btn-outline-action"
                       title="Отменить изменение федерального учебного плана">
                        <xsl:attribute name="href">
                            <xsl:choose>
                                <xsl:when test="$editing">
                                    <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id)"/>
                                </xsl:when>
                                <xsl:otherwise>/federal/curriculums</xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:text>Отмена</xsl:text>
                    </a>
                    <button class="btn-primary" type="submit">
                        <xsl:choose>
                            <xsl:when test="$editing">Сохранить</xsl:when>
                            <xsl:otherwise>Создать</xsl:otherwise>
                        </xsl:choose>
                    </button>
                </div>
            </form>
        </div>
    </xsl:template>

    <xsl:template name="requirements">
        <xsl:variable name="curriculum"
                      select="/schoodule/curriculums/curriculum"/>
        <nav class="breadcrumbs" aria-label="Навигационная цепочка">
            <a class="breadcrumb-link"
               href="/federal/curriculums"
               title="Перейти к федеральным учебным планам">
                <xsl:text>Федеральные учебные планы</xsl:text>
            </a>
            <span class="breadcrumb-separator">-&gt;</span>
            <a class="breadcrumb-link"
               title="Перейти к описанию учебного плана">
                <xsl:attribute name="href">
                    <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id)"/>
                </xsl:attribute>
                <xsl:value-of select="$curriculum/title"/>
            </a>
            <span class="breadcrumb-separator">-&gt;</span>
            <span class="breadcrumb-current">Требования</span>
        </nav>
        <div class="page-title-row" style="margin-bottom:1.5rem;">
            <div>
                <h1 class="title-with-action">
                    <span>Требования учебного плана</span>
                </h1>
                <p class="page-subtitle">
                    <xsl:value-of select="$curriculum/title"/>
                </p>
            </div>
        </div>
        <form class="req-add-panel" id="req-add-panel" method="post">
            <xsl:attribute name="action">
                <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id, '/requirements/create')"/>
            </xsl:attribute>
            <xsl:call-template name="csrf-input"/>
            <fieldset class="req-form-group req-add-group">
                <legend>Добавление требования</legend>
                <div class="req-form-grid req-form-grid-add">
                    <label class="req-field req-field-grade"
                           for="grade-number">
                        <input id="grade-number"
                               name="grade"
                               type="number"
                               max="11"
                               min="1"
                               placeholder="Класс"
                               required="required"/>
                    </label>
                    <label class="req-field" for="subject-name">
                        <input id="subject-name"
                               name="subjectName"
                               type="text"
                               placeholder="Предмет"
                               required="required"/>
                    </label>
                    <label class="req-field req-field-hours"
                           for="weekly-hours">
                        <input id="weekly-hours"
                               name="weeklyHours"
                               type="number"
                               min="1"
                               placeholder="Часов/нед."
                               required="required"/>
                    </label>
                    <select name="partType" required="required">
                        <option value="MANDATORY">MANDATORY</option>
                        <option value="OPTIONAL">OPTIONAL</option>
                    </select>
                    <button class="btn-primary" type="submit">Сохранить</button>
                </div>
            </fieldset>
        </form>
        <fieldset class="req-form-group req-search-panel">
            <legend>Поиск требований</legend>
            <div class="req-form-grid req-form-grid-search">
                <label class="req-field" for="filter-grade">
                    <input id="filter-grade"
                           type="number"
                           max="11"
                           min="1"
                           placeholder="Класс"/>
                </label>
                <label class="req-field" for="filter-subject">
                    <input id="filter-subject"
                           type="text"
                           placeholder="Поиск по предмету..."/>
                </label>
                <select id="filter-part">
                    <option value="">Все части</option>
                    <option value="MANDATORY">MANDATORY</option>
                    <option value="OPTIONAL">OPTIONAL</option>
                </select>
            </div>
        </fieldset>
        <xsl:call-template name="requirements-results"/>
    </xsl:template>

    <xsl:template name="requirements-results">
        <xsl:variable name="curriculum"
                      select="/schoodule/curriculums/curriculum"/>
        <xsl:variable name="requirements"
                      select="$curriculum/requirements"/>
        <div id="req-results">
            <table class="data-table">
                <thead>
                    <tr>
                        <xsl:call-template name="sort-heading">
                            <xsl:with-param name="field" select="'grade'"/>
                            <xsl:with-param name="label" select="'Класс'"/>
                            <xsl:with-param name="title"
                                            select="'Сортировать по классу'"/>
                            <xsl:with-param name="requirements"
                                            select="$requirements"/>
                        </xsl:call-template>
                        <xsl:call-template name="sort-heading">
                            <xsl:with-param name="field" select="'subject'"/>
                            <xsl:with-param name="label" select="'Предмет'"/>
                            <xsl:with-param name="title"
                                            select="'Сортировать по предмету'"/>
                            <xsl:with-param name="requirements"
                                            select="$requirements"/>
                        </xsl:call-template>
                        <xsl:call-template name="sort-heading">
                            <xsl:with-param name="field" select="'hours'"/>
                            <xsl:with-param name="label" select="'Часов/нед.'"/>
                            <xsl:with-param name="title"
                                            select="'Сортировать по часам в неделю'"/>
                            <xsl:with-param name="requirements"
                                            select="$requirements"/>
                        </xsl:call-template>
                        <xsl:call-template name="sort-heading">
                            <xsl:with-param name="field" select="'part'"/>
                            <xsl:with-param name="label" select="'Часть'"/>
                            <xsl:with-param name="title"
                                            select="'Сортировать по части'"/>
                            <xsl:with-param name="requirements"
                                            select="$requirements"/>
                        </xsl:call-template>
                        <th style="text-align:right;padding-right:1rem;">Действия</th>
                    </tr>
                </thead>
                <tbody id="req-tbody">
                    <xsl:attribute name="data-search-url">
                        <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id, '/requirements/fragment')"/>
                    </xsl:attribute>
                    <xsl:if test="not($requirements/requirement)">
                        <tr id="req-empty-row">
                            <td class="empty-state" colspan="5">
                                <xsl:text>Требования пока не добавлены.</xsl:text>
                            </td>
                        </tr>
                    </xsl:if>
                    <xsl:for-each select="$requirements/requirement">
                        <tr class="req-data-row">
                            <xsl:attribute name="data-grade"><xsl:value-of select="grade"/></xsl:attribute>
                            <xsl:attribute name="data-part"><xsl:value-of select="partType"/></xsl:attribute>
                            <xsl:attribute name="data-subject"><xsl:value-of select="subjectName"/></xsl:attribute>
                            <td class="view-grade"><xsl:value-of select="grade"/></td>
                            <td class="view-subject"><xsl:value-of select="subjectName"/></td>
                            <td class="view-hours"><xsl:value-of select="weeklyHours"/></td>
                            <td class="view-part"><xsl:value-of select="partType"/></td>
                            <td>
                                <div class="td-actions">
                                    <button class="td-icon-btn req-row-edit"
                                            type="button"
                                            title="Редактировать">
                                        <xsl:call-template name="edit-icon"/>
                                    </button>
                                    <button class="td-icon-btn btn-icon-delete req-row-delete btn-delete-requirement"
                                            type="button"
                                            title="Удалить">
                                        <xsl:attribute name="data-curriculum"><xsl:value-of select="$curriculum/id"/></xsl:attribute>
                                        <xsl:attribute name="data-id"><xsl:value-of select="id"/></xsl:attribute>
                                        <xsl:attribute name="data-name"><xsl:value-of select="subjectName"/></xsl:attribute>
                                        <xsl:call-template name="cross-icon"/>
                                    </button>
                                    <button class="td-icon-btn btn-icon-save req-row-save"
                                            type="button"
                                            title="Сохранить">
                                        <svg xmlns="http://www.w3.org/2000/svg"
                                             fill="none"
                                             stroke="currentColor"
                                             stroke-width="2.5"
                                             viewBox="0 0 24 24">
                                            <polyline points="20 6 9 17 4 12"/>
                                        </svg>
                                    </button>
                                    <button class="td-icon-btn req-row-cancel"
                                            type="button"
                                            title="Отмена">
                                        <xsl:call-template name="cross-icon"/>
                                    </button>
                                    <form class="req-edit-form"
                                          method="post"
                                          style="display:none;">
                                        <xsl:attribute name="action">
                                            <xsl:value-of select="concat('/federal/curriculums/', $curriculum/id, '/requirements/', id, '/edit')"/>
                                        </xsl:attribute>
                                        <xsl:call-template name="csrf-input"/>
                                        <input class="edit-grade"
                                               name="grade"
                                               type="hidden"/>
                                        <input class="edit-subject"
                                               name="subjectName"
                                               type="hidden"/>
                                        <input class="edit-hours"
                                               name="weeklyHours"
                                               type="hidden"/>
                                        <input class="edit-part"
                                               name="partType"
                                               type="hidden"/>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
            <xsl:call-template name="pagination">
                <xsl:with-param name="page" select="$requirements/page"/>
                <xsl:with-param name="prefix" select="'req'"/>
                <xsl:with-param name="ellipsis" select="'...'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template name="sort-heading">
        <xsl:param name="field"/>
        <xsl:param name="label"/>
        <xsl:param name="title"/>
        <xsl:param name="requirements"/>
        <xsl:variable name="direction"
                      select="$requirements/sorts/item[name = $field]/direction"/>
        <th>
            <button type="button">
                <xsl:attribute name="class">
                    <xsl:text>req-sort-btn</xsl:text>
                    <xsl:if test="$direction = 'ASC' or $direction = 'DESC'">
                        <xsl:text> active</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
                <xsl:attribute name="data-sort"><xsl:value-of select="$field"/></xsl:attribute>
                <span><xsl:value-of select="$label"/></span>
                <span class="req-sort-icon">
                    <xsl:choose>
                        <xsl:when test="$direction = 'ASC'">↑</xsl:when>
                        <xsl:when test="$direction = 'DESC'">↓</xsl:when>
                        <xsl:otherwise>↕</xsl:otherwise>
                    </xsl:choose>
                </span>
            </button>
        </th>
    </xsl:template>

    <xsl:template name="pagination">
        <xsl:param name="page"/>
        <xsl:param name="prefix"/>
        <xsl:param name="ellipsis"/>
        <xsl:variable name="offset" select="number($page/offset)"/>
        <xsl:variable name="limit" select="number($page/limit)"/>
        <xsl:variable name="total" select="number($page/totalPages)"/>
        <xsl:variable name="start">
            <xsl:choose>
                <xsl:when test="$offset &gt; 3"><xsl:value-of select="$offset - 2"/></xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="end">
            <xsl:choose>
                <xsl:when test="$total &lt; 5"><xsl:value-of select="$total"/></xsl:when>
                <xsl:when test="$offset + 2 &lt; 5">5</xsl:when>
                <xsl:when test="$offset + 2 &lt;= $total"><xsl:value-of select="$offset + 2"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$total"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <div class="pagination-wrap">
            <xsl:attribute name="id"><xsl:value-of select="concat($prefix, '-pagination')"/></xsl:attribute>
            <div class="pagination-pages">
                <xsl:if test="$page/hasPrev = 'true'">
                    <a class="pagination-btn pagination-prev"
                       href="#"
                       title="Предыдущая страница">
                        <xsl:attribute name="data-limit"><xsl:value-of select="$limit"/></xsl:attribute>
                        <xsl:attribute name="data-offset"><xsl:value-of select="$offset - 1"/></xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
                             style="width:14px;height:14px;"
                             viewBox="0 0 24 24">
                            <polyline points="15 18 9 12 15 6"/>
                        </svg>
                    </a>
                </xsl:if>
                <xsl:if test="$total &gt; 1">
                    <xsl:if test="number($start) &gt; 1">
                        <a class="pagination-btn"
                           href="#"
                           title="Перейти на первую страницу"
                           data-offset="1">
                            <xsl:attribute name="data-limit"><xsl:value-of select="$limit"/></xsl:attribute>
                            <xsl:text>1</xsl:text>
                        </a>
                        <xsl:if test="number($start) &gt; 2">
                            <span class="pagination-ellipsis"><xsl:value-of select="$ellipsis"/></span>
                        </xsl:if>
                    </xsl:if>
                    <xsl:call-template name="page-links">
                        <xsl:with-param name="current" select="number($start)"/>
                        <xsl:with-param name="end" select="number($end)"/>
                        <xsl:with-param name="offset" select="$offset"/>
                        <xsl:with-param name="limit" select="$limit"/>
                    </xsl:call-template>
                    <xsl:if test="number($end) &lt; $total">
                        <xsl:if test="number($end) &lt; $total - 1">
                            <span class="pagination-ellipsis"><xsl:value-of select="$ellipsis"/></span>
                        </xsl:if>
                        <a class="pagination-btn" href="#">
                            <xsl:attribute name="title"><xsl:value-of select="concat('Перейти на последнюю страницу (', $total, ')')"/></xsl:attribute>
                            <xsl:attribute name="data-limit"><xsl:value-of select="$limit"/></xsl:attribute>
                            <xsl:attribute name="data-offset"><xsl:value-of select="$total"/></xsl:attribute>
                            <xsl:value-of select="$total"/>
                        </a>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="$page/hasNext = 'true'">
                    <a class="pagination-btn pagination-next"
                       href="#"
                       title="Следующая страница">
                        <xsl:attribute name="data-limit"><xsl:value-of select="$limit"/></xsl:attribute>
                        <xsl:attribute name="data-offset"><xsl:value-of select="$offset + 1"/></xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
                             style="width:14px;height:14px;"
                             viewBox="0 0 24 24">
                            <polyline points="9 18 15 12 9 6"/>
                        </svg>
                    </a>
                </xsl:if>
            </div>
            <xsl:if test="$limit &gt;= 15">
                <div class="pagination-size">
                    <span class="pagination-size-label">На странице:</span>
                    <xsl:call-template name="page-size">
                        <xsl:with-param name="size" select="15"/>
                        <xsl:with-param name="limit" select="$limit"/>
                    </xsl:call-template>
                    <xsl:call-template name="page-size">
                        <xsl:with-param name="size" select="30"/>
                        <xsl:with-param name="limit" select="$limit"/>
                    </xsl:call-template>
                    <xsl:call-template name="page-size">
                        <xsl:with-param name="size" select="50"/>
                        <xsl:with-param name="limit" select="$limit"/>
                    </xsl:call-template>
                </div>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="page-links">
        <xsl:param name="current"/>
        <xsl:param name="end"/>
        <xsl:param name="offset"/>
        <xsl:param name="limit"/>
        <xsl:if test="$current &lt;= $end">
            <a href="#">
                <xsl:attribute name="class">
                    <xsl:text>pagination-btn</xsl:text>
                    <xsl:if test="$current = $offset"><xsl:text> active</xsl:text></xsl:if>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:choose>
                        <xsl:when test="$current = $offset">Текущая страница</xsl:when>
                        <xsl:otherwise><xsl:value-of select="concat('Перейти на страницу ', $current)"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="data-limit"><xsl:value-of select="$limit"/></xsl:attribute>
                <xsl:attribute name="data-offset"><xsl:value-of select="$current"/></xsl:attribute>
                <xsl:value-of select="$current"/>
            </a>
            <xsl:call-template name="page-links">
                <xsl:with-param name="current" select="$current + 1"/>
                <xsl:with-param name="end" select="$end"/>
                <xsl:with-param name="offset" select="$offset"/>
                <xsl:with-param name="limit" select="$limit"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="page-size">
        <xsl:param name="size"/>
        <xsl:param name="limit"/>
        <a href="#" data-offset="1">
            <xsl:attribute name="class">
                <xsl:text>pagination-size-btn</xsl:text>
                <xsl:if test="$size = $limit"><xsl:text> active</xsl:text></xsl:if>
            </xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="concat('Показывать по ', $size, ' элементов')"/></xsl:attribute>
            <xsl:attribute name="data-limit"><xsl:value-of select="$size"/></xsl:attribute>
            <xsl:value-of select="$size"/>
        </a>
    </xsl:template>

    <xsl:template name="csrf-input">
        <xsl:if test="/schoodule/csrf/token">
            <input type="hidden">
                <xsl:attribute name="name"><xsl:value-of select="/schoodule/csrf/parameter"/></xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="/schoodule/csrf/token"/></xsl:attribute>
            </input>
        </xsl:if>
    </xsl:template>

    <xsl:template name="option">
        <xsl:param name="value"/>
        <xsl:param name="selected"/>
        <option>
            <xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
            <xsl:if test="$selected"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
            <xsl:value-of select="$value"/>
        </option>
    </xsl:template>

    <xsl:template name="calendar-icon">
        <svg xmlns="http://www.w3.org/2000/svg"
             fill="none"
             stroke="currentColor"
             stroke-width="2"
             style="width:18px;height:18px;"
             viewBox="0 0 24 24">
            <rect height="18" rx="2" ry="2" width="18" x="3" y="4"/>
            <line x1="16" x2="16" y1="2" y2="6"/>
            <line x1="8" x2="8" y1="2" y2="6"/>
            <line x1="3" x2="21" y1="10" y2="10"/>
        </svg>
    </xsl:template>

    <xsl:template name="edit-icon">
        <svg xmlns="http://www.w3.org/2000/svg"
             fill="none"
             stroke="currentColor"
             stroke-width="2"
             viewBox="0 0 24 24">
            <path d="M12 20h9"/>
            <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>
        </svg>
    </xsl:template>

    <xsl:template name="cross-icon">
        <svg xmlns="http://www.w3.org/2000/svg"
             fill="none"
             stroke="currentColor"
             stroke-width="2"
             viewBox="0 0 24 24">
            <line x1="18" x2="6" y1="6" y2="18"/>
            <line x1="6" x2="18" y1="6" y2="18"/>
        </svg>
    </xsl:template>
</xsl:stylesheet>
