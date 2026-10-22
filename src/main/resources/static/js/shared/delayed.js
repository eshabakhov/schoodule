export const delayed = (timeout, action) => {
    let pending;
    return value => {
        clearTimeout(pending);
        pending = setTimeout(() => action(value), timeout);
    };
};
