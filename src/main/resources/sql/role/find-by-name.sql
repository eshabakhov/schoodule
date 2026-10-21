SELECT role.id
FROM public.role
WHERE role.name = CAST(? AS role_type)
