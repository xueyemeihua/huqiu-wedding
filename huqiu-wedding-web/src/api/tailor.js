// src/api/tailor.js
import request from './request'
export const createTailor = (data) => request.post('/tailor-order/create', data)
export const updateTailorStatus = (data) => request.put('/tailor-order/update-status', data)
export const pageTailor = (params) => request.get('/tailor-order/list', { params })
export const updateTailorDraft = (tailorId, data) => request.put(`/tailor-order/update-draft?tailorId=${tailorId}`, data)