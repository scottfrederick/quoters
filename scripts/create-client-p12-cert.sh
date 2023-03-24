#!/usr/bin/env bash

if ! command -v keytool &> /dev/null; then
    echo "keytool is required"
    exit
fi

#
# create a certificate and private key pair to identify the client applications
#
keytool -genkeypair -keystore client.p12 -alias spring-boot-client \
  -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -validity 10 -storepass secret \
  -dname "CN=com.example" -ext SAN=dns:example.com,dns:localhost

#
# make the certificate and private key pair available to the clients
#
for dir in client-resttemplate/src/main/resources client-webclient/src/main/resources; do
  cp client.p12 ${dir}
done

#
# extract the certificate from the store and save it in a new store
#
keytool -exportcert -keystore client.p12 -file client-trust.crt \
  -storepass secret -alias spring-boot-client \
  -rfc
keytool -importcert -file client-trust.crt -keystore client-trust.p12 \
  -storepass secret -storetype PKCS12 -alias spring-boot-client \
  -noprompt

#
# make the certificate available to the server
#
for dir in server/src/main/resources; do
  cp client-trust.p12 ${dir}
done

