$(() => {
    const $registerForm = $('#register-form');
    const $errorMessage = $('#error-message');
    const showError = (message) => $errorMessage.text('❌ ' + message).show();
    const hideError = () => $errorMessage.hide();
    $('.eye-btn').each((_, btn) => {
        $(btn).on('click', () => {
            const $btn = $(btn);
            const targetId = $btn.data('target');
            const $input = $('#' + targetId);
            const isPassword = $input.attr('type') === 'password';
            $input.attr('type', isPassword ? 'text' : 'password');
            $btn.find('.eye-off').css('display', isPassword ? 'none' : '');
            $btn.find('.eye-on').css('display', isPassword ? '' : 'none');
            $btn.attr('aria-label', isPassword ? 'Скрыть пароль' : 'Показать пароль');
        });
    });
    $registerForm.find('input').on('input', hideError);
    $registerForm.on('submit', (e) => {
        e.preventDefault();
        hideError();
        const username = $.trim($registerForm.find('input[name="username"]').val());
        const email = $.trim($registerForm.find('input[name="email"]').val());
        const password = $registerForm.find('input[name="password"]').val();
        const confirmPassword = $registerForm.find('input[name="confirmPassword"]').val();
        if (username.length < 3) {
            showError('Логин должен содержать минимум 3 символа');
            return;
        }
        if (!email.includes('@')) {
            showError('Введите корректный email');
            return;
        }
        if (password.length < 4) {
            showError('Пароль должен содержать минимум 4 символа');
            return;
        }
        if (password !== confirmPassword) {
            showError('Пароли не совпадают');
            return;
        }
        $.ajax({
            url: '/api/users',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ username, email, password })
        })
            .done(() => {
                window.location.href = '/users/login?registered';
            })
            .fail((xhr) => {
                showError(xhr.responseJSON?.message || 'Ошибка регистрации. Попробуйте позже');
            });
    });
});