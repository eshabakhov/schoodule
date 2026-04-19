/**
 * CRUD для школ
 * API: /api/schools
 */
window.initSchoolCrud = function(cfg) {
    const {
        container,
        createForm,
        editForm,
        createModal,
        editModal,
        openModalBtn
    } = cfg;
    let editId = null;
    const modals = window.initModals ? window.initModals() : null;
    const openModal = modals ? modals.openModal : ($m) => $m.show();
    $(openModalBtn).on('click', () => openModal($(createModal)));
    $(createForm).on('submit', function(e) {
        e.preventDefault();
        const name = $.trim($(this).find('input[name="name"]').val());
        if (!name) return;

        $.ajax({
            url: '/api/schools',
            method: 'POST',
            contentType: 'application/json',
            headers: { version: 'SIMPLE' },
            data: JSON.stringify({ name })
        })
            .done(() => (window.location.href = '/schools'))
            .fail(() => alert('Не удалось создать школу'));
    });
    $(container).on('click', '.btn-delete-row', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        const name = $(this).data('name');
        if (!confirm(`Удалить «${name}»? Это действие нельзя отменить.`)) return;
        $.ajax({
            url: `/api/schools/${id}`,
            method: 'DELETE',
            headers: { version: 'SIMPLE' }
        })
            .done(() => (window.location.href = '/schools'))
            .fail(() => alert('Не удалось удалить школу'));
    });
    if (editForm && editModal) {
        $(container).on('click', '.btn-edit-row', function(e) {
            e.stopPropagation();
            editId = $(this).data('id');
            $(editForm).find('input[name="name"]').val($(this).data('name'));
            openModal($(editModal));
        });
        $(editForm).on('submit', function(e) {
            e.preventDefault();
            const name = $.trim($(this).find('input[name="name"]').val());
            if (!name || !editId) return;
            $.ajax({
                url: `/api/schools/${editId}`,
                method: 'PUT',
                contentType: 'application/json',
                headers: { version: 'SIMPLE' },
                data: JSON.stringify({ name })
            })
                .done(() => (window.location.href = '/schools'))
                .fail(() => alert('Не удалось обновить школу'));
        });
    }
};