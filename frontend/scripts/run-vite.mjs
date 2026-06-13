import { copyFileSync, chmodSync, existsSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { tmpdir } from 'node:os';

const localEsbuild = join(process.cwd(), 'node_modules', '@esbuild', 'linux-x64', 'bin', 'esbuild');
const tempEsbuild = join(tmpdir(), 'gbo-frontend-tools', 'esbuild');

if (existsSync(localEsbuild)) {
  mkdirSync(dirname(tempEsbuild), { recursive: true });
  if (!existsSync(tempEsbuild)) {
    copyFileSync(localEsbuild, tempEsbuild);
  }
  chmodSync(tempEsbuild, 0o755);
  process.env.ESBUILD_BINARY_PATH = tempEsbuild;
}

const result = spawnSync(
  process.execPath,
  [join(process.cwd(), 'node_modules', 'vite', 'bin', 'vite.js'), ...process.argv.slice(2)],
  {
    stdio: 'inherit',
    env: process.env,
  },
);

process.exit(result.status ?? 1);
