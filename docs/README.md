# Demo project for the Postgres Listen/Notify mechanism

This repository is a small demo of the _Postgres Listen/Notify_ mechanism.
The repo is forked from the https://github.com/fyrkov/outbox-demo basic implementation.

## Notes

### Listen/Notify mechanism
PostgreSQL’s LISTEN/NOTIFY mechanism is often overlooked, 
yet it provides a powerful and efficient way to signal client applications directly from the database.
It allows the database to actively notify application services about state changes.

How does it work?

In this demo, a basic _outbox pattern_ implementation is used to demonstrate the mechanism.

A test `Consumer` component is generating new events and storing them to the `outbox` table.

A Postgres trigger is defined on the `outbox` table which fires on the insertion of new rows.
It sends a notification to the `outbox` PG channel with the payload of the `id` column of the inserted row:
```sql
pg_notify('outbox', new.id::text)
```

On the application side, a `PgListener` component opens a dedicated database connection and subscribes to the `outbox` channel::
```kotlin
dataSource.connection.use { conn ->
    conn.autoCommit = true
    conn.createStatement().use { st ->
        st.execute("listen outbox")
    }
}
```
When a notification is received, it is forwarded to the `Publisher` component, which triggers immediate processing of new outbox entries. 
As a result, scheduled polling of the `outbox` table on the publisher side can be reduced or disabled entirely.

#### Listen/Notify is a transactional mechanism.
It means that both `LISTEN` and `NOTIFY` take effect only after their respective transactions commit.
For that reason the `PgListener` sets `autoCommit=true` so that `st.execute("listen outbox")` statement commits immediately
and the listener connection becomes active right away.

In other words, `LISTEN` becomes active only after commit, and `NOTIFY` is delivered only when the sending transaction commits.

#### Reliability 


## How to run locally

### Dependencies

* JDK >= 21
* Docker

For running locally, start DB:

```bash
docker compose up -d
```

Fixed port 15433 is used which must be available!

Start the app:

```
./gradlew bootRun
```

## Data model

| Column           | Type           | Nullable | Description                                   |
|:-----------------|:---------------|:---------|:----------------------------------------------|
| `id`             | `bigint`       | No       | Primary key, auto-generated                   |
| `aggregate_type` | `varchar(255)` | No       | Type of the aggregate                         |
| `aggregate_id`   | `varchar(255)` | No       | ID of the aggregate                           |
| `payload`        | `jsonb`        | No       | Event data in JSON format                     |
| `created_at`     | `timestamptz`  | No       | Creation timestamp                            |
| `published_at`   | `timestamptz`  | Yes      | Publication timestamp (NULL if not published) |


## Links
* https://www.postgresql.org/docs/current/sql-notify.html
* https://www.postgresql.org/docs/current/sql-listen.html