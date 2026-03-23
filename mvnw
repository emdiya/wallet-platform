#!/bin/sh
# Copied from module wrapper for root multi-module builds.
set -e
basedir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec "$basedir/auth-service/mvnw" -f "$basedir/pom.xml" "$@"
