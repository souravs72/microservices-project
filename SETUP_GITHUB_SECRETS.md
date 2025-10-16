# 🔐 Setup GitHub Secrets - Step by Step Guide

## 🚨 URGENT: Add These Secrets to Your GitHub Repository

Follow these steps to add your secrets to GitHub:

### Step 1: Navigate to GitHub Secrets
1. Go to your repository: `https://github.com/souravs72/microservices-project`
2. Click on **Settings** tab (at the top of the repository)
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret** for each secret below

### Step 2: Add These Secrets (One by One)

| Secret Name | Secret Value | Description |
|-------------|--------------|-------------|
| `MAIL_USERNAME` | `souravsingh2609@gmail.com` | Your Gmail address |
| `MAIL_PASSWORD` | `bhooqxmxjnwkccgb` | Your Gmail app password |
| `JWT_SECRET` | `XK7fJ3mP9vN2wQ8rT5yU6iO1pA4sD7fG9hJ2kL5nM8qR3tY6uI9oP2aS5dF8gH1jK4lZ7xC0vB3nM6qW9eR2tY5uI8oP1aS4dF7gH0jK3lZ6xC9vB2nM5qW8eR1tY4u==` | JWT signing secret |
| `INTERNAL_API_KEY` | `change-this-in-production-xyz123` | Service-to-service API key |
| `REDIS_PASSWORD` | `devredis123` | Redis authentication password |
| `DB_PASSWORD` | `authpassword123` | Database password |

### Step 3: How to Add Each Secret

For each secret above:
1. Click **New repository secret**
2. **Name**: Enter the secret name (e.g., `MAIL_USERNAME`)
3. **Secret**: Enter the secret value (e.g., `souravsingh2609@gmail.com`)
4. Click **Add secret**

### Step 4: Verify Secrets Added

After adding all secrets, you should see them listed in the **Repository secrets** section:
- ✅ MAIL_USERNAME
- ✅ MAIL_PASSWORD  
- ✅ JWT_SECRET
- ✅ INTERNAL_API_KEY
- ✅ REDIS_PASSWORD
- ✅ DB_PASSWORD

## 🔧 Using Secrets in GitHub Actions

If you create a GitHub Actions workflow, reference secrets like this:

```yaml
env:
  MAIL_USERNAME: ${{ secrets.MAIL_USERNAME }}
  MAIL_PASSWORD: ${{ secrets.MAIL_PASSWORD }}
  JWT_SECRET: ${{ secrets.JWT_SECRET }}
  INTERNAL_API_KEY: ${{ secrets.INTERNAL_API_KEY }}
  REDIS_PASSWORD: ${{ secrets.REDIS_PASSWORD }}
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
```

## ⚠️ Important Notes

1. **Never share these values**: Keep them confidential
2. **Rotate regularly**: Change passwords periodically
3. **Access control**: Only give repository access to trusted team members
4. **Monitor usage**: Check repository audit logs regularly

## 🧪 Test Your Setup

After adding secrets, you can test with a simple GitHub Actions workflow:

```yaml
name: Test Secrets
on: [push]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Test email credentials
        run: |
          echo "Email: ${{ secrets.MAIL_USERNAME }}"
          echo "Password configured: ${{ secrets.MAIL_PASSWORD != '' }}"
```

---

**Complete this setup now to secure your repository!** 🔒
