
const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch();
    const page = await browser.newPage();
    await page.setViewportSize({ width: 1000, height: 800 });

    // Mock API
    await page.route('**/api/health', route => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ status: 'ok', yandex: { authorized: true, status: 'loaded' } })
    }));

    await page.route('**/api/config', route => {
        if (route.request().method() === 'GET') {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({ version: 1, widget: 'hud', theme: 'neon-cyber' })
            });
        } else {
            route.fulfill({ status: 200 });
        }
    });

    await page.route('**/api/player/state', route => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ title: 'Verify Track', artist: 'Tester', playing: true, progress: 0.5, volume: 0.8 })
    }));

    await page.goto('http://127.0.0.1:5173');
    await page.waitForTimeout(2000);

    // 1. HUD Mode
    await page.screenshot({ path: '/home/jules/verification/hud_mode_v2.png' });
    console.log('Captured HUD mode v2');

    // 2. Try to switch to Slim
    try {
        const minBtn = page.getByRole('button', { name: 'MIN' });
        await minBtn.click();
        await page.waitForTimeout(1000);
        await page.screenshot({ path: '/home/jules/verification/slim_mode.png' });
        console.log('Captured Slim mode');
    } catch (e) {
        console.log('Failed to capture Slim mode: ' + e.message);
    }

    // 3. Try to switch to Orb
    try {
        const orbBtn = page.getByRole('button', { name: 'ORB' });
        await orbBtn.click();
        await page.waitForTimeout(1000);
        await page.screenshot({ path: '/home/jules/verification/orb_mode.png' });
        console.log('Captured Orb mode');
    } catch (e) {
        console.log('Failed to capture Orb mode: ' + e.message);
    }

    // 4. Try to open Settings (back to HUD first if needed)
    try {
        // Refresh to get HUD back
        await page.goto('http://127.0.0.1:5173');
        await page.waitForTimeout(1000);
        const setBtn = page.getByRole('button', { name: 'SET' });
        await setBtn.click();
        await page.waitForTimeout(1000);
        await page.screenshot({ path: '/home/jules/verification/settings.png' });
        console.log('Captured Settings');
    } catch (e) {
        console.log('Failed to capture Settings: ' + e.message);
    }

    // 5. Test Onboarding (trigger by version 0)
    await page.route('**/api/config', route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ version: 0, widget: 'hud' })
        });
    }, { times: 1 });

    await page.goto('http://127.0.0.1:5173');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: '/home/jules/verification/onboarding.png' });
    console.log('Captured Onboarding');

    await browser.close();
})();
