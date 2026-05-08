import asyncio
from playwright.async_api import async_playwright
import json
import os

async def run_verification():
    async with async_playwright() as p:
        browser = await p.chromium.launch()
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

        # Disable animations via CSS injection
        async def disable_animations(page):
            await page.add_style_tag(content="""
                *, *::before, *::after {
                    animation: none !important;
                    transition: none !important;
                }
            """)

        url = 'http://127.0.0.1:5175'
        print(f"Connecting to {url}...")
        await page.goto(url)
        await disable_animations(page)

        # Wait for boot screen to finish (1.2s + buffer)
        await page.wait_for_timeout(3000)

        # 1. HUD Mode
        await page.screenshot(path='hud_v7.png')
        print('Captured HUD mode v7')

        # 2. Switch to Slim
        try:
            # Finding the MIN button in HUD
            await page.click('button:has-text("MIN")')
            await page.wait_for_timeout(1000)
            await page.screenshot(path='slim_v7.png')
            print('Captured Slim mode v7')
        except Exception as e:
            print(f'Failed to capture Slim mode: {e}')

        # 3. Switch to Orb (directly through API mock since UI buttons are tricky)
        try:
            # We can't easily mock the state change *inside* the app from here without re-injecting
            # or finding the button. In Slim, there is no ORB button currently (looking at SlimWidget.svelte).
            # Wait, let's check SlimWidget again. Ah, it has HUD but not ORB.
            # Let's go back to HUD and then ORB.
            await page.click('button:has-text("HUD")')
            await page.wait_for_timeout(500)
            await page.click('button:has-text("ORB")')
            await page.wait_for_timeout(1000)
            await page.screenshot(path='orb_v7.png')
            print('Captured Orb mode v7')
        except Exception as e:
            print(f'Failed to capture Orb mode: {e}')

        # 4. Settings
        try:
            # Right click on orb to open settings
            orb_element = page.locator('button.orb')
            await orb_element.click(button="right")
            await page.wait_for_timeout(1000)
            await page.screenshot(path='settings_v7.png')
            print('Captured Settings v7')
        except Exception as e:
            print(f'Failed to capture Settings: {e}')

        # 5. Onboarding
        try:
            async def handle_onboarding_config(route):
                await route.fulfill(
                    status=200,
                    content_type='application/json',
                    body=json.dumps({'version': 0, 'widget': 'hud'})
                )

            await page.route('**/api/config', handle_onboarding_config)
            await page.goto(url)
            await disable_animations(page)
            await page.wait_for_timeout(3000)
            await page.screenshot(path='onboarding_v7.png')
            print('Captured Onboarding v7')
        except Exception as e:
            print(f'Failed to capture Onboarding: {e}')

        await browser.close()

if __name__ == '__main__':
    asyncio.run(run_verification())
