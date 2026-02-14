#!/bin/sh
DIR="$(cd "$(dirname "$0")" && pwd)"
DB_USER=${DB_USER:-app_user}
DB_PASSWORD=${DB_PASSWORD:-student}
java -cp "$DIR/lib/postgresql.jar:$DIR" -DDB_USER="$DB_USER" -DDB_PASSWORD="$DB_PASSWORD" Main