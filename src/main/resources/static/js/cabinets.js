$(() => {
    const pathParts = window.location.pathname.split('/').filter(Boolean);
    const schoolId = pathParts[pathParts.indexOf('schools') + 1];

    $(document).on('click', '#cabinets-container .cabinet-row', function(e) {
        if ($(e.target).closest('.card-item-actions').length) return;
        const id = $(this).data('id');
        window.location.href = `/schools/${schoolId}/cabinets/${id}`;
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
                $('#cabinets-results').replaceWith(html);
            });
    }

    $(document).on(
        'click',
        '#cabinets-pagination .pagination-btn, #cabinets-pagination .pagination-size-btn',
        function(e) {
            e.preventDefault();
            const offset = $(this).data('offset') || 1;
            const limit = $(this).data('limit') || parseInt(getParam('limit', '15'));
            const name = $('#cabinet-search').val().trim();
            loadFragment(name, offset, limit);
        }
    );

    let searchTimer;
    $(document).on('input', '#cabinet-search', function() {
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
        $('#cabinet-search').val(name);
        $.get(window.location.pathname + '/fragment', { name, offset, limit })
            .done(function(html) {
                $('#cabinets-results').replaceWith(html);
            });
    });
});
