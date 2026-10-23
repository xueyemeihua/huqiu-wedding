// src/api/tailor.js
import request from './request'

export const createTailor = (data) => request.post('/tailor-order/create', data)
export const updateTailorStatus = (data) => request.put('/tailor-order/update-status', data)
export const pageTailor = (params) => request.get('/tailor-order/list', {params})
export const listTailorBySaleId = (saleOrderId) => request.get(`/tailor-order/list-by-sale/${saleOrderId}`)
export const updateTailorInfo = (tailorId, data) => request.put(`/tailor-order/update-info?tailorId=${tailorId}`, data)