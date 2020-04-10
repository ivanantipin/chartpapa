import {Component, default as React} from 'react';
import {EditorState} from 'draft-js';
import ReactDOM from 'react-dom';
import 'draft-js/dist/Draft.css'
import '../node_modules/react-draft-wysiwyg/dist/react-draft-wysiwyg.css';

var wys =  require('react-draft-wysiwyg');

//@ts-ignore
const tb = {
    image: {
        icon: "image",
        className: undefined,
        component: undefined,
        popupClassName: undefined,
        urlEnabled: true,
        uploadEnabled: true,
        alignmentEnabled: true,
        uploadCallback: undefined,
        previewImage: false,
        inputAccept: 'image/gif,image/jpeg,image/jpg,image/png,image/svg',
        alt: { present: false, mandatory: false },
        defaultSize: {
            height: 'auto',
            width: 'auto',
        },
    },
}


class ControlledEditor extends Component<any, { estate: EditorState }> {
    constructor(props: any) {
        super(props);
        this.state = {estate: EditorState.createEmpty()};
    }

    onEditorStateChange: Function = (estate : EditorState) => {
        this.setState({estate});
    };

    render() {
        const { estate } = this.state;
        let ret = <wys.Editor
            editorState={estate}
            // toolbar={tb}
            wrapperClassName="demo-wrapper"
            editorClassName="demo-editor"
            onEditorStateChange={this.onEditorStateChange}
        />;


        //@ts-ignore
        console.log("", ret.toolbar)

        return (
            ret
        )
    }
}



ReactDOM.render(
    <ControlledEditor />,
    document.getElementById('root')
);


/*
import React, { Component } from 'react';
import { EditorState } from 'draft-js';

*/


