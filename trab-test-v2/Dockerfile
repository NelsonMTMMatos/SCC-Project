FROM node:latest

WORKDIR /usr/src/app

# Copy the package.json file and install dependencies
COPY package.json ./
RUN npm install

# Copy your Artillery configuration files and scripts
COPY load-data.yml ./
COPY test-utils.js ./
COPY workload1.yml ./

COPY images/ ./images
COPY run_tests.sh .

RUN chmod +x run_tests.sh

# Global installation of Artillery and other dependencies
RUN npm install -g artillery \
    node-fetch \
    https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git

ENTRYPOINT ["./run_tests.sh"]
