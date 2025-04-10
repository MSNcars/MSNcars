# Endpoints

### Auth
To create account use localhost:80/auth/register endpoint (for now only supports accounts with user role),
there are also 3 default account that you can log into to test our application:
a) account with user role with username: "user" and password: "user"
b) account with company role with username: "company" and password: "company"
c) account with admin role with username: "admin" and password: "admin"

To log into the account use localhost:80/auth/login endpoint with x-www-form-urlencoded:
```bash
curl --location 'localhost:80/auth/login' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=MSNcars' \
--data-urlencode 'username=user' \
--data-urlencode 'password=user'
```

All other endpoints are described here:
http://localhost:8080/swagger-ui/index.html