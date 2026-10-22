// src/api/order.js
import request from './request'

export const createOrder = (data) => request.post('/sale-order/create', data)
export const updateOrderStatus = (data) => request.put('/sale-order/update-status', data)
export const pageOrder = (params) => request.get('/sale-order/list', {params})
export const orderDetail = (id) => request.get(`/sale-order/${id}`)
// 新增：更新草稿
export const updateOrderDraft = (orderId, data) => request.put(`/sale-order/update-draft?orderId=${orderId}`, data)