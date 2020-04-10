#!/bin/sh

set -e

jenkins_container=$(docker ps | grep kernelci-jenkins_jenkins) || echo -n ''
if [ -n "$jenkins_container" ]; then
  echo "$jenkins_container"
  container_id=$(echo "$jenkins_container" | cut -c-12)
  echo "Stopping Jenkins container: $container_id..."
  docker stop "$container_id"
else
  echo "Jenkins container is not running."
fi

echo "Updating Jenkins image..."
docker pull jenkins/jenkins

echo "Starting Jenkins container..."

docker-compose up --build -d

echo "Done."

exit 0
