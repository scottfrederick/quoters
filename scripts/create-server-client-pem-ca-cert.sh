#!/usr/bin/env bash

if ! command -v openssl &> /dev/null; then
    echo "openssl is required"
    exit
fi

#
# create a root CA certificate and private key
#
openssl req -out root-ca.crt -keyout root-ca.key \
  -x509 -newkey rsa:2048 -sha256 -days 365 \
  -subj "/CN=example.com"

cat > ca.ext << EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names
[alt_names]
DNS.1 = example.com
DNS.2 = localhost
EOF

#
# create a server certificate with the root CA
#
openssl req -out server.csr -keyout server.key \
  -newkey rsa:2048 -nodes \
  -subj "/CN=example.com"

openssl x509 -req -in server.csr -out server.crt \
  -CA root-ca.crt -CAkey root-ca.key -CAcreateserial \
  -days 365 -extfile ca.ext

#
# create a client certificate with the root CA
#
openssl req -out client.csr -keyout client.key \
  -newkey rsa:2048 -nodes \
  -subj "/CN=example.com"

openssl x509 -req -in client.csr -out client.crt \
  -CA root-ca.crt -CAkey root-ca.key -CAcreateserial \
  -days 365 -extfile ca.ext

#
# make the root CA certificate available to the clients and the server
#
for dir in server/src/main/resources client-resttemplate/src/main/resources client-webclient/src/main/resources; do
  cp root-ca.crt ${dir}
done

#
# make the server certificate private key available to the server only
#
for dir in server/src/main/resources; do
  cp server.crt ${dir}
  cp server.key ${dir}
done

#
# make the client certificate private key available to the server only
#
for dir in client-resttemplate/src/main/resources client-webclient/src/main/resources; do
  cp client.crt ${dir}
  cp client.key ${dir}
done

rm -f root-ca.srl