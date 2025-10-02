#!/bin/sh
if (echo 'USE "demo";' | /opt/cassandra/bin/cqlsh cassandra 9042); then
    exit 0
else
    exit 1
fi