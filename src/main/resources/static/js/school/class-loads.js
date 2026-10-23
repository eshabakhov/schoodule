import { fragment, remembered } from '../shared/fragment.js';
import { query } from '../shared/query.js';
import { confirmed, removal } from '../shared/removal.js';

$(() => {
    const dialog = document.querySelector('#load-create-dialog');
    if (dialog) {
        $(document).on('click', '[data-open-load-dialog]', event => {
            event.preventDefault();
            dialog.showModal();
        });
        $(document).on('click', '[data-close-load-dialog]', () => { dialog.close(); });
    }
    if ($('#loads-results').length) {
        const base = window.location.pathname.replace(/\/+$/, '');
        const results = fragment('#loads-results', `${base}/fragment`);
        const shown = remembered(results, base);
        $(document).on(
            'click',
            '#loads-pagination .pagination-btn, #loads-pagination .pagination-size-btn',
            function (event) {
                event.preventDefault();
                const current = query(window.location.search);
                shown.show(current.paged(
                    $(this).data('offset') || 1,
                    $(this).data('limit') || current.parameter('limit', '15')
                ));
            }
        );
        window.addEventListener('popstate', () => { results.show(query(window.location.search)); });
    }
    $(document).on('click', '.load-delete', function () {
        const button = $(this);
        confirmed(
            `Удалить нагрузку «${button.data('name')}»?`,
            removal(
                button.data('uri'),
                () => { window.location.href = button.data('redirect'); },
                () => alert('Не удалось удалить нагрузку')
            )
        ).execute();
    });
});
