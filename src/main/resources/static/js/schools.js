$(() => {
    $(document).on('click', '#schools-container .school-row', function(e) {
        if ($(e.target).closest('.school-list-item-actions').length) return;
        const id = $(this).data('id');
        window.location.href = '/schools/' + id;
    });
    $(document).on('click', '.btn-delete-row', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        const name = $(this).data('name');
        if (!confirm(`Удалить «${name}»? Это действие нельзя отменить.`)) return;
        $.ajax({
            url: `/api/schools/${id}`,
            method: 'DELETE',
            headers: { version: 'SIMPLE' }
        })
          .done(() => (window.location.href = '/schools'))
          .fail(() => alert('Не удалось удалить школу'));
    });
    function getParam(key, fallback) {
        return new URLSearchParams(window.location.search).get(key) || fallback;
    }
    function updateUrl(name, offset, limit) {
        const params = new URLSearchParams();
        if (name) params.set('name', name);
        params.set('offset', offset);
        params.set('limit', limit);
        history.pushState(null, '', '/schools?' + params.toString());
    }
    function loadFragment(name, offset, limit) {
        updateUrl(name, offset, limit);
        $.get('/schools/fragment', { name: name, offset: offset, limit: limit })
          .done(function(html) {
              $('#schools-results').replaceWith(html);
          });
    }
    $(document).on('click', '#schools-pagination .pagination-btn, #schools-pagination .pagination-size-btn', function(e) {
        e.preventDefault();
        const offset = $(this).data('offset') || 1;
        const limit = $(this).data('limit') || parseInt(getParam('limit', '15'));
        const name = $('#school-search').val().trim();
        loadFragment(name, offset, limit);
    });
    let searchTimer;
    $('#school-search').on('input', function() {
        clearTimeout(searchTimer);
        const q = $(this).val().trim();
        searchTimer = setTimeout(() => {
            loadFragment(q, 1, parseInt(getParam('limit', '15')));
        }, 300);
    });
    window.addEventListener('popstate', function() {
        const name = getParam('name', '');
        const offset = getParam('offset', '1');
        const limit = getParam('limit', '15');
        $('#school-search').val(name);
        $.get('/schools/fragment', { name: name, offset: offset, limit: limit })
          .done(function(html) {
              $('#schools-results').replaceWith(html);
          });
    });
});