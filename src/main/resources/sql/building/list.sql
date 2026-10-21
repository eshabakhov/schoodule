SELECT *
FROM public.building
WHERE {0}
ORDER BY building.name ASC
LIMIT {1}
OFFSET {2}
