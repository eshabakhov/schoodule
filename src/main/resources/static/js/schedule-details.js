$(() => {
    const { openModal } = window.initModals();
    const path = window.location.pathname;
    $('.tab-trigger').each(function () {
        const href = $(this).attr('href');
        if (href && path.endsWith(href.split('/').pop())) {
            $(this).addClass('active');
        }
    });
    $('#btn-edit-schedule').on('click', function () {
        const id   = $(this).data('id');
        const name = $(this).data('name');
        $('#edit-schedule-form').find('input[name="name"]').val(name);
        $('#edit-schedule-form').data('edit-id', id);
        openModal($('#edit-schedule'));
    });
    $('#edit-schedule-form').on('submit', function (e) {
        e.preventDefault();
        const id   = $(this).data('edit-id');
        const name = $.trim($(this).find('input[name="name"]').val());
        if (!name || !id) return;

        const parts    = window.location.pathname.split('/').filter(Boolean);
        const schoolId = parts[parts.indexOf('schools') + 1];

        $.ajax({
            url: `/api/schools/${schoolId}/schedules/${id}`,
            method: 'PUT',
            contentType: 'application/json',
            headers: { version: 'SIMPLE' },
            data: JSON.stringify({ name }),
        })
            .done(() => window.location.reload())
            .fail(() => alert('Не удалось обновить расписание'));
    });
});