$(() => {
    $('.btn-cancel-sub').on('click', (e) => {
        if (!window.confirm(
            'Отменить подписку и перейти на Базовый план?'
        )) {
            e.preventDefault();
        }
    });
});
