./mvnw clean package -DskipTests

docker compose down
docker compose build --no-cache app
docker compose up -d;
