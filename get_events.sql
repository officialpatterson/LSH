SELECT
	thread, number_users, entropy
FROM
	(SELECT
		thread,
		number_users,
		entropy(thread, maxid, 'LSH1') as entropy,
		maxid
	FROM
		(SELECT max(id) as maxid,
			thread, 
			count(user_id) as number_users
			
		FROM 
			tweets_from_hour(4, 'LSH1')

		GROUP BY
			thread

		ORDER BY
			number_users DESC

		LIMIT 100) threads) as events

WHERE
	entropy >= 3.5

ORDER BY 
	number_users DESC
