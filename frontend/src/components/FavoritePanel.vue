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

// Form
const showForm = ref(false)
const editMode = ref(false)
const editSn = ref(null)
const form = ref({ userId: '', productNo: '', purchaseQuantity: 1, account: '' })
const users = ref([])
const products = ref([])
const formError = ref('')

async function fetchLikeList() {
  loading.value = true
  error.value = ''
  try {
    const params = { page: page.value, pageSize: pageSize.value, sortBy: sortBy.value, sortDirection: sortDirection.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await favoriteApi.getLikeList(params)
    likeList.value = res.data.datas || []
    total.value = res.data.total || 0
  } catch (e) {
    error.value = e.response?.data?.message || '載入失敗'
  } finally {
    loading.value = false
  }
}

async function loadDropdowns() {
  try {
    const [u, p] = await Promise.all([userApi.getUsers({ pageSize: 100 }), productApi.getProducts({ pageSize: 100 })])
    users.value = u.data.datas || []
    products.value = p.data.datas || []
  } catch (_) {}
}

function openAdd() {
  editMode.value = false
  editSn.value = null
  form.value = { userId: '', productNo: '', purchaseQuantity: 1, account: '' }
  formError.value = ''
  showForm.value = true
  loadDropdowns()
}

function openEdit(sn, item) {
  editMode.value = true
  editSn.value = sn
  form.value = { userId: item.userId || '', productNo: String(item.productNo), purchaseQuantity: item.purchaseQuantity, account: item.account }
  formError.value = ''
  showForm.value = true
  loadDropdowns()
}

async function submitForm() {
  formError.value = ''
  try {
    if (editMode.value) {
      await favoriteApi.updateFavorite(editSn.value, { productNo: Number(form.value.productNo), purchaseQuantity: Number(form.value.purchaseQuantity), account: form.value.account })
    } else {
      await favoriteApi.createFavorite({ userId: form.value.userId, productNo: Number(form.value.productNo), purchaseQuantity: Number(form.value.purchaseQuantity), account: form.value.account })
    }
    showForm.value = false
    fetchLikeList()
  } catch (e) {
    formError.value = e.response?.data?.message || '操作失敗'
  }
}

async function deleteFavorite(sn) {
  if (!confirm('確定要刪除此筆喜好商品？')) return
  try {
    await favoriteApi.deleteFavorite(sn)
    fetchLikeList()
  } catch (e) {
    alert(e.response?.data?.message || '刪除失敗')
  }
}

function goPage(p) { page.value = p; fetchLikeList() }
function doSearch() { page.value = 1; fetchLikeList() }

const totalPages = () => Math.ceil(total.value / pageSize.value) || 1

onMounted(fetchLikeList)
</script>

<template>
  <section class="panel fade-in-up">
    <div class="panel-header">
      <div>
        <h2>❤️ 喜好商品清單</h2>
        <p class="panel-desc">查詢、新增、編輯與刪除使用者喜好的金融商品</p>
      </div>
      <button class="btn btn-primary" @click="openAdd">＋ 新增喜好商品</button>
    </div>

    <!-- Search Bar -->
    <div class="toolbar">
      <div class="search-box">
        <input v-model="keyword" placeholder="搜尋關鍵字（使用者名稱、帳號…）" @keyup.enter="doSearch" />
        <button class="btn btn-sm" @click="doSearch">搜尋</button>
      </div>
      <div class="toolbar-right">
        <select v-model="sortBy" @change="doSearch">
          <option value="user_id">使用者 ID</option>
          <option value="user_name">使用者名稱</option>
          <option value="email">Email</option>
          <option value="account">帳號</option>
        </select>
        <select v-model="sortDirection" @change="doSearch">
          <option value="ASC">升冪 ↑</option>
          <option value="DESC">降冪 ↓</option>
        </select>
      </div>
    </div>

    <!-- Loading / Error -->
    <div v-if="loading" class="state-msg"><span class="spinner"></span> 載入中…</div>
    <div v-else-if="error" class="state-msg state-error">{{ error }}</div>

    <!-- Table -->
    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>使用者 ID</th><th>名稱</th><th>Email</th><th>帳號</th><th>收藏商品</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="likeList.length === 0"><td colspan="6" class="empty">尚無資料</td></tr>
          <tr v-for="item in likeList" :key="item.userId">
            <td class="mono">{{ item.userId }}</td>
            <td>{{ item.userName }}</td>
            <td>{{ item.email }}</td>
            <td class="mono">{{ item.account }}</td>
            <td>
              <div v-if="item.favoriteProducts && item.favoriteProducts.length" class="fav-chips">
                <div v-for="fp in item.favoriteProducts" :key="fp.sn" class="fav-chip">
                  <span class="chip-name">{{ fp.productName }}</span>
                  <span class="chip-detail">數量 {{ fp.purchaseQuantity }} ・ 手續費 {{ fp.totalFee }} ・ 總額 {{ fp.totalAmount }}</span>
                  <span class="chip-actions">
                    <button class="icon-btn" title="編輯" @click="openEdit(fp.sn, { ...fp, userId: item.userId, account: fp.account })">✏️</button>
                    <button class="icon-btn danger" title="刪除" @click="deleteFavorite(fp.sn)">🗑️</button>
                  </span>
                </div>
              </div>
              <span v-else class="text-muted">—</span>
            </td>
            <td>—</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="!loading && !error" class="pagination">
      <span class="page-info">共 {{ total }} 筆，第 {{ page }} / {{ totalPages() }} 頁</span>
      <div class="page-btns">
        <button :disabled="page <= 1" @click="goPage(page - 1)">‹</button>
        <button :disabled="page >= totalPages()" @click="goPage(page + 1)">›</button>
      </div>
    </div>

    <!-- Modal -->
    <Teleport to="body">
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal fade-in-up">
          <h3>{{ editMode ? '編輯喜好商品' : '新增喜好商品' }}</h3>
          <div v-if="formError" class="form-error">{{ formError }}</div>
          <form @submit.prevent="submitForm" class="form-grid">
            <label v-if="!editMode">
              <span>使用者</span>
              <select v-model="form.userId" required>
                <option value="" disabled>請選擇</option>
                <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.userName }} ({{ u.userId }})</option>
              </select>
            </label>
            <label>
              <span>商品</span>
              <select v-model="form.productNo" required>
                <option value="" disabled>請選擇</option>
                <option v-for="p in products" :key="p.no" :value="String(p.no)">{{ p.productName }} — ${{ p.price }}</option>
              </select>
            </label>
            <label>
              <span>購買數量</span>
              <input v-model.number="form.purchaseQuantity" type="number" min="1" required />
            </label>
            <label>
              <span>扣款帳號</span>
              <input v-model="form.account" required placeholder="例：1111999666" />
            </label>
            <div class="form-actions">
              <button type="button" class="btn btn-ghost" @click="showForm = false">取消</button>
              <button type="submit" class="btn btn-primary">{{ editMode ? '更新' : '新增' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
/* Panel */
.panel { animation: fadeInUp 0.4s ease-out; }
.panel-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:var(--space-lg); flex-wrap:wrap; gap:var(--space-md); }
.panel-header h2 { font-size:1.35rem; font-weight:700; }
.panel-desc { font-size:0.82rem; color:var(--color-text-secondary); margin-top:2px; }

/* Toolbar */
.toolbar { display:flex; gap:var(--space-md); margin-bottom:var(--space-lg); flex-wrap:wrap; align-items:center; }
.search-box { display:flex; gap:var(--space-sm); flex:1; min-width:200px; }
.search-box input { flex:1; padding:8px 14px; border:1px solid var(--color-border); border-radius:var(--radius-md); font-size:0.88rem; font-family:var(--font-sans); transition:border var(--transition-fast); }
.search-box input:focus { outline:none; border-color:var(--color-primary); box-shadow:0 0 0 3px var(--color-primary-glow); }
.toolbar-right { display:flex; gap:var(--space-sm); }
.toolbar-right select { padding:8px 12px; border:1px solid var(--color-border); border-radius:var(--radius-md); font-size:0.82rem; font-family:var(--font-sans); background:var(--color-surface); cursor:pointer; }

/* Table */
.table-wrap { overflow-x:auto; border:1px solid var(--color-border); border-radius:var(--radius-lg); background:var(--color-surface); box-shadow:var(--shadow-sm); }
table { width:100%; border-collapse:collapse; }
thead { background:linear-gradient(135deg, #f7f9fc, #edf2f7); }
th { padding:12px 16px; text-align:left; font-size:0.78rem; font-weight:600; color:var(--color-text-secondary); text-transform:uppercase; letter-spacing:0.04em; border-bottom:2px solid var(--color-border); white-space:nowrap; }
td { padding:12px 16px; font-size:0.88rem; border-bottom:1px solid var(--color-border-light); vertical-align:top; }
tr:last-child td { border-bottom:none; }
tr:hover td { background:var(--color-surface-hover); }
.mono { font-family:var(--font-mono); font-size:0.82rem; }
.empty { text-align:center; color:var(--color-text-muted); padding:40px 16px !important; }

/* Fav Chips */
.fav-chips { display:flex; flex-direction:column; gap:6px; }
.fav-chip { display:flex; align-items:center; gap:8px; padding:6px 10px; background:var(--color-primary-bg); border-radius:var(--radius-sm); font-size:0.82rem; flex-wrap:wrap; }
.chip-name { font-weight:600; color:var(--color-primary-dark); }
.chip-detail { color:var(--color-text-secondary); font-size:0.76rem; }
.chip-actions { margin-left:auto; display:flex; gap:2px; }
.icon-btn { background:none; border:none; cursor:pointer; font-size:0.9rem; padding:2px 4px; border-radius:var(--radius-sm); transition:background var(--transition-fast); }
.icon-btn:hover { background:rgba(0,0,0,0.06); }
.icon-btn.danger:hover { background:var(--color-danger-bg); }

/* State */
.state-msg { text-align:center; padding:48px 16px; color:var(--color-text-secondary); font-size:0.92rem; }
.state-error { color:var(--color-danger); }
.spinner { display:inline-block; width:18px; height:18px; border:2px solid var(--color-border); border-top-color:var(--color-primary); border-radius:50%; animation:spin 0.7s linear infinite; vertical-align:middle; margin-right:6px; }

/* Pagination */
.pagination { display:flex; justify-content:space-between; align-items:center; margin-top:var(--space-md); padding-top:var(--space-md); }
.page-info { font-size:0.82rem; color:var(--color-text-secondary); }
.page-btns { display:flex; gap:var(--space-xs); }
.page-btns button { width:34px; height:34px; border:1px solid var(--color-border); background:var(--color-surface); border-radius:var(--radius-sm); cursor:pointer; font-size:1rem; transition:all var(--transition-fast); display:flex; align-items:center; justify-content:center; }
.page-btns button:hover:not(:disabled) { background:var(--color-primary-bg); border-color:var(--color-primary); color:var(--color-primary); }
.page-btns button:disabled { opacity:0.35; cursor:not-allowed; }

/* Buttons */
.btn { padding:8px 18px; border:none; border-radius:var(--radius-md); font-family:var(--font-sans); font-size:0.86rem; font-weight:600; cursor:pointer; transition:all var(--transition-fast); display:inline-flex; align-items:center; gap:6px; }
.btn-primary { background:linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)); color:#fff; box-shadow:0 2px 8px rgba(0,102,204,0.25); }
.btn-primary:hover { transform:translateY(-1px); box-shadow:0 4px 16px rgba(0,102,204,0.35); }
.btn-sm { padding:6px 14px; font-size:0.82rem; background:var(--color-primary-bg); color:var(--color-primary); font-weight:600; }
.btn-sm:hover { background:var(--color-primary); color:#fff; }
.btn-ghost { background:transparent; color:var(--color-text-secondary); border:1px solid var(--color-border); }
.btn-ghost:hover { background:var(--color-surface-hover); }

/* Modal */
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.4); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:1000; }
.modal { background:var(--color-surface); border-radius:var(--radius-lg); padding:var(--space-xl); width:100%; max-width:480px; box-shadow:var(--shadow-xl); }
.modal h3 { font-size:1.15rem; font-weight:700; margin-bottom:var(--space-lg); }
.form-grid { display:flex; flex-direction:column; gap:var(--space-md); }
.form-grid label { display:flex; flex-direction:column; gap:4px; }
.form-grid label span { font-size:0.82rem; font-weight:600; color:var(--color-text-secondary); }
.form-grid input, .form-grid select { padding:9px 12px; border:1px solid var(--color-border); border-radius:var(--radius-md); font-size:0.88rem; font-family:var(--font-sans); transition:border var(--transition-fast); }
.form-grid input:focus, .form-grid select:focus { outline:none; border-color:var(--color-primary); box-shadow:0 0 0 3px var(--color-primary-glow); }
.form-actions { display:flex; justify-content:flex-end; gap:var(--space-sm); margin-top:var(--space-sm); }
.form-error { background:var(--color-danger-bg); color:var(--color-danger); padding:8px 12px; border-radius:var(--radius-sm); font-size:0.82rem; margin-bottom:var(--space-sm); }
.text-muted { color:var(--color-text-muted); }
</style>
