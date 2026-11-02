#!/bin/bash
# Creates (if needed) a GitHub repository and pushes this repo's `main`.
# Credentials are read from the environment and never written to .git/config.
#
#   GITHUB_TOKEN=ghp_xxx GITHUB_OWNER=your-login GITHUB_REPO=odyssey-mythic-arena \
#   GITHUB_VISIBILITY=private ./scripts/publish-github.sh
set -euo pipefail
: "${GITHUB_TOKEN:?set GITHUB_TOKEN (repo/content:write)}"
: "${GITHUB_OWNER:?set GITHUB_OWNER}"
REPO="${GITHUB_REPO:-odyssey-mythic-arena}"
VIS="${GITHUB_VISIBILITY:-private}"
CREATE="${GITHUB_CREATE:-1}"

echo "[1/3] create repo $GITHUB_OWNER/$REPO ($VIS)"
if [ "$CREATE" = "0" ]; then
  echo "  skipped (GITHUB_CREATE=0, repo must already exist)"
else
code=$(curl -sS -o /tmp/gh-create.json -w '%{http_code}' -X POST \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/user/repos \
  -d "{\"name\":\"$REPO\",\"private\":$([ "$VIS" = private ] && echo true || echo false),\"description\":\"ODYSSEY · MYTHIC ARENA — buildable Compose home screen + APK build pipeline\"}")
if [ "$code" = 201 ]; then echo "  created"; else
  echo "  HTTP $code (already exists is fine)"; sed -n '1,6p' /tmp/gh-create.json; fi
fi

echo "[2/3] push main"
# Credentials go in a one-off http header so the token never lands in .git/config
# or in the URL printed by git.
AUTH="$(printf 'x-access-token:%s' "$GITHUB_TOKEN" | base64 -w0)"
git -c "http.extraHeader=AUTHORIZATION: basic ${AUTH}" push \
  "https://github.com/${GITHUB_OWNER}/${REPO}.git" main:main

echo "[3/3] set clean origin"
git remote remove origin 2>/dev/null || true
git remote add origin "https://github.com/${GITHUB_OWNER}/${REPO}.git"
git branch --set-upstream-to="origin/main" main 2>/dev/null || true
echo "done: https://github.com/${GITHUB_OWNER}/${REPO}"
