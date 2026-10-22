// src/api/inventory.js
import request from './request'

export const pageInventory = (params) => request.get('/inventory/list', {params})
export const changeStock = (data) => request.post('/inventory/stock-change', data)
export const pageLog = (params) => request.get('/inventory/log', {params})
// 下载模板 (注意这里 responseType 为 blob)
export const downloadTemplate = () => {
    return request({
        url: '/inventory/import-template',
        method: 'get',
        responseType: 'blob'
    })
}

// 导入 Excel
export const importExcel = (formData) => {
    return request({
        url: '/inventory/import',
        method: 'post',
        data: formData,
        headers: {'Content-Type': 'multipart/form-data'}
    })
}