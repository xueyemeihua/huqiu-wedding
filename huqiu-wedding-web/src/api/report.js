// src/api/report.js
import request from './request'
export const businessReport = () => request.get('/report/business')
export const stockReport = () => request.get('/report/stock')