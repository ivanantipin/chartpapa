import React from "react";
import moment from "moment-timezone";

const red = '#ee3227'
const green = '#32d542'

export function getRenderByValue(colName: string): any {
    let hasColor = true
    const lowered = colName.toLowerCase()
    if (lowered.includes("price") || lowered.includes("qty") || lowered === 'id' || lowered === 'cnt') {
        hasColor = false
    }
    return (value: any, record: any, index: any) => {
        if (value instanceof Date) {
            return value.toLocaleDateString()
        }
        if (typeof value === 'string') {
            const loweredVal = value.toLowerCase()
            if (['long', 'short', 'buy', 'sell'].indexOf(loweredVal) === -1) {
                return value
            }
            let color = green
            if (['short', 'sell'].indexOf(loweredVal) > -1) {
                color = red
            }
            return <span style={{color: color}}>{value}</span>

        }

        if (value > 14677002000) {
            return moment(value).format('YYYY-MM-DD HH:mm:ss')
        }

        let colorClass: string | undefined = value > 0 ? green : red
        if (!hasColor) {
            colorClass = undefined
        }

        try {
            return <span style={{color: colorClass}}>{value.toFixed(2)}</span>
        } catch (e) {
            return value
        }
    }


}