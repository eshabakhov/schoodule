$(() => {
    const $panel = $('#req-add-panel');
    const $toggleBtn = $('#btn-toggle-add');
    const $toggleLabel = $('#toggle-add-label');
    const $toggleIcon = $('#toggle-add-icon');
    $toggleBtn.on('click', () => {
        const isOpen = $panel.hasClass('open');
        $panel.toggleClass('open', !isOpen);
        if (isOpen) {
            $toggleLabel.text('Добавить требование');
            $toggleIcon.html(
                '<line x1="12" y1="5" x2="12" y2="19"/>' +
                '<line x1="5" y1="12" x2="19" y2="12"/>'
            );
        } else {
            $toggleLabel.text('Скрыть');
            $toggleIcon.html('<line x1="5" y1="12" x2="19" y2="12"/>');
        }
    });
    const PAGE_SIZE_DEFAULT = 15;
    let currentPage = 1;
    let pageSize = PAGE_SIZE_DEFAULT;
    let filterGrade = '';
    let filterSubject = '';
    let filterPart = '';
    let $allRows = $('#req-tbody tr.req-data-row');
    function cancelEdit($tr) {
        if (!$tr.hasClass('req-editing')) return;
        const orig = $tr.data('orig');
        $tr.find('.view-grade').text(orig.grade);
        $tr.find('.view-subject').text(orig.subject);
        $tr.find('.view-hours').text(orig.hours);
        $tr.find('.view-part').text(orig.part);
        $tr.removeClass('req-editing');
    }
    function renderPagination(total, totalPages) {
        const $wrap = $('#req-pagination');
        if (totalPages <= 1) {
            $wrap.empty();
            return;
        }
        const hasPrev = currentPage > 1;
        const hasNext = currentPage < totalPages;
        let start = Math.max(1, currentPage - 2);
        let end = Math.min(totalPages, currentPage + 2);
        if (end - start < 4) {
            if (start === 1) end = Math.min(totalPages, 5);
            else start = Math.max(1, end - 4);
        }
        let html = '<div class="pagination-pages">';
        if (hasPrev) {
            html += `<a href="#" class="pagination-btn pagination-prev req-page-btn" data-page="${currentPage - 1}">` +
                `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" style="width:14px;height:14px;">` +
                `<polyline points="15 18 9 12 15 6"/></svg></a>`;
        }
        if (start > 1) {
            html += `<a href="#" class="pagination-btn req-page-btn" data-page="1">1</a>`;
            if (start > 2) html += `<span class="pagination-ellipsis">…</span>`;
        }
        for (let p = start; p <= end; p++) {
            const active = p === currentPage ? ' active' : '';
            html += `<a href="#" class="pagination-btn req-page-btn${active}" data-page="${p}">${p}</a>`;
        }
        if (end < totalPages) {
            if (end < totalPages - 1) html += `<span class="pagination-ellipsis">…</span>`;
            html += `<a href="#" class="pagination-btn req-page-btn" data-page="${totalPages}">${totalPages}</a>`;
        }
        if (hasNext) {
            html += `<a href="#" class="pagination-btn pagination-next req-page-btn" data-page="${currentPage + 1}">` +
                `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" style="width:14px;height:14px;">` +
                `<polyline points="9 18 15 12 9 6"/></svg></a>`;
        }
        html += '</div>';
        if (total > 15) {
            html += '<div class="pagination-size"><span class="pagination-size-label">На странице:</span>';
            [15, 30, 50].forEach(s => {
                const active = s === pageSize ? ' active' : '';
                html += `<a href="#" class="pagination-size-btn req-pagesize-btn${active}" data-size="${s}">${s}</a>`;
            });
            html += '</div>';
        }
        $wrap.html(html);
    }
    function applyFiltersAndPaginate() {
        $('#req-tbody tr.req-editing').each(function () {
            cancelEdit($(this));
        });
        const filtered = $allRows;
        const total = filtered.length;
        const totalPages = Math.max(1, Math.ceil(total / pageSize));
        if (currentPage > totalPages) currentPage = totalPages;
        const start = (currentPage - 1) * pageSize;
        const end = start + pageSize;
        $allRows.hide();
        filtered.slice(start, end).show();
        const $emptyRow = $('#req-empty-row');
        if (total === 0) {
            $emptyRow.show();
        } else {
            $emptyRow.hide();
        }
        renderPagination(total, totalPages);
    }
    function reloadRequirements() {
        const url = $('#req-tbody').data('search-url');
        $.get(url, { grade: filterGrade, subject: filterSubject, part: filterPart })
            .done(html => {
                $('#req-tbody').replaceWith(html);
                $allRows = $('#req-tbody tr.req-data-row');
                applyFiltersAndPaginate();
            });
    }
    $(document).on('click', '.req-page-btn', function (e) {
        e.preventDefault();
        currentPage = parseInt($(this).data('page'));
        applyFiltersAndPaginate();
    });
    $(document).on('click', '.req-pagesize-btn', function (e) {
        e.preventDefault();
        pageSize = parseInt($(this).data('size'));
        currentPage = 1;
        applyFiltersAndPaginate();
    });
    let filterTimer;
    $('#filter-grade').on('input', function () {
        clearTimeout(filterTimer);
        const v = $(this).val().trim();
        filterTimer = setTimeout(() => {
            filterGrade = v;
            currentPage = 1;
            reloadRequirements();
        }, 250);
    });
    $('#filter-subject').on('input', function () {
        clearTimeout(filterTimer);
        const v = $(this).val().trim();
        filterTimer = setTimeout(() => {
            filterSubject = v;
            currentPage = 1;
            reloadRequirements();
        }, 250);
    });
    $('#filter-part').on('change', function () {
        filterPart = $(this).val();
        currentPage = 1;
        reloadRequirements();
    });
    applyFiltersAndPaginate();
    $(document).on('click', '.req-row-edit', function () {
        const $tr = $(this).closest('tr');
        if ($tr.hasClass('req-editing')) return;
        $('#req-tbody tr.req-editing').each(function () {
            cancelEdit($(this));
        });
        const grade = $tr.find('.view-grade').text().trim();
        const subject = $tr.find('.view-subject').text().trim();
        const hours = $tr.find('.view-hours').text().trim();
        const part = $tr.find('.view-part').text().trim();
        $tr.data('orig', { grade, subject, hours, part });
        $tr.find('.view-grade').html(
            `<input type="number" min="1" max="11" class="req-inline-input" style="width:60px;" value="${grade}">`
        );
        $tr.find('.view-subject').html(
            `<input type="text" class="req-inline-input" value="${subject}">`
        );
        $tr.find('.view-hours').html(
            `<input type="number" min="1" class="req-inline-input" style="width:70px;" value="${hours}">`
        );

        let opts = '';
        $('#req-add-panel select[name="partType"] option').each(function () {
            const val = $(this).val();
            const sel = val === part ? ' selected' : '';
            opts += `<option value="${val}"${sel}>${val}</option>`;
        });
        $tr.find('.view-part').html(
            `<select class="req-inline-input">${opts}</select>`
        );
        $tr.addClass('req-editing');
        $tr.find('.view-grade input').trigger('focus');
    });
    $(document).on('click', '.req-row-cancel', function () {
        cancelEdit($(this).closest('tr'));
    });
    $(document).on('click', '.req-row-save', function () {
        const $tr = $(this).closest('tr');
        const grade = $tr.find('.view-grade input').val().trim();
        const subject = $tr.find('.view-subject input').val().trim();
        const hours = $tr.find('.view-hours input').val().trim();
        const part = $tr.find('.view-part select').val();
        if (!grade || !subject || !hours || !part) {
            alert('Заполните все поля');
            return;
        }
        const $form = $tr.find('.req-edit-form');
        $form.find('.edit-grade').val(grade);
        $form.find('.edit-subject').val(subject);
        $form.find('.edit-hours').val(hours);
        $form.find('.edit-part').val(part);
        $form.trigger('submit');
    });
});