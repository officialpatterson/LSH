CREATE TABLE tweet (
    id bigint,
    text text,
    user_id bigint,
    created_at bigint,
    run text,
    thread bigint,
    tokens text
);


ALTER TABLE public.tweet OWNER TO lsh;


CREATE FUNCTION tweets_from_hour(parm1 integer, parm2 text) RETURNS TABLE(id bigint, text text, user_id bigint, created_at bigint, thread bigint, tokens text)
    LANGUAGE sql
    AS $_$
SELECT 
id, text, user_id, created_at, thread, tokens
FROM 
tweet,
(SELECT t FROM (SELECT DISTINCT (created_at - (created_at % 3600000)) / 1000 as t FROM tweet) as times LIMIT 1 OFFSET $1) as d
WHERE 
run = $2
AND (created_at - (created_at % 3600000)) / 1000 = t

ORDER BY id
$_$;


ALTER FUNCTION public.tweets_from_hour(parm1 integer, parm2 text) OWNER TO lsh;

CREATE FUNCTION entropy(bigint, bigint, text, OUT f1 double precision) RETURNS double precision
    LANGUAGE sql
    AS $_$ 
SELECT 
        SUM(log)
FROM
        (SELECT 
                regexp_split_to_table(tokens, E'\\s+') as token, 
                count(*) as co,
                (-1 * (count(tokens)::float/N) * ln(count(tokens)::float/N)) as log
        FROM 
                tweet, 
                (SELECT count(token) as N FROM (SELECT regexp_split_to_table(tokens, E'\\s+') as token FROM tweet WHERE thread = $1 AND run = $3 AND id <= $2) as a) c
        WHERE
                thread = $1
                AND run = $3
                AND id <= $2
        GROUP BY 
                token, n) logs

$_$;


ALTER FUNCTION public.entropy(bigint, bigint, text, OUT f1 double precision) OWNER TO lsh;