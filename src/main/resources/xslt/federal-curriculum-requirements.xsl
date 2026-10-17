<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="federal-common.xsl"/>

    <xsl:output method="html"
                encoding="UTF-8"
                omit-xml-declaration="yes"
                doctype-system="about:legacy-compat"/>

    <xsl:param name="view"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$view = 'list-fragment'">
                <xsl:call-template name="requirements-results"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="page"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="page-title">
        <xsl:text>Требования: </xsl:text>
        <xsl:value-of select="/schoodule/curriculums/curriculum/title"/>
    </xsl:template>

    <xsl:template name="styles">
        <link rel="stylesheet"
              href="/css/federal-curriculum-details.css"/>
    </xsl:template>

    <xsl:template name="content">
        <xsl:call-template name="requirements"/>
    </xsl:template>

    <xsl:template name="scripts">
        <script type="module"
                src="/js/federal-curriculum-requirements.js"></script>
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
                            <td class="view-grade">
                                <span class="req-cell-value"><xsl:value-of select="grade"/></span>
                                <input class="req-inline-input req-grade-input"
                                       type="number"
                                       min="1"
                                       max="11">
                                    <xsl:attribute name="value"><xsl:value-of select="grade"/></xsl:attribute>
                                </input>
                            </td>
                            <td class="view-subject">
                                <span class="req-cell-value"><xsl:value-of select="subjectName"/></span>
                                <input class="req-inline-input req-subject-input"
                                       type="text">
                                    <xsl:attribute name="value"><xsl:value-of select="subjectName"/></xsl:attribute>
                                </input>
                            </td>
                            <td class="view-hours">
                                <span class="req-cell-value"><xsl:value-of select="weeklyHours"/></span>
                                <input class="req-inline-input req-hours-input"
                                       type="number"
                                       min="1">
                                    <xsl:attribute name="value"><xsl:value-of select="weeklyHours"/></xsl:attribute>
                                </input>
                            </td>
                            <td class="view-part">
                                <span class="req-cell-value"><xsl:value-of select="partType"/></span>
                                <select class="req-inline-input req-part-input">
                                    <option value="MANDATORY">
                                        <xsl:if test="partType = 'MANDATORY'">
                                            <xsl:attribute name="selected">selected</xsl:attribute>
                                        </xsl:if>
                                        <xsl:text>MANDATORY</xsl:text>
                                    </option>
                                    <option value="OPTIONAL">
                                        <xsl:if test="partType = 'OPTIONAL'">
                                            <xsl:attribute name="selected">selected</xsl:attribute>
                                        </xsl:if>
                                        <xsl:text>OPTIONAL</xsl:text>
                                    </option>
                                </select>
                            </td>
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
</xsl:stylesheet>
