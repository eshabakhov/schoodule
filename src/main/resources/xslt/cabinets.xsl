<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="school-common.xsl"/>
    <xsl:output method="html" encoding="UTF-8" omit-xml-declaration="yes"
                doctype-system="about:legacy-compat"/>
    <xsl:param name="view"/>
    <xsl:variable name="entries" select="/surok/cabinets"/>
    <xsl:variable name="selected" select="$entries/cabinet"/>
    <xsl:variable name="base" select="concat($school-path, '/buildings/', $building/id, '/cabinets')"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$view = 'list-fragment'"><xsl:call-template name="results"/></xsl:when>
            <xsl:otherwise><xsl:call-template name="page"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="page-title">
        <xsl:choose>
            <xsl:when test="$view = 'create'">Новый кабинет</xsl:when>
            <xsl:when test="$view = 'edit'">Редактировать кабинет</xsl:when>
            <xsl:when test="$view = 'details'"><xsl:value-of select="$selected/name"/></xsl:when>
            <xsl:otherwise>Кабинеты</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="content">
        <xsl:choose>
            <xsl:when test="$view = 'create' or $view = 'edit'"><xsl:call-template name="form"/></xsl:when>
            <xsl:when test="$view = 'details'"><xsl:call-template name="details"/></xsl:when>
            <xsl:otherwise>
                <div class="page-title-row">
                    <div><h1>Кабинеты</h1><p class="page-subtitle">Управление кабинетами школы</p></div>
                    <a class="btn-primary" href="{$base}/create" title="Добавить кабинет">Добавить кабинет</a>
                </div>
                <div class="search-wrap" style="width:100%;max-width:320px;margin-bottom:1.25rem;">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" stroke="currentColor"
                         stroke-width="2" viewBox="0 0 24 24">
                        <circle cx="11" cy="11" r="8"/><line x1="21" x2="16.65" y1="21" y2="16.65"/>
                    </svg>
                    <label for="cabinet-search">
                        <input id="cabinet-search" type="text" placeholder="Поиск кабинета..."
                               value="{$entries/filters/item[name='name' or name='name_ct']/value}"/>
                    </label>
                </div>
                <xsl:call-template name="results"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="results">
        <div id="cabinets-results">
            <div class="card-grid" id="cabinets-container">
                <xsl:if test="not($entries/cabinet)">
                    <div style="grid-column:1/-1;text-align:center;padding:3rem;color:var(--muted-fg);">Кабинеты не найдены.</div>
                </xsl:if>
                <xsl:for-each select="$entries/cabinet">
                    <div class="card-item cabinet-row" style="cursor:pointer;" data-id="{id}" data-name="{name}">
                        <div class="card-item-header"><div class="card-item-title-row"><div class="card-item-icon">
                            <svg fill="none"
                                     stroke="currentColor"
                                     stroke-width="2"
                                     viewBox="0 0 24 24"
                                     xmlns="http://www.w3.org/2000/svg">
                                    <rect height="18" rx="1" width="10" x="7" y="3"/>
                                    <circle cx="14" cy="12" fill="currentColor" r="0.8"/>
                                    <path d="M9 7h4"/>
                                </svg>
                        </div></div></div>
                        <div class="card-item-period">
                            <a class="card-item-name" href="{$base}/{id}" title="Открыть"><xsl:value-of select="name"/></a>
                            <div class="card-item-period-label">ID: <span class="td-mono"><xsl:value-of select="id"/></span></div>
                        </div>
                    </div>
                </xsl:for-each>
            </div>
            <xsl:call-template name="pagination">
                <xsl:with-param name="page" select="$entries/page"/>
                <xsl:with-param name="prefix" select="'cabinets'"/>
                <xsl:with-param name="ellipsis" select="'…'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template name="details">
        <div class="page-title-row" style="margin-bottom:1.5rem;">
            <div>
                <h1 class="title-with-action">
                    <span><xsl:value-of select="$selected/name"/></span>
                    <a class="btn-icon-edit" href="{$base}/{$selected/id}/edit"
                       title="Редактировать" aria-label="Редактировать">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" height="15" width="15"
                             stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path d="M12 20h9"/><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>
                        </svg>
                    </a>
                </h1>
                <p class="page-subtitle">Информация о кабинете</p>
            </div>
        </div>
        <div class="school-info">
            <div class="info-row"><span class="info-label">ID:</span><span class="info-value"><xsl:value-of select="$selected/id"/></span></div>
            <div class="info-row"><span class="info-label">Название:</span><span class="info-value"><xsl:value-of select="$selected/name"/></span></div>
        </div>
        <div class="details-footer-actions">
            <button class="btn-outline-action btn-delete-row" type="button"
                    style="color:var(--destructive);border-color:#fecaca;background:#fef2f2;"
                    data-name="{$selected/name}" data-uri="{$base}/{$selected/id}" data-redirect="{$base}">Удалить</button>
        </div>
    </xsl:template>

    <xsl:template name="form">
        <div class="page-title-row" style="justify-content:center;">
            <h1><xsl:call-template name="page-title"/></h1>
        </div>
        <div style="max-width:480px;margin:0 auto;">
            <form method="post" style="display:flex;flex-direction:column;gap:1rem;">
                <xsl:attribute name="action">
                    <xsl:choose>
                        <xsl:when test="$view = 'edit'"><xsl:value-of select="concat($base, '/', $selected/id, '/edit')"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="concat($base, '/create')"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:call-template name="csrf-input"/>
                <label for="cabinet-name" style="display:flex;flex-direction:column;gap:.375rem;font-size:.875rem;font-weight:500;">
                    Название кабинета
                    <input id="cabinet-name" name="name" type="text" autofocus="autofocus"
                           required="required" value="{$selected/name}"/>
                </label>
                <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.25rem;">
                    <a class="btn-outline-action" title="Отменить изменение">
                        <xsl:attribute name="href">
                            <xsl:choose>
                                <xsl:when test="$view = 'edit'"><xsl:value-of select="concat($base, '/', $selected/id)"/></xsl:when>
                                <xsl:otherwise><xsl:value-of select="$base"/></xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:text>Отмена</xsl:text>
                    </a>
                    <button class="btn-primary" type="submit">
                        <xsl:choose><xsl:when test="$view = 'edit'">Сохранить</xsl:when><xsl:otherwise>Создать</xsl:otherwise></xsl:choose>
                    </button>
                </div>
            </form>
        </div>
    </xsl:template>

    <xsl:template name="scripts"><script type="module" src="/js/cabinets.js"></script></xsl:template>
</xsl:stylesheet>
