$(() => {
    const pathParts = window.location.pathname.split('/').filter(Boolean);
    const schoolId = pathParts[pathParts.indexOf('schools') + 1];
    initEditEntity({
        openBtn: '#edit-school-btn',
        modal: '#edit-school',
        form: '#edit-school-form',
        apiUrl: `/api/schools/${schoolId}`
    });
});
