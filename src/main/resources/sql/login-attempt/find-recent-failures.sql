SELECT *
FROM public.login_attempt
WHERE (((login_attempt.user_id = ?) AND (login_attempt.time >= CAST(? AS timestamptz))) AND
(login_attempt.time <= CAST(? AS timestamptz))) AND (login_attempt.success = ?)
