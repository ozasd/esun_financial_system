<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../api.js'

const users = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const sortBy = ref('user_id')
const sortDirection = ref('DESC')

const showForm = ref(false)
const editMode = ref(false)
const editId = ref(null)
const form = ref({ userId: '', userName: '', email: '', account: '' })
const formError = ref('')

async function fetchUsers() {
  loading.value = true; error.value = ''
  try {
    const params = { page: page.value, pageSize: pageSize.value, sortBy: sortBy.value, sortDirection: sortDirection.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await userApi.getUsers(params)
    users.value = res.data.datas || []
    total.value = res.data.total || 0
  } catch (e) { error.value = e.response?.data?.message || '載入失敗' }
  finally { loading.value = false }
}

function openAdd() {
  editMode.value = false; editId.value = null
  form.value = { userId: '', userName: '', email: '', account: '' }
  formError.value = ''; showForm.value = true
}

function openEdit(u) {
  editMode.value = true; editId.value = u.userId
  form.value = { userId: u.userId, userName: u.userName, email: u.email, account: u.account }
  formError.value = ''; showForm.value = true
}

async function submitForm() {
  formError.value = ''
  try {
    if (editMode.value) {
      await userApi.updateUser(editId.value, { userName: form.value.userName, email: form.value.email, account: form.value.account })
    } else {
      await userApi.createUser(form.value)
    }
    showForm.value = false; fetchUsers()
  } catch (e) { formError.value = e.response?.data?.message || '操作失敗' }
}

async function deleteUser(userId) {
  if (!confirm('確定要刪除此使用者？')) return
  try { await userApi.deleteUser(userId); fetchUsers() }
  catch (e) { alert(e.response?.data?.message || '刪除失敗') }
}

function goPage(p) { page.value = p; fetchUsers() }
function doSearch() { page.value = 1; fetchUsers() }
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

onMounted(fetchUsers)
</script>

<template>
  <div class="d-flex flex-column gap-4">
    <!-- Action Bar -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
      <!-- Modern Search -->
      <div class="search-wrapper d-flex align-items-center flex-grow-1" style="max-width: 400px;">
        <i class="bi bi-search text-muted px-3"></i>
        <input v-model="keyword" type="text" class="form-control" placeholder="搜尋名稱、Email..." @keyup.enter="doSearch">
        <button class="btn text-primary px-3 fw-medium" @click="doSearch">搜尋</button>
      </div>
      
      <!-- Primary Action -->
      <button class="btn btn-primary px-4 py-2 rounded-pill" @click="openAdd">
        <i class="bi bi-person-plus-fill me-2"></i>新增使用者
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
              <th @click="toggleSort('user_id')" style="cursor:pointer" class="user-select-none">使用者 ID <i :class="['bi ms-1', sortIcon('user_id')]"></i></th>
              <th @click="toggleSort('user_name')" style="cursor:pointer" class="user-select-none">名稱 <i :class="['bi ms-1', sortIcon('user_name')]"></i></th>
              <th @click="toggleSort('email')" style="cursor:pointer" class="user-select-none">Email <i :class="['bi ms-1', sortIcon('email')]"></i></th>
              <th @click="toggleSort('account')" style="cursor:pointer" class="user-select-none">帳號 <i :class="['bi ms-1', sortIcon('account')]"></i></th>
              <th @click="toggleSort('created_at')" style="cursor:pointer" class="user-select-none">加入時間 <i :class="['bi ms-1', sortIcon('created_at')]"></i></th>
              <th style="width:80px" class="text-end">操作</th>
            </tr>
          </thead>
          <tbody class="border-top-0">
            <tr v-if="users.length === 0">
              <td colspan="6" class="text-center py-5">
                <i class="bi bi-person-x text-muted opacity-25" style="font-size: 3rem;"></i>
                <p class="text-muted mt-2 mb-0 fw-medium">目前尚無使用者紀錄</p>
              </td>
            </tr>
            <tr v-for="u in users" :key="u.userId">
              <td class="font-mono text-muted">
                <div class="d-flex align-items-center gap-2">
                  <div class="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 28px; height: 28px; font-size: 0.85rem;">
                    <i class="bi bi-person-fill"></i>
                  </div>
                  {{ u.userId }}
                </div>
              </td>
              <td class="fw-medium text-dark">{{ u.userName }}</td>
              <td><a :href="'mailto:'+u.email" class="text-decoration-none text-muted"><i class="bi bi-envelope me-2"></i>{{ u.email }}</a></td>
              <td class="font-mono text-muted"><i class="bi bi-credit-card-2-front me-2"></i>{{ u.account }}</td>
              <td class="text-muted small"><i class="bi bi-calendar3 me-2"></i>{{ u.createdAt?.substring(0, 10) }}</td>
              <td class="text-end">
                <div class="d-flex gap-1 justify-content-end">
                  <button class="btn btn-action text-secondary" @click="openEdit(u)" title="編輯資料">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button class="btn btn-action btn-action-danger text-secondary" @click="deleteUser(u.userId)" title="刪除帳號">
                    <i class="bi bi-person-dash-fill"></i>
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
          顯示 <span class="fw-bold text-dark">{{ total }}</span> 位使用者中的第 <span class="fw-bold text-dark">{{ page }}</span> 頁
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
                  <i :class="editMode ? 'bi-pencil-square' : 'bi-person-plus-fill'"></i>
                </div>
                {{ editMode ? '編輯使用者資料' : '新增系統使用者' }}
              </h5>
              <button type="button" class="btn-close shadow-none" @click="showForm = false"></button>
            </div>
            <div class="modal-body p-4">
              <div v-if="formError" class="alert alert-danger py-3 small border-0 rounded-3 d-flex align-items-center"><i class="bi bi-exclamation-octagon-fill fs-5 me-2"></i>{{ formError }}</div>
              
              <form @submit.prevent="submitForm" class="d-flex flex-column gap-3">
                <div v-if="!editMode" class="form-floating">
                  <input v-model="form.userId" type="text" class="form-control bg-light" id="userIdInput" required placeholder="A1236456789" />
                  <label for="userIdInput">身分證 / 使用者 ID</label>
                </div>
                
                <div class="form-floating">
                  <input v-model="form.userName" type="text" class="form-control bg-light" id="userNameInput" required placeholder="王小明" />
                  <label for="userNameInput">真實姓名</label>
                </div>
                
                <div class="form-floating">
                  <input v-model="form.email" type="email" class="form-control bg-light" id="emailInput" required placeholder="name@example.com" />
                  <label for="emailInput">電子郵件信箱</label>
                </div>
                
                <div class="form-floating">
                  <input v-model="form.account" type="text" class="form-control bg-light" id="accInput" required placeholder="1111999666" />
                  <label for="accInput">金融扣款帳號</label>
                </div>

                <div class="d-flex justify-content-end gap-3 mt-4">
                  <button type="button" class="btn btn-light px-4 rounded-pill fw-medium" @click="showForm = false">取消</button>
                  <button type="submit" class="btn btn-primary px-4 rounded-pill fw-medium shadow-sm">
                    {{ editMode ? '儲存變更' : '建立帳號' }}
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
