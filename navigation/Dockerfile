FROM rust:1.84 AS builder

WORKDIR /usr/src/navigation
COPY ./navigation .

RUN cargo install --path .

FROM debian:stable-slim

RUN apt-get update & apt-get install -y extra-runtime-dependencies & rm -rf /var/lib/apt/lists/*
COPY --from=builder /usr/local/cargo/bin/navigation /usr/local/bin/navigation
EXPOSE 49985
CMD ["navigation"]