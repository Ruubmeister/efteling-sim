const path = require("path");

const config = {
  entry: "./src/index.js",
  mode: "development",
  module: {
    rules: [
      {
        exclude: /(node_modules)/,
        test: /\.(js|jsx)$/i,
        loader: "babel-loader"
      }
    ]
  },
  output: {
    path: path.resolve(__dirname, "dist")
  },
  plugins: [],
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx'],
    modules: ['.', 'node_modules']
  },
  watch: true,
};

module.exports = config;
