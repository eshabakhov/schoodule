$(() => {
    $(document).on('submit', '#subscription-cancel', (e) => {
        if (!window.confirm(
            'Отменить подписку и перейти на Базовый план?'
        )) {
            e.preventDefault();
        }
    });
});
