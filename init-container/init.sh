#!/bin/sh

cd /opt/cassandra/bin

while true; do
    if (echo "help" | ./cqlsh cassandra 9042); then
        # Check database here
        if (echo 'USE "demo";' | ./cqlsh cassandra 9042); then
            echo "Database present, exiting"
            break
        else
            echo "No demo database, doing db init"
            ./cqlsh -f /tmp/schema.cql cassandra 9042
            echo "Demo schema loaded"
            break
        fi
    else
        echo "Cassandra not available yet, sleeping 5 seconds"
        sleep 5
    fi
done

# The healthchecks will be a trigger for the
# docker-compose runtime to start the angular and dataservice
# containers
# This container will just sit in the background once it's
# job is finished.
while true; do
    sleep 3600
done