SELECT subscription.plan
FROM public.subscription
WHERE subscription.user_id = ?
