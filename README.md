# Audiobook Generator

## Running the API locally

**Prerequisites:** Java 21, Maven (or use `./mvnw`), AWS credentials with access to Bedrock, Polly, S3, and DynamoDB.

```bash
cd backend/api
```

Set the required environment variables (the app will use these defaults if unset):

| Variable | Default | Notes |
|---|---|---|
| `AWS_REGION` | `eu-west-1` | AWS region |
| `AWS_PROFILE` or `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` | — | AWS credentials |
| `BEDROCK_MODEL_ID` | `anthropic.claude-haiku-3-5-20241022-v1:0` | Bedrock model |
| `JOBS_TABLE_NAME` | `audiobook-jobs` | DynamoDB table |
| `GENERATED_AUDIO_BUCKET` | `audiobook-generated` | S3 bucket |
| `PRESIGNED_URL_EXPIRY_MINUTES` | `60` | Presigned URL TTL |

Run the server:

```bash
cd backend/api
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## Running the frontend locally

**Prerequisites:** Node.js

```bash
cd frontend
npm install
npm run dev
```

