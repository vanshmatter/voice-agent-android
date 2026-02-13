# Using GitHub Actions to Build APK

This is the **easiest way** to build your APK without installing anything on your computer!

## 📋 Prerequisites

- GitHub account (free)
- Git installed on your computer (or use GitHub Desktop)

## 🚀 Step-by-Step Instructions

### Step 1: Create GitHub Repository

1. Go to [github.com](https://github.com)
2. Click **"New repository"** (green button)
3. Name it: `voice-agent-android`
4. Make it **Public** or **Private** (your choice)
5. Click **"Create repository"**

### Step 2: Upload Your Project

#### Option A: Using Git Command Line
```powershell
cd c:\Users\HP\.vscode\VoiceAgent

# Initialize git (if not already)
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit - Voice Agent AI"

# Add remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/voice-agent-android.git

# Push to GitHub
git push -u origin main
```

#### Option B: Using GitHub Desktop (Easier)
1. Download [GitHub Desktop](https://desktop.github.com/)
2. Install and sign in
3. Click **"Add"** → **"Add Existing Repository"**
4. Select: `c:\Users\HP\.vscode\VoiceAgent`
5. Click **"Publish repository"**

#### Option C: Upload via Web (Simplest)
1. On your GitHub repository page, click **"uploading an existing file"**
2. Drag and drop all files from `VoiceAgent` folder
3. Click **"Commit changes"**

### Step 3: Enable GitHub Actions

The workflow file is already created at `.github/workflows/build-apk.yml`

1. Go to your repository on GitHub
2. Click **"Actions"** tab
3. If prompted, click **"I understand my workflows, go ahead and enable them"**

### Step 4: Trigger Build

The APK will build automatically when you push code. To trigger manually:

1. Go to **"Actions"** tab
2. Click **"Build Android APK"** workflow
3. Click **"Run workflow"** button
4. Click green **"Run workflow"** button

### Step 5: Download APK

#### From Artifacts (Immediate)
1. Wait for build to complete (~5-10 minutes)
2. Click on the completed workflow run
3. Scroll down to **"Artifacts"**
4. Click **"voice-agent-apk"** to download

#### From Releases (Automatic)
1. Go to **"Releases"** section (right sidebar)
2. Click on the latest release
3. Download **app-debug.apk**

## 🎯 What Happens

1. **You push code** to GitHub
2. **GitHub Actions** automatically:
   - Sets up Java environment
   - Installs Android SDK
   - Builds your APK
   - Uploads it as artifact
   - Creates a release
3. **You download** the APK

## ✅ Advantages

- ✅ No local installation needed
- ✅ Builds in the cloud
- ✅ Free for public repositories
- ✅ Automatic builds on every push
- ✅ Works on any computer

## 🔧 Troubleshooting

### Build Fails

**Check the logs**:
1. Go to Actions tab
2. Click on failed workflow
3. Click on "build" job
4. Expand failed step to see error

**Common issues**:
- Missing files: Make sure all files uploaded
- Gradle wrapper missing: Included in project
- Permissions: Workflow has execute permissions

### Can't Push to GitHub

**Authentication required**:
1. Use GitHub Desktop (easier)
2. Or generate Personal Access Token
3. Use token as password when pushing

## 📱 Installing APK on Phone

1. Download APK from GitHub
2. Transfer to phone (USB, email, cloud)
3. Open APK file on phone
4. Allow "Install from Unknown Sources"
5. Tap "Install"

## 🎉 Done!

Your APK builds automatically in the cloud, no local setup needed!

---

**Need help?** Check the Actions tab for build logs and errors.
