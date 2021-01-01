FROM clojure:lein AS build
RUN apt-get update
RUN apt-get install -y --no-install-recommends make
RUN curl -L https://git.io/n-install | bash
RUN n latest
RUN lein deps
RUN lein release

FROM nginx
COPY resources/public /usr/share/nginx/html
