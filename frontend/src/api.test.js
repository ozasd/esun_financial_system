import { afterEach, describe, expect, it, vi } from 'vitest'
import api from './api.js'

function headerValue(headers, name) {
  return headers?.[name] ?? headers?.get?.(name)
}

describe('api idempotency interceptor', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    api.defaults.adapter = undefined
  })

  it('adds Idempotency-Key to mutation requests', async () => {
    vi.stubGlobal('crypto', { randomUUID: () => 'fixed-idempotency-key' })
    api.defaults.adapter = async (config) => ({
      data: {},
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      request: {},
    })

    const response = await api.post('/users', { userId: 'A1236456789' })

    expect(headerValue(response.config.headers, 'Idempotency-Key')).toBe('fixed-idempotency-key')
  })

  it('does not add Idempotency-Key to read requests', async () => {
    vi.stubGlobal('crypto', { randomUUID: () => 'fixed-idempotency-key' })
    api.defaults.adapter = async (config) => ({
      data: {},
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      request: {},
    })

    const response = await api.get('/users')

    expect(headerValue(response.config.headers, 'Idempotency-Key')).toBeUndefined()
  })
})
