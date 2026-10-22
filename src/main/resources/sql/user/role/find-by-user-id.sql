SELECT user_role.id, user_role.user_id, role.name
FROM public.user_role
JOIN public.role ON user_role.role_id = role.id
WHERE user_role.user_id = ?
