<script setup>
import { ref } from 'vue'
import UserPanel from './components/UserPanel.vue'
import ProductPanel from './components/ProductPanel.vue'
import FavoritePanel from './components/FavoritePanel.vue'

const activeTab = ref('favorites')

const tabs = [
  { key: 'favorites', label: '喜好商品', icon: '❤️' },
  { key: 'users', label: '使用者管理', icon: '👤' },
  { key: 'products', label: '商品管理', icon: '📦' },
]
</script>

<template>
  <div class="app-layout">
    <!-- Header -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-brand">
          <div class="brand-logo">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <rect width="32" height="32" rx="8" fill="url(#logo-grad)" />
              <path d="M8 16L14 22L24 10" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
              <defs>
                <linearGradient id="logo-grad" x1="0" y1="0" x2="32" y2="32">
                  <stop stop-color="#0066cc" />
                  <stop offset="1" stop-color="#00b894" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <div class="brand-text">
            <h1>金融商品喜好紀錄系統</h1>
            <span class="brand-subtitle">E.SUN Financial Favorites</span>
          </div>
        </div>
        <div class="header-badge">
          <span class="status-dot"></span>
          系統運行中
        </div>
      </div>
    </header>

    <!-- Tab Navigation -->
    <nav class="tab-nav">
      <div class="tab-nav-inner">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
          <span v-if="activeTab === tab.key" class="tab-indicator"></span>
        </button>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="app-main">
      <Transition name="panel" mode="out-in">
        <FavoritePanel v-if="activeTab === 'favorites'" key="favorites" />
        <UserPanel v-else-if="activeTab === 'users'" key="users" />
        <ProductPanel v-else-if="activeTab === 'products'" key="products" />
      </Transition>
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ─── Header ─── */
.app-header {
  background: linear-gradient(135deg, #0a1628 0%, #132744 50%, #0d2137 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--space-xl);
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.brand-logo {
  display: flex;
  align-items: center;
  filter: drop-shadow(0 2px 8px rgba(0, 102, 204, 0.3));
}

.brand-text h1 {
  font-size: 1.15rem;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.02em;
  line-height: 1.3;
}

.brand-subtitle {
  font-size: 0.72rem;
  color: rgba(255, 255, 255, 0.45);
  font-weight: 400;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.header-badge {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 0.78rem;
  color: rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.06);
  padding: 6px 14px;
  border-radius: var(--radius-full);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-accent);
  box-shadow: 0 0 8px rgba(0, 184, 148, 0.5);
  animation: pulse 2s infinite;
}

/* ─── Tab Navigation ─── */
.tab-nav {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: var(--header-height);
  z-index: 90;
}

.tab-nav-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--space-xl);
  display: flex;
  gap: var(--space-xs);
}

.tab-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-lg);
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-family: var(--font-sans);
  font-size: 0.88rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.tab-btn:hover {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}

.tab-btn.active {
  color: var(--color-primary);
  font-weight: 600;
}

.tab-icon {
  font-size: 1rem;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: var(--space-md);
  right: var(--space-md);
  height: 2.5px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent));
  border-radius: var(--radius-full) var(--radius-full) 0 0;
  animation: scaleIn 0.2s ease-out;
}

/* ─── Main Content ─── */
.app-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: var(--space-xl);
}

/* ─── Panel Transitions ─── */
.panel-enter-active {
  animation: fadeInUp 0.3s ease-out;
}
.panel-leave-active {
  animation: fadeIn 0.15s ease-in reverse;
}
</style>
