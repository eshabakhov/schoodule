UPDATE public."user"
SET deleted = CAST(? AS timestamptz)
WHERE "user".id = ?
