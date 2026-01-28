# Postgres Listen/Notify mechanism
![image.png](image.png)

## Listen/Notify mechanism
PostgreSQL’s LISTEN/NOTIFY mechanism is often overlooked, yet it provides an efficient way for the database to emit signals to connected client applications, 
allowing it to take an active role rather than acting solely as a passive data store.

How does it work?

In this demo, we will use the basic _outbox pattern_ implementation to demonstrate the setup.
In short, the service consumes events, persists them in the database, and publishes them further downstream.

A mock `Consumer` component is generating new events and storing them to the `outbox` table.

A Postgres trigger is defined on the `outbox` table which fires on the insertion of new rows.
It sends a notification to the `outbox` channel with the payload of the `id` column of the inserted row:
```sql
create or replace function notify_outbox_insert()
    returns trigger
    language plpgsql
as $$
begin
    perform pg_notify('outbox', new.id::text);
    return new;
end;
$$;

create trigger outbox_insert_notify
    after insert on outbox_unpublished
    for each row
execute function notify_outbox_insert();
```

On the application side, a `PgListener` component opens a dedicated database connection and subscribes to the same `outbox` channel:
```kotlin
dataSource.connection.use { conn ->
    conn.autoCommit = true
    conn.createStatement().use { st ->
        st.execute("listen outbox")
    }
    ...
    val pg = conn.unwrap(org.postgresql.PGConnection::class.java)
    val notifications = pg.getNotifications(0)
}
```
When a notification is received, it is forwarded to the `Publisher` component, which triggers immediate processing of new outbox entries. 
As a result, scheduled polling of the `outbox` table on the publisher side can be reduced or disabled entirely.

### Listen/Notify is a transactional mechanism.
It means that both `LISTEN` and `NOTIFY` take effect only after their respective transactions commit.
For that reason the `PgListener` sets `autoCommit=true` so that `st.execute("listen outbox")` statement commits immediately
and the listener becomes active right away.

In other words, `LISTEN` becomes active only after commit, and `NOTIFY` is delivered only when the sending transaction commits.

### Reliability
The LISTEN/NOTIFY mechanism has certain limitations:
* notifications are not durable and will be lost if listeners are offline
* no replay
* no acknowledgements
* no exactly-once / at-least-once delivery guarantees
* no backpressure or flow control
* payloads are limited to approximately 8 KB

Given these limitations, the LISTEN/NOTIFY mechanism is not a drop-in replacement for polling in the production outbox, 
but it can be safely used as an additional signaling mechanism.

In other words, Listen/Notify provides **best-effort, non-durable signaling** rather than **reliable message delivery**.

### Applications scenarios
Other good scenarios for using Listen/Notify may include:

* wake-up signals ("new work available")
* cache invalidation
* reducing polling latency (the Outbox pattern case)
* coordination between DB-backed components

## Links
* Repository with the demo code: https://github.com/fyrkov/postgres-listen-notify-demo
* Postgres documentation:
  * https://www.postgresql.org/docs/current/sql-notify.html
  * https://www.postgresql.org/docs/current/sql-listen.html