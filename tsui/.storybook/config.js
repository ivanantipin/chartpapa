import {configure} from '@storybook/react';

require('react-error-overlay');
require('react-dev-utils/webpackHotDevClient');

function loadStories() {
    require('../stories/index');
    // You can require as many stories as you need.
}
configure(loadStories, module);
