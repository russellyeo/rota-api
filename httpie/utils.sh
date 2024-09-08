#!/bin/bash
#
# Utils for scripting requests for testing the API
# CF: https://httpie.io/docs/cli/scripting

check_http_status() {
    if "$@"; then
        echo 'OK!'
        return 0
    else
        case $? in
            2) echo 'Request timed out!' ;;
            3) echo 'Unexpected HTTP 3xx Redirection!' ;;
            4) echo 'HTTP 4xx Client Error!' ;;
            5) echo 'HTTP 5xx Server Error!' ;;
            6) echo 'Exceeded --max-redirects=<n> redirects!' ;;
            *) echo 'Other Error!' ;;
        esac
        return 1
    fi
}

make_http_request() {
    local method="$1"
    local url="$2"
    local body="$3"

    if [ -n "$body" ]; then
        check_http_status http --check-status --ignore-stdin --timeout=2.5 \
            "$method" "$url" \
            Accept:'application/json' \
            Content-Type:'application/json' \
            "$body"
    else
        check_http_status http --check-status --ignore-stdin --timeout=2.5 \
            "$method" "$url" \
            Accept:'application/json'
    fi
}