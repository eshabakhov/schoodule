import { delayed } from './federal/delayed.js';
import { filters } from './federal/filters.js';
import { fragment, remembered } from './federal/fragment.js';
import { query } from './federal/query.js';
import { confirmed, removal } from './federal/removal.js';
import { requirement } from './federal/requirement.js';
import { sorts } from './federal/sorts.js';

$(() => {
    const path = window.location.pathname;
    const filterFields = ['grade', 'subject', 'part'];
    const sortFields = ['grade', 'subject', 'hours', 'part'];
    const results = fragment('#req-results', $('#req-tbody').data('search-url'));
    const shown = remembered(results, path);
    const restored = filters(query(window.location.search), filterFields);
    $('#filter-grade').val(restored.value('grade'));
    $('#filter-subject').val(restored.value('subject'));
    $('#filter-part').val(restored.value('part'));

    const filtered = (name, value) => {
        const current = query(window.location.search);
        shown.show(
            filters(current, filterFields)
                .changed(name, value)
                .query()
                .paged(1, current.parameter('limit', '15'))
        );
    };
    const grade = delayed(250, value => filtered('grade', value));
    const subject = delayed(250, value => filtered('subject', value));

    $(document).on(
        'click',
        '#req-pagination .pagination-btn, #req-pagination .pagination-size-btn',
        function (event) {
            event.preventDefault();
            const current = query(window.location.search);
            shown.show(
                current.paged(
                    $(this).data('offset') || 1,
                    $(this).data('limit') || current.parameter('limit', '15')
                )
            );
        }
    );
    $(document).on('click', '.req-sort-btn', function () {
        const current = query(window.location.search);
        shown.show(
            sorts(current, sortFields)
                .cycled($(this).data('sort'))
                .query()
                .paged(1, current.parameter('limit', '15'))
        );
    });
    $('#filter-grade').on('input', function () {
        grade($(this).val().trim());
    });
    $('#filter-subject').on('input', function () {
        subject($(this).val().trim());
    });
    $('#filter-part').on('change', function () {
        filtered('part', $(this).val());
    });
    window.addEventListener('popstate', () => {
        const current = query(window.location.search);
        const currentFilters = filters(current, filterFields);
        $('#filter-grade').val(currentFilters.value('grade'));
        $('#filter-subject').val(currentFilters.value('subject'));
        $('#filter-part').val(currentFilters.value('part'));
        results.show(current);
    });
    $(document).on('click', '.req-row-edit', function () {
        $('#req-tbody tr.req-editing').each(function () {
            requirement($(this)).cancel();
        });
        const editable = requirement($(this).closest('tr'));
        editable.cancel();
        editable.edit();
    });
    $(document).on('click', '.req-row-cancel', function () {
        requirement($(this).closest('tr')).cancel();
    });
    $(document).on('click', '.req-row-save', function () {
        requirement($(this).closest('tr')).save();
    });
    $(document).on('click', '.btn-delete-requirement', function () {
        confirmed(
            `Удалить требование «${$(this).data('name')}»?`,
            removal(
                `/federal/curriculums/${$(this).data('curriculum')}`
                    + `/requirements/${$(this).data('id')}`,
                () => window.location.reload(),
                () => alert('Не удалось удалить требование')
            )
        ).execute();
    });
});
