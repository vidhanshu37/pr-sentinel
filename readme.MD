step 1 : docker run --name pr-sentinel-pg -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d pgvector/pgvector:pg16

step 2 : CREATE DATABASE prsentinel;
\c prsentinel
step 3 : CREATE EXTENSION IF NOT EXISTS vector;

step 4 : SELECT * FROM pg_extension WHERE extname = 'vector';