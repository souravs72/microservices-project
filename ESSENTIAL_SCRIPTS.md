# Essential Scripts Summary

I've cleaned up and kept only the most essential scripts for your microservices. Here's the streamlined overview:

## 🚀 Essential Scripts

### 1. **`rebuild-docker-v2.sh`** - Main Docker Script

**Best for:** Production-like environment with Docker

```bash
./rebuild-docker-v2.sh [OPTIONS]
```

**Features:**

- Comprehensive Docker container management
- Uses modern `docker compose` command with fallback
- Health checks and service validation
- JWT token validation testing
- Clean image removal option
- Detailed logging and status reporting

**Options:**

- `--clean-images` - Remove old Docker images
- `--logs` - Show logs after starting
- `--test` - Test JWT token validation
- `--status` - Show service status only

### 2. **`rebuild-maven.sh`** - Maven-based Script

**Best for:** Development without Docker

```bash
./rebuild-maven.sh [OPTIONS]
```

**Features:**

- Runs services directly with Maven
- No Docker required
- Faster startup for development
- Individual service management

**Options:**

- `--logs` - Show logs after starting
- `--test` - Test JWT token validation
- `--status` - Show service status only
- `--stop` - Stop all services

### 3. **`test-services.sh`** - Comprehensive Testing Script

**Best for:** Testing all service functionality

```bash
./test-services.sh [OPTIONS]
```

**Features:**

- Tests all services health
- Tests admin login functionality
- Tests JWT token validation
- Tests notification service
- Tests email functionality
- Comprehensive test suite

**Options:**

- `--health` - Test all services health
- `--login` - Test admin login
- `--jwt` - Test JWT token validation
- `--notification` - Test notification service
- `--email` - Test email functionality
- `--all` - Run comprehensive test suite (default)

### 4. **`verify-secrets.sh`** - Environment Verification

**Best for:** Checking environment setup

```bash
./verify-secrets.sh
```

**Features:**

- Verifies all required environment variables
- Checks JWT secret configuration
- Validates database passwords
- Ensures proper setup

### 5. **`open-swagger-docs.sh`** - API Documentation

**Best for:** Opening Swagger documentation

```bash
./open-swagger-docs.sh
```

**Features:**

- Opens Swagger UI for all services
- Quick access to API documentation

## 🎯 Quick Usage

### For Docker Users:

```bash
# Full rebuild with testing
./rebuild-docker-v2.sh --test

# Clean rebuild (removes old images)
./rebuild-docker-v2.sh --clean-images --test

# Check service status
./rebuild-docker-v2.sh --status
```

### For Maven Users:

```bash
# Rebuild and start all services
./rebuild-maven.sh

# Rebuild with JWT testing
./rebuild-maven.sh --test

# Stop all services
./rebuild-maven.sh --stop
```

### For Testing:

```bash
# Run all tests
./test-services.sh

# Test specific functionality
./test-services.sh --jwt --login

# Test service health only
./test-services.sh --health
```

## 📊 Service Status

After running any script, services will be available at:

- **Auth Service:** http://localhost:8082
- **User Service:** http://localhost:8081
- **API Gateway:** http://localhost:8080
- **Notification Service:** http://localhost:8083
- **Frontend:** http://localhost:3000 (if built)
- **H2 Console (Auth):** http://localhost:8082/h2-console
- **H2 Console (User):** http://localhost:8081/h2-console

## 🔧 Troubleshooting

### Docker Issues

If you get Docker connection errors:

1. Try the Maven script: `./rebuild-maven.sh`
2. Check Docker status: `docker info`

### Port Conflicts

If ports are already in use:

```bash
# Stop all services first
./rebuild-maven.sh --stop
# or
docker compose down
```

### JWT Token Issues

All scripts include JWT validation testing. If it fails:

1. Check that all services are using the same JWT secret
2. Verify the auth service `/api/auth/validate` endpoint is public
3. Ensure services can communicate with each other

## 🎯 Recommended Usage

### Development Workflow:

1. **Daily development:** `./rebuild-maven.sh` (fastest)
2. **Testing changes:** `./rebuild-maven.sh --test`
3. **Docker testing:** `./rebuild-docker-v2.sh --test`

### Production-like Testing:

1. **Full rebuild:** `./rebuild-docker-v2.sh --clean-images --test`
2. **Status check:** `./rebuild-docker-v2.sh --status`

### Comprehensive Testing:

1. **All tests:** `./test-services.sh`
2. **Specific tests:** `./test-services.sh --jwt --login`

## 📝 Notes

- All scripts automatically handle `.env` file creation
- JWT token validation is fixed and working
- Services use the same JWT secret for proper validation
- Logs are saved to the `logs/` directory
- Scripts include proper error handling and colored output

## 🆘 Need Help?

Run any script with `--help` to see detailed usage information:

```bash
./rebuild-docker-v2.sh --help
./rebuild-maven.sh --help
./test-services.sh --help
```

The scripts are designed to be robust and handle common issues automatically. Choose the one that best fits your development environment!
