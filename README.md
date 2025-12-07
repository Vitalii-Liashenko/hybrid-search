# Hybrid Search Project

This project provides a hybrid search solution using Spring Boot, Elasticsearch, and the embeddinggemma-300m model for semantic search capabilities.

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Java 21 (for backend development)

### 1. Obtain Hugging Face Access Token
To run the embedding model, you need a Hugging Face access token (`HUGGING_FACE_HUB_TOKEN`).

- Register or log in at [Hugging Face](https://huggingface.co/join)
- Go to your [Access Tokens page](https://huggingface.co/settings/tokens)
- Create a new token (read access is sufficient)
- Copy the token and set it as an environment variable before starting the project:
  
  ```powershell
  set HUGGING_FACE_HUB_TOKEN=your_token_here
  ```
  or in Linux/macOS:
  ```bash
  export HUGGING_FACE_HUB_TOKEN="your_token_here"
  ```

### 2. Configure Elasticsearch via environment variables (.env supported)
The application reads Elasticsearch settings from environment variables. A local `.env` file at the project root is supported and loaded automatically.

Supported variables:
- `SOURCE_ES_URL` — Elasticsearch URL (e.g., `http://localhost:9200`)
- `SOURCE_ES_USERNAME` — Basic auth username (optional)
- `SOURCE_ES_PASSWORD` — Basic auth password (optional)
- `SOURCE_ES_INDEX_NAME` — Target index name (defaults to `conferences_100`)

Example `.env` file:
```
SOURCE_ES_URL=http://localhost:9200
SOURCE_ES_USERNAME=elastic
SOURCE_ES_PASSWORD=changeme
SOURCE_ES_INDEX_NAME=conferences_100
```

Windows cmd (temporary for the session):
```cmd
set SOURCE_ES_URL=http://localhost:9200
set SOURCE_ES_USERNAME=elastic
set SOURCE_ES_PASSWORD=changeme
set SOURCE_ES_INDEX_NAME=conferences_100
```

These variables map to Spring properties:
- `spring.elasticsearch.uris` ← `SOURCE_ES_URL`
- `spring.elasticsearch.username` ← `SOURCE_ES_USERNAME`
- `spring.elasticsearch.password` ← `SOURCE_ES_PASSWORD`
- `elasticsearch.index` ← `SOURCE_ES_INDEX_NAME`

No code changes are needed—just set the variables or create the `.env` file.

### 3. Start the Project

From the project root directory, run:

```powershell
docker-compose up --build
```

This will start:
- **TEI embedding service** (embeddinggemma-300m) on port **8080**
- **Elasticsearch** on port **9200**

### 4. Access the Web Application

The web interface is available at: [http://localhost:8585](http://localhost:8585)

---

## Project Structure
- `docker-compose.yml` — Docker services configuration
- `Dockerfile.tei` — Dockerfile for the embedding model service
- `src/main/resources/application.yaml` — Spring Boot application configuration

## Notes
- The embedding model requires a valid Hugging Face token for download and use.
- Elasticsearch data is persisted in the `esdata` directory.
- Environment variables can be provided via OS env or a local `.env` file.

## Useful Links
- [Hugging Face Access Tokens](https://huggingface.co/settings/tokens)
- [embeddinggemma-300m model card](https://huggingface.co/google/embeddinggemma-300m)

---

## Check the local embedding service at:
POST: http://localhost:8080/embed

```
{
        "inputs": "some text"
}
```

## Check Vertex AI embedding service at:

POST: https://us-central1-aiplatform.googleapis.com/v1/projects/{pb-company-ecosystem-dev}/locations/{us-central1}/publishers/google/models/text-embedding-005:predict
+ Bearer Auth with OAuth token: cmd> gcloud auth print-access-token

```
{
  "instances": [
    { "content": "some text"}
  ],
  "parameters": { 
    "autoTruncate": true 
  }
}
```

# Inference Service Configuration
`PUT: http://localhost:9200/_inference/text_embedding/embeddinggemma` + BasicAuth
````
{
  "service": "hugging_face",
  "service_settings": {
    "url": "http://tei-embeddinggemma/embed",
    "api_key": "<huggingface access token>"
  },
  "task_type": "text_embedding"
}
````

### Example of successful response:
````
{
    "inference_id": "embeddinggemma",
    "task_type": "text_embedding",
    "service": "hugging_face",
    "service_settings": {
        "url": "http://tei-embeddinggemma/embed",
        "similarity": "cosine",
        "dimensions": 768,
        "rate_limit": {
            "requests_per_minute": 3000
        }
    },
    "chunking_settings": {
        "strategy": "sentence",
        "max_chunk_size": 250,
        "sentence_overlap": 1
    }
}
````

## Check that the inference is created
POST: http://localhost:9200/_inference/text_embedding/embeddinggemma + BasicAuth

```
{
  "input": "some text"
}
```

For any issues, please contact the project maintainer.
