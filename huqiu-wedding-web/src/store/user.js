import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    function setLogin(data) {
        token.value = data.token
        user.value = data
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data))
    }

    function logout() {
        token.value = ''
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    const isAdmin = () => user.value?.role === 1
    return { token, user, setLogin, logout, isAdmin }
})