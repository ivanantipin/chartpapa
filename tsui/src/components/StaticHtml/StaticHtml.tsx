import * as React from 'react';
import {Component} from 'react';
import {Select, Spin} from "antd";
import {mainControllerApi} from "../../repository";
import {RouteComponentProps, withRouter} from "react-router";
import {SelectParam} from "antd/lib/menu";

const Option = Select.Option;

const children = [];
for (let i = 10; i < 36; i++) {
    children.push(<Option key={i.toString(36) + i}>{i.toString(36) + i}</Option>);
}


class StaticHtml extends Component<RouteComponentProps<any>, {fetching : boolean, available: Array<string>, fetched: { [key: string]: string } }> {

    constructor(props: any) {
        super(props)
        this.state = {available: [], fetched: {}, fetching : false}
    }

    componentDidMount() {
        mainControllerApi.loadStaticPagesUsingGET().then((files: Array<string>) => {
            this.setState({...this.state, available: files})
        }).catch(e => {
            console.log(e)
        })
    }

    onSelect(param: SelectParam) {
        console.log("selected", param)
        this.props.history.push('/funcharts/' + param)
        this.setState({...this.state})
    }

    drawContent() {
        if(!this.state.available.includes(this.props.match.params.id)){
            return <div/>
        }

        if (this.state.fetching) {
            return <Spin style={{top: '50%', left: '50%', position: 'absolute'}} size='large'/>
        }

        const key = this.props.match.params.id

        console.log('key is ', this.props.history)

        const doc = this.state.fetched[key];
            if(doc){
                return <div dangerouslySetInnerHTML={{__html: doc}}/>
            }else {
                this.setState({...this.state, fetching : true})

                mainControllerApi.loadHtmContentUsingGET(key).then(wr => {

                    const cp = {...this.state.fetched}
                    cp[key] = wr.value
                    this.setState({...this.state, fetched: cp, fetching : false})
                }).catch(ee => {
                    this.setState({...this.state, fetching : false})
                    console.log(ee)
                })


                return <div/>
            }

    }

    render() {

        return <div>

            <Select
                style={{width: '100%'}}
                placeholder="Please select"
                value={this.props.match.params.id || ""}
                onChange={this.onSelect.bind(this)}
            >
                {this.state.available.map(s => {
                    return (<Option key={s}>{s}</Option>)
                })}
            </Select>

            {
                this.drawContent()
            }

        </div>
    }
}


export default withRouter(StaticHtml)