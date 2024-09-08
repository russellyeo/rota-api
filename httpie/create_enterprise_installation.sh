#!/bin/bash

source "$(dirname "$0")/utils.sh"

enterprise_id="EUDEF456"
enterprise_installation='{
  "enterprise": {
      "id": "'$enterprise_id'",
      "name": "Russell"
  },
  "user": {
      "id": "UUXYZ987"
  },
  "tokenType": "bot",
  "isEnterpriseInstall": true,
  "appId": "A08765",
  "authVersion": "v2",
  "bot": {
      "scopes": [
          "app_mentions:read",
          "chat:write"
      ],
      "token": "xoxb-87654",
      "userId": "U087654",
      "id": "B087654"
  }
}'

printf "Testing create enterprise installation $enterprise_id\n\n"
make_http_request POST 'http://localhost:9000/api/v1/slack-installation' "installation:=$enterprise_installation"