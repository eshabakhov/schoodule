import { confirmed, removal } from './shared/removal.js';

$(() => {
    $(document).on('click', '.btn-delete-curriculum', function () {
        confirmed(
            `Удалить «${$(this).data('name')}»?`,
            removal(
                `/federal/curriculums/${$(this).data('id')}`,
                () => {
                    window.location.href = '/federal/curriculums';
                },
                () => alert('Не удалось удалить федеральный учебный план')
            )
        ).execute();
    });
});
