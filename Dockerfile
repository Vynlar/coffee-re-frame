FROM clojure:lein AS build
RUN apt-get update
RUN apt-get install -y --no-install-recommends make

ENV NVM_DIR /usr/local/nvm
ENV NODE_VERSION lts

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.37.2/install.sh | bash
RUN source $NVM_DIR/nvm.sh \
    && nvm install $NODE_VERSION \
    && nvm alias default $NODE_VERSION \
    && nvm use default
ENV NODE_PATH $NVM_DIR/v$NODE_VERSION/lib/node_modules
ENV PATH $NVM_DIR/versions/node/v$NODE_VERSION/bin:$PATH
RUN node -v
RUN npm -v

WORKDIR /app

COPY . .

RUN lein deps
RUN lein release

FROM nginx
COPY resources/public /usr/share/nginx/html
