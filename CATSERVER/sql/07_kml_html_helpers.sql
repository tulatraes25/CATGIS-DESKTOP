\set ON_ERROR_STOP on

CREATE OR REPLACE FUNCTION admin.kml_desc_value(desc_html text, label text)
RETURNS text
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
AS $$
    WITH rows AS (
        SELECT btrim(row_html) AS row_html
        FROM regexp_split_to_table(
            coalesce(desc_html, ''),
            '(?is)</?tr[^>]*>'
        ) AS row_html
    ),
    pairs AS (
        SELECT
            m[1] AS key_name,
            regexp_replace(m[2], '<[^>]+>', '', 'g') AS raw_value
        FROM rows
        CROSS JOIN LATERAL regexp_match(
            row_html,
            '(?is)<td>([^<]+)</td>\s*<td>(.*)</td>'
        ) AS m
        WHERE row_html ~* '<td>[^<]+</td>\s*<td>'
    )
    SELECT nullif(btrim(replace(raw_value, '&nbsp;', ' ')), '')
    FROM pairs
    WHERE key_name = label
    LIMIT 1;
$$;

CREATE OR REPLACE FUNCTION admin.kml_desc_num(desc_html text, label text)
RETURNS numeric
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
AS $$
    SELECT CASE
        WHEN admin.kml_desc_value(desc_html, label) IS NULL THEN NULL
        ELSE nullif(
            replace(
                replace(
                    regexp_replace(admin.kml_desc_value(desc_html, label), '[^0-9,.-]', '', 'g'),
                    '.',
                    ''
                ),
                ',',
                '.'
            ),
            ''
        )::numeric
    END;
$$;

CREATE OR REPLACE FUNCTION admin.kml_desc_int(desc_html text, label text)
RETURNS bigint
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
AS $$
    SELECT CASE
        WHEN admin.kml_desc_value(desc_html, label) IS NULL THEN NULL
        ELSE nullif(regexp_replace(admin.kml_desc_value(desc_html, label), '[^0-9-]', '', 'g'), '')::bigint
    END;
$$;
