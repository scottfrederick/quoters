#!/usr/bin/env bash

if ! command -v openssl &> /dev/null; then
    echo "openssl is required"
    exit
fi

#
# create a certificate and private key pair to identify the client applications
#
openssl req -out client.crt -keyout client.key \
  -x509 -newkey rsa:4096 -sha256 -days 365 -nodes \
  -subj "/CN=example.com" -addext "subjectAltName=DNS:example.com,DNS:localhost"

#
# make the certificate available to the clients and the server
#
for dir in client-resttemplate/src/main/resources client-webclient/src/main/resources server/src/main/resources; do
  cp client.crt ${dir}
done

#
# make the private key available to the clients only
#
for dir in client-resttemplate/src/main/resources client-webclient/src/main/resources; do
  cp client.key ${dir}
done
