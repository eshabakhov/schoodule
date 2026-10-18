export const requirement = row => Object.freeze({
    edit: () => {
        row.addClass('requirement-editing');
        row.find('.requirement-grade-input').trigger('focus');
    },
    cancel: () => {
        row.find('.requirement-grade-input').val(
            row.find('.requirement-grade .requirement-value').text().trim()
        );
        row.find('.requirement-subject-input').val(
            row.find('.requirement-subject .requirement-value').text().trim()
        );
        row.find('.requirement-hours-input').val(
            row.find('.requirement-hours .requirement-value').text().trim()
        );
        row.find('.requirement-part-input').val(
            row.find('.requirement-part .requirement-value').text().trim()
        );
        row.removeClass('requirement-editing');
    },
    save: () => {
        const grade = row.find('.requirement-grade-input').val().trim();
        const subject = row.find('.requirement-subject-input').val().trim();
        const hours = row.find('.requirement-hours-input').val().trim();
        const part = row.find('.requirement-part-input').val();
        if (!grade || !subject || !hours || !part) {
            alert('Заполните все поля');
        } else {
            const form = row.find('.requirement-edit-form');
            form.find('.requirement-form-grade').val(grade);
            form.find('.requirement-form-subject').val(subject);
            form.find('.requirement-form-hours').val(hours);
            form.find('.requirement-form-part').val(part);
            form.trigger('submit');
        }
    }
});
