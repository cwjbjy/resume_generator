#!/bin/sh
# Generate production .env with random secrets. Safe to re-run: refuses to overwrite.
# Usage (on the server, in the directory containing compose.prod.yml): sh init-env.sh
set -e
if [ -f .env ]; then
  echo '.env already exists; nothing changed.'
  exit 0
fi
umask 077
cat > .env <<EOF
APP_PROFILE=dev
DB_PASSWORD=$(openssl rand -base64 32)
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 32)
REDIS_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 32)
PAYMENT_SECRET=$(openssl rand -base64 32)
EOF
echo 'Created .env with random secrets.'
echo 'Switch to prod when going live: edit APP_PROFILE in .env, then docker compose -f compose.prod.yml up -d backend'
