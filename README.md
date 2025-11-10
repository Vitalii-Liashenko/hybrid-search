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
  $env:HUGGING_FACE_HUB_TOKEN="your_token_here"
  ```
  or in Linux/macOS:
  ```bash
  export HUGGING_FACE_HUB_TOKEN="your_token_here"
  ```

### 2. Start the Project

From the project root directory, run:

```powershell
docker-compose up --build
```

This will start:
- **TEI embedding service** (embeddinggemma-300m) on port **8080**
- **Elasticsearch** on port **9200**

### 3. Access the Web Application

The web interface is available at: [http://localhost:8585](http://localhost:8585)

---

## Project Structure
- `docker-compose.yml` — Docker services configuration
- `Dockerfile.tei` — Dockerfile for the embedding model service
- `src/main/resources/application.yaml` — Spring Boot application configuration

## Notes
- The embedding model requires a valid Hugging Face token for download and use.
- Elasticsearch data is persisted in the `esdata` directory.

## Useful Links
- [Hugging Face Access Tokens](https://huggingface.co/settings/tokens)
- [embeddinggemma-300m model card](https://huggingface.co/google/embeddinggemma-300m)

---

For any issues, please contact the project maintainer.
