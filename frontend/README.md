# Social Media Frontend (Vite + React + TS)

## Run Backend

From repository root:

```bash
./mvnw spring-boot:test-run
```

This will start the API at `http://localhost:8080`.

## Run Frontend

From `frontend/`:

```bash
npm install
npm run dev
```

Open `http://localhost:5173`.

The dev server proxies `/api/*` to `http://localhost:8080`.

## Features

- List posts, create post
- View post details
- Add comments and replies
- Toggle likes on posts/comments/replies

## Configure API Host

You can change the backend API base via an env var:

```bash
# Example: point to a remote server
VITE_API_BASE=https://api.example.com npm run dev
```

Resolution order:
- If `VITE_API_BASE` is set, it is used.
- Otherwise, in development the app uses `/api` (going through Vite proxy).
- In production builds, it defaults to `http://localhost:8080`.


