import { delayed } from './federal/delayed.js';
import { filters } from './federal/filters.js';
import { fragment, remembered } from './federal/fragment.js';
import { query } from './federal/query.js';

$(() => {
    const path = window.location.pathname;
    const fields = ['title'];
    const results = fragment('#federal-curriculums-results', `${path}/fragment`);
    const shown = remembered(results, path);
    const search = $('#federal-curriculum-search');
    search.val(filters(query(window.location.search), fields).value('title'));

    $(document).on(
        'click',
        '#federal-curriculums-container .federal-curriculum-row',
        function (event) {
            if (!$(event.target).closest('.card-item-actions').length) {
                window.location.href = `/federal/curriculums/${$(this).data('id')}`;
            }
        }
    );
    $(document).on(
        'click',
        '#federal-curriculums-pagination .pagination-btn, '
            + '#federal-curriculums-pagination .pagination-size-btn',
        function (event) {
            event.preventDefault();
            const current = query(window.location.search);
            shown.show(
                filters(current, fields)
                    .changed('title', search.val().trim())
                    .query()
                    .paged(
                        $(this).data('offset') || 1,
                        $(this).data('limit') || current.parameter('limit', '15')
                    )
            );
        }
    );
    const lookup = delayed(300, title => {
        const current = query(window.location.search);
        shown.show(
            filters(current, fields)
                .changed('title', title)
                .query()
                .paged(1, current.parameter('limit', '15'))
        );
    });
    $(document).on('input', '#federal-curriculum-search', function () {
        lookup($(this).val().trim());
    });
    window.addEventListener('popstate', () => {
        const current = query(window.location.search);
        search.val(filters(current, fields).value('title'));
        results.show(current);
    });
});
