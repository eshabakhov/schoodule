INSERT INTO public.user_role (user_id, role_id)
VALUES (?, ?)
ON CONFLICT (user_id, role_id) DO NOTHING
