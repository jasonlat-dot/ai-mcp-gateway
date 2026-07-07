import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, getUser, setUser } from '@/utils/request'

const TEST_ACCOUNT = { username: 'admin', password: 'password123' }

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const user  = ref(getUser())

  const isAuthenticated = computed(() => !!token.value)

  async function login({ username, password }) {
    // 当前后端无 auth 接口,按 nginx 老 admin 保留的体验继续 mock。
    // 真实接入 /admin/auth 接口时,改造成 axios.post 即可。
    await new Promise((r) => setTimeout(r, 720))
    if (
      username === TEST_ACCOUNT.username &&
      password === TEST_ACCOUNT.password
    ) {
      const fakeToken = `Bearer-mock-${btoa(username + ':' + Date.now()).slice(0, 24)}`
      const u = { username, nickname: 'Admin', role: 'Administrator' }
      setToken(fakeToken)
      setUser(u)
      token.value = fakeToken
      user.value = u
      return u
    }
    throw new Error('账号或密码不正确')
  }

  function logout() {
    setToken(null)
    setUser(null)
    token.value = null
    user.value = null
  }

  return { token, user, isAuthenticated, login, logout }
})
