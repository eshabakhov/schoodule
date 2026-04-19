$(() => {
    initEntityCrud({
        entity: 'classes',
        container: '#school-classes-container',
        createForm: '#create-school-class-form',
        editForm: '#edit-school-class-form',
        createModal: '#create-school-class',
        editModal: '#edit-school-class',
        openModalBtn: '#open-create-school-class'
    });
    $('#entity-search').on('input', function() {
        const q = $(this).val().toLowerCase();
        $('.entity-row').each(function() {
            $(this).toggle($(this).data('name').includes(q));
        });
    });
});