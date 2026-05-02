/**
 * Модуль для редактирования отдельной сущности
 * cfg: { openBtn, modal, form, apiUrl }
 */
window.initEditEntity = function(cfg) {
    const { openBtn, modal, form, apiUrl } = cfg;
    if (!$(openBtn).length) return;
    const openModal = ($m) => $m.show();
    const closeModal = ($m) => $m.hide();
    $(openBtn).on('click', () => openModal($(modal)));
    $(`${modal} .close`).on('click', () => closeModal($(modal)));
    $(window).on('click', (e) => {
        if ($(e.target).is(modal)) closeModal($(modal));
    });
    $(form).on('submit', function(e) {
        e.preventDefault();
        const name = $.trim($(this).find('input[name="name"]').val());
        if (!name) return;
        $.ajax({
            url: apiUrl,
            method: 'PUT',
            contentType: 'application/json',
            headers: { version: 'SIMPLE' },
            data: JSON.stringify({ name })
        })
            .done(() => window.location.reload())
            .fail(() => alert('Не удалось обновить'));
    });
};