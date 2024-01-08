FROM rust:1.61.0 AS build

WORKDIR /usr/src/navigation
COPY . .

RUN cargo install --path .

FROM debian:buster-slim

RUN apt-get update & apt-get install -y extra-runtime-dependencies & rm -rf /var/lib/apt/lists/*
COPY --from=builder /usr/local/cargo/bin/navigation /usr/local/bin/navigation
CMD ["navigation"]