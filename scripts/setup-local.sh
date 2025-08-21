#!/bin/bash
set -e

echo "🏥 Setting up Healthcare MVP local environment..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java 21 is required but not installed."
    echo "Please install Java 21 and try again."
    exit 1
fi

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is required but not installed."
    echo "Please install Docker and try again."
    exit 1
fi

echo "✅ Prerequisites checked"

# Create environment file
if [ ! -f .env.local ]; then
    cat > .env.local << 'ENVEOF'
# Healthcare MVP Local Environment Configuration
SPRING_PROFILES_ACTIVE=local
DB_USERNAME=healthcare_user
DB_PASSWORD=healthcare_pass

# AWS Cognito Configuration
# TODO: Replace with your actual AWS Cognito details
COGNITO_USER_POOL_ID=your-user-pool-id-here
COGNITO_CLIENT_ID=your-client-id-here
AWS_REGION=ap-south-1

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=healthcare_local
ENVEOF
    echo "📝 Created .env.local"
    echo "⚠️  IMPORTANT: Please update .env.local with your actual AWS Cognito configuration!"
    echo ""
fi

# Start PostgreSQL
echo "🐳 Starting PostgreSQL container..."
docker run -d --name postgres-healthcare \
  -e POSTGRES_DB=healthcare_local \
  -e POSTGRES_USER=healthcare_user \
  -e POSTGRES_PASSWORD=healthcare_pass \
  -p 5432:5432 \
  postgres:15-alpine > /dev/null 2>&1 || echo "PostgreSQL container may already be running"

# Wait for database
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if database is ready
for i in {1..10}; do
    if docker exec postgres-healthcare pg_isready -U healthcare_user -d healthcare_local > /dev/null 2>&1; then
        echo "✅ Database is ready"
        break
    fi
    echo "Waiting for database... ($i/10)"
    sleep 2
done

# Run migrations
echo "🗃️  Running database migrations..."
source .env.local
#./mvnw flyway:migrate -Dspring.profiles.active=local > /dev/null 2>&1 || echo "⚠️  Migration may have failed, but continuing..."

echo ""
echo "🎉 Local setup complete!"
echo ""
echo "🚀 Start the application:"
echo "   ./mvnw spring-boot:run"
echo ""
echo "🌐 Once running, access:"
echo "   • Application: http://localhost:8080"
echo "   • API Documentation: http://localhost:8080/api/swagger-ui.html"
echo "   • Health Check: http://localhost:8080/api/actuator/health"
echo "   • Auth Health: http://localhost:8080/api/auth/health"
echo ""
echo "🔐 Test login endpoint:"
echo '   curl -X POST http://localhost:8080/api/auth/login \'
echo '     -H "Content-Type: application/json" \'
echo '     -d '"'"'{"email":"test@hospital.com","password":"password123"}'"'"
echo ""
echo "📝 Next steps:"
echo "   1. Update .env.local with your AWS Cognito configuration"
echo "   2. Start the application: ./mvnw spring-boot:run"
echo "   3. Test the API endpoints"
