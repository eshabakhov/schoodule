/**
 * Универсальный CRUD-модуль для сущностей школы.
 * cfg:
 *   entity        — сегмент URL (cabinets / teachers / classes / subjects / schedules)
 *   container     — селектор контейнера со строками
 *   createForm    — селектор формы создания
 *   editForm      — селектор формы редактирования
 *   createModal   — селектор модала создания
 *   editModal     — селектор модала редактирования
 *   openModalBtn  — селектор кнопки открытия модала создания
 */
window.initEntityCrud = function(cfg) {
    const {
        entity,
        container,
        createForm,
        editForm,
        createModal,
        editModal,
        openModalBtn
    } = cfg;
    function getSchoolId() {
        const parts = window.location.pathname.split('/').filter(Boolean);
        const idx = parts.indexOf('schools');
        return idx >= 0 ? parts[idx + 1] : null;
    }
    function apiBase() {
        const school = getSchoolId();
        return `/api/schools/${school}/${entity}`;
    }
    function redirectToList() {
        const school = getSchoolId();
        window.location.href = `/schools/${school}/${entity}`;
    }
    let editId = null;
    const modals = window.initModals ? window.initModals() : null;
    const openModal = modals ? modals.openModal : ($m) => $m.show();
    /*const closeModal = modals ? modals.closeModal : ($m) => $m.hide();*/
    $(openModalBtn).on('click', () => openModal($(createModal)));
    $(createForm).on('submit', function(e) {
        e.preventDefault();
        const name = $.trim($(this).find('input[name="name"]').val());
        if (!name) return;
        $.ajax({
            url: apiBase(),
            method: 'POST',
            contentType: 'application/json',
            headers: { version: 'SIMPLE' },
            data: JSON.stringify({ name })
        })
            .done(() => redirectToList())
            .fail(() => alert('Не удалось создать запись'));
    });
    $(container).on('click', '.btn-delete-row, .delete-card', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        const name = $(this).data('name');
        if (!confirm(`Удалить «${name}»? Это действие нельзя отменить.`)) return;
        $.ajax({
            url: `${apiBase()}/${id}`,
            method: 'DELETE',
            headers: { version: 'SIMPLE' }
        })
            .done(() => redirectToList())
            .fail(() => alert('Не удалось удалить запись'));
    });
    if (editForm && editModal) {
        $(container).on('click', '.btn-edit-row', function(e) {
            e.stopPropagation();
            editId = $(this).data('id');
            $(editForm).find('input[name="name"]').val($(this).data('name'));
            openModal($(editModal));
        });
        $(container).on('dblclick', '.card', function() {
            editId = $(this).data('id');
            $(editForm).find('input[name="name"]').val($(this).find('.card-name').text());
            openModal($(editModal));
        });
        $(editForm).on('submit', function(e) {
            e.preventDefault();
            const name = $.trim($(this).find('input[name="name"]').val());
            if (!name || !editId) return;
            $.ajax({
                url: `${apiBase()}/${editId}`,
                method: 'PUT',
                contentType: 'application/json',
                headers: { version: 'SIMPLE' },
                data: JSON.stringify({ name })
            })
                .done(() => redirectToList())
                .fail(() => alert('Не удалось обновить запись'));
        });
    }
};