import { defineConfig } from "vite";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import Icons from "unplugin-icons/vite";
import IconsResolver from "unplugin-icons/resolver";
// https://vitejs.dev/config/
import path from "path";
const pathSrc = path.resolve(__dirname, "docs");

export default defineConfig({
  ssr: { noExternal: ["element-plus"] },
  resolve: {
    alias: {
      "@": pathSrc,
    },
  },
  plugins: [
    AutoImport({
      resolvers: [
        ElementPlusResolver(),
        IconsResolver({
          prefix: "Icon",
        }),
      ],
      dts: path.resolve(pathSrc, "auto-imports.d.ts"),
    }),
    Components({
      resolvers: [
        ElementPlusResolver(),
        IconsResolver({
          enabledCollections: ["ep"],
        }),
      ],
      dts: path.resolve(pathSrc, "components.d.ts"),
    }),
    Icons({
      autoInstall: true,
    }),
  ],
});
