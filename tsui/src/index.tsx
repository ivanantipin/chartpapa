import * as React from 'react';
import * as ReactDOM from 'react-dom';

import "antd/lib/style/themes/default.less";
import "antd/lib/button/style/index.less";


import './index.css';
import registerServiceWorker from './registerServiceWorker';
//import App from "./components/WidgetsPanel/App";
import {Provider} from "react-redux";
import {mainReducer} from "./reducers";
import {createStore} from "redux";
import {Router} from 'react-router';
import createHist from "history/createBrowserHistory";
import MainMenu from "./MainMenu";
import {fetchInstruments} from "./repository";
import {makeAmend} from "./actions";

const store = createStore(mainReducer);

fetchInstruments().then(inst => {
    store.dispatch(makeAmend((state) => {
        return Object.assign({}, state, { instruments: inst });
    }));
}).catch(er => {
    console.log('err', er);
});


ReactDOM.render(<div style={{height : "100vh"}}><Provider  store={store}>


    <Router history={createHist()}>
            <MainMenu />
    </Router>


</Provider></div>, document.getElementById('root'));
registerServiceWorker();