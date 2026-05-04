$(() => {
    const pathParts = window.location.pathname.split('/').filter(Boolean);
    const schoolId = pathParts[pathParts.indexOf('schools') + 1];

    $(document).on('click', '#schedules-container .schedule-row', function(e) {
        if ($(e.target).closest('.card-item-actions').length) return;
        const id = $(this).data('id');
        window.location.href = `/schools/${schoolId}/schedules/${id}`;
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
            .done(function(html) {
                $('#schedules-results').replaceWith(html);
            });
    }

    $(document).on(
        'click',
        '#schedules-pagination .pagination-btn, #schedules-pagination .pagination-size-btn',
        function(e) {
            e.preventDefault();
            const offset = $(this).data('offset') || 1;
            const limit = $(this).data('limit') || parseInt(getParam('limit', '15'));
            const name = $('#schedule-search').val().trim();
            loadFragment(name, offset, limit);
        }
    );

    let searchTimer;
    $(document).on('input', '#schedule-search', function() {
        clearTimeout(searchTimer);
        const q = $(this).val().trim();
        searchTimer = setTimeout(function() {
            loadFragment(q, 1, parseInt(getParam('limit', '15')));
        }, 300);
    });

    window.addEventListener('popstate', function() {
        const name = getParam('name', '');
        const offset = getParam('offset', '1');
        const limit = getParam('limit', '15');
        $('#schedule-search').val(name);
        $.get(window.location.pathname + '/fragment', { name, offset, limit })
            .done(function(html) {
                $('#schedules-results').replaceWith(html);
            });
    });
});
