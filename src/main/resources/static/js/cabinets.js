$(() => {
    initEntityCrud({
        entity: 'cabinets',
        container: '#cabinets-container',
        createForm: '#create-cabinet-form',
        editForm: '#edit-cabinet-form',
        createModal: '#create-cabinet',
        editModal: '#edit-cabinet',
        openModalBtn: '#open-create-cabinet'
    });
    $('#entity-search').on('input', function() {
        const q = $(this).val().toLowerCase();
        $('.entity-row').each(function() {
            $(this).toggle($(this).data('name').includes(q));
        });
    });
});