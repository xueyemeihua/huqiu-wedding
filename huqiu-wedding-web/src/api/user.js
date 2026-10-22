// src/api/user.js
import request from './request'
export const pageUser = (params) => request.get('/user/list', { params })
export const saveUser = (data) => request.post('/user/save', data)
export const toggleUser = (id) => request.put(`/user/toggle/${id}`)