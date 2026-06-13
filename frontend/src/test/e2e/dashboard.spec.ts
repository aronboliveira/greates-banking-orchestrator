import { test, expect } from '@playwright/test';

test('unauthenticated page renders only the login card', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('#gbo-login-app-title')).toContainText('The Greatest Banking Orchestrator');
  await expect(page.locator('#gbo-login-username-input')).toBeVisible();
  await expect(page.locator('#gbo-login-password-input')).toBeVisible();
  await expect(page.locator('#gbo-dashboard-layout')).toHaveCount(0);
  await expect(page.locator('#gbo-overview-panel')).toHaveCount(0);

  const usernameBox = await page.locator('#gbo-login-username-input').boundingBox();
  const passwordBox = await page.locator('#gbo-login-password-input').boundingBox();
  expect(usernameBox).not.toBeNull();
  expect(passwordBox).not.toBeNull();
  expect(passwordBox!.y).toBeGreaterThan(usernameBox!.y + usernameBox!.height);
});

test('successful login opens the dashboard shell', async ({ page }) => {
  await page.route('**/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        access_token: 'mock.jwt.token',
        token_type: 'Bearer',
        expires_at: '2026-06-12T18:00:00Z',
        user: {
          user_id: 2,
          username: 'admin',
          display_name: 'Admin Operator',
          email: 'admin@example.test',
          role: 'ADMIN',
          avatar_id: 'gbo-logo-cutout',
          notifications_enabled: false,
          updated_at: '2026-06-12T18:00:00Z',
        },
      }),
    });
  });

  await page.route('**/actuator/health', async (route) => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: '{"status":"UP"}' });
  });

  await page.goto('/');
  await page.locator('#gbo-login-username-input').fill('admin');
  await page.locator('#gbo-login-password-input').fill('approve-flow');
  await page.locator('#gbo-login-submit-button').click();

  await expect(page.locator('#gbo-dashboard-layout')).toBeVisible();
  await expect(page.locator('#gbo-overview-panel')).toBeVisible();
  await expect(page.locator('#gbo-login-section')).toHaveCount(0);
  await expect(page.locator('#gbo-session-badge')).toContainText('Admin Operator');
});
