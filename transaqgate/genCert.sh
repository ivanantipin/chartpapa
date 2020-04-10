# note it is necessary to set common name ( will be asked) to localhost so then client will be able to connect to server that reside on localhost

# Generate CA key:
openssl genrsa -out ca.key 4096

# Generate CA certificate:
openssl req -new -x509 -days 365 -key ca.key -out ca.crt -config req.conf -extensions 'v3_req'

# Generate server key:
openssl genrsa -out server.key 4096

# Generate server signing request:
openssl req -new -key server.key -out server.csr -config req.conf -extensions 'v3_req'

# Self-sign server certificate:
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt

# Remove passphrase from the server key:
openssl rsa -in server.key -out server.key

# Generate client key:
openssl genrsa -out client.key 4096

# Generate client signing request:
openssl req -new -key client.key -out client.csr -config req.conf -extensions 'v3_req'

# Self-sign client certificate:
openssl x509 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt

# Remove passphrase from the client key:
#openssl rsa -in client.key -out client.key

openssl pkcs8 -topk8 -nocrypt -in client.key -out client.pem

openssl pkcs8 -topk8 -nocrypt -in server.key -out server.pem