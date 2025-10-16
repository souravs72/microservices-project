#!/bin/bash

# 🔐 GitHub Secrets Verification Script
# This script helps verify that your GitHub Secrets are properly configured

echo "🔍 GitHub Secrets Verification"
echo "=============================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in a GitHub Actions environment
if [ -n "$GITHUB_ACTIONS" ]; then
    echo -e "${YELLOW}Running in GitHub Actions environment${NC}"
    echo ""
    
    # Check each secret
    secrets=("MAIL_USERNAME" "MAIL_PASSWORD" "JWT_SECRET" "INTERNAL_API_KEY" "REDIS_PASSWORD" "DB_PASSWORD")
    
    all_secrets_configured=true
    
    for secret in "${secrets[@]}"; do
        if [ -n "${!secret}" ]; then
            echo -e "${GREEN}✅ $secret: configured${NC}"
        else
            echo -e "${RED}❌ $secret: missing${NC}"
            all_secrets_configured=false
        fi
    done
    
    echo ""
    if [ "$all_secrets_configured" = true ]; then
        echo -e "${GREEN}🎉 All secrets are properly configured!${NC}"
        exit 0
    else
        echo -e "${RED}⚠️  Some secrets are missing. Please add them to GitHub Secrets.${NC}"
        echo ""
        echo "To add secrets:"
        echo "1. Go to your GitHub repository"
        echo "2. Settings → Secrets and variables → Actions"
        echo "3. Add the missing secrets"
        exit 1
    fi
    
else
    echo -e "${YELLOW}Running locally - GitHub Secrets verification not applicable${NC}"
    echo ""
    echo "To verify secrets in GitHub Actions:"
    echo "1. Push this script to your repository"
    echo "2. Create a GitHub Actions workflow that runs this script"
    echo "3. Check the Actions tab for verification results"
    echo ""
    echo "See SETUP_GITHUB_SECRETS.md for detailed setup instructions."
fi
