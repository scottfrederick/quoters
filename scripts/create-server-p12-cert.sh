#!/usr/bin/env bash

if ! command -v keytool &> /dev/null; then
    echo "keytool is required"
    exit
fi

#
# create a certificate and private key pair to identify the server application
#
keytool -genkeypair -keystore server.p12 \
  -storepass secret -storetype PKCS12 -alias spring-boot-server \
  -keyalg RSA -keysize 2048 -validity 10 \
  -dname "CN=com.example" -ext SAN=dns:example.com,dns:localhost

#
# make the certificate and private key pair available to the server
#
for dir in server/src/main/resources; do
  cp server.p12 ${dir}
done

#
# extract the certificate from the store and save it in a new store
#
keytool -exportcert -keystore server.p12 -file server-trust.crt \
  -storepass secret -alias spring-boot-server \
  -rfc
keytool -importcert -file server-trust.crt -keystore server-trust.p12 \
  -storepass secret -storetype PKCS12 -alias spring-boot-server \
  -noprompt

#
# make the certificate available to the clients
#
for dir in client-resttemplate/src/main/resources client-webclient/src/main/resources; do
  cp server-trust.p12 ${dir}
done
