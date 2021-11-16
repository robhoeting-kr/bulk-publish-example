#
#  Please add your own event hub namespace URL, event hub topic name, OAuth Creds
#

export EVENTHUB_NAMESPACE="despmisc-dev"
export EVENTHUB_TOPIC="healthcheck"
export OAUTH_CLIENT_ID="see DESP TEAM"
export OAUTH_CLIENT_KEY="see DESP TEAM"
export OAUTH_TENANT_ID="see DESP TEAM"
export SCHEMA_REGISTRY_SECRET='see DESP TEAM'
#
#  If running on local host, do the following:
#     * make sure you are off VPN
#     * get your IP address white-listed
#
export SCHEMA_REGISTRY_URL="https://desp-schema-registry-nonprod.internal.kroger.com:8443/dev"
export CURRENT_USER=${USER:-${USERNAME:-${LOGNAME}}}
export QUICK_START_CONSUMER_GROUP="quickstart_healthcheck_group_${CURRENT_USER}"
mvn clean install
java -Dspring.profiles.active=eventhub_oauth -jar target/desp-producer-consumer-quick-start-1.0.0.jar
