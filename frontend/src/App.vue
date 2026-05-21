<script setup>
import { ref } from 'vue'
import UserPanel from './components/UserPanel.vue'
import ProductPanel from './components/ProductPanel.vue'
import FavoritePanel from './components/FavoritePanel.vue'

const activeTab = ref('favorites')
</script>

<template>
  <div class="min-vh-100 d-flex flex-column">
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg glass-navbar sticky-top">
      <div class="container-lg py-1">
        <a class="navbar-brand d-flex align-items-center gap-2" href="#">
          <div class="brand-icon-wrapper fs-4">
            <i class="bi bi-bank2"></i>
          </div>
          <div class="navbar-brand-text">
            <div class="fw-bold text-dark fs-6" style="letter-spacing: 0.5px;">金融商品喜好系統</div>
            <small class="text-uppercase d-block text-muted" style="font-size:0.65rem; letter-spacing:1px;">E.SUN Financial</small>
          </div>
        </a>
        <div class="d-none d-md-flex ms-auto align-items-center gap-3">
          <span class="badge rounded-pill bg-success bg-opacity-10 text-success px-3 py-2 border border-success border-opacity-25 d-flex align-items-center gap-2 shadow-sm">
            <span class="status-dot"></span>
            System Online
          </span>
          <div class="vr bg-secondary opacity-25"></div>
          <div class="text-muted small">
            <i class="bi bi-person-circle me-1"></i> Admin User
          </div>
        </div>
      </div>
    </nav>

    <!-- Main App Container -->
    <main class="container-lg py-4 flex-grow-1 d-flex flex-column gap-4">
      
      <!-- Top Section (Header & Tabs) -->
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-end gap-3 mb-1">
        <div>
          <h4 class="fw-bold text-dark mb-1">系統儀表板</h4>
          <p class="text-muted small mb-0">管理您的應用程式資料與使用者設定。</p>
        </div>
        
        <!-- Pill Tabs -->
        <div class="nav-pills-custom" role="tablist">
          <button
            :class="['nav-link border-0 bg-transparent', { active: activeTab === 'favorites' }]"
            @click="activeTab = 'favorites'"
          >
            <i class="bi bi-heart-fill me-2"></i>喜好商品
          </button>
          <button
            :class="['nav-link border-0 bg-transparent', { active: activeTab === 'users' }]"
            @click="activeTab = 'users'"
          >
            <i class="bi bi-people-fill me-2"></i>使用者管理
          </button>
          <button
            :class="['nav-link border-0 bg-transparent', { active: activeTab === 'products' }]"
            @click="activeTab = 'products'"
          >
            <i class="bi bi-box-seam-fill me-2"></i>商品管理
          </button>
        </div>
      </div>

      <!-- Tab Content Area -->
      <div class="tab-content flex-grow-1">
        <div v-if="activeTab === 'favorites'" class="tab-pane active" key="favorites">
          <FavoritePanel />
        </div>
        <div v-if="activeTab === 'users'" class="tab-pane active" key="users">
          <UserPanel />
        </div>
        <div v-if="activeTab === 'products'" class="tab-pane active" key="products">
          <ProductPanel />
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="mt-auto py-4 text-center text-muted border-top border-black border-opacity-10" style="background: rgba(255,255,255,0.4)">
      <small class="fw-medium">© 2026 E.SUN Financial System. All rights reserved.</small>
    </footer>
  </div>
</template>
