RUN:

```bash
docker run --rm -v ~/secrets/credentials.json:/app/config/credentials.json -e GOOGLE_APPLICATION_CREDENTIALS=/app/config/credentials.json issue-tracker create --description "Test issue"
```