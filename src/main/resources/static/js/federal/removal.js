export const removal = (uri, succeeded, failed) => Object.freeze({
    execute: () => {
        $.ajax({
            url: uri,
            method: 'DELETE'
        }).done(succeeded).fail(failed);
    }
});

export const confirmed = (message, origin) => Object.freeze({
    execute: () => {
        if (confirm(message)) {
            origin.execute();
        }
    }
});
