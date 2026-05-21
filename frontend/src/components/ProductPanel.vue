<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../api.js'

const products = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const sortBy = ref('no')
const sortDirection = ref('ASC')

const showForm = ref(false)
const editMode = ref(false)
const editNo = ref(null)
const form = ref({ productName: '', price: '', feeRate: '' })
const formError = ref('')

async function fetchProducts() {
  loading.value = true; error.value = ''
  try {
    const params = { page: page.value, pageSize: pageSize.value, sortBy: sortBy.value, sortDirection: sortDirection.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await productApi.getProducts(params)
    products.value = res.data.datas || []
    total.value = res.data.total || 0
  } catch (e) { error.value = e.response?.data?.message || '載入失敗' }
  finally { loading.value = false }
}

function openAdd() {
  editMode.value = false; editNo.value = null
  form.value = { productName: '', price: '', feeRate: '' }
  formError.value = ''; showForm.value = true
}

function openEdit(p) {
  editMode.value = true; editNo.value = p.no
  form.value = { productName: p.productName, price: String(p.price), feeRate: String(p.feeRate) }
  formError.value = ''; showForm.value = true
}

async function submitForm() {
  formError.value = ''
  try {
    const data = { productName: form.value.productName, price: Number(form.value.price), feeRate: Number(form.value.feeRate) }
    if (editMode.value) {
      await productApi.updateProduct(editNo.value, data)
    } else {
      await productApi.createProduct(data)
    }
    showForm.value = false; fetchProducts()
  } catch (e) { formError.value = e.response?.data?.message || '操作失敗' }
}

async function deleteProduct(no) {
  if (!confirm('確定要下架並刪除此金融商品？')) return
  try { await productApi.deleteProduct(no); fetchProducts() }
  catch (e) { alert(e.response?.data?.message || '刪除失敗') }
}

function goPage(p) { page.value = p; fetchProducts() }
function doSearch() { page.value = 1; fetchProducts() }
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

function formatRate(rate) { return rate != null ? (Number(rate) * 100).toFixed(2) + '%' : '—' }
function formatPrice(price) { return price != null ? Number(price).toLocaleString() : '—' }

onMounted(fetchProducts)
</script>

<template>
  <div class="d-flex flex-column gap-4">
    <!-- Action Bar -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
      <!-- Modern Search -->
      <div class="search-wrapper d-flex align-items-center flex-grow-1" style="max-width: 400px;">
        <i class="bi bi-search text-muted px-3"></i>
        <input v-model="keyword" type="text" class="form-control" placeholder="搜尋商品名稱..." @keyup.enter="doSearch">
        <button class="btn text-primary px-3 fw-medium" @click="doSearch">搜尋</button>
      </div>
      
      <!-- Primary Action -->
      <button class="btn btn-primary px-4 py-2 rounded-pill" @click="openAdd">
        <i class="bi bi-box-seam-fill me-2"></i>新增商品
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
        <table class="table table-hover align-middle text-nowrap">
          <thead>
            <tr>
              <th @click="toggleSort('no')" style="cursor:pointer" class="user-select-none">商品編號 <i :class="['bi ms-1', sortIcon('no')]"></i></th>
              <th @click="toggleSort('product_name')" style="cursor:pointer" class="user-select-none">金融商品名稱 <i :class="['bi ms-1', sortIcon('product_name')]"></i></th>
              <th @click="toggleSort('price')" style="cursor:pointer" class="user-select-none">申購價格 <i :class="['bi ms-1', sortIcon('price')]"></i></th>
              <th @click="toggleSort('fee_rate')" style="cursor:pointer" class="user-select-none">手續費率 <i :class="['bi ms-1', sortIcon('fee_rate')]"></i></th>
              <th @click="toggleSort('created_at')" style="cursor:pointer" class="user-select-none">上架時間 <i :class="['bi ms-1', sortIcon('created_at')]"></i></th>
              <th style="width:80px" class="text-end">操作</th>
            </tr>
          </thead>
          <tbody class="border-top-0">
            <tr v-if="products.length === 0">
              <td colspan="6" class="text-center py-5">
                <i class="bi bi-box-seam-fill text-muted opacity-25" style="font-size: 3rem;"></i>
                <p class="text-muted mt-2 mb-0 fw-medium">目前尚無商品紀錄</p>
              </td>
            </tr>
            <tr v-for="p in products" :key="p.no">
              <td class="font-mono text-muted">
                <span class="badge bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25 rounded-pill px-2"># {{ String(p.no).padStart(4, '0') }}</span>
              </td>
              <td class="fw-bold text-dark">{{ p.productName }}</td>
              <td><span class="fw-bold text-primary fs-6"><span class="opacity-50 fw-normal me-1">$</span>{{ formatPrice(p.price) }}</span></td>
              <td>
                <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-2 py-1">
                  <i class="bi bi-percent me-1"></i>{{ formatRate(p.feeRate) }}
                </span>
              </td>
              <td class="text-muted small"><i class="bi bi-clock me-2"></i>{{ p.createdAt?.substring(0, 10) }}</td>
              <td class="text-end">
                <div class="d-flex gap-1 justify-content-end">
                  <button class="btn btn-action text-secondary" @click="openEdit(p)" title="編輯商品">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button class="btn btn-action btn-action-danger text-secondary" @click="deleteProduct(p.no)" title="刪除商品">
                    <i class="bi bi-trash3"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="!loading && !error && total > 0" class="d-flex flex-column flex-md-row justify-content-between align-items-center mt-4 border-top pt-3">
        <div class="text-muted small mb-3 mb-md-0">
          顯示 <span class="fw-bold text-dark">{{ total }}</span> 筆商品中的第 <span class="fw-bold text-dark">{{ page }}</span> 頁
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
                {{ editMode ? '編輯商品設定' : '新增金融商品' }}
              </h5>
              <button type="button" class="btn-close shadow-none" @click="showForm = false"></button>
            </div>
            <div class="modal-body p-4">
              <div v-if="formError" class="alert alert-danger py-3 small border-0 rounded-3 d-flex align-items-center"><i class="bi bi-exclamation-octagon-fill fs-5 me-2"></i>{{ formError }}</div>
              
              <form @submit.prevent="submitForm" class="d-flex flex-column gap-3">
                <div class="form-floating">
                  <input v-model="form.productName" type="text" class="form-control bg-light" id="prodNameInput" required placeholder="台股基金" />
                  <label for="prodNameInput">金融商品名稱</label>
                </div>
                
                <div class="row g-3">
                  <div class="col-sm-6">
                    <div class="form-floating">
                      <input v-model="form.price" type="number" min="0" step="0.01" class="form-control bg-light text-primary fw-medium" id="priceInput" required placeholder="10000" />
                      <label for="priceInput">發行/申購價格 ($)</label>
                    </div>
                  </div>
                  <div class="col-sm-6">
                    <div class="form-floating">
                      <input v-model="form.feeRate" type="number" min="0" max="1" step="0.001" class="form-control bg-light text-success fw-medium" id="feeInput" required placeholder="0.01" />
                      <label for="feeInput">手續費率 (例: 0.01 為 1%)</label>
                    </div>
                  </div>
                </div>

                <div class="d-flex justify-content-end gap-3 mt-4">
                  <button type="button" class="btn btn-light px-4 rounded-pill fw-medium" @click="showForm = false">取消</button>
                  <button type="submit" class="btn btn-primary px-4 rounded-pill fw-medium shadow-sm">
                    {{ editMode ? '儲存變更' : '上架商品' }}
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
