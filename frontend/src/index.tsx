import React from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';
import '@fontsource/roboto';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'ol/ol.css';

import { Provider } from 'react-redux'
import store from './redux/store'

const container = document.getElementById('root');
const root = createRoot(container!);
root.render(
<Provider store={store}>
  <App />
</Provider>
);
