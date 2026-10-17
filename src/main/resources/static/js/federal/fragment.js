export const fragment = (target, uri) => Object.freeze({
    show: state => {
        $.get(`${uri}?${state.text()}`).done(html => {
            $(target).replaceWith(html);
        });
    }
});

export const remembered = (origin, uri) => Object.freeze({
    show: state => {
        history.pushState(null, '', `${uri}?${state.text()}`);
        origin.show(state);
    }
});
