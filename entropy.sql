SELECT 
	SUM(log)
FROM
	(SELECT 
		regexp_split_to_table(tokens, E'\\s+') as token, 
		count(*) as co,
		(-1 * (count(tokens)::float/N) * ln(count(tokens)::float/N)) as log
		
	FROM 
		tweet, 
		(SELECT count(token) as N FROM (SELECT regexp_split_to_table(tokens, E'\\s+') as token FROM tweet WHERE thread = 10) as a) c
		
	WHERE
		thread = 94
		
	GROUP BY 
		token, n) logs
