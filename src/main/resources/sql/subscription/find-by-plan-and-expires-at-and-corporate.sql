SELECT subscription.user_id
FROM public.subscription
JOIN public."user" ON "user".id = subscription.user_id
WHERE ((((subscription.plan <> ?) AND (subscription.expires_at IS NOT NULL)) AND
(subscription.expires_at < CAST(? AS timestamptz))) AND ("user".corporate = ?)) AND ("user".deleted IS NULL)
