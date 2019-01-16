import React, {Component} from 'react'
import {Icon, Layout, Menu} from 'antd';
import {SelectParam} from "antd/lib/menu";
import {MenuConf, routerConfig} from "./routerConfig";
import {Route, RouteComponentProps, Switch, withRouter} from "react-router";


const { Header, Sider, Content } = Layout;

interface HomeProps extends RouteComponentProps<any>, React.Props<any> {
}



class MainMenu extends Component<HomeProps,any> {
    state = {
        collapsed: false,
    };

    toggle = () => {
        this.setState({
            collapsed: !this.state.collapsed,
        });
    }

    render() {

        return <Layout>
            <Sider
                trigger={null}
                collapsible
                collapsed={this.state.collapsed}
            >
                <div className="logo"/>

                <Menu theme="dark" mode="inline" defaultSelectedKeys={['1']} onSelect={this.selectMenu.bind(this)}>
                    {
                        routerConfig.filter(cfg=>cfg.shown).map((cfg : MenuConf)=>{
                            return <Menu.Item key={cfg.path}>
                                <Icon type="user"/>
                                <span>{cfg.name}</span>
                            </Menu.Item>
                        })
                    }
                </Menu>
            </Sider>
            <Layout>
                <Header style={{background: '#fff', padding: 0}}>
                    <Icon
                        className="trigger"
                        type={this.state.collapsed ? 'menu-unfold' : 'menu-fold'}
                        onClick={this.toggle}
                    />
                </Header>
                <Content style={{margin: '24px 16px', padding: 24, background: '#fff', minHeight: 700}}>
                    <Switch>

                        {
                            routerConfig.filter(cfg=>cfg.shown).map(rc => {
                                console.log('rc ' + rc.path)
                                return (
                                <Route
                                    path={rc.path}
                                    component={rc.component}
                                />)
                            })

                        }
                    </Switch>
                </Content>
            </Layout>
        </Layout>;
    }

    private selectMenu(param : SelectParam ) {
        console.log('hist ' + param.key)
        this.props.history.push(param.key)
    }
}

export default withRouter(MainMenu)

/*

#components-layout-demo-custom-trigger .trigger {
    font-size: 18px;
    line-height: 64px;
    padding: 0 24px;
    cursor: pointer;
    transition: color .3s;
}

#components-layout-demo-custom-trigger .trigger:hover {
    color: #1890ff;
}

#components-layout-demo-custom-trigger .logo {
    height: 32px;
    background: rgba(255,255,255,.2);
    margin: 16px;
}*/
