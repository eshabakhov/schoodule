$(() => {
    const $registerForm = $('#register-form');
    const $errorMessage = $('#error-message');
    const showError = (message) => $errorMessage.text('❌ ' + message).prop('hidden', false);
    const hideError = () => $errorMessage.prop('hidden', true);
    $('.eye-btn').each((_, btn) => {
        $(btn).on('click', () => {
            const $btn = $(btn);
            const targetId = $btn.data('target');
            const $input = $('#' + targetId);
            const isPassword = $input.attr('type') === 'password';
            $input.attr('type', isPassword ? 'text' : 'password');
            $btn.find('.eye-off').toggleClass('is-hidden', isPassword);
            $btn.find('.eye-on').toggleClass('is-hidden', !isPassword);
            $btn.attr('aria-label', isPassword ? 'Скрыть пароль' : 'Показать пароль');
        });
    });
    $registerForm.find('input').on('input', hideError);
    $registerForm.on('submit', (e) => {
        hideError();
        const username = $.trim($registerForm.find('input[name="username"]').val());
        const email = $.trim($registerForm.find('input[name="email"]').val());
        const password = $registerForm.find('input[name="password"]').val();
        const confirmPassword = $registerForm.find('input[name="confirmPassword"]').val();
        if (username.length < 3) {
            e.preventDefault();
            showError('Логин должен содержать минимум 3 символа');
            return;
        }
        if (!email.includes('@')) {
            e.preventDefault();
            showError('Введите корректный email');
            return;
        }
        if (password.length < 8) {
            e.preventDefault();
            showError('Пароль должен содержать минимум 8 символов');
            return;
        }
        if (password !== confirmPassword) {
            e.preventDefault();
            showError('Пароли не совпадают');
            return;
        }
    });
});
