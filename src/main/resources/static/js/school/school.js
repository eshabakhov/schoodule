    import { confirmed, removal } from '../shared/removal.js';

$(() => {
    $(document).on('click', '.btn-delete-school', function () {
        confirmed(
            `Удалить «${$(this).data('name')}»?`,
            removal(
                `/schools/${$(this).data('id')}`,
                () => {
                    window.location.href = '/schools';
                },
                () => alert('Не удалось удалить школу')
            )
        ).execute();
    });
});
