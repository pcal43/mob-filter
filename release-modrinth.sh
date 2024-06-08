#!/bin/sh

#
# Publish to modrinth.
#

set -eu

if [ -n "$(git status --porcelain)" ]; then
  echo "Working directory not clean, cannot release"
  exit 1
fi

if [ -z "${MODRINTH_TOKEN:-}" ]; then
    echo "Set MODRINTH_TOKEN"
    exit 1
fi

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "${CURRENT_BRANCH}" != 'backports/1.20.1' ]; then
  echo "Releases must be performed on backports/1.20.1.  Currently on '${CURRENT_BRANCH}'"
  exit 1
fi
#
# Do modrinth release
#
./gradlew modrinth
