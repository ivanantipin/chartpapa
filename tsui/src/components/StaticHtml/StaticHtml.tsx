import * as React from 'react';
import {Component} from 'react';
import {MainControllerApi} from 'api';
import {Select} from "antd";
//var _ = require('lodash');



const Option = Select.Option;

const children = [];
for (let i = 10; i < 36; i++) {
    children.push(<Option key={i.toString(36) + i}>{i.toString(36) + i}</Option>);
}




export class StaticHtml extends Component<any, { selected : Array<string>, available : Array<string>, fetched : { [key : string] : string} }> {

    constructor(props : any){
        super(props)
        this.state = {selected : [], available : [], fetched : {}}
    }

    componentDidMount() {
        new MainControllerApi().getStaticPagesUsingGET().then((files : Array<string>) => {
            this.setState({...this.state, available: files})
        }).catch(e=>{
            console.log(e)
        })
    }

    onSelect(sel : Array<string>){
        console.log("selected", sel)
        this.setState({...this.state, selected : sel})
        sel.forEach(fl=>{
            new MainControllerApi().loadHtmContentUsingGET(fl).then(wr=>{
                console.log("content", wr)
                const cp = {...this.state.fetched}
                cp[fl] = wr.value
                this.setState({...this.state,  fetched : cp})
            }).catch(ee=>{
                console.log(ee)
            })
        })
    }

    render() {

        return <div>

        <Select
            mode="multiple"
            style={{ width: '100%' }}
            placeholder="Please select"
            //value={this.state.selected}
            onChange={this.onSelect.bind(this)}
        >
            {this.state.available.map(s=>{
                return (<Option key={s}>{s}</Option>)
            })}
        </Select>

            {
                this.state.selected.map(sel=>{
                    let doc = this.state.fetched[sel];
                    if(doc){
                        console.log("somithengi interensteu")
                        return <div dangerouslySetInnerHTML={{__html: doc}}/>
                    }else {
                        console.log("empty")
                        return <div/>
                    }
                })
            }


        </div>
    }
}
