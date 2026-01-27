#!/bin/bash
# Environment Switcher Script
# Usage: ./switch-env.sh [dev|staging|prod]

set -e

ENV=${1:-dev}

case $ENV in
  dev|development)
    echo "🔧 Switching to DEVELOPMENT environment..."
    cp .env.development .env.local
    echo "✅ Copied .env.development to .env.local"
    echo "📝 Profile: dev"
    echo "💾 Database: H2 (in-memory)"
    echo "🔍 Log Level: DEBUG"
    ;;
  
  staging)
    echo "🧪 Switching to STAGING environment..."
    cp .env.staging .env.local
    echo "✅ Copied .env.staging to .env.local"
    echo "⚠️  IMPORTANT: Edit .env.local with actual staging credentials!"
    echo "📝 Profile: staging"
    echo "💾 Database: PostgreSQL"
    echo "🔍 Log Level: INFO"
    ;;
  
  prod|production)
    echo "🚀 Switching to PRODUCTION environment..."
    cp .env.production .env.local
    echo "✅ Copied .env.production to .env.local"
    echo ""
    echo "⚠️  ⚠️  ⚠️  CRITICAL WARNING ⚠️  ⚠️  ⚠️"
    echo "DO NOT use .env files in production!"
    echo "Use secrets manager (AWS Secrets Manager, etc.)"
    echo "This file is for reference only!"
    echo ""
    echo "📝 Profile: prod"
    echo "💾 Database: PostgreSQL (Managed)"
    echo "🔍 Log Level: WARN"
    ;;
  
  *)
    echo "❌ Invalid environment: $ENV"
    echo "Usage: ./switch-env.sh [dev|staging|prod]"
    exit 1
    ;;
esac

echo ""
echo "📋 Next steps:"
echo "  1. Review .env.local"
echo "  2. Add your credentials if needed"
echo "  3. Run: mvn spring-boot:run"
echo ""
