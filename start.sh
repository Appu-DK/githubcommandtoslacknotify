#!/bin/bash

# ============================================
# GitHub to Slack Commit Tracker - Start Script
# ============================================

# Load secrets from .env file
ENV_FILE="$(dirname "$0")/.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ .env file not found! Create one with:"
    echo "   SLACK_WEBHOOK_URL=your_slack_webhook_url"
    echo "   GITHUB_TOKEN=your_github_token"
    echo "   GITHUB_REPO=owner/repo"
    echo "   WEBHOOK_ID=your_webhook_id"
    exit 1
fi
source "$ENV_FILE"
APP_PORT=8080

echo "🚀 Starting GitHub to Slack Commit Tracker..."

# Step 1: Kill anything on port 8080
echo "🔄 Cleaning up port $APP_PORT..."
lsof -ti:$APP_PORT | xargs kill -9 2>/dev/null
sleep 2

# Step 2: Start Spring Boot app
echo "☕ Starting Spring Boot app..."
export SLACK_WEBHOOK_URL
cd "$(dirname "$0")"
nohup mvn spring-boot:run > app.log 2>&1 &
APP_PID=$!

# Wait for app to start
echo "⏳ Waiting for app to start..."
for i in {1..30}; do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:$APP_PORT/h2-console 2>/dev/null | grep -q "302"; then
        echo "✅ App started on port $APP_PORT (PID: $APP_PID)"
        break
    fi
    sleep 2
done

# Step 3: Start tunnel and capture URL
echo "🌐 Starting tunnel..."
ssh -R 80:localhost:$APP_PORT nokey@localhost.run > /tmp/tunnel.log 2>&1 &
TUNNEL_PID=$!

# Wait for tunnel URL
echo "⏳ Waiting for tunnel URL..."
TUNNEL_URL=""
for i in {1..20}; do
    TUNNEL_URL=$(grep -o 'https://[a-z0-9]*\.lhr\.life' /tmp/tunnel.log | head -1)
    if [ -n "$TUNNEL_URL" ]; then
        echo "✅ Tunnel URL: $TUNNEL_URL"
        break
    fi
    sleep 2
done

if [ -z "$TUNNEL_URL" ]; then
    echo "❌ Failed to get tunnel URL. Check /tmp/tunnel.log"
    exit 1
fi

# Step 4: Update GitHub webhook
echo "🔗 Updating GitHub webhook..."
RESPONSE=$(curl -s -X PATCH \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"config\":{\"url\":\"$TUNNEL_URL/api/webhook/github\",\"content_type\":\"json\",\"insecure_ssl\":\"0\"},\"events\":[\"push\"],\"active\":true}" \
    "https://api.github.com/repos/$GITHUB_REPO/hooks/$WEBHOOK_ID")

UPDATED_URL=$(echo "$RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['config']['url'])" 2>/dev/null)

if [ -n "$UPDATED_URL" ]; then
    echo "✅ GitHub webhook updated to: $UPDATED_URL"
else
    echo "❌ Failed to update webhook. Response: $RESPONSE"
    exit 1
fi

echo ""
echo "============================================"
echo "🎉 Everything is running!"
echo "============================================"
echo "📱 App:      http://localhost:$APP_PORT"
echo "🗄️  H2 Console: http://localhost:$APP_PORT/h2-console"
echo "🌐 Tunnel:   $TUNNEL_URL"
echo "🔗 Webhook:  $TUNNEL_URL/api/webhook/github"
echo "============================================"
echo "Now just push code to GitHub and check Slack!"
echo "Press Ctrl+C to stop everything."
echo ""

# Keep running and cleanup on exit
cleanup() {
    echo ""
    echo "🛑 Shutting down..."
    kill $APP_PID 2>/dev/null
    kill $TUNNEL_PID 2>/dev/null
    lsof -ti:$APP_PORT | xargs kill -9 2>/dev/null
    echo "✅ Stopped."
}
trap cleanup EXIT

# Keep script alive and show tunnel logs
tail -f /tmp/tunnel.log
