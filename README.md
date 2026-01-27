# Demo project for the Postgres listen-notify mechanism

This repository is a small demo of the _Postgres listen-notify_ mechanism.
The repo is forked from the https://github.com/fyrkov/outbox-demo basic implementation.

## Notes

### Listen/Notify mechanism
Postgres is capable of establishing a channel for exchanging messages between clients and DB.

In this demo, a basic _outbox pattern_ implementation is used to demonstrate the mechanism.

There is a PG trigger on the `outbox` table which triggers on the insertion of new rows.
It sends a notification to the `outbox` channel with the payload of the `id` column of the inserted row.

A `PgListener` component of the app opens a connection and listens for notifications from the `outbox` channel.
Once it receives a notification, it forwards it to the Publisher component.
As a result, it is possible to disable scheduled polling of the `outbox` table on the service side.

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