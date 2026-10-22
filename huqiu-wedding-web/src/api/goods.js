// src/api/goods.js
import request from './request'
export const pageGoods = (params) => request.get('/goods/list', { params })
export const saveGoods = (data) => request.post('/goods/save', data)
export const offlineGoods = (id) => request.put(`/goods/offline/${id}`)
export const listCategories = () => request.get('/goods/category/list')