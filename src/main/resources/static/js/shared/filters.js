export const filters = (origin, fields) => {
    const selected = new Map();
    origin.values('filter').forEach(value => {
        const separator = value.indexOf(':');
        const name = value.substring(0, separator);
        if (separator > 0 && fields.includes(name)) {
            selected.set(name, value.substring(separator + 1));
        }
    });
    return Object.freeze({
        value: name => selected.get(name) || '',
        changed: (name, value) => {
            const changed = new Map(selected);
            if (value) {
                changed.set(name, value);
            } else {
                changed.delete(name);
            }
            return filters(
                origin.replaced(
                    'filter',
                    fields
                        .filter(field => changed.has(field))
                        .map(field => `${field}:${changed.get(field)}`)
                ),
                fields
            );
        },
        query: () => origin
    });
};
