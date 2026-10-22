SELECT *
FROM public."user"
WHERE ("user".deleted IS NULL) AND (("user".username = ?) OR ("user".email = ?))
