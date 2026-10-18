export const sorts = (origin, fields) => {
    const selected = new Map();
    origin.values('sort').forEach(value => {
        const separator = value.indexOf(':');
        const name = value.substring(0, separator);
        const direction = value.substring(separator + 1).toLowerCase();
        if (
            separator > 0
                && fields.includes(name)
                && ['asc', 'desc'].includes(direction)
        ) {
            selected.set(name, direction);
        }
    });
    return Object.freeze({
        direction: name => selected.get(name) || '',
        cycled: name => {
            const changed = new Map(selected);
            if (!changed.has(name)) {
                changed.set(name, 'asc');
            } else if (changed.get(name) === 'asc') {
                changed.set(name, 'desc');
            } else {
                changed.delete(name);
            }
            return sorts(
                origin.replaced(
                    'sort',
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
