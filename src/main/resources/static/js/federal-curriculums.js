$(() => {
    $(document).on('click', '#federal-curriculums-container .federal-curriculum-row', function(e) {
        if ($(e.target).closest('.card-item-actions').length) return;
        const id = $(this).data('id');
        window.location.href = `/federal/curriculums/${id}`;
    });
    function getParam(key, fallback) {
        return new URLSearchParams(window.location.search).get(key) || fallback;
    }
    function updateUrl(title, offset, limit) {
        const params = new URLSearchParams();
        if (title) params.set('title', title);
        params.set('offset', offset);
        params.set('limit', limit);
        history.pushState(null, '', window.location.pathname + '?' + params.toString());
    }
    function loadFragment(title, offset, limit) {
        updateUrl(title, offset, limit);
        $.get(window.location.pathname + '/fragment', { title, offset, limit })
            .done(function(html) {
                $('#federal-curriculums-results').replaceWith(html);
            });
    }
    $(document).on(
        'click',
        '#federal-curriculums-pagination .pagination-btn',
        function(e) {
            e.preventDefault();
            const offset = $(this).data('offset') || 1;
            const limit = $(this).data('limit') || parseInt(getParam('limit', '15'));
            const title = $('#federal-curriculum-search').val().trim();
            loadFragment(title, offset, limit);
        }
    );
    let searchTimer;
    $(document).on('input', '#federal-curriculum-search', function() {
        clearTimeout(searchTimer);
        const q = $(this).val().trim();
        searchTimer = setTimeout(function() {
            loadFragment(q, 1, parseInt(getParam('limit', '15')));
        }, 300);
    });
    $(document).on('click', '.btn-delete-curriculum', function() {
        const id = $(this).data('id');
        const name = $(this).data('name');
        if (!confirm(`Удалить «${name}»?`)) return;
        $.ajax({
            url: `/api/federal/curriculums/${id}`,
            method: 'DELETE',
            headers: { version: 'SIMPLE' }
        })
            .done(() => {
                window.location.href = '/federal/curriculums';
            })
            .fail(() => alert('Не удалось удалить федеральный учебный план'));
    });

    $(document).on('click', '.btn-delete-requirement', function() {
        const id = $(this).data('id');
        const curriculum = $(this).data('curriculum');
        const name = $(this).data('name');
        if (!confirm(`Удалить требование «${name}»?`)) return;
        $.ajax({
            url: `/api/federal/curriculums/${curriculum}/requirements/${id}`,
            method: 'DELETE',
            headers: { version: 'SIMPLE' }
        })
            .done(() => {
                window.location.reload();
            })
            .fail(() => alert('Не удалось удалить требование'));
    });
});
