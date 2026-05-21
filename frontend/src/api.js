import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

function createIdempotencyKey() {
  if (globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

api.interceptors.request.use((config) => {
  const method = config.method?.toLowerCase()
  if (['post', 'put', 'patch', 'delete'].includes(method)) {
    config.headers = config.headers || {}
    config.headers['Idempotency-Key'] = config.headers['Idempotency-Key'] || createIdempotencyKey()
  }
  return config
})

// ─── User API ───
export const userApi = {
  getUsers(params = {}) {
    return api.get('/users', { params })
  },
  getUserById(userId) {
    return api.get(`/users/${userId}`)
  },
  createUser(data) {
    return api.post('/users', data)
  },
  updateUser(userId, data) {
    return api.put(`/users/${userId}`, data)
  },
  deleteUser(userId) {
    return api.delete(`/users/${userId}`)
  },
}

// ─── Product API ───
export const productApi = {
  getProducts(params = {}) {
    return api.get('/products', { params })
  },
  getProductById(no) {
    return api.get(`/products/${no}`)
  },
  createProduct(data) {
    return api.post('/products', data)
  },
  updateProduct(no, data) {
    return api.put(`/products/${no}`, data)
  },
  deleteProduct(no) {
    return api.delete(`/products/${no}`)
  },
}

// ─── Favorite Product API ───
export const favoriteApi = {
  getLikeList(params = {}) {
    return api.get('/favorite-products/like-list', { params })
  },
  getFavoritesByUser(userId) {
    return api.get(`/favorite-products/users/${userId}`)
  },
  createFavorite(data) {
    return api.post('/favorite-products', data)
  },
  updateFavorite(sn, data) {
    return api.put(`/favorite-products/${sn}`, data)
  },
  deleteFavorite(sn) {
    return api.delete(`/favorite-products/${sn}`)
  },
}

export default api
