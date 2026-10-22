SELECT *
FROM public.cabinet
WHERE {0}
ORDER BY cabinet.name ASC
LIMIT {1}
OFFSET {2}
