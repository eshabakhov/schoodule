(() => {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    const token = tokenMeta ? tokenMeta.content : '';
    const header = headerMeta ? headerMeta.content : '';
    window.schooduleSecurity = {
        csrfToken: token,
        csrfHeaderName: header
    };
    if (!window.jQuery || !token || !header) {
        return;
    }
    $.ajaxSetup({
        beforeSend(xhr, settings) {
            const method = (settings.type || settings.method || 'GET').toUpperCase();
            if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
                xhr.setRequestHeader(header, token);
            }
        }
    });
})();
