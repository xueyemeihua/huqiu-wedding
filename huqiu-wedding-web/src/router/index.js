import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    { path: '/login', component: () => import('../views/Login.vue') },
    {
        path: '/',
        component: () => import('../layout/MainLayout.vue'),
        children: [
            { path: '', redirect: '/dashboard' },
            { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页看板', admin: true } },
            { path: 'warehouse', component: () => import('../views/warehouse/WarehouseList.vue'), meta: { title: '仓库管理', admin: true } },
            { path: 'goods', component: () => import('../views/goods/GoodsList.vue'), meta: { title: '商品SKU', admin: true } },
            { path: 'inventory', component: () => import('../views/inventory/InventoryList.vue'), meta: { title: '库存管理', admin: true } },
            { path: 'user', component: () => import('../views/user/UserList.vue'), meta: { title: '销售员管理', admin: true } },
            { path: 'sale-order', component: () => import('../views/order/SaleOrderList.vue'), meta: { title: '销售订单' } },
            { path: 'sale-order/create', component: () => import('../views/order/SaleOrderForm.vue'), meta: { title: '新建销售单' } },
            { path: 'tailor', component: () => import('../views/tailor/TailorOrderList.vue'), meta: { title: '改衣订单' } },
            { path: 'report', component: () => import('../views/report/Report.vue'), meta: { title: '经营报表', admin: true } },
            { path: 'mobile', component: () => import('../views/mobile/MobileHome.vue'), meta: { title: '移动开单' } }
        ]
    }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) return '/login'
    if (to.meta.admin) {
        const user = JSON.parse(localStorage.getItem('user') || 'null')
        if (user?.role !== 1) return '/sale-order'
    }
})

export default router