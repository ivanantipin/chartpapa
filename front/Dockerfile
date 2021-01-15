FROM node:latest

WORKDIR /app/frontend
COPY package*.json /app/frontend/
COPY tsconfig.json /app/frontend
COPY webpack.config.js /app/frontend
ENV PATH /app/frontend/node_modules/.bin:$PATH


RUN npm install
RUN npm install react-scripts@3.4.1 -g --silent

EXPOSE 3000
CMD ["npm", "start"]