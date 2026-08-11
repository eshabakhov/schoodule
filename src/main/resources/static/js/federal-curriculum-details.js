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
    function getParam(key, fallback) {
        return new URLSearchParams(window.location.search).get(key) || fallback;
    }
    let currentPage = parseInt(getParam('offset', '1'));
    let pageSize = parseInt(getParam('limit', '15'));
    let filterGrade = getParam('grade', '');
    let filterSubject = getParam('subject', '');
    let filterPart = getParam('part', '');
    $('#filter-grade').val(filterGrade);
    $('#filter-subject').val(filterSubject);
    $('#filter-part').val(filterPart);
    function cancelEdit($tr) {
        if (!$tr.hasClass('req-editing')) return;
        const orig = $tr.data('orig');
        $tr.find('.view-grade').text(orig.grade);
        $tr.find('.view-subject').text(orig.subject);
        $tr.find('.view-hours').text(orig.hours);
        $tr.find('.view-part').text(orig.part);
        $tr.removeClass('req-editing');
    }
    function updateUrl() {
        const params = new URLSearchParams();
        if (filterGrade) params.set('grade', filterGrade);
        if (filterSubject) params.set('subject', filterSubject);
        if (filterPart) params.set('part', filterPart);
        params.set('offset', currentPage);
        params.set('limit', pageSize);
        history.pushState(null, '', window.location.pathname + '?' + params.toString());
    }
    function loadRequirements(pushState = true) {
        $('#req-tbody tr.req-editing').each(function () {
            cancelEdit($(this));
        });
        if (pushState) updateUrl();
        const url = $('#req-tbody').data('search-url');
        $.get(url, {
            grade: filterGrade,
            subject: filterSubject,
            part: filterPart,
            offset: currentPage,
            limit: pageSize
        }).done(html => {
            $('#req-results').replaceWith(html);
        });
    }
    $(document).on(
        'click',
        '#req-pagination .pagination-btn, #req-pagination .pagination-size-btn',
        function (e) {
            e.preventDefault();
            currentPage = parseInt($(this).data('offset')) || 1;
            pageSize = parseInt($(this).data('limit')) || pageSize;
            loadRequirements();
        }
    );
    let filterTimer;
    $('#filter-grade').on('input', function () {
        clearTimeout(filterTimer);
        const v = $(this).val().trim();
        filterTimer = setTimeout(() => {
            filterGrade = v;
            currentPage = 1;
            loadRequirements();
        }, 250);
    });
    $('#filter-subject').on('input', function () {
        clearTimeout(filterTimer);
        const v = $(this).val().trim();
        filterTimer = setTimeout(() => {
            filterSubject = v;
            currentPage = 1;
            loadRequirements();
        }, 250);
    });
    $('#filter-part').on('change', function () {
        filterPart = $(this).val();
        currentPage = 1;
        loadRequirements();
    });
    window.addEventListener('popstate', function () {
        currentPage = parseInt(getParam('offset', '1'));
        pageSize = parseInt(getParam('limit', '15'));
        filterGrade = getParam('grade', '');
        filterSubject = getParam('subject', '');
        filterPart = getParam('part', '');
        $('#filter-grade').val(filterGrade);
        $('#filter-subject').val(filterSubject);
        $('#filter-part').val(filterPart);
        loadRequirements(false);
    });
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