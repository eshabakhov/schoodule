SELECT *
FROM public.subject
WHERE {0}
ORDER BY subject.name ASC
LIMIT {1}
OFFSET {2}
