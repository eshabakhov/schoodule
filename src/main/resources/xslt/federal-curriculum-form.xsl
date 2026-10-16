<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
</xsl:stylesheet>
