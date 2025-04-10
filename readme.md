# Endpoints

### 🔐 Auth
##### To create account use localhost:80/auth/register endpoint (for now only supports accounts with user role).

### 👤 Default Accounts for Testing

If you don't want to register you can use one of the following preconfigured accounts to test the application:

- 🧍 **User Account**
  - Username: `user`
  - Password: `user`

- 🏢 **Company Account**
  - Username: `company`
  - Password: `company`

- 🛡️ **Admin Account**
  - Username: `admin`
  - Password: `admin`

---

### 🔓 Login

To log into an account, send a `POST` request to the following endpoint with `x-www-form-urlencoded` data:
```bash
curl --location 'localhost:80/auth/login' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=MSNcars' \
--data-urlencode 'username=user' \
--data-urlencode 'password=user'
```

You will then receive access_token (that expires after 5 minutes) in response. Use it for all consecutive requests by passing it as bearer token:
```bash
curl --location 'localhost:80/user' \
--header 'Authorization: Bearer *your-access-token*'
```

### 📘 All other endpoints are described on swagger (after launching application):
http://localhost:8080/swagger-ui/index.html
