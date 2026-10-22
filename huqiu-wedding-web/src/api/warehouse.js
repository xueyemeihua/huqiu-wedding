// src/api/warehouse.js
import request from './request'
export const pageWarehouse = (params) => request.get('/warehouse/list', { params })
export const saveWarehouse = (data) => request.post('/warehouse/save', data)
export const deleteWarehouse = (id) => request.delete(`/warehouse/${id}`)