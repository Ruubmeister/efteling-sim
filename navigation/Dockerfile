FROM rust:1.93 AS builder

WORKDIR /usr/src/navigation
COPY ./navigation .

RUN cargo install --path .

FROM debian:stable-slim

COPY --from=builder /usr/local/cargo/bin/navigation /usr/local/bin/navigation
EXPOSE 49985
CMD ["navigation"]