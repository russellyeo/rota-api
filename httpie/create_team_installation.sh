#!/bin/bash

source "$(dirname "$0")/utils.sh"

team_id="TUABC123"
team_installation='{
  "team": {
      "id": "'$team_id'",
      "name": "Russell"
  },
  "user": {
      "id": "UUABC123"
  },
  "tokenType": "bot",
  "isEnterpriseInstall": false,
  "appId": "A012345",
  "authVersion": "v2",
  "bot": {
      "scopes": [
          "app_mentions:read",
          "chat:write"
      ],
      "token": "xoxb-12345",
      "userId": "U012345",
      "id": "B012345"
  }
}'

printf "Testing create team installation $team_id\n\n"
make_http_request POST 'http://localhost:9000/api/v1/slack-installation' "installation:=$team_installation"