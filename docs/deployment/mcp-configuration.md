# MCP Configuration

This project keeps MCP server definitions in three places so different agents
can share the same baseline tools:

- `.mcp.json`: generic JSON MCP configuration
- `.codex/config.toml`: Codex project MCP configuration
- `.gemini/settings.json`: Gemini CLI MCP configuration

## Servers

### drawio

Uses the official draw.io MCP tool server:

```bash
npx -y @drawio/mcp
```

Use it when creating or editing `.drawio` architecture diagrams.

### github

Uses GitHub's official MCP server Docker image:

```bash
docker run -i --rm -e GITHUB_PERSONAL_ACCESS_TOKEN ghcr.io/github/github-mcp-server
```

Before starting an MCP client, set a token in the shell or OS environment:

```bash
set GITHUB_PERSONAL_ACCESS_TOKEN=your-token
```

The project config does not store the token value.

### postgres

Uses the PostgreSQL MCP server:

```bash
npx -y @modelcontextprotocol/server-postgres %MCP_POSTGRES_URL%
```

The default connection matches the local `docker-compose.yml` PostgreSQL
service:

- host: `localhost`
- port: `5432`
- database: `micro_db`
- user: `user`
- password: `password`

Use this server for schema inspection and read-only query exploration. Do not
use it for schema changes; add database changes through Flyway migrations.

## Requirements

- Node.js and `npx` for `drawio` and `postgres`
- Docker for `github`
- `GITHUB_PERSONAL_ACCESS_TOKEN` environment variable for GitHub access
- Local PostgreSQL container running for the default Postgres connection

## Verification

Restart the MCP client after changing these files. For Postgres, start the local
database first:

```bash
docker-compose up -d postgres
```
