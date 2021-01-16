import React, {useState} from 'react'
import {useHistory} from "react-router-dom";
import {Layout, Menu} from 'antd';
import 'antd/dist/antd.css';
import {AreaChartOutlined, DeploymentUnitOutlined} from '@ant-design/icons';
import {MenuInfo, SelectInfo} from "rc-menu/lib/interface";


const {Sider} = Layout;


export const SiderMenu = (props: any) => {

    const [collapsed, setCollapsed] = useState(false)
    const [selectedKey, setSelectedKey] = useState("/total")

    const history = useHistory();

    return (
        <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
            <Menu theme="dark" mode="inline"
                  defaultSelectedKeys={['/total']}
                  selectedKeys={[selectedKey]}
                  onClick={(info: MenuInfo) => {
                      history.push(info.key as string)
                      setSelectedKey(info.key as string)
                  }}
            >
                <Menu.Item key="/my-portfolios" icon={<AreaChartOutlined/>}>
                    Portfolios
                </Menu.Item>
                <Menu.Item key="/total" icon={<AreaChartOutlined/>}>
                    Total
                </Menu.Item>
                <Menu.Item key="/by-symbol" icon={<AreaChartOutlined/>}>
                    By Symbol
                </Menu.Item>
                <Menu.Item key="/aggregation" icon={<AreaChartOutlined/>}>
                    Aggregation
                </Menu.Item>
                <Menu.Item key="6" icon={<DeploymentUnitOutlined/>}>Compare</Menu.Item>
            </Menu>
        </Sider>
    )
}