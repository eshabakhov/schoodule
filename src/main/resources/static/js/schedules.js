$(() => {
    initEntityCrud({
        entity: 'schedules',
        container: '#schedules-container',
        createForm: '#create-schedule-form',
        editForm: '#edit-schedule-form',
        createModal: '#create-schedule',
        editModal: '#edit-schedule',
        openModalBtn: '#open-create-schedule'
    });
    $('#open-create-schedule-empty').on('click', function() {
        $('#open-create-schedule').trigger('click');
    });
});