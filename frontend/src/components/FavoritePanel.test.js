import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import FavoritePanel from './FavoritePanel.vue'
import { favoriteApi, productApi, userApi } from '../api.js'

vi.mock('../api.js', () => ({
  favoriteApi: {
    getLikeList: vi.fn(),
    createFavorite: vi.fn(),
    updateFavorite: vi.fn(),
    deleteFavorite: vi.fn(),
  },
  userApi: {
    getUsers: vi.fn(),
  },
  productApi: {
    getProducts: vi.fn(),
  },
}))

describe('FavoritePanel', () => {
  beforeEach(() => {
    favoriteApi.getLikeList.mockResolvedValue({
      data: {
        datas: [
          {
            userId: 'A1236456789',
            userName: '王小明',
            email: 'test@email.com',
            account: '1111999666',
            favoriteProducts: [
              {
                sn: 1,
                productNo: 1,
                productName: '台股基金',
                purchaseQuantity: 2,
                totalFee: 200,
                totalAmount: 20200,
                account: '1111999666',
              },
            ],
          },
        ],
        total: 1,
      },
    })
    userApi.getUsers.mockResolvedValue({ data: { datas: [] } })
    productApi.getProducts.mockResolvedValue({ data: { datas: [] } })
  })

  afterEach(() => {
    vi.clearAllMocks()
    document.body.innerHTML = ''
  })

  it('loads like-list data on mount and renders aggregated products', async () => {
    const wrapper = mount(FavoritePanel)
    await flushPromises()

    expect(favoriteApi.getLikeList).toHaveBeenCalledWith({
      page: 1,
      pageSize: 10,
      sortBy: 'user_id',
      sortDirection: 'ASC',
    })
    expect(wrapper.text()).toContain('王小明')
    expect(wrapper.text()).toContain('台股基金')
    expect(wrapper.text()).toContain('顯示 1 筆紀錄中的第 1 頁')
  })

  it('searches from page one with keyword', async () => {
    const wrapper = mount(FavoritePanel)
    await flushPromises()

    await wrapper.find('input[placeholder="搜尋使用者或帳號..."]').setValue('王')
    await wrapper.find('button.btn.text-primary').trigger('click')
    await flushPromises()

    expect(favoriteApi.getLikeList).toHaveBeenLastCalledWith({
      page: 1,
      pageSize: 10,
      sortBy: 'user_id',
      sortDirection: 'ASC',
      keyword: '王',
    })
  })
})
