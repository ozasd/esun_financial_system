import asyncio
import aiohttp
import time
import random
import json
import statistics

BASE_URL = "http://localhost:8081"
TOTAL_REQUESTS = 1000
CONCURRENCY = 100

async def fetch(session, url, method="GET", data=None):
    start = time.time()
    try:
        if method == "GET":
            async with session.get(url) as response:
                await response.read()
                return time.time() - start, response.status
        else:
            async with session.post(url, json=data) as response:
                await response.read()
                return time.time() - start, response.status
    except Exception as e:
        return time.time() - start, 500

async def run_test(name, url, method="GET", get_data_func=None):
    print(f"\n[{name}] 開始壓測... (總請求: {TOTAL_REQUESTS}, 併發: {CONCURRENCY})")
    print(f"URL: {method} {url}")
    
    async with aiohttp.ClientSession() as session:
        sem = asyncio.Semaphore(CONCURRENCY)
        
        async def bounded_fetch():
            async with sem:
                data = get_data_func() if get_data_func else None
                return await fetch(session, url, method, data)

        start_time = time.time()
        tasks = [asyncio.create_task(bounded_fetch()) for _ in range(TOTAL_REQUESTS)]
        results = await asyncio.gather(*tasks)
        total_time = time.time() - start_time

        latencies = [r[0] for r in results]
        status_codes = [r[1] for r in results]
        
        success_count = len([s for s in status_codes if 200 <= s < 300])
        fail_count = len([s for s in status_codes if s >= 400])

        avg_latency = statistics.mean(latencies) * 1000
        p95_latency = statistics.quantiles(latencies, n=20)[18] * 1000
        rps = TOTAL_REQUESTS / total_time

        print(f"完成時間: {total_time:.2f} 秒")
        print(f"成功: {success_count}, 失敗: {fail_count} (例如被擋下或錯誤)")
        print(f"吞吐量 (RPS): {rps:.2f} req/s")
        print(f"平均延遲: {avg_latency:.2f} ms")
        print(f"P95 延遲: {p95_latency:.2f} ms")
        return rps, avg_latency

async def main():
    print("=== ESUN Financial System 壓測開始 ===")
    
    # 1. 測試有快取 vs 無快取
    await run_test(
        "情境一 (對照組)：無快取 (PostgreSQL JSON_AGG)", 
        f"{BASE_URL}/api/favorite-products/like-list"
    )
    
    await run_test(
        "情境一 (實驗組)：有快取 (Redis Cache)", 
        f"{BASE_URL}/api/favorite-products/users/A1236456789"
    )
    
    # 2. 測試非同步 Queue 寫入
    def random_payload():
        return {
            "userId": f"TEST{random.randint(1000, 9999)}",
            "productNo": random.randint(1, 100),
            "purchaseQuantity": random.randint(1, 10),
            "account": f"ACC{random.randint(10000, 99999)}"
        }

    await run_test(
        "情境二：削峰填谷高併發寫入 (Redis Queue)", 
        f"{BASE_URL}/api/favorite-products",
        method="POST",
        get_data_func=random_payload
    )

if __name__ == "__main__":
    asyncio.run(main())
