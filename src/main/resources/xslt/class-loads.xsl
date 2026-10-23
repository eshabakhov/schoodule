<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="school-common.xsl"/>
    <xsl:output method="html" encoding="UTF-8" omit-xml-declaration="yes"
                doctype-system="about:legacy-compat"/>
    <xsl:param name="view"/>
    <xsl:variable name="schedule" select="/surok/schedule"/>
    <xsl:variable name="entries" select="/surok/loads"/>
    <xsl:variable name="selected" select="$entries/load[$view = 'details' or $view = 'edit']"/>
    <xsl:variable name="base" select="concat($school-path, '/schedules/', $schedule/id, '/loads')"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$view = 'list-fragment'"><xsl:call-template name="results"/></xsl:when>
            <xsl:otherwise><xsl:call-template name="page"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="page-title">
        <xsl:choose>
            <xsl:when test="$view = 'create'">Добавить нагрузку</xsl:when>
            <xsl:when test="$view = 'edit'">Редактировать нагрузку</xsl:when>
            <xsl:otherwise><xsl:value-of select="concat($schedule/name, ' - Учебная нагрузка')"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="styles"><link rel="stylesheet" href="/css/school/class-loads.css"/></xsl:template>

    <xsl:template name="content">
        <nav class="breadcrumbs" aria-label="Навигация">
            <a href="{$school-path}/schedules/{$schedule/id}" title="Перейти к расписанию"><xsl:value-of select="$schedule/name"/></a>
            <span>/</span><a href="{$base}" title="Учебная нагрузка">Учебная нагрузка</a>
        </nav>
        <xsl:choose>
            <xsl:when test="$view = 'create' or $view = 'edit'">
                <div class="page-title-row load-form-heading"><h1><xsl:call-template name="page-title"/></h1></div>
                <div class="load-form-wrap"><xsl:call-template name="errors"/><xsl:call-template name="editor"/></div>
            </xsl:when>
            <xsl:when test="$view = 'details'"><xsl:call-template name="details"/></xsl:when>
            <xsl:otherwise>
                <div class="page-title-row">
                    <div><h1>Учебная нагрузка</h1><p class="page-subtitle"><xsl:value-of select="$schedule/name"/></p></div>
                    <a class="btn btn-primary loads-add" href="{$base}/create" title="Добавить нагрузку" data-open-load-dialog="true">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <line x1="12" x2="12" y1="5" y2="19"/><line x1="5" x2="19" y1="12" y2="12"/>
                        </svg>
                        <xsl:text>Добавить нагрузку</xsl:text>
                    </a>
                </div>
                <xsl:call-template name="errors"/>
                <xsl:call-template name="results"/>
                <dialog id="load-create-dialog" class="load-dialog" aria-labelledby="load-dialog-title">
                    <div class="load-dialog-heading">
                        <h2 id="load-dialog-title">Добавить нагрузку</h2>
                        <button type="button" class="btn-outline-action" data-close-load-dialog="true" aria-label="Закрыть">×</button>
                    </div>
                    <xsl:call-template name="editor"><xsl:with-param name="modal" select="true()"/></xsl:call-template>
                </dialog>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="results">
        <div id="loads-results">
            <div class="data-table-wrap loads-table-wrap">
                <table class="shadcn-table loads-table">
                    <thead><tr><th scope="col">Класс</th><th scope="col">Предмет</th><th scope="col">Часов/неделю</th><th scope="col">Действия</th></tr></thead>
                    <tbody>
                        <xsl:if test="not($entries/load)"><tr><td class="table-empty" colspan="4">Нет данных. Добавьте нагрузку.</td></tr></xsl:if>
                        <xsl:for-each select="$entries/load">
                            <tr data-id="{id}">
                                <td><a href="{$base}/{id}" title="Открыть нагрузку"><xsl:value-of select="schoolClassName"/></a></td>
                                <td><xsl:value-of select="subjectName"/></td>
                                <td class="td-mono"><xsl:value-of select="hoursPerWeek"/></td>
                                <td><div class="load-actions">
                                    <a class="btn-outline-action" href="{$base}/{id}/edit" title="Редактировать нагрузку">Редактировать</a>
                                    <xsl:call-template name="delete-button"/>
                                </div></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div>
            <xsl:call-template name="pagination">
                <xsl:with-param name="page" select="$entries/page"/>
                <xsl:with-param name="prefix" select="'loads'"/>
                <xsl:with-param name="ellipsis" select="'…'"/>
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template name="details">
        <div class="page-title-row"><div>
            <h1>Нагрузка <xsl:value-of select="$selected/schoolClassName"/></h1>
            <p class="page-subtitle"><xsl:value-of select="$selected/subjectName"/></p>
        </div><a class="btn-outline-action" href="{$base}/{$selected/id}/edit" title="Редактировать нагрузку">Редактировать</a></div>
        <div class="school-info">
            <div class="info-row"><span class="info-label">Класс:</span><span class="info-value"><xsl:value-of select="$selected/schoolClassName"/></span></div>
            <div class="info-row"><span class="info-label">Предмет:</span><span class="info-value"><xsl:value-of select="$selected/subjectName"/></span></div>
            <div class="info-row"><span class="info-label">Часов/неделю:</span><span class="info-value"><xsl:value-of select="$selected/hoursPerWeek"/></span></div>
        </div>
        <div class="details-footer-actions"><xsl:for-each select="$selected"><xsl:call-template name="delete-button"/></xsl:for-each></div>
    </xsl:template>

    <xsl:template name="delete-button">
        <button type="button" class="btn-outline-action load-delete" data-uri="{$base}/{id}"
                data-redirect="{$base}" data-name="{concat(schoolClassName, ' — ', subjectName)}">Удалить</button>
    </xsl:template>

    <xsl:template name="errors">
        <xsl:choose>
            <xsl:when test="/surok/error = 'duplicate'"><div class="error-message" role="alert">Нагрузка для выбранного класса и предмета уже существует.</div></xsl:when>
            <xsl:when test="/surok/error = 'input'"><div class="error-message" role="alert">Выберите действующие класс и предмет этой школы и укажите положительное целое число часов.</div></xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="editor">
        <xsl:param name="modal" select="false()"/>
        <form method="post" class="load-form">
            <xsl:attribute name="action"><xsl:choose>
                <xsl:when test="$view = 'edit'"><xsl:value-of select="concat($base, '/', $selected/id, '/edit')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat($base, '/create')"/></xsl:otherwise>
            </xsl:choose></xsl:attribute>
            <xsl:call-template name="csrf-input"/>
            <label for="load-class" class="load-field">Класс
                <select id="load-class" name="schoolClassId" required="required">
                    <option value="">Выберите класс</option>
                    <xsl:if test="$view = 'edit' and not(/surok/classes/school-class/id = $selected/schoolClassId)">
                        <option value="" selected="selected" disabled="disabled"><xsl:value-of select="$selected/schoolClassName"/> (удалён)</option>
                    </xsl:if>
                    <xsl:for-each select="/surok/classes/school-class"><option value="{id}">
                        <xsl:if test="id = $selected/schoolClassId"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                        <xsl:value-of select="name"/>
                    </option></xsl:for-each>
                </select>
            </label>
            <label for="load-subject" class="load-field">Предмет
                <select id="load-subject" name="subjectId" required="required">
                    <option value="">Выберите предмет</option>
                    <xsl:if test="$view = 'edit' and not(/surok/subjects/subject/id = $selected/subjectId)">
                        <option value="" selected="selected" disabled="disabled"><xsl:value-of select="$selected/subjectName"/> (удалён)</option>
                    </xsl:if>
                    <xsl:for-each select="/surok/subjects/subject"><option value="{id}">
                        <xsl:if test="id = $selected/subjectId"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
                        <xsl:value-of select="name"/>
                    </option></xsl:for-each>
                </select>
            </label>
            <label for="load-hours" class="load-field">Часов в неделю
                <input id="load-hours" name="hoursPerWeek" type="number" min="1" max="2147483647" step="1" required="required" value="{$selected/hoursPerWeek}"/>
            </label>
            <div class="load-form-actions">
                <xsl:choose>
                    <xsl:when test="$modal"><button type="button" class="btn-outline-action" data-close-load-dialog="true">Отмена</button></xsl:when>
                    <xsl:otherwise><a class="btn-outline-action" href="{$base}" title="Отменить изменение нагрузки">Отмена</a></xsl:otherwise>
                </xsl:choose>
                <button type="submit" class="btn-primary"><xsl:choose><xsl:when test="$view = 'edit'">Сохранить</xsl:when><xsl:otherwise>Добавить</xsl:otherwise></xsl:choose></button>
            </div>
        </form>
    </xsl:template>

    <xsl:template name="scripts"><script type="module" src="/js/school/class-loads.js"></script></xsl:template>
</xsl:stylesheet>
