import asyncio
from playwright.async_api import async_playwright
import json
import os

async def run_verification():
    async with async_playwright() as p:
        browser = await p.chromium.launch()
        # Increased viewport to ensure elements are visible
        context = await browser.new_context(viewport={'width': 1280, 'height': 1024})
        page = await context.new_page()

        # Mock API
        async def handle_health(route):
            await route.fulfill(
                status=200,
                content_type='application/json',
                body=json.dumps({'status': 'ok', 'yandex': {'authorized': True, 'status': 'loaded'}})
            )

        async def handle_config(route):
            if route.request.method == 'GET':
                await route.fulfill(
                    status=200,
                    content_type='application/json',
                    body=json.dumps({'version': 1, 'widget': 'hud', 'theme': 'neon-cyber', 'lastPosition': {'x': 100, 'y': 100}})
                )
            else:
                await route.fulfill(status=200)

        async def handle_player_state(route):
            await route.fulfill(
                status=200,
                content_type='application/json',
                body=json.dumps({'title': 'Verify Track', 'artist': 'Tester', 'playing': True, 'progress': 0.5, 'volume': 0.8, 'cover': 'https://avatars.yandex.net/get-music-content/123/456/m1000x1000'})
            )

        await page.route('**/api/health', handle_health)
        await page.route('**/api/config', handle_config)
        await page.route('**/api/player/state', handle_player_state)

        # Using port 5175 as seen in logs
        url = 'http://127.0.0.1:5175'
        print(f"Connecting to {url}...")
        await page.goto(url)

        # Wait for boot screen to finish (1.2s + buffer)
        await page.wait_for_timeout(3000)

        # 1. HUD Mode
        await page.screenshot(path='hud_v5.png')
        print('Captured HUD mode v5')

        # 2. Switch to Slim
        try:
            # Look for button that contains 'MIN'
            min_btn = page.get_by_role('button', name='MIN')
            await min_btn.click()
            await page.wait_for_timeout(1000)
            await page.screenshot(path='slim_v5.png')
            print('Captured Slim mode v5')
        except Exception as e:
            print(f'Failed to capture Slim mode: {e}')

        # 3. Switch to Orb
        try:
            # We are currently in Slim mode, but the Orb button is also there
            orb_btn = page.get_by_role('button', name='ORB')
            await orb_btn.click()
            await page.wait_for_timeout(1000)
            await page.screenshot(path='orb_v5.png')
            print('Captured Orb mode v5')
        except Exception as e:
            print(f'Failed to capture Orb mode: {e}')

        # 4. Settings
        try:
            # Switch back to HUD to find SET button easily or just find it in Orb (right click)
            # Actually, let's just go back to HUD
            orb_element = page.locator('button.orb')
            await orb_element.click()
            await page.wait_for_timeout(1000)

            set_btn = page.get_by_role('button', name='SET')
            await set_btn.click()
            await page.wait_for_timeout(1000)
            await page.screenshot(path='settings_v5.png')
            print('Captured Settings v5')
        except Exception as e:
            print(f'Failed to capture Settings: {e}')

        # 5. Onboarding
        try:
            # Trigger onboarding via mock config
            async def handle_onboarding_config(route):
                await route.fulfill(
                    status=200,
                    content_type='application/json',
                    body=json.dumps({'version': 0, 'widget': 'hud'})
                )

            await page.route('**/api/config', handle_onboarding_config)
            await page.goto(url)
            await page.wait_for_timeout(3000)
            await page.screenshot(path='onboarding_v5.png')
            print('Captured Onboarding v5')
        except Exception as e:
            print(f'Failed to capture Onboarding: {e}')

        await browser.close()

if __name__ == '__main__':
    asyncio.run(run_verification())
