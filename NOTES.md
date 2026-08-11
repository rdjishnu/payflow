
## Day 8 — Auth 500 error: real root cause found

Not a Postgres schema permissions bug (that was a red herring chased
Days 1-7). Root cause: a native Homebrew postgresql@18 install was
bound to 127.0.0.1:5432, silently intercepting all localhost:5432
connections meant for the Docker Postgres container. The app was
never actually talking to the intended database.

Fix: brew services stop postgresql@18
Confirmed not set to autostart (brew services list -> status "none").

Verified working end-to-end:
- POST /auth/register -> 200 + JWT
- POST /auth/login -> 200 + JWT

If schema permission errors resurface, check for a port collision
first: lsof -nP -iTCP:5432 -sTCP:LISTEN
