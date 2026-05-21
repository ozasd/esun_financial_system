import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import App from './App.vue'

describe('App', () => {
  it('starts on favorites tab and switches panels from the navigation', async () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          FavoritePanel: { template: '<section data-testid="favorites-panel">favorites</section>' },
          UserPanel: { template: '<section data-testid="users-panel">users</section>' },
          ProductPanel: { template: '<section data-testid="products-panel">products</section>' },
        },
      },
    })

    expect(wrapper.find('[data-testid="favorites-panel"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="users-panel"]').exists()).toBe(false)

    const usersButton = wrapper.findAll('button').find((button) => button.text().includes('使用者管理'))
    await usersButton.trigger('click')

    expect(wrapper.find('[data-testid="favorites-panel"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="users-panel"]').exists()).toBe(true)

    const productsButton = wrapper.findAll('button').find((button) => button.text().includes('商品管理'))
    await productsButton.trigger('click')

    expect(wrapper.find('[data-testid="products-panel"]').exists()).toBe(true)
  })
})
