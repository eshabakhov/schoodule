export const requirement = row => Object.freeze({
    edit: () => {
        row.addClass('req-editing');
        row.find('.req-grade-input').trigger('focus');
    },
    cancel: () => {
        row.find('.req-grade-input').val(
            row.find('.view-grade .req-cell-value').text().trim()
        );
        row.find('.req-subject-input').val(
            row.find('.view-subject .req-cell-value').text().trim()
        );
        row.find('.req-hours-input').val(
            row.find('.view-hours .req-cell-value').text().trim()
        );
        row.find('.req-part-input').val(
            row.find('.view-part .req-cell-value').text().trim()
        );
        row.removeClass('req-editing');
    },
    save: () => {
        const grade = row.find('.req-grade-input').val().trim();
        const subject = row.find('.req-subject-input').val().trim();
        const hours = row.find('.req-hours-input').val().trim();
        const part = row.find('.req-part-input').val();
        if (!grade || !subject || !hours || !part) {
            alert('Заполните все поля');
        } else {
            const form = row.find('.req-edit-form');
            form.find('.edit-grade').val(grade);
            form.find('.edit-subject').val(subject);
            form.find('.edit-hours').val(hours);
            form.find('.edit-part').val(part);
            form.trigger('submit');
        }
    }
});
