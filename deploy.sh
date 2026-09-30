mvn clean package -DskipTests
docker compose down
docker compose build --no-cache app
docker login
sudo docker push huyhai1994/file-upload-service:1.0.7