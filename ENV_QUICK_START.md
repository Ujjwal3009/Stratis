# Environment Files - Quick Reference

## 🎯 What We Have Now (Like Real Startups!)

```
.env.development   ← Development template (committed to git)
.env.staging       ← Staging template (committed to git)
.env.production    ← Production template (committed to git)
.env.local         ← Your personal settings (NOT committed)
.env.example       ← General template (committed to git)
```

## 🚀 Quick Start

### Option 1: Use the Script (Easiest!)

```bash
# Switch to development
./switch-env.sh dev

# Switch to staging
./switch-env.sh staging

# Switch to production
./switch-env.sh prod
```

### Option 2: Manual Copy

```bash
# For development
cp .env.development .env.local

# For staging
cp .env.staging .env.local

# For production
cp .env.production .env.local
```

### Option 3: Just Run (Uses defaults)

```bash
# No .env.local needed for development
mvn spring-boot:run
```

## 📋 File Purposes

| File | What It's For | Committed? |
|------|---------------|------------|
| `.env.development` | Development defaults | ✅ Yes |
| `.env.staging` | Staging template | ✅ Yes |
| `.env.production` | Production template | ✅ Yes |
| `.env.example` | General reference | ✅ Yes |
| `.env.local` | **Your personal settings** | ❌ **NO!** |
| `.env` | Runtime (auto) | ❌ No |

## 🔐 Security

### ✅ Safe to Commit
- `.env.development` (has placeholder values)
- `.env.staging` (has placeholder values)
- `.env.production` (has placeholder values)
- `.env.example` (has placeholder values)

### ❌ NEVER Commit
- `.env.local` (has YOUR real credentials)
- `.env` (runtime file)

## 💡 Common Scenarios

### New Developer Joins Team

```bash
# 1. Clone repo
git clone <repo>

# 2. Switch to dev (optional, works without this too)
./switch-env.sh dev

# 3. Run
mvn spring-boot:run

# That's it! No setup needed for development
```

### Adding Your Google OAuth Keys

```bash
# 1. Create local env
./switch-env.sh dev

# 2. Edit .env.local
nano .env.local

# 3. Add your keys
GOOGLE_CLIENT_ID=your-real-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-real-secret

# 4. Run
mvn spring-boot:run
```

### Testing Staging Locally

```bash
# 1. Switch to staging
./switch-env.sh staging

# 2. Edit .env.local with staging DB credentials
nano .env.local

# 3. Run with Docker
docker-compose -f docker-compose.staging.yml up
```

## 🎨 What Makes This "Startup-Like"

✅ **Environment-specific files** (`.env.development`, `.env.staging`, `.env.production`)
✅ **Personal overrides** (`.env.local`)
✅ **Templates committed** (so team has examples)
✅ **Actual secrets NOT committed** (`.env.local` is gitignored)
✅ **Easy switching** (`switch-env.sh` script)
✅ **Works out of the box** (no setup needed for dev)

## 📚 More Info

- **Detailed Guide**: See `ENV_FILES_GUIDE.md`
- **Environment Docs**: See `ENVIRONMENTS.md`
- **All Variables**: See `.env.example`

## 🎯 TL;DR

```bash
# Development (default)
mvn spring-boot:run

# Or use the switcher
./switch-env.sh dev
mvn spring-boot:run

# That's it! 🎉
```
