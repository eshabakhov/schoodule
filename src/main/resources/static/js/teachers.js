$(() => {
    const pathParts = window.location.pathname.split('/').filter(Boolean);
    const schoolId = pathParts[pathParts.indexOf('schools') + 1];

    $(document).on('click', '#teachers-container .teacher-row', function (e) {
        if ($(e.target).closest('.card-item-actions').length) return;
        const id = $(this).data('id');
        window.location.href = `/schools/${schoolId}/teachers/${id}`;
    });

    $(document).on('click', '.btn-delete-row', function (e) {
        e.stopPropagation();
        const id   = $(this).data('id');
        const name = $(this).data('name');
        if (!confirm(`Удалить «${name}»? Это действие нельзя отменить.`)) return;
        $.ajax({
            url: `/api/schools/${schoolId}/teachers/${id}`,
            method: 'DELETE',
            headers: { version: 'SIMPLE' }
        })
            .done(() => {
                window.location.href = `/schools/${schoolId}/teachers`;
            })
            .fail(() => alert('Не удалось удалить учителя'));
    });

    function getParam(key, fallback) {
        return new URLSearchParams(window.location.search).get(key) || fallback;
    }

    function updateUrl(name, offset, limit) {
        const params = new URLSearchParams();
        if (name) params.set('name', name);
        params.set('offset', offset);
        params.set('limit', limit);
        history.pushState(null, '', window.location.pathname + '?' + params.toString());
    }

    function loadFragment(name, offset, limit) {
        updateUrl(name, offset, limit);
        $.get(window.location.pathname + '/fragment', { name, offset, limit })
            .done(function (html) {
                $('#teachers-results').replaceWith(html);
            });
    }

    $(document).on(
        'click',
        '#teachers-pagination .pagination-btn, #teachers-pagination .pagination-size-btn',
        function (e) {
            e.preventDefault();
            const offset = $(this).data('offset') || 1;
            const limit  = $(this).data('limit')  || parseInt(getParam('limit', '15'));
            const name   = $('#teacher-search').val().trim();
            loadFragment(name, offset, limit);
        }
    );

    let searchTimer;
    $(document).on('input', '#teacher-search', function () {
        clearTimeout(searchTimer);
        const q = $(this).val().trim();
        searchTimer = setTimeout(function () {
            loadFragment(q, 1, parseInt(getParam('limit', '15')));
        }, 300);
    });

    window.addEventListener('popstate', function () {
        const name   = getParam('name', '');
        const offset = getParam('offset', '1');
        const limit  = getParam('limit', '15');
        $('#teacher-search').val(name);
        $.get(window.location.pathname + '/fragment', { name, offset, limit })
            .done(function (html) {
                $('#teachers-results').replaceWith(html);
            });
    });
});