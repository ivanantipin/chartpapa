import * as React from 'react';
import * as ReactDOM from 'react-dom';

import './index.css';
import registerServiceWorker from './registerServiceWorker';
import App from "./components/WidgetsPanel/App";
import {Provider} from "react-redux";
import {mainReducer} from "./reducers";
import {createStore} from "redux";

const store = createStore(mainReducer);

ReactDOM.render(<Provider store={store}>
    <App instruments={[]} widgets={[]}  />
</Provider>, document.getElementById('root') );
registerServiceWorker();