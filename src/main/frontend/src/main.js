import { createApp } from 'vue';
import App from './App.vue';
import router from './router/index.js';
import './assets/styles.css';
import './assets/themes.css';

const app = createApp(App);

app.use(router);
app.mount('#app');