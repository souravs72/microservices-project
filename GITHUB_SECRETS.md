# 🔐 GitHub Secrets Configuration

This document explains how to securely configure sensitive information for deployment using GitHub Secrets.

## 🚨 Security Notice

**CRITICAL**: Never commit sensitive information like passwords, API keys, or email credentials to the repository. All sensitive data should be configured as GitHub Secrets for secure deployment.

## 📋 Required GitHub Secrets

To deploy this microservices platform securely, configure the following secrets in your GitHub repository:

### 1. Navigate to GitHub Secrets
1. Go to your GitHub repository
2. Click on **Settings** tab
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret** for each secret below

### 2. Configure Required Secrets

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `MAIL_USERNAME` | Email address for sending notifications | `your-email@gmail.com` |
| `MAIL_PASSWORD` | App password for email authentication | `your-16-char-app-password` |
| `JWT_SECRET` | Secret key for JWT token signing | `256-bit-random-string` |
| `INTERNAL_API_KEY` | API key for service-to-service communication | `secure-random-api-key` |
| `REDIS_PASSWORD` | Redis authentication password | `secure-redis-password` |
| `DB_PASSWORD` | Database password for all services | `secure-db-password` |

### 3. Email Configuration (Gmail Example)

For Gmail SMTP configuration:

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
   - Use this 16-character password (not your regular Gmail password)

3. **Set GitHub Secrets**:
   ```
   MAIL_USERNAME: your-email@gmail.com
   MAIL_PASSWORD: your-16-char-app-password
   ```

## 🔧 Local Development Setup

For local development, create a `.env` file in the project root:

```bash
# Copy the example file
cp env.dev.example .env

# Edit .env with your actual credentials
nano .env
```

**Important**: The `.env` file is already in `.gitignore` and will not be committed to the repository.

## 🚀 Docker Deployment with Secrets

### Using GitHub Secrets in Actions

If you're using GitHub Actions for deployment, reference secrets like this:

```yaml
env:
  MAIL_USERNAME: ${{ secrets.MAIL_USERNAME }}
  MAIL_PASSWORD: ${{ secrets.MAIL_PASSWORD }}
  JWT_SECRET: ${{ secrets.JWT_SECRET }}
```

### Using Docker Compose with Environment Files

For local Docker deployment:

```bash
# Create .env file with your credentials
cp env.dev.example .env
# Edit .env with actual values

# Start services
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

## 🔒 Security Best Practices

1. **Never commit secrets**: Always use `.env` files or GitHub Secrets
2. **Rotate credentials regularly**: Change passwords and API keys periodically
3. **Use strong passwords**: Minimum 16 characters with mixed case, numbers, symbols
4. **Limit access**: Only grant necessary permissions to team members
5. **Monitor usage**: Regularly check logs for unauthorized access attempts

## 📝 Environment-Specific Configuration

- **Development**: Uses relaxed settings, H2 databases, local Redis/Kafka
- **Staging**: Uses PostgreSQL, moderate security settings
- **Production**: Uses PostgreSQL, strict security, production SMTP

## 🆘 Troubleshooting

### Common Issues

1. **Email not sending**: Check SMTP credentials and app password
2. **Authentication failures**: Verify JWT secret is consistent across services
3. **Database connection errors**: Ensure database passwords match

### Verification Commands

```bash
# Check if services can access secrets
docker compose exec auth-service env | grep MAIL
docker compose exec notification-service env | grep MAIL

# Test email functionality
curl -X POST http://localhost:8085/api/notifications/test-email \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"testuser","firstName":"Test","lastName":"User"}'
```

## 🔄 Updating Secrets

To update secrets:

1. **GitHub**: Go to repository Settings → Secrets and variables → Actions
2. **Local**: Update `.env` file and restart services
3. **Production**: Update secrets in your deployment platform

---

**Remember**: Security is a shared responsibility. Always follow best practices and never share credentials in plain text.
