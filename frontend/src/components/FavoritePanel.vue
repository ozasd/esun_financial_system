<script setup>
import { ref, onMounted } from 'vue'
import { favoriteApi, userApi, productApi } from '../api.js'

const likeList = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const sortBy = ref('user_id')
const sortDirection = ref('ASC')

const showForm = ref(false)
const editMode = ref(false)
const editSn = ref(null)
const form = ref({ userId: '', productNo: '', purchaseQuantity: 1, account: '' })
const users = ref([])
const products = ref([])
const formError = ref('')

async function fetchLikeList() {
  loading.value = true; error.value = ''
  try {
    const params = { page: page.value, pageSize: pageSize.value, sortBy: sortBy.value, sortDirection: sortDirection.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await favoriteApi.getLikeList(params)
    likeList.value = res.data.datas || []
    total.value = res.data.total || 0
  } catch (e) {
    error.value = e.response?.data?.message || '載入失敗'
  } finally { loading.value = false }
}

async function loadDropdowns() {
  try {
    const [u, p] = await Promise.all([userApi.getUsers({ pageSize: 100 }), productApi.getProducts({ pageSize: 100 })])
    users.value = u.data.datas || []
    products.value = p.data.datas || []
  } catch (_) {}
}

function openAdd() {
  editMode.value = false; editSn.value = null
  form.value = { userId: '', productNo: '', purchaseQuantity: 1, account: '' }
  formError.value = ''; showForm.value = true; loadDropdowns()
}

function openEdit(sn, item) {
  editMode.value = true; editSn.value = sn
  form.value = { userId: item.userId || '', productNo: String(item.productNo), purchaseQuantity: item.purchaseQuantity, account: item.account }
  formError.value = ''; showForm.value = true; loadDropdowns()
}

async function submitForm() {
  formError.value = ''
  try {
    if (editMode.value) {
      await favoriteApi.updateFavorite(editSn.value, { productNo: Number(form.value.productNo), purchaseQuantity: Number(form.value.purchaseQuantity), account: form.value.account })
      showForm.value = false; fetchLikeList()
    } else {
      await favoriteApi.createFavorite({ userId: form.value.userId, productNo: Number(form.value.productNo), purchaseQuantity: Number(form.value.purchaseQuantity), account: form.value.account })
      showForm.value = false;
      
      // Optimistic UI 樂觀更新：立刻在畫面上顯示假資料，讓使用者覺得沒有延遲
      const userItem = likeList.value.find(u => u.userId === form.value.userId)
      if (userItem) {
        const prod = products.value.find(p => String(p.no) === String(form.value.productNo))
        if (!userItem.favoriteProducts) userItem.favoriteProducts = []
        userItem.favoriteProducts.push({
          sn: Date.now(), // 假的暫時 ID
          productNo: form.value.productNo,
          productName: prod ? prod.productName : '處理中...',
          purchaseQuantity: form.value.purchaseQuantity,
          totalFee: '...', // 後端非同步計算中
          totalAmount: '...', // 後端非同步計算中
          account: form.value.account
        })
      }
      
      // Polling：由於後端 Worker 大約 1 秒消費一次佇列，延遲刷新確保拿到最新 DB 資料
      setTimeout(fetchLikeList, 1200)
      setTimeout(fetchLikeList, 2500)
    }
  } catch (e) { formError.value = e.response?.data?.message || '操作失敗' }
}

async function deleteFavorite(sn) {
  if (!confirm('確定要刪除此筆喜好商品？')) return
  try { await favoriteApi.deleteFavorite(sn); fetchLikeList() }
  catch (e) { alert(e.response?.data?.message || '刪除失敗') }
}

function goPage(p) { page.value = p; fetchLikeList() }
function doSearch() { page.value = 1; fetchLikeList() }
const totalPages = () => Math.ceil(total.value / pageSize.value) || 1

function toggleSort(col) {
  if (sortBy.value === col) {
    sortDirection.value = sortDirection.value === 'ASC' ? 'DESC' : 'ASC'
  } else {
    sortBy.value = col
    sortDirection.value = 'ASC'
  }
  doSearch()
}
function sortIcon(col) {
  if (sortBy.value !== col) return 'bi-arrow-down-up text-muted opacity-25'
  return sortDirection.value === 'ASC' ? 'bi-arrow-up text-primary' : 'bi-arrow-down text-primary'
}

function onUserChange() {
  const selectedUser = users.value.find(u => u.userId === form.value.userId)
  if (selectedUser) {
    form.value.account = selectedUser.account
  }
}

onMounted(fetchLikeList)
</script>

<template>
  <div class="d-flex flex-column gap-4">
    <!-- Action Bar -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
      <!-- Modern Search -->
      <div class="search-wrapper d-flex align-items-center flex-grow-1" style="max-width: 400px;">
        <i class="bi bi-search text-muted px-3"></i>
        <input v-model="keyword" type="text" class="form-control" placeholder="搜尋使用者或帳號..." @keyup.enter="doSearch">
        <button class="btn text-primary px-3 fw-medium" @click="doSearch">搜尋</button>
      </div>
      
      <!-- Primary Action -->
      <button class="btn btn-primary px-4 py-2 rounded-pill" @click="openAdd">
        <i class="bi bi-plus-lg me-2"></i>新增喜好商品
      </button>
    </div>

    <!-- Main Card -->
    <div class="card p-3 p-md-4">
      <div v-if="loading" class="text-center py-5 text-muted">
        <div class="spinner-border text-primary me-2" role="status"></div><br><small class="mt-2 d-inline-block">載入資料中...</small>
      </div>
      <div v-else-if="error" class="alert alert-danger bg-danger bg-opacity-10 text-danger border-0 d-flex align-items-center rounded-3">
        <i class="bi bi-exclamation-triangle-fill fs-5 me-3"></i>
        <div>{{ error }}</div>
      </div>
      <div v-else class="table-responsive">
        <table class="table table-hover align-top text-nowrap">
          <thead>
            <tr>
              <th @click="toggleSort('user_id')" style="cursor:pointer" class="user-select-none">使用者 ID <i :class="['bi ms-1', sortIcon('user_id')]"></i></th>
              <th @click="toggleSort('user_name')" style="cursor:pointer" class="user-select-none">名稱 <i :class="['bi ms-1', sortIcon('user_name')]"></i></th>
              <th @click="toggleSort('email')" style="cursor:pointer" class="user-select-none">Email <i :class="['bi ms-1', sortIcon('email')]"></i></th>
              <th @click="toggleSort('account')" style="cursor:pointer" class="user-select-none">帳號 <i :class="['bi ms-1', sortIcon('account')]"></i></th>
              <th>收藏資產詳情</th>
            </tr>
          </thead>
          <tbody class="border-top-0">
            <tr v-if="likeList.length === 0">
              <td colspan="5" class="text-center py-5">
                <i class="bi bi-folder-x text-muted opacity-25" style="font-size: 3rem;"></i>
                <p class="text-muted mt-2 mb-0 fw-medium">目前尚無任何紀錄</p>
              </td>
            </tr>
            <tr v-for="item in likeList" :key="item.userId">
              <td class="font-mono text-muted">{{ item.userId }}</td>
              <td class="fw-medium text-dark">{{ item.userName }}</td>
              <td><a :href="'mailto:'+item.email" class="text-decoration-none text-muted"><i class="bi bi-envelope me-1"></i>{{ item.email }}</a></td>
              <td class="font-mono text-muted">{{ item.account }}</td>
              <td>
                <div v-if="item.favoriteProducts && item.favoriteProducts.length" class="d-flex flex-column gap-2">
                  <div v-for="fp in item.favoriteProducts" :key="fp.sn" class="p-2 rounded bg-light border border-black border-opacity-10 d-flex align-items-center justify-content-between">
                    <div class="d-flex align-items-center gap-3">
                      <span class="badge rounded-pill bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 px-3 py-1">{{ fp.productName }}</span>
                      <div class="text-muted small">
                        <span class="me-3"><i class="bi bi-layers me-1 opacity-75"></i>數量 <span class="fw-medium text-dark">{{ fp.purchaseQuantity }}</span></span>
                        <span class="me-3"><i class="bi bi-receipt me-1 opacity-75"></i>手續費 <span class="fw-medium text-dark">${{ fp.totalFee }}</span></span>
                        <span><i class="bi bi-cash-stack me-1 opacity-75"></i>總額 <span class="fw-bold text-success">${{ fp.totalAmount }}</span></span>
                      </div>
                    </div>
                    <div class="d-flex gap-1 ms-3">
                      <button class="btn btn-action text-secondary" title="編輯" @click="openEdit(fp.sn, { ...fp, userId: item.userId, account: fp.account })">
                        <i class="bi bi-pencil-square"></i>
                      </button>
                      <button class="btn btn-action btn-action-danger text-secondary" title="刪除" @click="deleteFavorite(fp.sn)">
                        <i class="bi bi-trash3"></i>
                      </button>
                    </div>
                  </div>
                </div>
                <span v-else class="text-muted fst-italic small">尚無收藏</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="!loading && !error && total > 0" class="d-flex flex-column flex-md-row justify-content-between align-items-center mt-4 border-top pt-3">
        <div class="text-muted small mb-3 mb-md-0">
          顯示 <span class="fw-bold text-dark">{{ total }}</span> 筆紀錄中的第 <span class="fw-bold text-dark">{{ page }}</span> 頁
        </div>
        <nav>
          <ul class="pagination pagination-sm mb-0">
            <li :class="['page-item', { disabled: page <= 1 }]">
              <button class="page-link border-0 shadow-none bg-transparent text-secondary" @click="goPage(page - 1)"><i class="bi bi-chevron-left"></i></button>
            </li>
            <li v-for="p in totalPages()" :key="p" :class="['page-item', { active: page === p }]">
              <button :class="['page-link border-0 rounded-circle mx-1 fw-medium', page === p ? 'bg-primary text-white shadow-sm' : 'bg-transparent text-secondary']" style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;" @click="goPage(p)">{{ p }}</button>
            </li>
            <li :class="['page-item', { disabled: page >= totalPages() }]">
              <button class="page-link border-0 shadow-none bg-transparent text-secondary" @click="goPage(page + 1)"><i class="bi bi-chevron-right"></i></button>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- Modern Modal -->
    <Teleport to="body">
      <div v-if="showForm" class="modal d-block show fade" tabindex="-1" style="background:rgba(15, 23, 42, 0.4);">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0">
            <div class="modal-header border-0 px-4 pt-4 pb-0">
              <h5 class="modal-title fw-bold text-dark">
                <div class="d-inline-flex align-items-center justify-content-center bg-primary bg-opacity-10 text-primary rounded p-2 me-3">
                  <i :class="editMode ? 'bi-pencil-square' : 'bi-plus-lg'"></i>
                </div>
                {{ editMode ? '編輯喜好商品' : '新增喜好商品' }}
              </h5>
              <button type="button" class="btn-close shadow-none" @click="showForm = false"></button>
            </div>
            <div class="modal-body p-4">
              <div v-if="formError" class="alert alert-danger py-3 small border-0 rounded-3 d-flex align-items-center"><i class="bi bi-exclamation-octagon-fill fs-5 me-2"></i>{{ formError }}</div>
              
              <form @submit.prevent="submitForm" class="d-flex flex-column gap-3">
                <div v-if="!editMode" class="form-floating">
                  <select v-model="form.userId" class="form-select bg-light" id="userSelect" required @change="onUserChange">
                    <option value="" disabled>點擊選擇...</option>
                    <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.userName }} ({{ u.userId }})</option>
                  </select>
                  <label for="userSelect">選擇使用者</label>
                </div>
                
                <div class="form-floating">
                  <select v-model="form.productNo" class="form-select bg-light" id="productSelect" required>
                    <option value="" disabled>點擊選擇...</option>
                    <option v-for="p in products" :key="p.no" :value="String(p.no)">{{ p.productName }} — 價格: ${{ p.price }}</option>
                  </select>
                  <label for="productSelect">選擇目標商品</label>
                </div>
                
                <div class="row g-3">
                  <div class="col-sm-5">
                    <div class="form-floating">
                      <input v-model.number="form.purchaseQuantity" type="number" min="1" class="form-control bg-light" id="qtyInput" required placeholder="1" />
                      <label for="qtyInput">購買數量</label>
                    </div>
                  </div>
                  <div class="col-sm-7">
                    <div class="form-floating">
                      <input v-model="form.account" type="text" class="form-control bg-light" id="accInput" required placeholder="帳號" />
                      <label for="accInput">扣款帳號 (自動帶入)</label>
                    </div>
                  </div>
                </div>

                <div class="d-flex justify-content-end gap-3 mt-4">
                  <button type="button" class="btn btn-light px-4 rounded-pill fw-medium" @click="showForm = false">取消</button>
                  <button type="submit" class="btn btn-primary px-4 rounded-pill fw-medium shadow-sm">
                    {{ editMode ? '儲存變更' : '確認新增' }}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
