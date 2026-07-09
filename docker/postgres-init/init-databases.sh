#!/bin/bash
# Containerul oficial postgres creeaza automat o singura baza (POSTGRES_DB) -
# celelalte doua trebuie create explicit aici, la prima pornire.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE sales_db;
    CREATE DATABASE user_db;
EOSQL
