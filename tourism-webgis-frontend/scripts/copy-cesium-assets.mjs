import { cp, mkdir } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDirectory = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(scriptDirectory, "..");
const cesiumRoot = resolve(projectRoot, "node_modules", "cesium", "Build", "Cesium");
const outputRoot = resolve(projectRoot, "public", "cesium");
const folders = ["Assets", "ThirdParty", "Widgets", "Workers"];

await mkdir(outputRoot, { recursive: true });

for (const folder of folders) {
  await cp(resolve(cesiumRoot, folder), resolve(outputRoot, folder), {
    recursive: true,
    force: false,
    errorOnExist: false
  });
}

console.log("Cesium static assets are ready.");
