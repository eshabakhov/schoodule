export const query = source => {
    const params = new URLSearchParams(source);
    return Object.freeze({
        parameter: (name, fallback) => params.get(name) || fallback,
        values: name => params.getAll(name),
        replaced: (name, values) => {
            const changed = new URLSearchParams(params);
            changed.delete(name);
            values.forEach(value => {
                changed.append(name, value);
            });
            return query(changed);
        },
        paged: (offset, limit) => {
            const changed = new URLSearchParams(params);
            changed.set('offset', offset);
            changed.set('limit', limit);
            return query(changed);
        },
        text: () => params.toString()
    });
};
