INSERT INTO public.login_attempt (user_id, time, success)
VALUES (?, CAST(? AS timestamptz), ?)
