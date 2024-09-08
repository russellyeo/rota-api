#!/bin/bash

source "$(dirname "$0")/utils.sh"

enterprise_id="EUDEF456"

printf "Testing delete enterprise installation $enterprise_id\n\n"
make_http_request DELETE "http://localhost:9000/api/v1/slack-installation/$enterprise_id"