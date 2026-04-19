$(() => {
    initEntityCrud({
        entity: 'teachers',
        container: '#teachers-container',
        createForm: '#create-teacher-form',
        editForm: '#edit-teacher-form',
        createModal: '#create-teacher',
        editModal: '#edit-teacher',
        openModalBtn: '#open-create-teacher',
        base: '/schools',
        apiBase: '/api/schools'
    });
    $('#entity-search').on('input', function() {
        const q = $(this).val().toLowerCase();
        $('.entity-row').each(function() {
            $(this).toggle($(this).data('name').includes(q));
        });
    });
});