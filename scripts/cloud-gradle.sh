#!/bin/sh
# Convenience launcher for this cloud environment; regular machines should use ./gradlew.
set -eu
cd "$(dirname "$0")/.."
export JAVA_HOME="${JAVA_HOME:-/workspace/toolchains/jdk-21.0.8}"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/workspace/.gradle-cache}"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 -Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080 -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
exec ./gradlew --no-daemon "$@"
