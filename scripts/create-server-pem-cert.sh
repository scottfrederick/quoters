#!/usr/bin/env bash

if ! command -v openssl &> /dev/null; then
    echo "openssl is required"
    exit
fi

#
# create a certificate and private key pair to identify the server application
#
openssl req -out server.crt -keyout server.key \
  -x509 -newkey rsa:4096 -sha256 -days 365 -nodes \
  -subj "/CN=example.com" -addext "subjectAltName=DNS:example.com,DNS:localhost"

#
# make the certificate available to the clients and the server
#
for dir in server/src/main/resources client-resttemplate/src/main/resources client-webclient/src/main/resources; do
  cp server.crt ${dir}
done

#
# make the private key available to the server only
#
for dir in server/src/main/resources; do
  cp server.key ${dir}
done
