<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="page-title">
        <xsl:text>Schoodule</xsl:text>
    </xsl:template>

    <xsl:template name="styles"/>

    <xsl:template name="content"/>

    <xsl:template name="scripts"/>

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
                <xsl:call-template name="styles"/>
            </head>
            <body>
                <xsl:call-template name="header"/>
                <main>
                    <xsl:call-template name="sidebar"/>
                    <div class="app-content" id="app-content">
                        <div class="page-body">
                            <xsl:call-template name="content"/>
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
                <xsl:call-template name="scripts"/>
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
                    <a class="sidebar-link" title="Школы">
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

    <xsl:template name="pagination">
        <xsl:param name="page"/>
        <xsl:param name="prefix"/>
        <xsl:param name="ellipsis"/>
        <xsl:variable name="offset" select="number($page/offset)"/>
        <xsl:variable name="limit" select="number($page/limit)"/>
        <xsl:variable name="total" select="number($page/totalPages)"/>
        <xsl:variable name="start">
            <xsl:choose>
                <xsl:when test="$offset &gt; 3">
                    <xsl:value-of select="$offset - 2"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="end">
            <xsl:choose>
                <xsl:when test="$total &lt; 5">
                    <xsl:value-of select="$total"/>
                </xsl:when>
                <xsl:when test="$offset + 2 &lt; 5">5</xsl:when>
                <xsl:when test="$offset + 2 &lt;= $total">
                    <xsl:value-of select="$offset + 2"/>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="$total"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <div class="pagination-wrap">
            <xsl:attribute name="id">
                <xsl:value-of select="concat($prefix, '-pagination')"/>
            </xsl:attribute>
            <div class="pagination-pages">
                <xsl:if test="$page/hasPrev = 'true'">
                    <a class="pagination-btn pagination-prev"
                       href="#"
                       title="Предыдущая страница">
                        <xsl:attribute name="data-limit">
                            <xsl:value-of select="$limit"/>
                        </xsl:attribute>
                        <xsl:attribute name="data-offset">
                            <xsl:value-of select="$offset - 1"/>
                        </xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
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
                            <xsl:attribute name="data-limit">
                                <xsl:value-of select="$limit"/>
                            </xsl:attribute>
                            <xsl:text>1</xsl:text>
                        </a>
                        <xsl:if test="number($start) &gt; 2">
                            <span class="pagination-ellipsis">
                                <xsl:value-of select="$ellipsis"/>
                            </span>
                        </xsl:if>
                    </xsl:if>
                    <xsl:call-template name="page-links">
                        <xsl:with-param name="current"
                                        select="number($start)"/>
                        <xsl:with-param name="end" select="number($end)"/>
                        <xsl:with-param name="offset" select="$offset"/>
                        <xsl:with-param name="limit" select="$limit"/>
                    </xsl:call-template>
                    <xsl:if test="number($end) &lt; $total">
                        <xsl:if test="number($end) &lt; $total - 1">
                            <span class="pagination-ellipsis">
                                <xsl:value-of select="$ellipsis"/>
                            </span>
                        </xsl:if>
                        <a class="pagination-btn" href="#">
                            <xsl:attribute name="title">
                                <xsl:value-of select="concat('Перейти на последнюю страницу (', $total, ')')"/>
                            </xsl:attribute>
                            <xsl:attribute name="data-limit">
                                <xsl:value-of select="$limit"/>
                            </xsl:attribute>
                            <xsl:attribute name="data-offset">
                                <xsl:value-of select="$total"/>
                            </xsl:attribute>
                            <xsl:value-of select="$total"/>
                        </a>
                    </xsl:if>
                </xsl:if>
                <xsl:if test="$page/hasNext = 'true'">
                    <a class="pagination-btn pagination-next"
                       href="#"
                       title="Следующая страница">
                        <xsl:attribute name="data-limit">
                            <xsl:value-of select="$limit"/>
                        </xsl:attribute>
                        <xsl:attribute name="data-offset">
                            <xsl:value-of select="$offset + 1"/>
                        </xsl:attribute>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="2"
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
                    <xsl:if test="$current = $offset">
                        <xsl:text> active</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:choose>
                        <xsl:when test="$current = $offset">
                            <xsl:text>Текущая страница</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat('Перейти на страницу ', $current)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="data-limit">
                    <xsl:value-of select="$limit"/>
                </xsl:attribute>
                <xsl:attribute name="data-offset">
                    <xsl:value-of select="$current"/>
                </xsl:attribute>
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
                <xsl:if test="$size = $limit">
                    <xsl:text> active</xsl:text>
                </xsl:if>
            </xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="concat('Показывать по ', $size, ' элементов')"/>
            </xsl:attribute>
            <xsl:attribute name="data-limit">
                <xsl:value-of select="$size"/>
            </xsl:attribute>
            <xsl:value-of select="$size"/>
        </a>
    </xsl:template>

    <xsl:template name="csrf-input">
        <xsl:if test="/schoodule/csrf/token">
            <input type="hidden">
                <xsl:attribute name="name">
                    <xsl:value-of select="/schoodule/csrf/parameter"/>
                </xsl:attribute>
                <xsl:attribute name="value">
                    <xsl:value-of select="/schoodule/csrf/token"/>
                </xsl:attribute>
            </input>
        </xsl:if>
    </xsl:template>

    <xsl:template name="option">
        <xsl:param name="value"/>
        <xsl:param name="selected"/>
        <option>
            <xsl:attribute name="value">
                <xsl:value-of select="$value"/>
            </xsl:attribute>
            <xsl:if test="$selected">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$value"/>
        </option>
    </xsl:template>

    <xsl:template name="calendar-icon">
        <svg xmlns="http://www.w3.org/2000/svg"
             fill="none"
             stroke="currentColor"
             stroke-width="2"
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
