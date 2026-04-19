$(() => {
    initEntityCrud({
        entity: 'subjects',
        container: '#subjects-container',
        createForm: '#create-subject-form',
        editForm: '#edit-subject-form',
        createModal: '#create-subject',
        editModal: '#edit-subject',
        openModalBtn: '#open-create-subject',
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