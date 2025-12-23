#!/bin/sh
SERVER_TYPE=${SERVER_TYPE:-game}

if [ "$SERVER_TYPE" = "game" ]; then
    echo "Starting Game Server..."
    exec ./bin/bobsgame-server
elif [ "$SERVER_TYPE" = "stun" ]; then
    echo "Starting STUN Server..."
    exec ./bin/bobsgame-stun-server
else
    echo "Unknown SERVER_TYPE: $SERVER_TYPE"
    exit 1
fi
