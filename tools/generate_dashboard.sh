#!/bin/bash
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_FILE="$REPO_ROOT/DASHBOARD.md"

echo "# Project Dashboard" > "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "This dashboard lists the status of all submodules and the project structure." >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "## Project Structure" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "- **client/**: Contains the main game client source code, assets, and logic. Uses LWJGL 3." >> "$OUTPUT_FILE"
echo "- **server/**: Contains the dedicated game server code. Uses Netty 4." >> "$OUTPUT_FILE"
echo "- **shared/**: Contains shared data structures, networking packets, and utility classes used by both client and server." >> "$OUTPUT_FILE"
echo "- **libs/**: Contains external libraries used by the project. Some are included as Git submodules." >> "$OUTPUT_FILE"
echo "- **references/**: Contains Git submodules of external open-source projects (editors, engines) used for feature research and reference." >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "## Submodule Status" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "| Path | Branch | Commit | Date | Version/Build |" >> "$OUTPUT_FILE"
echo "| --- | --- | --- | --- | --- |" >> "$OUTPUT_FILE"

cd "$REPO_ROOT"
git submodule status | while read -r line; do
    [ -z "$line" ] && continue
    sha=$(echo "$line" | awk '{print $1}' | tr -d '+-U')
    path=$(echo "$line" | awk '{print $2}')

    name=$(git config -f .gitmodules --get-regexp path | grep "[[:space:]]$path$" | awk '{print $1}' | sed 's/\.path//')
    branch=""
    if [ -n "$name" ]; then
        branch=$(git config -f .gitmodules --get "submodule.$name.branch" || true)
    fi
    if [ -z "$branch" ]; then branch="HEAD"; fi

    date="unknown"
    version="unknown"

    if [ -d "$path/.git" ] || [ -f "$path/.git" ]; then
        date=$(git -C "$path" log -1 --format=%cd --date=short 2>/dev/null || echo "uninitialized")
        version=$(git -C "$path" describe --tags 2>/dev/null || true)
        if [ -z "$version" ]; then
            count=$(git -C "$path" rev-list --count HEAD 2>/dev/null || echo "0")
            version="build-$count"
        fi
    else
        date="uninitialized"
        version="uninitialized"
    fi

    echo "| $path | $branch | ${sha:0:7} | $date | $version |" >> "$OUTPUT_FILE"
done

echo "" >> "$OUTPUT_FILE"
echo "Generated on $(date -u)" >> "$OUTPUT_FILE"
