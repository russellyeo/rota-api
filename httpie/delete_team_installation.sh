#!/bin/bash

source "$(dirname "$0")/utils.sh"

team_id="TUABC123"

printf "Testing delete team installation $team_id\n\n"
make_http_request DELETE "http://localhost:9000/api/v1/slack-installation/$team_id"