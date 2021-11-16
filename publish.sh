#
#  USAGE: ./publish.sh 10
#   will publish 10 messages
#
curl -X POST http://localhost:8080/publish\?count\=${1:-1}\&sampleInt\=${2:-1}