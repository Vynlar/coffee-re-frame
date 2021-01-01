FROM clojure:lein AS build
RUN apt-get update
RUN apt-get install -y --no-install-recommends make

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.37.2/install.sh | bash
RUN source $HOME/.bashrc && nvm install 14.15.3
RUN ln -s $HOME/.nvm/versions/node/v14.15.3/bin/node /usr/bin/node
RUN ln -s $HOME/.nvm/versions/node/v14.15.3/bin/npm /usr/bin/npm
RUN node -v
RUN npm -v

WORKDIR /app

COPY . .

RUN lein deps
RUN lein release

FROM nginx
COPY resources/public /usr/share/nginx/html
